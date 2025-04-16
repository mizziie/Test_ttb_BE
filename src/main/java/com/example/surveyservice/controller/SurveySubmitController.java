package com.example.surveyservice.controller;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveySubmit;
import com.example.surveyservice.service.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Survey Submit", description = "Survey Submit API")
@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "false")
public class SurveySubmitController {

    private final SurveyService surveyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/submit-survey")
    @Operation(summary = "บันทึกข้อมูลการตอบแบบสำรวจ")
    @ApiResponse(responseCode = "201", description = "Survey submitted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    public ResponseEntity<Map<String, Object>> submitSurvey(@RequestBody Object requestData) {
        log.info("submitSurvey: Received submission request of type {}: {}", requestData.getClass().getName(), requestData);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> requestBody;

        try {
            if (requestData instanceof List) {
                List<?> requestList = (List<?>) requestData;
                log.info("submitSurvey: Request is an array with {} items", requestList.size());
                
                if (requestList.isEmpty()) {
                    String errorMessage = "Empty request array";
                    log.error("submitSurvey: {}", errorMessage);
                    response.put("success", false);
                    response.put("message", errorMessage);
                    return ResponseEntity.badRequest().body(response);
                }
                
                Object firstItem = requestList.get(0);
                log.info("submitSurvey: First item in array is of type: {}", firstItem != null ? firstItem.getClass().getName() : "null");
                
                if (!(firstItem instanceof Map)) {
                    String errorMessage = "First item in array is not a Map";
                    log.error("submitSurvey: {}", errorMessage);
                    response.put("success", false);
                    response.put("message", errorMessage);
                    return ResponseEntity.badRequest().body(response);
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> firstItemMap = (Map<String, Object>) firstItem;
                log.info("submitSurvey: First item keys: {}", firstItemMap.keySet());
                
                if (firstItemMap.containsKey("survey")) {
                    Object surveyValue = firstItemMap.get("survey");
                    log.info("submitSurvey: Survey value in first item is of type: {}, value: {}", 
                              surveyValue != null ? surveyValue.getClass().getName() : "null", surveyValue);
                    
                    requestBody = new HashMap<>();
                    
                    if (surveyValue instanceof String) {
                        String surveyIdStr = surveyValue.toString().trim();
                        log.info("submitSurvey: Survey ID as string: {}", surveyIdStr);
                        
                        try {
                            String normalizedId = surveyIdStr.replaceFirst("^0+(?!$)", "");
                            log.info("submitSurvey: Normalized survey ID: {}", normalizedId);
                            
                            Long surveyId = Long.valueOf(normalizedId);
                            log.info("submitSurvey: Converted survey ID to Long: {}", surveyId);
                            requestBody.put("surveyId", surveyId);
                        } catch (NumberFormatException e) {
                            log.error("submitSurvey: Invalid survey ID format: {}, error: {}", surveyValue, e.getMessage());
                            response.put("success", false);
                            response.put("message", "Invalid survey ID format: " + e.getMessage());
                            return ResponseEntity.badRequest().body(response);
                        }
                    } else if (surveyValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> surveyMap = (Map<String, Object>) surveyValue;
                        log.info("submitSurvey: Survey map keys: {}", surveyMap.keySet());
                        
                        if (surveyMap.containsKey("id")) {
                            Object idValue = surveyMap.get("id");
                            log.info("submitSurvey: Survey ID in map is of type: {}, value: {}", 
                                     idValue != null ? idValue.getClass().getName() : "null", idValue);
                            
                            try {
                                if (idValue instanceof String) {
                                    String idStr = ((String) idValue).trim();
                                    String normalizedId = idStr.replaceFirst("^0+(?!$)", "");
                                    log.info("submitSurvey: Normalized ID from survey map: {}", normalizedId);
                                    
                                    Long surveyId = Long.valueOf(normalizedId);
                                    log.info("submitSurvey: Converted ID from survey map to Long: {}", surveyId);
                                    requestBody.put("surveyId", surveyId);
                                } else {
                                    Long surveyId = Long.valueOf(idValue.toString());
                                    log.info("submitSurvey: Converted non-string ID to Long: {}", surveyId);
                                    requestBody.put("surveyId", surveyId);
                                }
                            } catch (NumberFormatException e) {
                                log.error("submitSurvey: Invalid survey ID format: {}, error: {}", idValue, e.getMessage());
                                response.put("success", false);
                                response.put("message", "Invalid survey ID format: " + e.getMessage());
                                return ResponseEntity.badRequest().body(response);
                            }
                        } else {
                            String errorMessage = "Missing ID in survey object";
                            log.error("submitSurvey: {}", errorMessage);
                            response.put("success", false);
                            response.put("message", errorMessage);
                            return ResponseEntity.badRequest().body(response);
                        }
                    } else {
                        String errorMessage = "Invalid survey value format: " + surveyValue.getClass().getName();
                        log.error("submitSurvey: {}", errorMessage);
                        response.put("success", false);
                        response.put("message", errorMessage);
                        return ResponseEntity.badRequest().body(response);
                    }
                    
                    if (requestList.size() > 1) {
                        Object secondItem = requestList.get(1);
                        log.info("submitSurvey: Second item in array is of type: {}", 
                                 secondItem != null ? secondItem.getClass().getName() : "null");
                        
                        if (secondItem instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> secondItemMap = (Map<String, Object>) secondItem;
                            log.info("submitSurvey: Second item keys: {}", secondItemMap.keySet());
                            
                            if (secondItemMap.containsKey("answers")) {
                                Object answersValue = secondItemMap.get("answers");
                                log.info("submitSurvey: Answers in second item is of type: {}", 
                                         answersValue != null ? answersValue.getClass().getName() : "null");
                                
                                requestBody.put("answers", answersValue);
                            } else {
                                log.warn("submitSurvey: No 'answers' key found in second item");
                            }
                        } else {
                            log.warn("submitSurvey: Second item is not a Map");
                        }
                    } else {
                        log.warn("submitSurvey: Request list has only one item, no answers found");
                    }
                } else {
                    String errorMessage = "Missing survey information in request";
                    log.error("submitSurvey: {}", errorMessage);
                    response.put("success", false);
                    response.put("message", errorMessage);
                    return ResponseEntity.badRequest().body(response);
                }
            } else if (requestData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) requestData;
                log.info("submitSurvey: Request is a Map with keys: {}", map.keySet());
                requestBody = map;
            } else {
                String errorMessage = "Unsupported request data type: " + requestData.getClass().getName();
                log.error("submitSurvey: {}", errorMessage);
                response.put("success", false);
                response.put("message", errorMessage);
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("submitSurvey: Processed request body: {}", requestBody);

            Long surveyId;
            if (requestBody.containsKey("surveyId")) {
                Object surveyIdValue = requestBody.get("surveyId");
                log.info("submitSurvey: Survey ID in request body is of type: {}, value: {}", 
                         surveyIdValue != null ? surveyIdValue.getClass().getName() : "null", surveyIdValue);
                
                if (surveyIdValue instanceof String) {
                    String surveyIdStr = ((String) surveyIdValue).trim();
                    String normalizedId = surveyIdStr.replaceFirst("^0+(?!$)", "");
                    log.info("submitSurvey: Normalized survey ID from request body: {}", normalizedId);
                    
                    surveyId = Long.valueOf(normalizedId);
                } else {
                    surveyId = Long.valueOf(surveyIdValue.toString());
                }
                log.info("submitSurvey: Final survey ID: {}", surveyId);
            } else {
                String errorMessage = "Missing surveyId in the request";
                log.error("submitSurvey: {}", errorMessage);
                response.put("success", false);
                response.put("message", errorMessage);
                return ResponseEntity.badRequest().body(response);
            }

            log.info("submitSurvey: Processing submission for survey ID: {}", surveyId);
            
            try {
                Survey survey = surveyService.getSurveyById(surveyId);
                if (survey == null) {
                    String errorMessage = "Survey not found with ID: " + surveyId;
                    log.error("submitSurvey: {}", errorMessage);
                    response.put("success", false);
                    response.put("message", errorMessage);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                log.info("submitSurvey: Found survey with ID {}: {}", surveyId, survey.getTitle());
                
                List<Map<String, Object>> formattedAnswers = new ArrayList<>();
                
                if (requestBody.containsKey("answers")) {
                    Object answersObj = requestBody.get("answers");
                    log.info("submitSurvey: Found 'answers' field of type: {}", 
                             answersObj != null ? answersObj.getClass().getName() : "null");
                    
                    if (answersObj instanceof List) {
                        List<?> answersList = (List<?>) answersObj;
                        log.info("submitSurvey: 'answers' is a List with {} items", answersList.size());
                        
                        for (Object answerItem : answersList) {
                            if (answerItem instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> answerMap = (Map<String, Object>) answerItem;
                                log.info("submitSurvey: Processing answer item: {}", answerMap);
                                formattedAnswers.add(processAnswer(answerMap));
                            } else {
                                log.warn("submitSurvey: Skipping non-map answer item: {}", answerItem);
                            }
                        }
                    } else if (answersObj instanceof Map) {
                        log.info("submitSurvey: 'answers' is a Map, converting to single answer");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> answerMap = (Map<String, Object>) answersObj;
                        log.info("submitSurvey: Processing single answer map: {}", answerMap);
                        formattedAnswers.add(processAnswer(answerMap));
                    } else if (answersObj instanceof String) {
                        log.info("submitSurvey: 'answers' is a String, attempting to parse: {}", answersObj);
                        try {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> parsedAnswers = 
                                objectMapper.readValue((String) answersObj, List.class);
                            
                            for (Map<String, Object> answerMap : parsedAnswers) {
                                log.info("submitSurvey: Processing parsed answer: {}", answerMap);
                                formattedAnswers.add(processAnswer(answerMap));
                            }
                        } catch (Exception e) {
                            log.error("submitSurvey: Failed to parse 'answers' string: {}", e.getMessage());
                        }
                    } else {
                        log.warn("submitSurvey: 'answers' is of unsupported type: {}", 
                                 answersObj != null ? answersObj.getClass().getName() : "null");
                    }
                } else {
                    log.warn("submitSurvey: No 'answers' field found in request body");
                }
                
                if (formattedAnswers.isEmpty()) {
                    log.warn("submitSurvey: No valid answers found in request");
                }
                
                log.info("submitSurvey: Formatted answers: {}", formattedAnswers);
    
                LocalDateTime submittedAt = LocalDateTime.now();
                log.info("submitSurvey: Using submission time: {}", submittedAt);
    
                SurveySubmit submit = SurveySubmit.builder()
                        .survey(survey)
                        .submittedAt(submittedAt)
                        .build();
                
                submit.setAnswers(formattedAnswers);
                log.info("submitSurvey: Created SurveySubmit object: {}", submit);
                
                try {
                    SurveySubmit savedSubmit = surveyService.submitSurvey(survey.getId(), submit);
                    log.info("submitSurvey: Survey submitted successfully with ID: {}", savedSubmit.getId());
    
                    response.put("success", true);
                    response.put("message", "Survey submitted successfully");
                    response.put("surveySubmitId", savedSubmit.getId());
                    response.put("submittedAt", savedSubmit.getSubmittedAt());
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                } catch (Exception e) {
                    log.error("submitSurvey: Error saving survey submission: {}", e.getMessage(), e);
                    response.put("success", false);
                    response.put("message", "Error saving survey submission: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } catch (Exception e) {
                log.error("submitSurvey: Error retrieving survey: {}", e.getMessage(), e);
                response.put("success", false);
                response.put("message", "Error retrieving survey: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("submitSurvey: Unexpected error: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    

    private Map<String, Object> processAnswer(Map<String, Object> answer) {
        Map<String, Object> processedAnswer = new HashMap<>();
        
        if (!answer.containsKey("questionId")) {
            log.warn("processAnswer: Missing questionId in answer: {}", answer);
            
            if (answer.containsKey("question_id")) {
                processedAnswer.put("questionId", answer.get("question_id"));
            } else if (answer.containsKey("id")) {
                processedAnswer.put("questionId", answer.get("id"));
            } else {
                processedAnswer.put("questionId", "unknown");
            }
        } else {
            processedAnswer.put("questionId", answer.get("questionId"));
        }
        
        if (!answer.containsKey("answer")) {
            log.warn("processAnswer: Missing answer field in: {}", answer);
            
            if (answer.containsKey("value")) {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", answer.get("value"));
                processedAnswer.put("answer", answerObject);
            } else if (answer.containsKey("response")) {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", answer.get("response"));
                processedAnswer.put("answer", answerObject);
            } else if (answer.containsKey("text")) {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", answer.get("text"));
                processedAnswer.put("answer", answerObject);
            } else {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", "-");
                processedAnswer.put("answer", answerObject);
            }
        } else {
            Object answerValue = answer.get("answer");
            
            if (answerValue == null) {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", "-");
                processedAnswer.put("answer", answerObject);
            } else if (answerValue instanceof String && ((String) answerValue).isEmpty()) {
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", "-");
                processedAnswer.put("answer", answerObject);
            } else if (answerValue instanceof Map) {
                processedAnswer.put("answer", answerValue);
                Map<String, Object> answerObject = new HashMap<>();
                answerObject.put("value", answerValue);
                processedAnswer.put("answer", answerObject);
            }
        }
        
        for (Map.Entry<String, Object> entry : answer.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("questionId") && !key.equals("answer") && 
                !processedAnswer.containsKey(key)) {
                processedAnswer.put(key, entry.getValue());
            }
        }
        
        return processedAnswer;
    }


    private List<Map<String, Object>> formatSubmitsForResponse(List<SurveySubmit> submits) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (SurveySubmit submit : submits) {
            if (submit.getSurvey() == null) continue;
            
            Map<String, Object> submissionData = new HashMap<>();
            submissionData.put("survey", String.format("%05d", submit.getSurvey().getId()));
            
            // ทำความสะอาดคำตอบ เอาเฉพาะ questionId และ answer
            List<Map<String, Object>> cleanedAnswers = new ArrayList<>();
            for (Map<String, Object> answer : submit.getAnswers()) {
                Map<String, Object> cleanedAnswer = new HashMap<>();
                
                if (answer.containsKey("questionId")) {
                    cleanedAnswer.put("questionId", answer.get("questionId"));
                }
                
                if (answer.containsKey("answer")) {
                    cleanedAnswer.put("answer", answer.get("answer"));
                }
                
                cleanedAnswers.add(cleanedAnswer);
            }
            
            submissionData.put("answers", cleanedAnswers);
            submissionData.put("id", String.format("%05d", submit.getId()));
            submissionData.put("submittedAt", submit.getSubmittedAt());
            
            result.add(submissionData);
        }
        
        return result;
    }
    
    @GetMapping("/submits")
    @Operation(summary = "ดึงข้อมูลการตอบแบบสำรวจทั้งหมด")
    @ApiResponse(responseCode = "200", description = "All survey submits retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SurveySubmit.class)))
    public ResponseEntity<List<Map<String, Object>>> getAllSubmits() {
        List<SurveySubmit> submits = surveyService.getAllSubmits();
        List<Map<String, Object>> result = formatSubmitsForResponse(submits);
        return ResponseEntity.ok(result);
    }

} 