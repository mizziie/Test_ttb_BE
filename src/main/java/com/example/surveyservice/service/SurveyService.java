package com.example.surveyservice.service;

import com.example.surveyservice.model.*;
import com.example.surveyservice.repository.SurveyQuestionRepository;
import com.example.surveyservice.repository.SurveyRepository;
import com.example.surveyservice.repository.SurveySubmitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveySubmitRepository surveySubmitRepository;

    // ==================== Survey Management ====================
    
    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }

    public Survey getSurveyById(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ไม่พบแบบสำรวจหมายเลข: " + String.format("%05d", id)));
    }

    @Transactional
    public Survey createSurvey(Survey survey) {
        // เชื่อมโยงคำถามกับแบบสำรวจโดยใช้เมธอดที่เพิ่มใน model
        if (survey.getQuestions() != null) {
            List<SurveyQuestion> questions = new ArrayList<>(survey.getQuestions());
            survey.getQuestions().clear();
            
            for (SurveyQuestion question : questions) {
                survey.addQuestion(question);
            }
        }
        
        return surveyRepository.save(survey);
    }

    @Transactional
    public Survey updateSurvey(Long id, Survey surveyDetails) {
        Survey survey = getSurveyById(id);
        
        survey.setTitle(surveyDetails.getTitle());
        survey.setDescription(surveyDetails.getDescription());
        
        // อัปเดตคำถาม
        if (surveyDetails.getQuestions() != null) {
            // ใช้เมธอด setQuestions แทนการใช้ clear และ addAll
            List<SurveyQuestion> newQuestions = new ArrayList<>();
            
            for (SurveyQuestion questionDetails : surveyDetails.getQuestions()) {
                SurveyQuestion question;
                
                if (questionDetails.getId() != null) {
                    // อัปเดตคำถามที่มีอยู่แล้ว
                    question = surveyQuestionRepository.findById(questionDetails.getId())
                            .orElse(new SurveyQuestion());
                } else {
                    // สร้างคำถามใหม่
                    question = new SurveyQuestion();
                }
                
                question.setTitle(questionDetails.getTitle());
                question.setType(questionDetails.getType());
                question.setChoicesJson(questionDetails.getChoicesJson());
                question.setSettingsJson(questionDetails.getSettingsJson());
                
                newQuestions.add(question);
            }
            
            survey.setQuestions(newQuestions);
        }
        
        return surveyRepository.save(survey);
    }

    @Transactional
    public void deleteSurvey(Long id) {
        Survey survey = getSurveyById(id);
        
        // ลบข้อมูลคำตอบที่เกี่ยวข้องกับแบบสอบถามนี้ก่อน
        List<SurveySubmit> submits = surveySubmitRepository.findBySurveyId(id);
        if (!submits.isEmpty()) {
            surveySubmitRepository.deleteAll(submits);
        }
        
        // ลบแบบสอบถาม
        surveyRepository.delete(survey);
    }
    
    // ==================== Survey Question Management ====================
    
    public List<SurveyQuestion> getQuestionsBySurveyId(Long surveyId) {
        Survey survey = getSurveyById(surveyId);
        return surveyQuestionRepository.findBySurvey(survey);
    }
    
    public SurveyQuestion getQuestionById(Long id) {
        return surveyQuestionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ไม่พบคำถามหมายเลข: " + id));
    }
    
    @Transactional
    public SurveyQuestion createQuestion(Long surveyId, SurveyQuestion question) {
        Survey survey = getSurveyById(surveyId);
        question.setSurvey(survey);
        return surveyQuestionRepository.save(question);
    }
    
    @Transactional
    public SurveyQuestion updateQuestion(Long id, SurveyQuestion questionDetails) {
        SurveyQuestion question = getQuestionById(id);
        
        question.setTitle(questionDetails.getTitle());
        question.setType(questionDetails.getType());
        
        // อัปเดตตัวเลือกและการตั้งค่า
        if (questionDetails.getChoicesJson() != null) {
            question.setChoicesJson(questionDetails.getChoicesJson());
        }
        
        if (questionDetails.getSettingsJson() != null) {
            question.setSettingsJson(questionDetails.getSettingsJson());
        }
        
        return surveyQuestionRepository.save(question);
    }
    
    @Transactional
    public void deleteQuestion(Long id) {
        surveyQuestionRepository.deleteById(id);
    }
    
    // ==================== Survey Submit Management ====================
    
    public List<SurveySubmit> getAllSubmits() {
        return surveySubmitRepository.findAll();
    }
    
    public List<SurveySubmit> getSubmitsBySurveyId(Long surveyId) {
        return surveySubmitRepository.findBySurveyId(surveyId);
    }
    
    public SurveySubmit getSubmitById(Long id) {
        return surveySubmitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ไม่พบการตอบแบบสำรวจหมายเลข: " + id));
    }
    
    @Transactional
    public SurveySubmit submitSurvey(Long surveyId, SurveySubmit submit) {
        Survey survey = getSurveyById(surveyId);
        submit.setSurvey(survey);
        return surveySubmitRepository.save(submit);
    }
    
    @Transactional
    public SurveySubmit updateSubmit(Long id, SurveySubmit submitDetails) {
        SurveySubmit submit = getSubmitById(id);
        
        // อัปเดตคำตอบ
        if (submitDetails.getAnswersJson() != null) {
            submit.setAnswersJson(submitDetails.getAnswersJson());
        }
        
        return surveySubmitRepository.save(submit);
    }
    
    @Transactional
    public void deleteSubmit(Long id) {
        surveySubmitRepository.deleteById(id);
    }
} 