package com.example.surveyservice.controller;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveyQuestion;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurveyController.class)
public class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurveyService surveyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateSurvey() throws Exception {
        // สร้างข้อมูลแบบสำรวจสำหรับทดสอบ
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "แบบสำรวจความพึงพอใจ TTB Touch");
        requestData.put("description", "แบบสำรวจนี้จัดทำขึ้นเพื่อประเมินความพึงพอใจและรับฟังข้อเสนอแนะ");
        
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // สร้างคำถามที่ 1 (แบบ rank)
        Map<String, Object> question1 = new HashMap<>();
        question1.put("type", "rank");
        question1.put("title", "จากการใช้งาน TTB Touch ท่านพึงพอใจระดับใด");
        
        List<Map<String, String>> settings1 = new ArrayList<>();
        settings1.add(Map.of("key", "require", "value", "true"));
        settings1.add(Map.of("key", "min", "value", "1"));
        settings1.add(Map.of("key", "max", "value", "5"));
        settings1.add(Map.of("key", "min_title", "value", "1 คือไม่พอใจมาก"));
        settings1.add(Map.of("key", "max_title", "value", "5 คือพอใจมาก"));
        question1.put("settings", settings1);
        
        // สร้างคำถามที่ 2 (แบบ radio)
        Map<String, Object> question2 = new HashMap<>();
        question2.put("type", "radio");
        question2.put("title", "หัวข้อไหนของ TTB Touch ที่ท่านคิดว่าควรปรับปรุงมากที่สุด");
        
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(Map.of("title", "ความเร็วในการเปิด", "value", "1"));
        choices.add(Map.of("title", "การค้นหาเมนูที่ใช้บ่อย", "value", "2"));
        choices.add(Map.of("title", "การถอนเงินโดยไม่ใช้บัตร", "value", "3"));
        question2.put("choices", choices);
        
        List<Map<String, String>> settings2 = new ArrayList<>();
        settings2.add(Map.of("key", "require", "value", "true"));
        question2.put("settings", settings2);
        
        // สร้างคำถามที่ 3 (แบบ text)
        Map<String, Object> question3 = new HashMap<>();
        question3.put("type", "text");
        question3.put("title", "คำแนะนำอื่นๆ");
        
        List<Map<String, String>> settings3 = new ArrayList<>();
        settings3.add(Map.of("key", "max_length", "value", "500"));
        settings3.add(Map.of("key", "require", "value", "false"));
        question3.put("settings", settings3);
        
        // เพิ่มคำถามทั้งหมดเข้าไปในแบบสำรวจ
        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        requestData.put("questions", questions);
        
        // สร้าง mock ของ Survey ที่จะถูกคืนกลับจาก service
        Survey mockSurvey = new Survey();
        mockSurvey.setId(1L);
        mockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch");
        mockSurvey.setDescription("แบบสำรวจนี้จัดทำขึ้นเพื่อประเมินความพึงพอใจและรับฟังข้อเสนอแนะ");
        
        when(surveyService.createSurvey(any(Survey.class))).thenReturn(mockSurvey);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(post("/api/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.surveyId").exists())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    public void testGetSurveyById() throws Exception {
        // สร้าง mock ของแบบสำรวจที่จะถูกคืนกลับจาก service
        Survey mockSurvey = new Survey();
        mockSurvey.setId(1L);
        mockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch");
        mockSurvey.setCreatedAt(LocalDateTime.parse("2025-04-14T00:05:26"));
        mockSurvey.setUpdatedAt(LocalDateTime.parse("2025-04-14T00:05:26"));
        
        // สร้างรายการคำถาม
        List<SurveyQuestion> questions = new ArrayList<>();
        
        // คำถามที่ 1: แบบ rank
        SurveyQuestion q1 = new SurveyQuestion();
        q1.setTitle("จากการใช้งาน TTB Touch ท่านพึงพอใจระดับใด");
        q1.setType("rank");
        List<Map<String, String>> settings1 = new ArrayList<>();
        settings1.add(Map.of("key", "require", "value", "true"));
        settings1.add(Map.of("key", "min", "value", "1"));
        settings1.add(Map.of("key", "max", "value", "5"));
        settings1.add(Map.of("key", "min_title", "value", "1 คือไม่พอใจมาก"));
        settings1.add(Map.of("key", "max_title", "value", "5 คือพอใจมาก"));
        q1.setSettings(settings1);
        questions.add(q1);
        
        // คำถามที่ 2: แบบ radio
        SurveyQuestion q2 = new SurveyQuestion();
        q2.setTitle("หัวข้อไหนของ TTB Touch ที่ท่านคิดว่าควรปรับปรุงมากที่สุด");
        q2.setType("radio");
        List<Map<String, String>> settings2 = new ArrayList<>();
        settings2.add(Map.of("key", "require", "value", "true"));
        q2.setSettings(settings2);
        
        List<Map<String, Object>> choices2 = new ArrayList<>();
        choices2.add(Map.of("title", "ความเร็วในการเปิด", "value", "1"));
        choices2.add(Map.of("title", "การค้นหาเมนูที่ใช้บ่อย", "value", "2"));
        choices2.add(Map.of("title", "การถอนเงินโดยไม่ใช้บัตร", "value", "3"));
        q2.setChoices(choices2);
        questions.add(q2);
        
        // คำถามที่ 3: แบบ text
        SurveyQuestion q3 = new SurveyQuestion();
        q3.setTitle("คำแนะนำอื่นๆ");
        q3.setType("text");
        List<Map<String, String>> settings3 = new ArrayList<>();
        settings3.add(Map.of("key", "max_length", "value", "100"));
        settings3.add(Map.of("key", "require", "value", "false"));
        q3.setSettings(settings3);
        questions.add(q3);
        
        mockSurvey.setQuestions(questions);
        
        // กำหนดพฤติกรรมของ mock service - ใช้ Long เท่านั้น
        when(surveyService.getSurveyById(1L)).thenReturn(mockSurvey);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(get("/api/surveys/0001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("แบบสำรวจความพึงพอใจ TTB Touch"))
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions.length()").value(3))
                .andExpect(jsonPath("$.questions[0].title").value("จากการใช้งาน TTB Touch ท่านพึงพอใจระดับใด"))
                .andExpect(jsonPath("$.questions[0].type").value("rank"))
                .andExpect(jsonPath("$.questions[0].required").value(true))
                .andExpect(jsonPath("$.questions[1].title").value("หัวข้อไหนของ TTB Touch ที่ท่านคิดว่าควรปรับปรุงมากที่สุด"))
                .andExpect(jsonPath("$.questions[1].type").value("radio"))
                .andExpect(jsonPath("$.questions[1].choices").isArray())
                .andExpect(jsonPath("$.questions[1].choices.length()").value(3))
                .andExpect(jsonPath("$.questions[2].title").value("คำแนะนำอื่นๆ"))
                .andExpect(jsonPath("$.questions[2].type").value("text"))
                .andExpect(jsonPath("$.questions[2].required").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
    
    @Test
    public void testUpdateSurvey() throws Exception {
        // สร้างข้อมูลอัปเดตสำหรับทดสอบ
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "แบบสำรวจความพึงพอใจ TTB Touch ฉบับปรับปรุง");
        updateRequest.put("description", "แบบสำรวจนี้จัดทำขึ้นเพื่อประเมินความพึงพอใจผู้ใช้งาน");
        
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // สร้างคำถามที่ 1 (แบบ rank)
        Map<String, Object> question1 = new HashMap<>();
        question1.put("type", "rank");
        question1.put("title", "ท่านพึงพอใจการใช้งาน TTB Touch ในภาพรวมระดับใด");
        
        List<Map<String, String>> settings1 = new ArrayList<>();
        settings1.add(Map.of("key", "require", "value", "true"));
        settings1.add(Map.of("key", "min", "value", "1"));
        settings1.add(Map.of("key", "max", "value", "5"));
        question1.put("settings", settings1);
        
        // สร้างคำถามที่ 2 (แบบ radio)
        Map<String, Object> question2 = new HashMap<>();
        question2.put("type", "radio");
        question2.put("title", "ฟีเจอร์ไหนที่ท่านชื่นชอบมากที่สุด");
        
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(Map.of("title", "การโอนเงิน", "value", "1"));
        choices.add(Map.of("title", "การชำระบิล", "value", "2"));
        choices.add(Map.of("title", "การเช็คยอด", "value", "3"));
        question2.put("choices", choices);
        
        List<Map<String, String>> settings2 = new ArrayList<>();
        settings2.add(Map.of("key", "require", "value", "true"));
        question2.put("settings", settings2);
        
        // เพิ่มคำถามทั้งหมดเข้าไปในแบบสำรวจ
        questions.add(question1);
        questions.add(question2);
        updateRequest.put("questions", questions);
        
        // สร้าง mock ของ Survey ที่จะถูกคืนกลับจาก service หลังการอัปเดต
        Survey updatedMockSurvey = new Survey();
        updatedMockSurvey.setId(1L);
        updatedMockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch ฉบับปรับปรุง");
        updatedMockSurvey.setDescription("แบบสำรวจนี้จัดทำขึ้นเพื่อประเมินความพึงพอใจผู้ใช้งาน");
        updatedMockSurvey.setCreatedAt(LocalDateTime.parse("2025-04-13T16:44:48"));
        updatedMockSurvey.setUpdatedAt(LocalDateTime.parse("2025-04-13T17:41:11"));
        
        // สร้างคำถามสำหรับแบบสำรวจที่อัปเดตแล้ว
        List<SurveyQuestion> updatedQuestions = new ArrayList<>();
        
        // คำถามที่ 1: rank
        SurveyQuestion uq1 = new SurveyQuestion();
        uq1.setTitle("ท่านพึงพอใจการใช้งาน TTB Touch ในภาพรวมระดับใด");
        uq1.setType("rank");
        List<Map<String, String>> uSettings1 = List.of(
            Map.of("key", "require", "value", "true"),
            Map.of("key", "min", "value", "1"),
            Map.of("key", "max", "value", "5")
        );
        uq1.setSettings(uSettings1);
        updatedQuestions.add(uq1);
        
        // คำถามที่ 2: radio
        SurveyQuestion uq2 = new SurveyQuestion();
        uq2.setTitle("ฟีเจอร์ไหนที่ท่านชื่นชอบมากที่สุด");
        uq2.setType("radio");
        List<Map<String, String>> uSettings2 = List.of(Map.of("key", "require", "value", "true"));
        uq2.setSettings(uSettings2);
        
        List<Map<String, Object>> uChoices2 = new ArrayList<>();
        uChoices2.add(Map.of("title", "การโอนเงิน", "value", "1"));
        uChoices2.add(Map.of("title", "การชำระบิล", "value", "2"));
        uChoices2.add(Map.of("title", "การเช็คยอด", "value", "3"));
        uq2.setChoices(uChoices2);
        updatedQuestions.add(uq2);
        
        updatedMockSurvey.setQuestions(updatedQuestions);
        
        // กำหนดพฤติกรรมของ mock service
        when(surveyService.updateSurvey(eq(1L), any(Survey.class))).thenReturn(updatedMockSurvey);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(put("/api/surveys/00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("แบบสำรวจความพึงพอใจ TTB Touch ฉบับปรับปรุง"))
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.questions[0].title").value("ท่านพึงพอใจการใช้งาน TTB Touch ในภาพรวมระดับใด"))
                .andExpect(jsonPath("$.questions[0].type").value("rank"))
                .andExpect(jsonPath("$.questions[0].required").value(true))
                .andExpect(jsonPath("$.questions[0].settings").isArray())
                .andExpect(jsonPath("$.questions[0].settings.length()").value(3))
                .andExpect(jsonPath("$.questions[1].title").value("ฟีเจอร์ไหนที่ท่านชื่นชอบมากที่สุด"))
                .andExpect(jsonPath("$.questions[1].type").value("radio"))
                .andExpect(jsonPath("$.questions[1].choices").isArray())
                .andExpect(jsonPath("$.questions[1].choices.length()").value(3))
                .andExpect(jsonPath("$.questions[1].choices[0].title").value("การโอนเงิน"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    public void testGetSurveyAfterUpdate() throws Exception {
        // สร้าง mock ของแบบสำรวจที่ถูกอัปเดตแล้วและจะถูกคืนกลับจาก service
        Survey updatedMockSurvey = new Survey();
        updatedMockSurvey.setId(1L);
        updatedMockSurvey.setTitle("แบบสำรวจความพึงพอใจ TTB Touch ฉบับปรับปรุง");
        updatedMockSurvey.setCreatedAt(LocalDateTime.parse("2025-04-14T02:53:14"));
        updatedMockSurvey.setUpdatedAt(LocalDateTime.parse("2025-04-14T02:53:20"));
        
        // สร้างรายการคำถามสำหรับแบบสำรวจหลังอัปเดต
        List<SurveyQuestion> updatedQuestions = new ArrayList<>();
        
        // คำถามที่ 1: แบบ rank ที่อัปเดตแล้ว
        SurveyQuestion q1 = new SurveyQuestion();
        q1.setTitle("ท่านพึงพอใจการใช้งาน TTB Touch ในภาพรวมระดับใด");
        q1.setType("rank");
        List<Map<String, String>> settings1 = new ArrayList<>();
        settings1.add(Map.of("key", "require", "value", "true"));
        settings1.add(Map.of("key", "min", "value", "1"));
        settings1.add(Map.of("key", "max", "value", "5"));
        q1.setSettings(settings1);
        updatedQuestions.add(q1);
        
        // คำถามที่ 2: แบบ radio ที่อัปเดตแล้ว
        SurveyQuestion q2 = new SurveyQuestion();
        q2.setTitle("ฟีเจอร์ไหนที่ท่านชื่นชอบมากที่สุด");
        q2.setType("radio");
        List<Map<String, String>> settings2 = new ArrayList<>();
        settings2.add(Map.of("key", "require", "value", "true"));
        q2.setSettings(settings2);
        
        List<Map<String, Object>> choices2 = new ArrayList<>();
        choices2.add(Map.of("title", "การโอนเงิน", "value", "1"));
        choices2.add(Map.of("title", "การชำระบิล", "value", "2"));
        choices2.add(Map.of("title", "การเช็คยอด", "value", "3"));
        q2.setChoices(choices2);
        updatedQuestions.add(q2);
        
        updatedMockSurvey.setQuestions(updatedQuestions);
        
        // กำหนดพฤติกรรมของ mock service
        when(surveyService.getSurveyById(1L)).thenReturn(updatedMockSurvey);
        
        // ทำการเรียก API และตรวจสอบผลลัพธ์
        mockMvc.perform(get("/api/surveys/0001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("แบบสำรวจความพึงพอใจ TTB Touch ฉบับปรับปรุง"))
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.questions[0].title").value("ท่านพึงพอใจการใช้งาน TTB Touch ในภาพรวมระดับใด"))
                .andExpect(jsonPath("$.questions[0].type").value("rank"))
                .andExpect(jsonPath("$.questions[0].required").value(true))
                .andExpect(jsonPath("$.questions[0].settings").isArray())
                .andExpect(jsonPath("$.questions[0].settings.length()").value(3))
                .andExpect(jsonPath("$.questions[1].title").value("ฟีเจอร์ไหนที่ท่านชื่นชอบมากที่สุด"))
                .andExpect(jsonPath("$.questions[1].type").value("radio"))
                .andExpect(jsonPath("$.questions[1].choices").isArray())
                .andExpect(jsonPath("$.questions[1].choices.length()").value(3))
                .andExpect(jsonPath("$.questions[1].choices[0].title").value("การโอนเงิน"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    public void testDeleteSurvey() throws Exception {
        // กำหนดพฤติกรรมของ mock service ให้ไม่ทำอะไรเมื่อเรียก deleteSurvey
        doNothing().when(surveyService).deleteSurvey(1L);
        
        // กำหนดให้ surveyService.getSurveyById คืนค่า null หลังจากลบแล้ว
        when(surveyService.getSurveyById(1L)).thenReturn(null);
        
        // ทำการเรียก API เพื่อลบแบบสำรวจ
        mockMvc.perform(delete("/api/surveys/00001"))
                .andDo(print())
                .andExpect(status().isOk());
        
        // ทดสอบการเรียก API ค้นหาแบบสำรวจหลังจากลบแล้ว
        // เมื่อไม่พบข้อมูล API อาจไม่ส่ง JSON กลับมาเลย
        mockMvc.perform(get("/api/surveys/00001"))
                .andDo(print())
                .andExpect(status().isOk());
        
        // ทวนสอบว่า getSurveyById ถูกเรียกด้วย ID = 1 จริงๆ
        org.mockito.Mockito.verify(surveyService).getSurveyById(1L);
    }
} 