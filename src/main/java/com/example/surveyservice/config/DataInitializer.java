package com.example.surveyservice.config;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveyQuestion;
import com.example.surveyservice.service.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final SurveyService surveyService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initData() {
        try {
            // อ่านข้อมูลจากไฟล์ init-data.json
            ClassPathResource resource = new ClassPathResource("init-data.json");
            InputStream inputStream = resource.getInputStream();
            
            // แปลงข้อมูล JSON เป็น Map
            Map<String, Object> surveyData = objectMapper.readValue(inputStream, Map.class);
            
            // สร้างแบบสำรวจ
            Survey survey = new Survey();
            survey.setTitle((String) surveyData.get("title"));
            survey.setDescription((String) surveyData.get("description"));
            
            // แปลงข้อมูลคำถาม
            List<Map<String, Object>> questionDataList = (List<Map<String, Object>>) surveyData.get("questions");
            List<SurveyQuestion> questions = new ArrayList<>();
            
            for (Map<String, Object> questionData : questionDataList) {
                SurveyQuestion question = new SurveyQuestion();
                question.setTitle((String) questionData.get("title"));
                question.setType((String) questionData.get("type"));
                
                // บันทึกการตั้งค่า
                if (questionData.containsKey("settings")) {
                    question.setSettings((List<Map<String, String>>) questionData.get("settings"));
                }
                
                // บันทึกตัวเลือก (ถ้ามี)
                if (questionData.containsKey("choices")) {
                    question.setChoices((List<Map<String, Object>>) questionData.get("choices"));
                }
                
                questions.add(question);
            }
            
            survey.setQuestions(questions);
            
            // บันทึกแบบสำรวจ
            surveyService.createSurvey(survey);
            
            log.info("โหลดข้อมูลเริ่มต้นเรียบร้อยแล้ว");
        } catch (IOException e) {
            log.error("เกิดข้อผิดพลาดในการโหลดข้อมูลเริ่มต้น", e);
        }
    }
} 