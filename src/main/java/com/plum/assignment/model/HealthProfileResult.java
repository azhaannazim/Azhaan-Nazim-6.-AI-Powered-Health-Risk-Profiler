package com.plum.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfileResult {
    
    private OcrParseResult parseResult;
    private FactorExtractionResult factorResult;
    private RiskClassificationResult riskResult;
    private RecommendationResult recommendationResult;
    
    @JsonProperty("overall_status")
    private String overallStatus;
    
    private String message;
}
