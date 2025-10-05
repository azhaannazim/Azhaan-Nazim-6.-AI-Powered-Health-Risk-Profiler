package com.plum.assignment.service;

import com.plum.assignment.model.RiskClassificationResult;
import com.plum.assignment.model.SurveyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RiskClassificationServiceTest {
    
    private RiskClassificationService riskClassificationService;
    
    @BeforeEach
    void setUp() {
        riskClassificationService = new RiskClassificationService();
    }
    
    @Test
    void testClassifyRisk_HighRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(65);
        response.setSmoker(true);
        response.setExercise("never");
        response.setDiet("high sugar");
        response.setAlcohol("heavy");
        
        List<String> factors = Arrays.asList("smoking", "no exercise", "poor diet", "excessive alcohol", "advanced age");
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertEquals("high", result.getRiskLevel());
        assertTrue(result.getScore() >= 70);
        assertNotNull(result.getRationale());
        assertTrue(result.getRationale().contains("smoking"));
    }
    
    @Test
    void testClassifyRisk_ModerateRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(45);
        response.setSmoker(false);
        response.setExercise("occasionally");
        response.setDiet("processed");
        response.setStress("moderate");
        
        List<String> factors = Arrays.asList("low exercise", "poor diet", "moderate stress", "middle age");
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertEquals("moderate", result.getRiskLevel());
        assertTrue(result.getScore() >= 40 && result.getScore() < 70);
        assertNotNull(result.getRationale());
    }
    
    @Test
    void testClassifyRisk_LowRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(35);
        response.setSmoker(false);
        response.setExercise("frequent");
        response.setDiet("balanced");
        response.setStress("low");
        
        List<String> factors = Arrays.asList("good exercise", "good diet", "low stress");
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertEquals("minimal", result.getRiskLevel());
        assertTrue(result.getScore() < 20);
        assertNotNull(result.getRationale());
    }
    
    @Test
    void testClassifyRisk_MinimalRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(25);
        response.setSmoker(false);
        response.setExercise("daily");
        response.setDiet("healthy");
        response.setSleep("excellent");
        response.setAlcohol("never");
        
        List<String> factors = Arrays.asList("excellent exercise", "excellent diet", "excellent sleep", "no alcohol");
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertEquals("minimal", result.getRiskLevel());
        assertTrue(result.getScore() < 20);
        assertNotNull(result.getRationale());
    }
    
    @Test
    void testClassifyRisk_ScoreBounds() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(30);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        
        List<String> factors = Arrays.asList("moderate exercise", "good diet");
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getScore() >= 0 && result.getScore() <= 100);
    }
    
    @Test
    void testClassifyRisk_EmptyFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(30);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        
        List<String> factors = Arrays.asList();
        
        // When
        RiskClassificationResult result = riskClassificationService.classifyRisk(response, factors);
        
        // Then
        assertNotNull(result);
        assertEquals("minimal", result.getRiskLevel());
        assertTrue(result.getScore() < 20);
    }
    
    @Test
    void testClassifyRisk_AgeBasedScoring() {
        // Given - Same factors, different ages
        List<String> factors = Arrays.asList("moderate exercise", "good diet");
        
        SurveyResponse youngResponse = new SurveyResponse();
        youngResponse.setAge(25);
        youngResponse.setSmoker(false);
        youngResponse.setExercise("moderate");
        youngResponse.setDiet("balanced");
        
        SurveyResponse oldResponse = new SurveyResponse();
        oldResponse.setAge(70);
        oldResponse.setSmoker(false);
        oldResponse.setExercise("moderate");
        oldResponse.setDiet("balanced");
        
        // When
        RiskClassificationResult youngResult = riskClassificationService.classifyRisk(youngResponse, factors);
        RiskClassificationResult oldResult = riskClassificationService.classifyRisk(oldResponse, factors);
        
        // Then
        assertNotNull(youngResult);
        assertNotNull(oldResult);
        assertTrue(oldResult.getScore() > youngResult.getScore());
    }
    
    @Test
    void testGetRiskLevelDescription() {
        // When & Then
        assertEquals("High risk - Multiple risk factors present requiring immediate attention", 
            riskClassificationService.getRiskLevelDescription("high"));
        assertEquals("Moderate risk - Some risk factors present, lifestyle changes recommended", 
            riskClassificationService.getRiskLevelDescription("moderate"));
        assertEquals("Low risk - Few risk factors, maintain current healthy habits", 
            riskClassificationService.getRiskLevelDescription("low"));
        assertEquals("Minimal risk - Excellent health profile, continue current practices", 
            riskClassificationService.getRiskLevelDescription("minimal"));
        assertEquals("Unknown risk level", 
            riskClassificationService.getRiskLevelDescription("unknown"));
    }
    
    @Test
    void testGetRiskScoreInterpretation() {
        // When & Then
        assertTrue(riskClassificationService.getRiskScoreInterpretation(85).contains("Very high risk"));
        assertTrue(riskClassificationService.getRiskScoreInterpretation(65).contains("High risk"));
        assertTrue(riskClassificationService.getRiskScoreInterpretation(50).contains("Moderate risk"));
        assertTrue(riskClassificationService.getRiskScoreInterpretation(25).contains("Low risk"));
        assertTrue(riskClassificationService.getRiskScoreInterpretation(10).contains("Very low risk"));
    }
}
