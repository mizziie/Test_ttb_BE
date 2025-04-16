package com.example.surveyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class SurveySubmit {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_submit_seq_gen")
    @SequenceGenerator(name = "survey_submit_seq_gen", sequenceName = "survey_submit_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;
    
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String answersJson = "[]"; // เก็บคำตอบในรูปแบบ JSON string ค่าเริ่มต้นเป็นอาร์เรย์ว่าง
    
    @CreationTimestamp
    private LocalDateTime submittedAt;
    
    // --- วิธีการเข้าถึง answers ในรูปแบบ List<Map<String, Object>> ---
    
    @Transient
    public List<Map<String, Object>> getAnswers() {
        if (answersJson == null || answersJson.isEmpty() || "[]".equals(answersJson)) {
            log.debug("SurveySubmit.getAnswers: answersJson is null or empty for submission ID: {}", id);
            return new ArrayList<>();
        }
        
        try {
            log.debug("SurveySubmit.getAnswers: answersJson = {} for submission ID: {}", answersJson, id);
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
            List<Map<String, Object>> result = objectMapper.readValue(answersJson, typeRef);
            
            if (result == null) {
                log.warn("SurveySubmit.getAnswers: Deserialized to null result for submission ID: {}", id);
                return new ArrayList<>();
            }
            
            for (Map<String, Object> answer : result) {
                if (!answer.containsKey("questionId")) {
                    log.warn("SurveySubmit.getAnswers: Missing questionId in answer: {}", answer);
                    answer.put("questionId", "unknown");
                }
                if (!answer.containsKey("answer")) {
                    log.warn("SurveySubmit.getAnswers: Missing answer value in answer: {}", answer);
                    Map<String, Object> answerObject = new HashMap<>();
                    answerObject.put("value", "-");
                    answer.put("answer", answerObject);
                } else {
                    Object answerValue = answer.get("answer");
                    if (!(answerValue instanceof Map)) {
                        Map<String, Object> answerObject = new HashMap<>();
                        answerObject.put("value", answerValue);
                        answer.put("answer", answerObject);
                    }
                }
            }
            
            return result;
        } catch (JsonProcessingException e) {
            log.error("SurveySubmit.getAnswers: Error parsing JSON: {} for submission ID: {}", e.getMessage(), id);
            return new ArrayList<>();
        }
    }
    
    public void setAnswers(List<Map<String, Object>> answers) {
        try {
            if (answers == null) {
                log.debug("SurveySubmit.setAnswers: answers is null");
                this.answersJson = "[]";
                return;
            }
            
            if (answers.isEmpty()) {
                log.debug("SurveySubmit.setAnswers: answers is empty list");
                this.answersJson = "[]";
                return;
            }
            
            List<Map<String, Object>> processedAnswers = new ArrayList<>();
            
            for (Map<String, Object> answer : answers) {
                Map<String, Object> processedAnswer = new HashMap<>();
                
                if (!answer.containsKey("questionId")) {
                    log.warn("SurveySubmit.setAnswers: answer missing questionId: {}", answer);
                    processedAnswer.put("questionId", "unknown");
                } else {
                    processedAnswer.put("questionId", answer.get("questionId"));
                }
                
                for (Map.Entry<String, Object> entry : answer.entrySet()) {
                    String key = entry.getKey();
                    if (!key.equals("questionId") && !key.equals("answer")) {
                        processedAnswer.put(key, entry.getValue());
                    }
                }
                
                Map<String, Object> answerObject = new HashMap<>();
                
                if (!answer.containsKey("answer")) {
                    log.warn("SurveySubmit.setAnswers: answer missing answer value: {}", answer);
                    answerObject.put("value", "-");
                } else {
                    Object answerValue = answer.get("answer");
                    
                    if (answerValue instanceof Map) {
                        Map<?, ?> answerMap = (Map<?, ?>) answerValue;
                        if (answerMap.containsKey("value")) {
                            answerObject.put("value", answerMap.get("value"));
                        } else {
                            answerObject.put("value", answerValue);
                        }
                    } else {
                        if (answerValue == null) {
                            answerObject.put("value", "-");
                        } else if (answerValue instanceof String && ((String) answerValue).isEmpty()) {
                            answerObject.put("value", "-");
                        } else {
                            answerObject.put("value", answerValue);
                        }
                    }
                }
                
                processedAnswer.put("answer", answerObject);
                processedAnswers.add(processedAnswer);
            }
            
            this.answersJson = objectMapper.writeValueAsString(processedAnswers);
            log.debug("SurveySubmit.setAnswers: answersJson = {}", answersJson);
        } catch (JsonProcessingException e) {
            log.error("SurveySubmit.setAnswers: Error writing JSON: {}", e.getMessage());
            this.answersJson = "[]";
        }
    }
    
    
    @Transient
    public Map<String, Object> getAnswerByQuestionId(String questionId) {
        for (Map<String, Object> answer : getAnswers()) {
            if (answer.containsKey("questionId") && 
                questionId.equals(answer.get("questionId").toString())) {
                return answer;
            }
        }
        return null;
    }
    
    @Transient
    public void addAnswer(String questionId, Object answerValue) {
        List<Map<String, Object>> answers = getAnswers();
        

        boolean foundAnswer = false;
        for (Map<String, Object> answer : answers) {
            if (answer.containsKey("questionId") && 
                questionId.equals(answer.get("questionId").toString())) {
                
     
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", answerValue);
                
                answer.put("answer", answerObject);
                foundAnswer = true;
                break;
            }
        }
        

        if (!foundAnswer) {
            Map<String, Object> newAnswer = new HashMap<>();
            newAnswer.put("questionId", questionId);
            
        
            Map<String, Object> answerObject = new HashMap<>();
            answerObject.put("value", answerValue);
            
            newAnswer.put("answer", answerObject);
            answers.add(newAnswer);
        }
        
        setAnswers(answers);
    }
} 