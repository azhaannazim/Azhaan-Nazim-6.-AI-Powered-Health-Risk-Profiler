package com.plum.assignment.service;

import com.plum.assignment.model.RecommendationResult;
import com.plum.assignment.model.SurveyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    
    private RecommendationService recommendationService;
    
    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService();
    }
    
    @Test
    void testGenerateRecommendations_HighRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(65);
        response.setSmoker(true);
        response.setExercise("never");
        response.setDiet("high sugar");
        
        List<String> factors = Arrays.asList("smoking", "no exercise", "poor diet", "advanced age");
        String riskLevel = "high";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertEquals("high", result.getRiskLevel());
        assertEquals("ok", result.getStatus());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().size() > 0);
        assertTrue(result.getRecommendations().contains("Quit smoking immediately"));
        assertTrue(result.getRecommendations().contains("Consult with a healthcare provider immediately"));
    }
    
    @Test
    void testGenerateRecommendations_ModerateRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(45);
        response.setSmoker(false);
        response.setExercise("occasionally");
        response.setDiet("processed");
        
        List<String> factors = Arrays.asList("low exercise", "poor diet", "middle age");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertEquals("moderate", result.getRiskLevel());
        assertEquals("ok", result.getStatus());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().contains("Start with 30 minutes of daily walking"));
        assertTrue(result.getRecommendations().contains("Schedule regular health check-ups"));
    }
    
    @Test
    void testGenerateRecommendations_LowRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(35);
        response.setSmoker(false);
        response.setExercise("frequent");
        response.setDiet("balanced");
        
        List<String> factors = Arrays.asList("good exercise", "good diet");
        String riskLevel = "low";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertEquals("low", result.getRiskLevel());
        assertEquals("ok", result.getStatus());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().contains("Maintain current healthy habits"));
    }
    
    @Test
    void testGenerateRecommendations_MinimalRisk() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(25);
        response.setSmoker(false);
        response.setExercise("daily");
        response.setDiet("healthy");
        
        List<String> factors = Arrays.asList("excellent exercise", "excellent diet");
        String riskLevel = "minimal";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertEquals("minimal", result.getRiskLevel());
        assertEquals("ok", result.getStatus());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().contains("Continue excellent health practices"));
    }
    
    @Test
    void testGenerateRecommendations_SmokingFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(40);
        response.setSmoker(true);
        response.setExercise("moderate");
        response.setDiet("balanced");
        
        List<String> factors = Arrays.asList("smoking");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Quit smoking immediately"));
        assertTrue(result.getRecommendations().contains("Consider smoking cessation programs"));
    }
    
    @Test
    void testGenerateRecommendations_ExerciseFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(35);
        response.setSmoker(false);
        response.setExercise("rarely");
        response.setDiet("balanced");
        
        List<String> factors = Arrays.asList("low exercise");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Start with 30 minutes of daily walking"));
        assertTrue(result.getRecommendations().contains("Gradually increase physical activity"));
    }
    
    @Test
    void testGenerateRecommendations_DietFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(30);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("high sugar");
        
        List<String> factors = Arrays.asList("poor diet", "high sugar intake");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Reduce sugar and processed food intake"));
        assertTrue(result.getRecommendations().contains("Increase fruits and vegetables"));
    }
    
    @Test
    void testGenerateRecommendations_AlcoholFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(45);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        response.setAlcohol("heavy");
        
        List<String> factors = Arrays.asList("excessive alcohol");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Reduce alcohol consumption"));
        assertTrue(result.getRecommendations().contains("Follow recommended daily limits"));
    }
    
    @Test
    void testGenerateRecommendations_StressFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(40);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        response.setStress("high");
        
        List<String> factors = Arrays.asList("high stress");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Practice stress management techniques"));
        assertTrue(result.getRecommendations().contains("Consider meditation or yoga"));
    }
    
    @Test
    void testGenerateRecommendations_SleepFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(35);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        response.setSleep("poor");
        
        List<String> factors = Arrays.asList("poor sleep");
        String riskLevel = "moderate";
        
        // When
        RecommendationResult result = recommendationService.generateRecommendations(response, factors, riskLevel);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRecommendations().contains("Maintain consistent sleep schedule"));
        assertTrue(result.getRecommendations().contains("Create a relaxing bedtime routine"));
    }
    
    @Test
    void testGeneratePersonalizedRecommendations() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(30);
        response.setSmoker(false);
        response.setExercise("never");
        response.setDiet("high sugar");
        response.setStress("high");
        
        // When
        List<String> recommendations = recommendationService.generatePersonalizedRecommendations(response);
        
        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.size() > 0);
        assertTrue(recommendations.contains("Start with 10-minute daily walks"));
        assertTrue(recommendations.contains("Replace sugary snacks with fruits"));
        assertTrue(recommendations.contains("Practice deep breathing exercises daily"));
    }
    
    @Test
    void testGetRecommendationPriority() {
        // When & Then
        assertEquals("Immediate action required", recommendationService.getRecommendationPriority("high"));
        assertEquals("Action recommended within 1-3 months", recommendationService.getRecommendationPriority("moderate"));
        assertEquals("Gradual improvement over 3-6 months", recommendationService.getRecommendationPriority("low"));
        assertEquals("Maintenance and prevention focus", recommendationService.getRecommendationPriority("minimal"));
        assertEquals("Standard recommendations", recommendationService.getRecommendationPriority("unknown"));
    }
}
