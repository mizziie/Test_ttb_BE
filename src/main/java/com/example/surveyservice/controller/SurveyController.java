package com.example.surveyservice.controller;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveyQuestion;
import com.example.surveyservice.model.SurveySubmit;
import com.example.surveyservice.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey API", description = "API สำหรับจัดการแบบสำรวจและคำตอบ")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping
    @Operation(summary = "ดึงข้อมูลแบบสำรวจทั้งหมด")
    public ResponseEntity<List<Survey>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดึงข้อมูลแบบสำรวจตาม ID")
    public ResponseEntity<Survey> getSurveyById(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurveyById(id));
    }

    @PostMapping
    @Operation(summary = "สร้างแบบสำรวจใหม่")
    public ResponseEntity<?> createSurvey(@Valid @RequestBody Survey survey) {
        try {
            // ตรวจสอบข้อมูลแบบสำรวจ
            if (survey.getTitle() == null || survey.getTitle().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "กรุณาระบุชื่อแบบสำรวจ");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // ตรวจสอบคำถามในแบบสำรวจ
            if (survey.getQuestions() != null) {
                for (int i = 0; i < survey.getQuestions().size(); i++) {
                    SurveyQuestion question = survey.getQuestions().get(i);
                    if (question.getTitle() == null || question.getTitle().isEmpty()) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "กรุณาระบุคำถามที่ " + (i + 1));
                        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                    }
                    if (question.getType() == null || question.getType().isEmpty()) {
                        question.setType("text"); // ตั้งค่าเริ่มต้นเป็น text
                    }
                }
            }

            // บันทึกแบบสำรวจ
            Survey createdSurvey = surveyService.createSurvey(survey);
            
            // สร้างข้อความตอบกลับ
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "สร้างแบบสำรวจเรียบร้อยแล้ว");
            response.put("surveyId", String.format("%05d", createdSurvey.getId()));
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "เกิดข้อผิดพลาดในระบบ: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "อัพเดตแบบสำรวจที่มีอยู่")
    public ResponseEntity<Survey> updateSurvey(@PathVariable Long id, @Valid @RequestBody Survey survey) {
        return ResponseEntity.ok(surveyService.updateSurvey(id, survey));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ลบแบบสำรวจ")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long id) {
        try {
            surveyService.deleteSurvey(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ลบแบบสำรวจเรียบร้อยแล้ว");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "เกิดข้อผิดพลาดในการลบแบบสำรวจ: " + e.getMessage());
            
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

} 