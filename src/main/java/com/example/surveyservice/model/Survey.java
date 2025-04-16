package com.example.surveyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_seq_gen")
    @SequenceGenerator(name = "survey_seq_gen", sequenceName = "survey_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @NotBlank(message = "ชื่อแบบสำรวจไม่สามารถเป็นค่าว่างได้")
    private String title;
    
    @JsonIgnore
    private String description;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SurveyQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // เพิ่มเมธอดสำหรับจัดการ questions
    public void setQuestions(List<SurveyQuestion> questions) {
        this.questions.clear();
        if (questions != null) {
            for (SurveyQuestion question : questions) {
                addQuestion(question);
            }
        }
    }
    
    public void addQuestion(SurveyQuestion question) {
        if (question != null) {
            questions.add(question);
            question.setSurvey(this);
        }
    }
    
    public void removeQuestion(SurveyQuestion question) {
        if (question != null) {
            questions.remove(question);
            question.setSurvey(null);
        }
    }
} 