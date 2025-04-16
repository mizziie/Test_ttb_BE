package com.example.surveyservice.repository;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveySubmit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveySubmitRepository extends JpaRepository<SurveySubmit, Long> {
    List<SurveySubmit> findBySurvey(Survey survey);
    List<SurveySubmit> findBySurveyId(Long surveyId);
} 