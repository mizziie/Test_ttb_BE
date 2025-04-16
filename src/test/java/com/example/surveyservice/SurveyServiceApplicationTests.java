package com.example.surveyservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SurveyServiceApplicationTests {

    @Test
    void contextLoads() {
        // เพียงแค่ทดสอบว่า Spring Context สามารถโหลดได้โดยไม่มีข้อผิดพลาด
    }
} 