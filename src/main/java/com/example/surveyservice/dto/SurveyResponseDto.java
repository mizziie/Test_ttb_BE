package com.example.surveyservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDto {
    @NotBlank(message = "กรุณาระบุ ID ของแบบสำรวจ")
    @Pattern(regexp = "\\d{5}", message = "ID ต้องเป็นตัวเลข 5 หลัก")
    private String surveyId;
    
    @NotBlank(message = "กรุณาระบุชื่อของแบบสำรวจ")
    private String surveyTitle;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime submittedAt;
    
    @NotEmpty(message = "กรุณาระบุคำตอบอย่างน้อย 1 ข้อ")
    @Valid
    private List<AnswerDto> answers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        @NotBlank(message = "กรุณาระบุ ID ของคำถาม")
        private String questionId;
        
        @NotBlank(message = "กรุณาระบุข้อความของคำถาม")
        private String questionText;
        
        @NotBlank(message = "กรุณาระบุประเภทของคำถาม")
        private String questionType;
        
        @NotNull(message = "กรุณาระบุคำตอบ")
        private Map<String, Object> answer;
    }
} 