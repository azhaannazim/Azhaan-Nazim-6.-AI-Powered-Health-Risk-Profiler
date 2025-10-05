package com.plum.assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plum.assignment.model.OcrParseResult;
import com.plum.assignment.model.SurveyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OcrParsingServiceTest {
    
    @InjectMocks
    private OcrParsingService ocrParsingService;
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ocrParsingService = new OcrParsingService(objectMapper);
    }
    
    @Test
    void testParseJsonInput_ValidJson() {
        // Given
        String jsonInput = "{\"age\":42,\"smoker\":true,\"exercise\":\"rarely\",\"diet\":\"high sugar\"}";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(jsonInput);
        
        // Then
        assertNotNull(result);
        assertEquals("incomplete_profile", result.getStatus());
        assertNotNull(result.getAnswers());
        assertEquals(42, result.getAnswers().getAge());
        assertTrue(result.getAnswers().getSmoker());
        assertEquals("rarely", result.getAnswers().getExercise());
        assertEquals("high sugar", result.getAnswers().getDiet());
        assertTrue(result.getConfidence() >= 0.0);
    }
    
    @Test
    void testParseJsonInput_IncompleteData() {
        // Given
        String jsonInput = "{\"age\":42}";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(jsonInput);
        
        // Then
        assertNotNull(result);
        assertEquals("incomplete_profile", result.getStatus());
        assertEquals(">50% fields missing", result.getReason());
        assertTrue(result.getMissingFields().size() > 4);
    }
    
    @Test
    void testParseStructuredText_ValidInput() {
        // Given
        String textInput = "Age: 42\nSmoker: yes\nExercise: rarely\nDiet: high sugar";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(textInput);
        
        // Then
        assertNotNull(result);
        assertEquals("incomplete_profile", result.getStatus());
        assertNotNull(result.getAnswers());
        assertEquals(42, result.getAnswers().getAge());
        assertTrue(result.getAnswers().getSmoker());
        assertEquals("rarely", result.getAnswers().getExercise());
        assertEquals("high sugar", result.getAnswers().getDiet());
    }
    
    @Test
    void testParseStructuredText_MissingFields() {
        // Given
        String textInput = "Age: 42";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(textInput);
        
        // Then
        assertNotNull(result);
        assertEquals("incomplete_profile", result.getStatus());
        assertTrue(result.getMissingFields().size() > 4);
    }
    
    @Test
    void testParseText_InvalidJson() {
        // Given
        String invalidJson = "invalid json input";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(invalidJson);
        
        // Then
        assertNotNull(result);
        assertEquals("incomplete_profile", result.getStatus());
        assertTrue(result.getReason().contains(">50% fields missing"));
    }
    
    @Test
    void testParseImage_InvalidFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "invalid image content".getBytes());
        
        // When
        OcrParseResult result = ocrParsingService.parseImage(file);
        
        // Then
        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertTrue(result.getReason().contains("Failed to process image"));
    }
    
    @Test
    void testExtractAge_ValidInput() {
        // Given
        String text = "Age: 42 years old";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(text);
        
        // Then
        assertNotNull(result.getAnswers());
        assertEquals(42, result.getAnswers().getAge());
    }
    
    @Test
    void testExtractSmokerStatus_Yes() {
        // Given
        String text = "Smoker: yes";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(text);
        
        // Then
        assertNotNull(result.getAnswers());
        assertTrue(result.getAnswers().getSmoker());
    }
    
    @Test
    void testExtractSmokerStatus_No() {
        // Given
        String text = "Smoker: no";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(text);
        
        // Then
        assertNotNull(result.getAnswers());
        assertFalse(result.getAnswers().getSmoker());
    }
    
    @Test
    void testExtractExercise_ValidInput() {
        // Given
        String text = "Exercise: rarely";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(text);
        
        // Then
        assertNotNull(result.getAnswers());
        assertEquals("rarely", result.getAnswers().getExercise());
    }
    
    @Test
    void testExtractDiet_ValidInput() {
        // Given
        String text = "Diet: high sugar";
        
        // When
        OcrParseResult result = ocrParsingService.parseText(text);
        
        // Then
        assertNotNull(result.getAnswers());
        assertEquals("high sugar", result.getAnswers().getDiet());
    }
}
