package com.plum.assignment.service;

import com.plum.assignment.model.FactorExtractionResult;
import com.plum.assignment.model.SurveyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FactorExtractionServiceTest {
    
    private FactorExtractionService factorExtractionService;
    
    @BeforeEach
    void setUp() {
        factorExtractionService = new FactorExtractionService();
    }
    
    @Test
    void testExtractFactors_Smoker() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(42);
        response.setSmoker(true);
        response.setExercise("rarely");
        response.setDiet("high sugar");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("smoking"));
        assertTrue(result.getFactors().contains("low exercise"));
        assertTrue(result.getFactors().contains("poor diet"));
        assertTrue(result.getConfidence() > 0.0);
    }
    
    @Test
    void testExtractFactors_NonSmoker() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(30);
        response.setSmoker(false);
        response.setExercise("daily");
        response.setDiet("healthy");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertFalse(result.getFactors().contains("smoking"));
        assertTrue(result.getFactors().contains("excellent exercise"));
        assertTrue(result.getFactors().contains("excellent diet"));
    }
    
    @Test
    void testExtractFactors_AlcoholFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(35);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        response.setAlcohol("heavy");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("excessive alcohol"));
    }
    
    @Test
    void testExtractFactors_StressFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(40);
        response.setSmoker(false);
        response.setExercise("occasionally");
        response.setDiet("processed");
        response.setStress("high");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("high stress"));
        assertTrue(result.getFactors().contains("poor diet"));
    }
    
    @Test
    void testExtractFactors_SleepFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(45);
        response.setSmoker(false);
        response.setExercise("frequent");
        response.setDiet("vegetarian");
        response.setSleep("poor");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("poor sleep"));
        assertTrue(result.getFactors().contains("good exercise"));
        assertTrue(result.getFactors().contains("good diet"));
    }
    
    @Test
    void testExtractFactors_FamilyHistory() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(50);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("balanced");
        response.setFamilyHistory("diabetes and heart disease");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("family history"));
    }
    
    @Test
    void testExtractFactors_ChronicConditions() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(55);
        response.setSmoker(false);
        response.setExercise("rarely");
        response.setDiet("high fat");
        response.setChronicConditions("diabetes, hypertension");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("chronic conditions"));
        assertTrue(result.getFactors().contains("poor diet"));
    }
    
    @Test
    void testExtractFactors_AgeFactors() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(70);
        response.setSmoker(false);
        response.setExercise("moderate");
        response.setDiet("healthy");
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getFactors().contains("advanced age"));
    }
    
    @Test
    void testExtractFactors_MinimalData() {
        // Given
        SurveyResponse response = new SurveyResponse();
        response.setAge(25);
        response.setSmoker(false);
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertTrue(result.getConfidence() >= 0.0);
    }
    
    @Test
    void testExtractFactors_EmptyResponse() {
        // Given
        SurveyResponse response = new SurveyResponse();
        
        // When
        FactorExtractionResult result = factorExtractionService.extractFactors(response);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getFactors());
        assertEquals(0.0, result.getConfidence());
    }
}
