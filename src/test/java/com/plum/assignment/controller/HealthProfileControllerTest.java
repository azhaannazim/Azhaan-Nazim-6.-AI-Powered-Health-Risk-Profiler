package com.plum.assignment.controller;

import com.plum.assignment.model.*;
import com.plum.assignment.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthProfileControllerTest {
    
    @Mock
    private OcrParsingService ocrParsingService;
    
    @Mock
    private FactorExtractionService factorExtractionService;
    
    @Mock
    private RiskClassificationService riskClassificationService;
    
    @Mock
    private RecommendationService recommendationService;
    
    @InjectMocks
    private HealthProfileController healthProfileController;
    
    private SurveyResponse sampleSurveyResponse;
    private OcrParseResult sampleParseResult;
    private FactorExtractionResult sampleFactorResult;
    private RiskClassificationResult sampleRiskResult;
    private RecommendationResult sampleRecommendationResult;
    
    @BeforeEach
    void setUp() {
        // Setup sample survey response
        sampleSurveyResponse = new SurveyResponse();
        sampleSurveyResponse.setAge(42);
        sampleSurveyResponse.setSmoker(true);
        sampleSurveyResponse.setExercise("rarely");
        sampleSurveyResponse.setDiet("high sugar");
        
        // Setup sample parse result
        sampleParseResult = OcrParseResult.builder()
            .answers(sampleSurveyResponse)
            .missingFields(Arrays.asList())
            .confidence(0.92)
            .status("ok")
            .build();
        
        // Setup sample factor result
        sampleFactorResult = FactorExtractionResult.builder()
            .factors(Arrays.asList("smoking", "poor diet", "low exercise"))
            .confidence(0.88)
            .build();
        
        // Setup sample risk result
        sampleRiskResult = RiskClassificationResult.builder()
            .riskLevel("high")
            .score(78)
            .rationale(Arrays.asList("smoking", "high sugar diet", "low activity"))
            .build();
        
        // Setup sample recommendation result
        sampleRecommendationResult = RecommendationResult.builder()
            .riskLevel("high")
            .factors(Arrays.asList("smoking", "poor diet", "low exercise"))
            .recommendations(Arrays.asList("Quit smoking", "Reduce sugar", "Walk 30 mins daily"))
            .status("ok")
            .build();
    }
    
    @Test
    void testAnalyzeText_Success() {
        // Given
        String textInput = "{\"age\":42,\"smoker\":true,\"exercise\":\"rarely\",\"diet\":\"high sugar\"}";
        
        when(ocrParsingService.parseText(anyString())).thenReturn(sampleParseResult);
        when(factorExtractionService.extractFactors(any(SurveyResponse.class))).thenReturn(sampleFactorResult);
        when(riskClassificationService.classifyRisk(any(SurveyResponse.class), any(List.class))).thenReturn(sampleRiskResult);
        when(recommendationService.generateRecommendations(any(SurveyResponse.class), any(List.class), anyString())).thenReturn(sampleRecommendationResult);
        
        // When
        ResponseEntity<HealthProfileResult> response = healthProfileController.analyzeText(textInput);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getOverallStatus());
        assertNotNull(response.getBody().getParseResult());
        assertNotNull(response.getBody().getFactorResult());
        assertNotNull(response.getBody().getRiskResult());
        assertNotNull(response.getBody().getRecommendationResult());
    }
    
    @Test
    void testAnalyzeText_IncompleteData() {
        // Given
        String textInput = "{\"age\":42}";
        OcrParseResult incompleteResult = OcrParseResult.builder()
            .answers(sampleSurveyResponse)
            .missingFields(Arrays.asList("smoker", "exercise", "diet"))
            .confidence(0.25)
            .status("incomplete_profile")
            .reason(">50% fields missing")
            .build();
        
        when(ocrParsingService.parseText(anyString())).thenReturn(incompleteResult);
        
        // When
        ResponseEntity<HealthProfileResult> response = healthProfileController.analyzeText(textInput);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("incomplete", response.getBody().getOverallStatus());
        assertTrue(response.getBody().getMessage().contains("incomplete"));
    }
    
    @Test
    void testAnalyzeImage_Success() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(ocrParsingService.parseImage(any())).thenReturn(sampleParseResult);
        when(factorExtractionService.extractFactors(any(SurveyResponse.class))).thenReturn(sampleFactorResult);
        when(riskClassificationService.classifyRisk(any(SurveyResponse.class), any(List.class))).thenReturn(sampleRiskResult);
        when(recommendationService.generateRecommendations(any(SurveyResponse.class), any(List.class), anyString())).thenReturn(sampleRecommendationResult);
        
        // When
        ResponseEntity<HealthProfileResult> response = healthProfileController.analyzeImage(file);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getOverallStatus());
    }
    
    @Test
    void testAnalyzeJson_Success() {
        // Given
        when(factorExtractionService.extractFactors(any(SurveyResponse.class))).thenReturn(sampleFactorResult);
        when(riskClassificationService.classifyRisk(any(SurveyResponse.class), any(List.class))).thenReturn(sampleRiskResult);
        when(recommendationService.generateRecommendations(any(SurveyResponse.class), any(List.class), anyString())).thenReturn(sampleRecommendationResult);
        
        // When
        ResponseEntity<HealthProfileResult> response = healthProfileController.analyzeJson(sampleSurveyResponse);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getOverallStatus());
    }
    
    @Test
    void testParseText() {
        // Given
        String textInput = "Age: 42\nSmoker: yes\nExercise: rarely";
        
        when(ocrParsingService.parseText(anyString())).thenReturn(sampleParseResult);
        
        // When
        ResponseEntity<OcrParseResult> response = healthProfileController.parseText(textInput);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ok", response.getBody().getStatus());
    }
    
    @Test
    void testParseImage() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(ocrParsingService.parseImage(any())).thenReturn(sampleParseResult);
        
        // When
        ResponseEntity<OcrParseResult> response = healthProfileController.parseImage(file);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ok", response.getBody().getStatus());
    }
    
    @Test
    void testExtractFactors() {
        // Given
        when(factorExtractionService.extractFactors(any(SurveyResponse.class))).thenReturn(sampleFactorResult);
        
        // When
        ResponseEntity<FactorExtractionResult> response = healthProfileController.extractFactors(sampleSurveyResponse);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getFactors().size());
    }
    
    @Test
    void testClassifyRisk() {
        // Given
        when(riskClassificationService.classifyRisk(any(SurveyResponse.class), any(List.class))).thenReturn(sampleRiskResult);
        
        // When
        ResponseEntity<RiskClassificationResult> response = healthProfileController.classifyRisk(sampleSurveyResponse, "smoking,poor diet,low exercise");
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("high", response.getBody().getRiskLevel());
        assertEquals(78, response.getBody().getScore());
    }
    
    @Test
    void testGenerateRecommendations() {
        // Given
        when(recommendationService.generateRecommendations(any(SurveyResponse.class), any(List.class), anyString())).thenReturn(sampleRecommendationResult);
        
        // When
        ResponseEntity<RecommendationResult> response = healthProfileController.generateRecommendations(
            sampleSurveyResponse, "smoking,poor diet,low exercise", "high");
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("high", response.getBody().getRiskLevel());
        assertEquals(3, response.getBody().getRecommendations().size());
    }
    
    @Test
    void testHealth() {
        // When
        ResponseEntity<java.util.Map<String, String>> response = healthProfileController.health();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("healthy", response.getBody().get("status"));
        assertEquals("AI-Powered Health Risk Profiler", response.getBody().get("service"));
    }
}
