package com.plum.assignment.controller;

import com.plum.assignment.model.*;
import com.plum.assignment.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health-profile")
@Slf4j
public class HealthProfileController {
    
    private final OcrParsingService ocrParsingService;
    private final FactorExtractionService factorExtractionService;
    private final RiskClassificationService riskClassificationService;
    private final RecommendationService recommendationService;
    
    public HealthProfileController(OcrParsingService ocrParsingService,
                                 FactorExtractionService factorExtractionService,
                                 RiskClassificationService riskClassificationService,
                                 RecommendationService recommendationService) {
        this.ocrParsingService = ocrParsingService;
        this.factorExtractionService = factorExtractionService;
        this.riskClassificationService = riskClassificationService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/analyze-text")
    public ResponseEntity<HealthProfileResult> analyzeText(@RequestBody String textInput) {
        try {
            log.info("Starting health profile analysis for text input");
            
            // Step 1: OCR/Text Parsing
            OcrParseResult parseResult = ocrParsingService.parseText(textInput);
            
            if (parseResult.isIncomplete()) {
                return ResponseEntity.ok(HealthProfileResult.builder()
                    .parseResult(parseResult)
                    .overallStatus("incomplete")
                    .message("Survey data is incomplete - " + parseResult.getReason())
                    .build());
            }
            
            if (!"ok".equals(parseResult.getStatus())) {
                return ResponseEntity.badRequest().body(HealthProfileResult.builder()
                    .parseResult(parseResult)
                    .overallStatus("error")
                    .message("Failed to parse input: " + parseResult.getReason())
                    .build());
            }
            FactorExtractionResult factorResult = factorExtractionService.extractFactors(parseResult.getAnswers());

            RiskClassificationResult riskResult = riskClassificationService.classifyRisk(
                parseResult.getAnswers(), factorResult.getFactors());

            RecommendationResult recommendationResult = recommendationService.generateRecommendations(
                parseResult.getAnswers(), factorResult.getFactors(), riskResult.getRiskLevel());
            
            HealthProfileResult result = HealthProfileResult.builder()
                .parseResult(parseResult)
                .factorResult(factorResult)
                .riskResult(riskResult)
                .recommendationResult(recommendationResult)
                .overallStatus("success")
                .message("Health profile analysis completed successfully")
                .build();
            
            log.info("Health profile analysis completed successfully");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during health profile analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(HealthProfileResult.builder()
                .overallStatus("error")
                .message("Internal server error: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<HealthProfileResult> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Starting health profile analysis for image upload: {}", file.getOriginalFilename());
            
            // Step 1: OCR/Text Parsing
            OcrParseResult parseResult = ocrParsingService.parseImage(file);
            
            if (parseResult.isIncomplete()) {
                return ResponseEntity.ok(HealthProfileResult.builder()
                    .parseResult(parseResult)
                    .overallStatus("incomplete")
                    .message("Survey data is incomplete - " + parseResult.getReason())
                    .build());
            }
            
            if (!"ok".equals(parseResult.getStatus())) {
                return ResponseEntity.badRequest().body(HealthProfileResult.builder()
                    .parseResult(parseResult)
                    .overallStatus("error")
                    .message("Failed to process image: " + parseResult.getReason())
                    .build());
            }

            FactorExtractionResult factorResult = factorExtractionService.extractFactors(parseResult.getAnswers());

            RiskClassificationResult riskResult = riskClassificationService.classifyRisk(
                parseResult.getAnswers(), factorResult.getFactors());

            RecommendationResult recommendationResult = recommendationService.generateRecommendations(
                parseResult.getAnswers(), factorResult.getFactors(), riskResult.getRiskLevel());
            
            HealthProfileResult result = HealthProfileResult.builder()
                .parseResult(parseResult)
                .factorResult(factorResult)
                .riskResult(riskResult)
                .recommendationResult(recommendationResult)
                .overallStatus("success")
                .message("Health profile analysis completed successfully")
                .build();
            
            log.info("Health profile analysis completed successfully for image");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during image analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(HealthProfileResult.builder()
                .overallStatus("error")
                .message("Internal server error: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/analyze-json")
    public ResponseEntity<HealthProfileResult> analyzeJson(@Valid @RequestBody SurveyResponse surveyResponse) {
        try {
            log.info("Starting health profile analysis for JSON input");

            OcrParseResult parseResult = OcrParseResult.builder()
                .answers(surveyResponse)
                .missingFields(java.util.Collections.emptyList())
                .confidence(1.0)
                .status("ok")
                .build();

            FactorExtractionResult factorResult = factorExtractionService.extractFactors(surveyResponse);

            RiskClassificationResult riskResult = riskClassificationService.classifyRisk(
                surveyResponse, factorResult.getFactors());

            RecommendationResult recommendationResult = recommendationService.generateRecommendations(
                surveyResponse, factorResult.getFactors(), riskResult.getRiskLevel());
            
            HealthProfileResult result = HealthProfileResult.builder()
                .parseResult(parseResult)
                .factorResult(factorResult)
                .riskResult(riskResult)
                .recommendationResult(recommendationResult)
                .overallStatus("success")
                .message("Health profile analysis completed successfully")
                .build();
            
            log.info("Health profile analysis completed successfully for JSON");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during JSON analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(HealthProfileResult.builder()
                .overallStatus("error")
                .message("Internal server error: " + e.getMessage())
                .build());
        }
    }
    
    /**
     * OCR/Text parsing
     */
    @PostMapping("/parse-text")
    public ResponseEntity<OcrParseResult> parseText(@RequestBody String textInput) {
        try {
            OcrParseResult result = ocrParsingService.parseText(textInput);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during text parsing: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/parse-image")
    public ResponseEntity<OcrParseResult> parseImage(@RequestParam("file") MultipartFile file) {
        try {
            OcrParseResult result = ocrParsingService.parseImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during image parsing: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/extract-factors")
    public ResponseEntity<FactorExtractionResult> extractFactors(@Valid @RequestBody SurveyResponse surveyResponse) {
        try {
            FactorExtractionResult result = factorExtractionService.extractFactors(surveyResponse);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during factor extraction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/classify-risk")
    public ResponseEntity<RiskClassificationResult> classifyRisk(@Valid @RequestBody SurveyResponse surveyResponse,
                                                               @RequestParam(required = false) String factors) {
        try {
            // If factors not provided, extract them first
            java.util.List<String> factorList;
            if (factors != null && !factors.isEmpty()) {
                factorList = java.util.Arrays.asList(factors.split(","));
            } else {
                FactorExtractionResult factorResult = factorExtractionService.extractFactors(surveyResponse);
                factorList = factorResult.getFactors();
            }
            
            RiskClassificationResult result = riskClassificationService.classifyRisk(surveyResponse, factorList);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during risk classification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-recommendations")
    public ResponseEntity<RecommendationResult> generateRecommendations(@Valid @RequestBody SurveyResponse surveyResponse,
                                                                      @RequestParam(required = false) String factors,
                                                                      @RequestParam(required = false) String riskLevel) {
        try {
            // If factors not provided, extract them first
            java.util.List<String> factorList;
            if (factors != null && !factors.isEmpty()) {
                factorList = java.util.Arrays.asList(factors.split(","));
            } else {
                FactorExtractionResult factorResult = factorExtractionService.extractFactors(surveyResponse);
                factorList = factorResult.getFactors();
            }
            
            // If risk level not provided, classify it first
            String calculatedRiskLevel;
            if (riskLevel != null && !riskLevel.isEmpty()) {
                calculatedRiskLevel = riskLevel;
            } else {
                RiskClassificationResult riskResult = riskClassificationService.classifyRisk(surveyResponse, factorList);
                calculatedRiskLevel = riskResult.getRiskLevel();
            }
            
            RecommendationResult result = recommendationService.generateRecommendations(
                surveyResponse, factorList, calculatedRiskLevel);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during recommendation generation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "AI-Powered Health Risk Profiler");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
