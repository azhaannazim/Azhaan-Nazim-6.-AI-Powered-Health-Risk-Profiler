package com.plum.assignment.validator;

import com.plum.assignment.exception.HealthProfileException;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class HealthProfileValidator {
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    private static final List<String> REQUIRED_FIELDS = Arrays.asList(
        "age", "smoker", "exercise", "diet"
    );
    
    public void validateSurveyResponse(SurveyResponse response) {
        if (response == null) {
            throw new HealthProfileException("INVALID_SURVEY_RESPONSE", 
                "Survey response cannot be null", 
                "Please provide valid survey data");
        }

        if (response.getAge() == null) {
            throw new HealthProfileException("MISSING_AGE", 
                "Age is required", 
                "Please provide your age");
        }
        
        if (response.getAge() < 18 || response.getAge() > 120) {
            throw new HealthProfileException("INVALID_AGE", 
                "Age must be between 18 and 120", 
                "Please provide a valid age between 18 and 120 years");
        }
        
        if (response.getSmoker() == null) {
            throw new HealthProfileException("MISSING_SMOKER_STATUS", 
                "Smoker status is required", 
                "Please indicate if you are a smoker");
        }
        
        if (response.getExercise() == null || response.getExercise().trim().isEmpty()) {
            throw new HealthProfileException("MISSING_EXERCISE",
                    "Exercise information is required",
                    "Please provide information about your exercise habits");
        }
        if (response.getDiet() == null || response.getDiet().trim().isEmpty()) {
            throw new HealthProfileException("MISSING_DIET", 
                "Diet information is required", 
                "Please provide information about your diet");
        }
        
        log.info("Survey response validation passed");
    }
    
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new HealthProfileException("EMPTY_FILE", 
                "No file provided", 
                "Please select an image file to upload");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new HealthProfileException("FILE_TOO_LARGE", 
                "File size exceeds maximum limit", 
                "File size must be less than 10MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new HealthProfileException("INVALID_FILE_TYPE", 
                "Invalid file type", 
                "Only JPEG, PNG, GIF, and BMP images are allowed");
        }
        
        log.info("Image file validation passed for file: {}", file.getOriginalFilename());
    }
    
    public void validateTextInput(String textInput) {
        if (textInput == null || textInput.trim().isEmpty()) {
            throw new HealthProfileException("EMPTY_TEXT_INPUT", 
                "No text input provided", 
                "Please provide text input to analyze");
        }
        
        if (textInput.trim().length() < 10) {
            throw new HealthProfileException("TEXT_TOO_SHORT", 
                "Text input is too short", 
                "Please provide more detailed information");
        }
        
        if (textInput.trim().length() > 10000) {
            throw new HealthProfileException("TEXT_TOO_LONG", 
                "Text input is too long", 
                "Text input must be less than 10,000 characters");
        }
        
        log.info("Text input validation passed");
    }
    
    public void validateMinimalData(SurveyResponse response) {
        if (response == null) {
            throw new HealthProfileException("INVALID_SURVEY_RESPONSE", 
                "Survey response cannot be null", 
                "Please provide valid survey data");
        }
        
        int filledFields = 0;
        
        if (response.getAge() != null) filledFields++;
        if (response.getSmoker() != null) filledFields++;
        if (response.getExercise() != null && !response.getExercise().trim().isEmpty()) filledFields++;
        if (response.getDiet() != null && !response.getDiet().trim().isEmpty()) filledFields++;
        if (response.getAlcohol() != null && !response.getAlcohol().trim().isEmpty()) filledFields++;
        if (response.getStress() != null && !response.getStress().trim().isEmpty()) filledFields++;
        if (response.getSleep() != null && !response.getSleep().trim().isEmpty()) filledFields++;
        if (response.getFamilyHistory() != null && !response.getFamilyHistory().trim().isEmpty()) filledFields++;
        if (response.getChronicConditions() != null && !response.getChronicConditions().trim().isEmpty()) filledFields++;
        
        if (filledFields < 3) {
            throw new HealthProfileException("INSUFFICIENT_DATA", 
                "Insufficient data for analysis", 
                "Please provide at least 3 pieces of health information");
        }
        
        log.info("Minimal data validation passed with {} filled fields", filledFields);
    }
    
    public boolean hasMinimumRequiredData(SurveyResponse response) {
        if (response == null) return false;
        
        int requiredFieldsPresent = 0;
        
        if (response.getAge() != null) requiredFieldsPresent++;
        if (response.getSmoker() != null) requiredFieldsPresent++;
        if (response.getExercise() != null && !response.getExercise().trim().isEmpty()) requiredFieldsPresent++;
        if (response.getDiet() != null && !response.getDiet().trim().isEmpty()) requiredFieldsPresent++;
        
        return requiredFieldsPresent >= 3; // At least 3 out of 4 required fields
    }
    
    public void validateRiskFactors(List<String> factors) {
        if (factors == null || factors.isEmpty()) {
            throw new HealthProfileException("NO_RISK_FACTORS", 
                "No risk factors identified", 
                "Unable to identify any health risk factors");
        }
        
        log.info("Risk factors validation passed with {} factors", factors.size());
    }
}
