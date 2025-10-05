package com.plum.assignment.service;

import com.plum.assignment.model.RecommendationResult;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RecommendationService {
    
    private final GeminiService geminiService;
    
    public RecommendationService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }
    
    // Risk-specific recommendations
    private static final Map<String, List<String>> RISK_RECOMMENDATIONS = new HashMap<String, List<String>>() {{
        put("high", Arrays.asList(
            "Consult with a healthcare provider immediately",
            "Consider comprehensive health screening",
            "Implement major lifestyle changes",
            "Monitor health metrics regularly"
        ));
        put("moderate", Arrays.asList(
            "Schedule regular health check-ups",
            "Focus on key lifestyle improvements",
            "Monitor progress monthly",
            "Consider preventive measures"
        ));
        put("low", Arrays.asList(
            "Maintain current healthy habits",
            "Continue regular health monitoring",
            "Consider minor lifestyle enhancements"
        ));
        put("minimal", Arrays.asList(
            "Continue excellent health practices",
            "Maintain regular health check-ups",
            "Share healthy habits with others"
        ));
    }};
    
    public RecommendationResult generateRecommendations(SurveyResponse surveyResponse, 
                                                       List<String> factors, 
                                                       String riskLevel) {
        try {
            // Use Gemini AI to generate recommendations
            List<String> aiRecommendations = geminiService.generateHealthRecommendations(
                surveyResponse, factors, riskLevel);
            
            // Fallback to rule-based recommendations if AI fails
            if (aiRecommendations.isEmpty()) {
                log.warn("AI recommendation generation failed, falling back to rule-based recommendations");
                return generateRecommendationsFallback(surveyResponse, factors, riskLevel);
            }
            
            // Limit to top 10 recommendations
            List<String> uniqueRecommendations = aiRecommendations.stream()
                .distinct()
                .limit(10)
                .toList();
            
            log.info("Generated {} AI recommendations for risk level: {}", uniqueRecommendations.size(), riskLevel);
            
            return RecommendationResult.builder()
                .riskLevel(riskLevel)
                .factors(factors)
                .recommendations(uniqueRecommendations)
                .status("ok")
                .build();
                
        } catch (Exception e) {
            log.error("Error in recommendation generation, using fallback: {}", e.getMessage(), e);
            // Fallback to rule-based recommendations
            return generateRecommendationsFallback(surveyResponse, factors, riskLevel);
        }
    }
    
    /**
     * Fallback method for recommendation generation using rule-based approach
     */
    private RecommendationResult generateRecommendationsFallback(SurveyResponse surveyResponse, 
                                                               List<String> factors, 
                                                               String riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        // Add risk-level specific recommendations
        List<String> riskBasedRecommendations = RISK_RECOMMENDATIONS.get(riskLevel);
        if (riskBasedRecommendations != null) {
            recommendations.addAll(riskBasedRecommendations);
        }
        
        // Add factor-specific recommendations
        recommendations.addAll(generateFactorBasedRecommendations(factors, surveyResponse));
        
        // Add age-specific recommendations
        recommendations.addAll(generateAgeBasedRecommendations(surveyResponse));
        
        // Remove duplicates and limit to most important recommendations
        List<String> uniqueRecommendations = recommendations.stream()
            .distinct()
            .limit(10) // Limit to top 10 recommendations
            .toList();
        
        String status = "ok";
        if (uniqueRecommendations.isEmpty()) {
            status = "no_recommendations";
        }
        
        log.info("Generated {} fallback recommendations for risk level: {}", uniqueRecommendations.size(), riskLevel);
        
        return RecommendationResult.builder()
            .riskLevel(riskLevel)
            .factors(factors)
            .recommendations(uniqueRecommendations)
            .status(status)
            .build();
    }
    
    private List<String> generateFactorBasedRecommendations(List<String> factors, SurveyResponse response) {
        List<String> recommendations = new ArrayList<>();
        
        for (String factor : factors) {
            switch (factor) {
                case "smoking" -> {
                    recommendations.add("Quit smoking immediately");
                    recommendations.add("Consider smoking cessation programs");
                    recommendations.add("Avoid secondhand smoke exposure");
                }
                case "low exercise", "no exercise", "sedentary lifestyle" -> {
                    recommendations.add("Start with 30 minutes of daily walking");
                    recommendations.add("Gradually increase physical activity");
                    recommendations.add("Consider joining a fitness program");
                    recommendations.add("Take breaks from sitting every hour");
                }
                case "poor diet", "high sugar intake" -> {
                    recommendations.add("Reduce sugar and processed food intake");
                    recommendations.add("Increase fruits and vegetables");
                    recommendations.add("Choose whole grains over refined grains");
                    recommendations.add("Limit sugary beverages");
                }
                case "high fat intake" -> {
                    recommendations.add("Choose lean protein sources");
                    recommendations.add("Limit saturated and trans fats");
                    recommendations.add("Use healthy cooking methods");
                }
                case "excessive alcohol" -> {
                    recommendations.add("Reduce alcohol consumption");
                    recommendations.add("Follow recommended daily limits");
                    recommendations.add("Consider alcohol-free days");
                }
                case "high stress" -> {
                    recommendations.add("Practice stress management techniques");
                    recommendations.add("Consider meditation or yoga");
                    recommendations.add("Ensure adequate work-life balance");
                    recommendations.add("Seek professional help if needed");
                }
                case "poor sleep" -> {
                    recommendations.add("Maintain consistent sleep schedule");
                    recommendations.add("Create a relaxing bedtime routine");
                    recommendations.add("Limit screen time before bed");
                    recommendations.add("Ensure comfortable sleep environment");
                }
                case "family history" -> {
                    recommendations.add("Discuss family history with healthcare provider");
                    recommendations.add("Consider genetic counseling if appropriate");
                    recommendations.add("Increase preventive screening frequency");
                }
                case "chronic conditions" -> {
                    recommendations.add("Follow treatment plan consistently");
                    recommendations.add("Monitor condition regularly");
                    recommendations.add("Maintain medication compliance");
                }
                case "advanced age" -> {
                    recommendations.add("Increase preventive health screenings");
                    recommendations.add("Focus on fall prevention");
                    recommendations.add("Maintain social connections");
                    recommendations.add("Consider bone health supplements");
                }
            }
        }
        
        return recommendations;
    }
    
    private List<String> generateAgeBasedRecommendations(SurveyResponse response) {
        List<String> recommendations = new ArrayList<>();
        
        if (response.getAge() != null) {
            int age = response.getAge();
            
            if (age >= 65) {
                recommendations.add("Annual comprehensive health assessment");
                recommendations.add("Regular vision and hearing checks");
                recommendations.add("Fall risk assessment");
                recommendations.add("Medication review with pharmacist");
            } else if (age >= 50) {
                recommendations.add("Colon cancer screening");
                recommendations.add("Bone density testing");
                recommendations.add("Cardiovascular risk assessment");
            } else if (age >= 40) {
                recommendations.add("Baseline health screening");
                recommendations.add("Cholesterol and blood pressure monitoring");
                recommendations.add("Diabetes screening");
            } else if (age >= 30) {
                recommendations.add("Regular health check-ups");
                recommendations.add("Establish healthy lifestyle habits");
                recommendations.add("Preventive care focus");
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate personalized recommendations based on specific survey responses
     */
    public List<String> generatePersonalizedRecommendations(SurveyResponse response) {
        List<String> personalized = new ArrayList<>();
        
        // Exercise recommendations based on current level
        if (response.getExercise() != null) {
            String exercise = response.getExercise().toLowerCase().trim();
            if (exercise.contains("never") || exercise.contains("rarely")) {
                personalized.add("Start with 10-minute daily walks");
                personalized.add("Use stairs instead of elevators");
                personalized.add("Park farther from destinations");
            } else if (exercise.contains("occasionally")) {
                personalized.add("Increase to 3-4 times per week");
                personalized.add("Try different types of activities");
                personalized.add("Set weekly exercise goals");
            }
        }
        
        // Diet recommendations based on current diet
        if (response.getDiet() != null) {
            String diet = response.getDiet().toLowerCase().trim();
            if (diet.contains("sugar")) {
                personalized.add("Replace sugary snacks with fruits");
                personalized.add("Read nutrition labels for hidden sugars");
                personalized.add("Gradually reduce sugar in beverages");
            }
            if (diet.contains("processed")) {
                personalized.add("Cook more meals at home");
                personalized.add("Choose fresh ingredients over packaged foods");
                personalized.add("Plan weekly meal preparation");
            }
        }
        
        // Stress management based on stress level
        if (response.getStress() != null) {
            String stress = response.getStress().toLowerCase().trim();
            if (stress.contains("high") || stress.contains("severe")) {
                personalized.add("Practice deep breathing exercises daily");
                personalized.add("Consider professional counseling");
                personalized.add("Identify and address stress triggers");
            }
        }
        
        return personalized;
    }
    
    /**
     * Get recommendation priority based on risk level
     */
    public String getRecommendationPriority(String riskLevel) {
        return switch (riskLevel) {
            case "high" -> "Immediate action required";
            case "moderate" -> "Action recommended within 1-3 months";
            case "low" -> "Gradual improvement over 3-6 months";
            case "minimal" -> "Maintenance and prevention focus";
            default -> "Standard recommendations";
        };
    }
}
