package com.example.surveyservice.controller;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveySubmit;
import com.example.surveyservice.service.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurveySubmitController.class)
public class SurveySubmitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSubmitSurvey() throws Exception {
        // สร้างข้อมูลสำหรับทดสอบการส่งคำตอบแบบสำรวจ
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("surveyId", "00001");
        submitRequest.put("surveyTitle", "แบบสำรวจความพึงพอใจ TTB Touch");
        submitRequest.put("submittedAt", "2025-04-12T09:54:25.607Z");
        
        List<Map<String, Object>> answers = new ArrayList<>();
        
        // คำตอบที่ 1: แบบ rank
        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("questionId", "q1");
        answer1.put("questionText", "จากการใช้งาน TTB Touch ท่านพึงพอใจระดับใด");
        answer1.put("questionType", "rank");
        answer1.put("answer", Map.of("value", 2));
        answers.add(answer1);
        
        // คำตอบที่ 2: แบบ radio
        Map<String, Object> answer2 = new HashMap<>();
        answer2.put("questionId", "q2");
        answer2.put("questionText", "หัวข้อไหนของ TTB Touch ที่ท่านคิดว่าควรปรับปรุงมากที่สุด");
        answer2.put("questionType", "radio");
        answer2.put("answer", Map.of("value", "การค้นหาเมนูที่ใช้บ่อย"));
        answers.add(answer2);
        
        // คำตอบที่ 3: แบบ text
        Map<String, Object> answer3 = new HashMap<>();
        answer3.put("questionId", "q3");
        answer3.put("questionText", "คำแนะนำอื่นๆ");
        answer3.put("questionType", "text");
        answer3.put("answer", Map.of("value", "test"));
        answers.add(answer3);
        
        submitRequest.put("answers", answers);
        
        // สร้าง mock ของ Survey สำหรับทดสอบ
        Survey mockSurvey = new Survey();
        mockSurvey.setId(1L);
        mockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch");
        
        // สร้าง mock ของ SurveySubmit ที่จะถูกคืนกลับจาก service
        SurveySubmit mockSubmit = SurveySubmit.builder()
                .id(1L)
                .survey(mockSurvey)
                .submittedAt(LocalDateTime.parse("2025-04-12T09:54:25.607"))
                .build();
        
        // กำหนดพฤติกรรมของ mock service
        when(surveyService.getSurveyById(1L)).thenReturn(mockSurvey);
        when(surveyService.submitSurvey(eq(1L), any(SurveySubmit.class))).thenReturn(mockSubmit);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(post("/api/submit-survey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey submitted successfully"));
    }
    
    @Test
    public void testSubmitSurveyInArrayFormat() throws Exception {
        // สร้างข้อมูลในรูปแบบอาร์เรย์สำหรับทดสอบการส่งคำตอบแบบสำรวจ
        List<Object> submitRequestArray = new ArrayList<>();
        
        // ส่วนแรก: ข้อมูลแบบสำรวจ
        Map<String, Object> surveyInfo = new HashMap<>();
        surveyInfo.put("survey", "00001");
        submitRequestArray.add(surveyInfo);
        
        // ส่วนที่สอง: คำตอบ
        Map<String, Object> answersContainer = new HashMap<>();
        List<Map<String, Object>> answers = new ArrayList<>();
        
        // คำตอบที่ 1: แบบ rank
        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("questionId", "q1");
        answer1.put("answer", Map.of("value", 1));
        answers.add(answer1);
        
        // คำตอบที่ 2: แบบ radio
        Map<String, Object> answer2 = new HashMap<>();
        answer2.put("questionId", "q2");
        answer2.put("answer", Map.of("value", "การค้นหาเมนูที่ใช้บ่อย"));
        answers.add(answer2);
        
        // คำตอบที่ 3: แบบ text
        Map<String, Object> answer3 = new HashMap<>();
        answer3.put("questionId", "q3");
        answer3.put("answer", Map.of("value", "ฟ"));
        answers.add(answer3);
        
        answersContainer.put("answers", answers);
        submitRequestArray.add(answersContainer);
        
        // สร้าง mock ของ Survey สำหรับทดสอบ
        Survey mockSurvey = new Survey();
        mockSurvey.setId(1L);
        mockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch");
        
        // สร้าง mock ของ SurveySubmit ที่จะถูกคืนกลับจาก service
        SurveySubmit mockSubmit = SurveySubmit.builder()
                .id(1L)
                .survey(mockSurvey)
                .submittedAt(LocalDateTime.now())
                .build();
        
        // กำหนดพฤติกรรมของ mock service
        when(surveyService.getSurveyById(1L)).thenReturn(mockSurvey);
        when(surveyService.submitSurvey(eq(1L), any(SurveySubmit.class))).thenReturn(mockSubmit);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(post("/api/submit-survey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitRequestArray)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Survey submitted successfully"));
    }
} 