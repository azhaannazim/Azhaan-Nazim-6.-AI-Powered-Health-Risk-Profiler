package com.plum.assignment.service;

import com.plum.assignment.model.RiskClassificationResult;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RiskClassificationService {
    
    private final GeminiService geminiService;
    
    public RiskClassificationService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }
    
    // Risk factor weights for scoring
    private static final Map<String, Integer> RISK_FACTOR_WEIGHTS = new HashMap<String, Integer>() {{
        put("smoking", 25);
        put("excessive alcohol", 20);
        put("poor diet", 15);
        put("high sugar intake", 15);
        put("high fat intake", 12);
        put("processed foods", 10);
        put("low exercise", 20);
        put("no exercise", 25);
        put("sedentary lifestyle", 18);
        put("high stress", 15);
        put("poor sleep", 12);
        put("chronic conditions", 20);
        put("family history", 10);
        put("advanced age", 15);
        put("middle age", 5);
    }};
    
    // Positive factor weights (reduce risk)
    private static final Map<String, Integer> POSITIVE_FACTOR_WEIGHTS = new HashMap<String, Integer>() {{
        put("no alcohol", -5);
        put("good diet", -8);
        put("excellent diet", -12);
        put("good exercise", -10);
        put("excellent exercise", -15);
        put("low stress", -5);
        put("good sleep", -8);
        put("excellent sleep", -10);
    }};
    
    public RiskClassificationResult classifyRisk(SurveyResponse surveyResponse, List<String> factors) {
        try {
            // Use Gemini AI to classify risk
            String aiRiskLevel = geminiService.classifyHealthRisk(surveyResponse, factors);
            
            // Fallback to rule-based classification if AI fails
            if ("unknown".equals(aiRiskLevel)) {
                log.warn("AI risk classification failed, falling back to rule-based classification");
                return classifyRiskFallback(surveyResponse, factors);
            }
            
            // Calculate score based on AI classification
            int score = calculateScoreFromRiskLevel(aiRiskLevel);
            
            log.info("Risk classification using AI - Level: {}, Score: {}", aiRiskLevel, score);
            
            return RiskClassificationResult.builder()
                .riskLevel(aiRiskLevel)
                .score(score)
                .rationale(factors)
                .build();
                
        } catch (Exception e) {
            log.error("Error in risk classification, using fallback: {}", e.getMessage(), e);
            // Fallback to rule-based classification
            return classifyRiskFallback(surveyResponse, factors);
        }
    }
    
    /**
     * Fallback method for risk classification using rule-based approach
     */
    private RiskClassificationResult classifyRiskFallback(SurveyResponse surveyResponse, List<String> factors) {
        int totalScore = 0;
        List<String> rationale = new ArrayList<>();
        
        // Calculate base score from factors
        for (String factor : factors) {
            Integer weight = RISK_FACTOR_WEIGHTS.get(factor);
            if (weight != null) {
                totalScore += weight;
                rationale.add(factor);
            } else {
                // Check positive factors
                Integer positiveWeight = POSITIVE_FACTOR_WEIGHTS.get(factor);
                if (positiveWeight != null) {
                    totalScore += positiveWeight; // negative weight reduces score
                }
            }
        }
        
        // Additional scoring based on raw survey data
        totalScore += calculateAdditionalScore(surveyResponse);
        
        // Ensure score is within bounds (0-100)
        totalScore = Math.max(0, Math.min(100, totalScore));
        
        // Determine risk level
        String riskLevel = determineRiskLevel(totalScore);
        
        log.info("Risk classification fallback - Score: {}, Level: {}, Rationale: {}", 
                totalScore, riskLevel, rationale);
        
        return RiskClassificationResult.builder()
            .riskLevel(riskLevel)
            .score(totalScore)
            .rationale(rationale)
            .build();
    }
    
    /**
     * Calculate score based on AI-determined risk level
     */
    private int calculateScoreFromRiskLevel(String riskLevel) {
        return switch (riskLevel.toLowerCase()) {
            case "minimal" -> 10;
            case "low" -> 25;
            case "moderate" -> 50;
            case "high" -> 80;
            default -> 30;
        };
    }
    
    private int calculateAdditionalScore(SurveyResponse response) {
        int additionalScore = 0;
        
        // Age-based scoring
        if (response.getAge() != null) {
            if (response.getAge() >= 65) {
                additionalScore += 10;
            } else if (response.getAge() >= 55) {
                additionalScore += 7;
            } else if (response.getAge() >= 45) {
                additionalScore += 4;
            }
        }
        
        // Smoking intensity (if we had more detailed data)
        if (Boolean.TRUE.equals(response.getSmoker())) {
            additionalScore += 5; // Base smoking already counted in factors
        }
        
        // Exercise frequency scoring
        if (response.getExercise() != null) {
            String exercise = response.getExercise().toLowerCase().trim();
            if (exercise.contains("never") || exercise.contains("rarely")) {
                additionalScore += 8;
            } else if (exercise.contains("occasionally")) {
                additionalScore += 5;
            }
        }
        
        // Diet quality scoring
        if (response.getDiet() != null) {
            String diet = response.getDiet().toLowerCase().trim();
            if (diet.contains("high sugar") || diet.contains("junk") || diet.contains("processed")) {
                additionalScore += 10;
            } else if (diet.contains("high fat")) {
                additionalScore += 8;
            }
        }
        
        // Alcohol consumption scoring
        if (response.getAlcohol() != null) {
            String alcohol = response.getAlcohol().toLowerCase().trim();
            if (alcohol.contains("heavy") || alcohol.contains("daily")) {
                additionalScore += 12;
            } else if (alcohol.contains("frequent")) {
                additionalScore += 8;
            }
        }
        
        // Stress level scoring
        if (response.getStress() != null) {
            String stress = response.getStress().toLowerCase().trim();
            if (stress.contains("high") || stress.contains("severe")) {
                additionalScore += 8;
            } else if (stress.contains("moderate")) {
                additionalScore += 4;
            }
        }
        
        // Sleep quality scoring
        if (response.getSleep() != null) {
            String sleep = response.getSleep().toLowerCase().trim();
            if (sleep.contains("poor") || sleep.contains("insufficient")) {
                additionalScore += 6;
            } else if (sleep.contains("disrupted")) {
                additionalScore += 4;
            }
        }
        
        return additionalScore;
    }
    
    private String determineRiskLevel(int score) {
        if (score >= 70) {
            return "high";
        } else if (score >= 40) {
            return "moderate";
        } else if (score >= 20) {
            return "low";
        } else {
            return "minimal";
        }
    }
    
    /**
     * Get risk level description
     */
    public String getRiskLevelDescription(String riskLevel) {
        return switch (riskLevel) {
            case "high" -> "High risk - Multiple risk factors present requiring immediate attention";
            case "moderate" -> "Moderate risk - Some risk factors present, lifestyle changes recommended";
            case "low" -> "Low risk - Few risk factors, maintain current healthy habits";
            case "minimal" -> "Minimal risk - Excellent health profile, continue current practices";
            default -> "Unknown risk level";
        };
    }
    
    /**
     * Get risk score interpretation
     */
    public String getRiskScoreInterpretation(int score) {
        if (score >= 80) {
            return "Very high risk - Immediate lifestyle changes and medical consultation recommended";
        } else if (score >= 60) {
            return "High risk - Significant lifestyle modifications needed";
        } else if (score >= 40) {
            return "Moderate risk - Some lifestyle improvements recommended";
        } else if (score >= 20) {
            return "Low risk - Minor adjustments may be beneficial";
        } else {
            return "Very low risk - Excellent health profile maintained";
        }
    }
}
