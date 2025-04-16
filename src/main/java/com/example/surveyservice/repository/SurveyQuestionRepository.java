package com.example.surveyservice.repository;

import com.example.surveyservice.model.Survey;
import com.example.surveyservice.model.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findBySurvey(Survey survey);
} 