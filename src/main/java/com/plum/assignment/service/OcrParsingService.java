package com.plum.assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plum.assignment.model.OcrParseResult;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OcrParsingService {
    
    private final GeminiService geminiService;
    
    private final ObjectMapper objectMapper;
    private final ITesseract tesseract;

    private static final List<String> EXPECTED_FIELDS = Arrays.asList(
        "age", "smoker", "exercise", "diet", "alcohol", "stress", "sleep", 
        "familyHistory", "chronicConditions"
    );
    
    public OcrParsingService(ObjectMapper objectMapper, GeminiService geminiService) {
        this.objectMapper = objectMapper;
        this.geminiService = geminiService;
        this.tesseract = new Tesseract();
        // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
    }

    public OcrParseResult parseText(String textInput) {
        try {
            if (textInput.trim().startsWith("{")) {
                return parseJsonInput(textInput);
            } else {
                return parseStructuredText(textInput);
            }
        } catch (Exception e) {
            log.error("Error parsing text input: {}", e.getMessage());
            return OcrParseResult.builder()
                .status("error")
                .reason("Failed to parse text input: " + e.getMessage())
                .confidence(0.0)
                .build();
        }
    }

    public OcrParseResult parseImage(MultipartFile imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            String ocrText = tesseract.doOCR(image);
            log.info("OCR extracted text: {}", ocrText);
            return parseStructuredText(ocrText);
        } catch (IOException | TesseractException e) {
            log.error("Error processing image: {}", e.getMessage());
            return OcrParseResult.builder()
                .status("error")
                .reason("Failed to process image: " + e.getMessage())
                .confidence(0.0)
                .build();
        }
    }

    private OcrParseResult parseJsonInput(String jsonInput) {
        try {
            SurveyResponse response = objectMapper.readValue(jsonInput, SurveyResponse.class);
            List<String> missingFields = findMissingFields(response);
            
            double confidence = calculateConfidence(response, missingFields);
            
            if (missingFields.size() > EXPECTED_FIELDS.size() / 2) {
                return OcrParseResult.builder()
                    .answers(response)
                    .missingFields(missingFields)
                    .confidence(confidence)
                    .status("incomplete_profile")
                    .reason(">50% fields missing")
                    .build();
            }
            
            return OcrParseResult.builder()
                .answers(response)
                .missingFields(missingFields)
                .confidence(confidence)
                .status("ok")
                .build();
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return OcrParseResult.builder()
                .status("error")
                .reason("Invalid JSON format: " + e.getMessage())
                .confidence(0.0)
                .build();
        }
    }
    

    private OcrParseResult parseStructuredText(String textInput) {
        try {
            SurveyResponse aiResponse = geminiService.parseStructuredTextWithAI(textInput);

            if (aiResponse.getAge() == null && aiResponse.getSmoker() == null && 
                (aiResponse.getExercise() == null || aiResponse.getExercise().trim().isEmpty())) {
                log.warn("AI text parsing failed, falling back to rule-based parsing");
                return parseStructuredTextFallback(textInput);
            }

            List<String> missingFields = findMissingFields(aiResponse);
            double confidence = calculateConfidence(aiResponse, missingFields);
            
            if (missingFields.size() > EXPECTED_FIELDS.size() / 2) {
                return OcrParseResult.builder()
                    .answers(aiResponse)
                    .missingFields(missingFields)
                    .confidence(confidence)
                    .status("incomplete_profile")
                    .reason(">50% fields missing")
                    .build();
            }
            
            log.info("Parsed text using AI with confidence: {}", confidence);
            
            return OcrParseResult.builder()
                .answers(aiResponse)
                .missingFields(missingFields)
                .confidence(confidence)
                .status("ok")
                .build();
                
        } catch (Exception e) {
            log.error("Error in AI text parsing, using fallback: {}", e.getMessage(), e);
            return parseStructuredTextFallback(textInput);
        }
    }

    private OcrParseResult parseStructuredTextFallback(String textInput) {
        SurveyResponse response = new SurveyResponse();
        List<String> missingFields = new ArrayList<>();
        
        // Parse age
        response.setAge(extractAge(textInput));
        
        // Parse smoker status
        response.setSmoker(extractSmokerStatus(textInput));
        
        // Parse exercise
        response.setExercise(extractExercise(textInput));
        
        // Parse diet
        response.setDiet(extractDiet(textInput));
        
        // Parse alcohol
        response.setAlcohol(extractAlcohol(textInput));
        
        // Parse stress
        response.setStress(extractStress(textInput));
        
        // Parse sleep
        response.setSleep(extractSleep(textInput));
        
        // Parse family history
        response.setFamilyHistory(extractFamilyHistory(textInput));
        
        // Parse chronic conditions
        response.setChronicConditions(extractChronicConditions(textInput));
        
        // Find missing fields
        missingFields = findMissingFields(response);
        
        double confidence = calculateConfidence(response, missingFields);
        
        if (missingFields.size() > EXPECTED_FIELDS.size() / 2) {
            return OcrParseResult.builder()
                .answers(response)
                .missingFields(missingFields)
                .confidence(confidence)
                .status("incomplete_profile")
                .reason(">50% fields missing")
                .build();
        }
        
        return OcrParseResult.builder()
            .answers(response)
            .missingFields(missingFields)
            .confidence(confidence)
            .status("ok")
            .build();
    }
    
    private Integer extractAge(String text) {
        Pattern pattern = Pattern.compile("(?i)age\\s*:?\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
    
    private Boolean extractSmokerStatus(String text) {
        Pattern pattern = Pattern.compile("(?i)smoker\\s*:?\\s*(yes|no|true|false)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).toLowerCase();
            return "yes".equals(value) || "true".equals(value);
        }
        return null;
    }
    
    private String extractExercise(String text) {
        Pattern pattern = Pattern.compile("(?i)exercise\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private String extractDiet(String text) {
        Pattern pattern = Pattern.compile("(?i)diet\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private String extractAlcohol(String text) {
        Pattern pattern = Pattern.compile("(?i)alcohol\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private String extractStress(String text) {
        Pattern pattern = Pattern.compile("(?i)stress\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private String extractSleep(String text) {
        Pattern pattern = Pattern.compile("(?i)sleep\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private String extractFamilyHistory(String text) {
        Pattern pattern = Pattern.compile("(?i)(family\\s*history|familyhistory)\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return null;
    }
    
    private String extractChronicConditions(String text) {
        Pattern pattern = Pattern.compile("(?i)(chronic\\s*conditions|chronicconditions)\\s*:?\\s*([^\\n\\r]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return null;
    }
    
    private List<String> findMissingFields(SurveyResponse response) {
        List<String> missing = new ArrayList<>();
        
        if (response.getAge() == null) missing.add("age");
        if (response.getSmoker() == null) missing.add("smoker");
        if (response.getExercise() == null || response.getExercise().trim().isEmpty()) missing.add("exercise");
        if (response.getDiet() == null || response.getDiet().trim().isEmpty()) missing.add("diet");
        if (response.getAlcohol() == null || response.getAlcohol().trim().isEmpty()) missing.add("alcohol");
        if (response.getStress() == null || response.getStress().trim().isEmpty()) missing.add("stress");
        if (response.getSleep() == null || response.getSleep().trim().isEmpty()) missing.add("sleep");
        if (response.getFamilyHistory() == null || response.getFamilyHistory().trim().isEmpty()) missing.add("familyHistory");
        if (response.getChronicConditions() == null || response.getChronicConditions().trim().isEmpty()) missing.add("chronicConditions");
        
        return missing;
    }
    
    private double calculateConfidence(SurveyResponse response, List<String> missingFields) {
        int totalFields = EXPECTED_FIELDS.size();
        int filledFields = totalFields - missingFields.size();
        return (double) filledFields / totalFields;
    }
}
