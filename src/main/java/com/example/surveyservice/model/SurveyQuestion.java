package com.example.surveyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyQuestion {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_question_seq_gen")
    @SequenceGenerator(name = "survey_question_seq_gen", sequenceName = "survey_question_seq", initialValue = 1, allocationSize = 1)
    @JsonIgnore
    private Long id;

    @NotBlank(message = "หัวข้อคำถามไม่สามารถเป็นค่าว่างได้")
    private String title;

    @NotBlank(message = "ประเภทคำถามไม่สามารถเป็นค่าว่างได้")
    private String type; 
    
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String choicesJson;
    
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String settingsJson; 

    @ManyToOne
    @JoinColumn(name = "survey_id")
    @JsonIgnore
    private Survey survey;

    
    @Transient
    public List<Map<String, Object>> getChoices() {
        if (choicesJson == null || choicesJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(choicesJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    public void setChoices(List<Map<String, Object>> choices) {
        try {
            this.choicesJson = objectMapper.writeValueAsString(choices);
        } catch (JsonProcessingException e) {
            this.choicesJson = "[]";
        }
    }
    
    
    @Transient
    public List<Map<String, String>> getSettings() {
        if (settingsJson == null || settingsJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(settingsJson, new TypeReference<List<Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    public void setSettings(List<Map<String, String>> settings) {
        try {
            this.settingsJson = objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            this.settingsJson = "[]";
        }
    }
    
    
    @Transient
    public boolean isRequired() {
        for (Map<String, String> setting : getSettings()) {
            if ("require".equals(setting.get("key")) && "true".equals(setting.get("value"))) {
                return true;
            }
        }
        return false;
    }
    
    @Transient
    public void setRequired(boolean required) {
        List<Map<String, String>> settings = getSettings();
        boolean foundRequireSetting = false;
        
        for (Map<String, String> setting : settings) {
            if ("require".equals(setting.get("key"))) {
                setting.put("value", required ? "true" : "false");
                foundRequireSetting = true;
                break;
            }
        }
        
        if (!foundRequireSetting && required) {
            Map<String, String> requireSetting = Map.of("key", "require", "value", "true");
            settings.add(requireSetting);
        }
        
        setSettings(settings);
    }
} 