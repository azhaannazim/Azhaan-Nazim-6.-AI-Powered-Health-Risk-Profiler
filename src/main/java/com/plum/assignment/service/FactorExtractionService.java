package com.plum.assignment.service;

import com.plum.assignment.model.FactorExtractionResult;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class FactorExtractionService {
    
    private final GeminiService geminiService;
    
    public FactorExtractionService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }
    
    // Risk factor mappings
    private static final Map<String, List<String>> EXERCISE_FACTORS = new HashMap<String, List<String>>() {{
        put("rarely", Arrays.asList("low exercise", "sedentary lifestyle"));
        put("never", Arrays.asList("no exercise", "sedentary lifestyle"));
        put("occasionally", Arrays.asList("low exercise"));
        put("moderate", Arrays.asList("moderate exercise"));
        put("frequent", Arrays.asList("good exercise"));
        put("daily", Arrays.asList("excellent exercise"));
    }};
    
    private static final Map<String, List<String>> DIET_FACTORS = new HashMap<String, List<String>>() {{
        put("high sugar", Arrays.asList("poor diet", "high sugar intake"));
        put("high fat", Arrays.asList("poor diet", "high fat intake"));
        put("processed", Arrays.asList("poor diet", "processed foods"));
        put("unhealthy", Arrays.asList("poor diet"));
        put("balanced", Arrays.asList("good diet"));
        put("healthy", Arrays.asList("excellent diet"));
        put("low sugar", Arrays.asList("good diet"));
        put("vegetarian", Arrays.asList("good diet"));
        put("vegan", Arrays.asList("good diet"));
    }};
    
    private static final Map<String, List<String>> ALCOHOL_FACTORS = new HashMap<String, List<String>>() {{
        put("heavy", Arrays.asList("excessive alcohol"));
        put("frequent", Arrays.asList("high alcohol intake"));
        put("moderate", Arrays.asList("moderate alcohol"));
        put("occasional", Arrays.asList("low alcohol"));
        put("never", Arrays.asList("no alcohol"));
    }};
    
    private static final Map<String, List<String>> STRESS_FACTORS = new HashMap<String, List<String>>() {{
        put("high", Arrays.asList("high stress"));
        put("severe", Arrays.asList("high stress"));
        put("moderate", Arrays.asList("moderate stress"));
        put("low", Arrays.asList("low stress"));
        put("minimal", Arrays.asList("low stress"));
    }};
    
    private static final Map<String, List<String>> SLEEP_FACTORS = new HashMap<String, List<String>>() {{
        put("poor", Arrays.asList("poor sleep"));
        put("insufficient", Arrays.asList("poor sleep"));
        put("disrupted", Arrays.asList("poor sleep"));
        put("good", Arrays.asList("good sleep"));
        put("excellent", Arrays.asList("excellent sleep"));
    }};
    
    public FactorExtractionResult extractFactors(SurveyResponse surveyResponse) {
        try {
            List<String> aiFactors = geminiService.extractHealthFactors(surveyResponse);
            
            // Fallback to rule-based extraction if AI fails
            if (aiFactors.isEmpty()) {
                log.warn("AI factor extraction failed, falling back to rule-based extraction");
                aiFactors = extractFactorsFallback(surveyResponse);
            }
            List<String> uniqueFactors = aiFactors.stream().distinct().toList();
            double confidence = calculateConfidence(uniqueFactors, surveyResponse);
            
            log.info("Extracted factors using AI: {}, confidence: {}", uniqueFactors, confidence);
            
            return FactorExtractionResult.builder()
                .factors(uniqueFactors)
                .confidence(confidence)
                .build();
                
        } catch (Exception e) {
            log.error("Error in factor extraction, using fallback: {}", e.getMessage(), e);
            // Fallback to rule-based extraction
            List<String> fallbackFactors = extractFactorsFallback(surveyResponse);
            double confidence = calculateConfidence(fallbackFactors, surveyResponse);
            
            return FactorExtractionResult.builder()
                .factors(fallbackFactors)
                .confidence(confidence)
                .build();
        }
    }

    private List<String> extractFactorsFallback(SurveyResponse surveyResponse) {
        List<String> factors = new ArrayList<>();
        
        // Extract smoking factor
        if (Boolean.TRUE.equals(surveyResponse.getSmoker())) {
            factors.add("smoking");
        }
        
        // Extract exercise factors
        if (surveyResponse.getExercise() != null) {
            String exercise = surveyResponse.getExercise().toLowerCase().trim();
            List<String> exerciseFactors = EXERCISE_FACTORS.get(exercise);
            if (exerciseFactors != null) {
                factors.addAll(exerciseFactors);
            } else {
                // Try partial matching for exercise
                if (exercise.contains("rare") || exercise.contains("never") || exercise.contains("none")) {
                    factors.add("low exercise");
                } else if (exercise.contains("daily") || exercise.contains("regular")) {
                    factors.add("good exercise");
                }
            }
        }
        
        // Extract diet factors
        if (surveyResponse.getDiet() != null) {
            String diet = surveyResponse.getDiet().toLowerCase().trim();
            List<String> dietFactors = DIET_FACTORS.get(diet);
            if (dietFactors != null) {
                factors.addAll(dietFactors);
            } else {
                // Try partial matching for diet
                if (diet.contains("sugar") || diet.contains("sweet") || diet.contains("junk")) {
                    factors.add("poor diet");
                } else if (diet.contains("healthy") || diet.contains("balanced") || diet.contains("vegetable")) {
                    factors.add("good diet");
                }
            }
        }
        
        // Extract alcohol factors
        if (surveyResponse.getAlcohol() != null) {
            String alcohol = surveyResponse.getAlcohol().toLowerCase().trim();
            List<String> alcoholFactors = ALCOHOL_FACTORS.get(alcohol);
            if (alcoholFactors != null) {
                factors.addAll(alcoholFactors);
            } else {
                // Try partial matching for alcohol
                if (alcohol.contains("heavy") || alcohol.contains("frequent") || alcohol.contains("daily")) {
                    factors.add("excessive alcohol");
                } else if (alcohol.contains("never") || alcohol.contains("none")) {
                    factors.add("no alcohol");
                }
            }
        }
        
        // Extract stress factors
        if (surveyResponse.getStress() != null) {
            String stress = surveyResponse.getStress().toLowerCase().trim();
            List<String> stressFactors = STRESS_FACTORS.get(stress);
            if (stressFactors != null) {
                factors.addAll(stressFactors);
            } else {
                // Try partial matching for stress
                if (stress.contains("high") || stress.contains("severe") || stress.contains("extreme")) {
                    factors.add("high stress");
                } else if (stress.contains("low") || stress.contains("minimal")) {
                    factors.add("low stress");
                }
            }
        }
        
        // Extract sleep factors
        if (surveyResponse.getSleep() != null) {
            String sleep = surveyResponse.getSleep().toLowerCase().trim();
            List<String> sleepFactors = SLEEP_FACTORS.get(sleep);
            if (sleepFactors != null) {
                factors.addAll(sleepFactors);
            } else {
                // Try partial matching for sleep
                if (sleep.contains("poor") || sleep.contains("insufficient") || sleep.contains("disrupted")) {
                    factors.add("poor sleep");
                } else if (sleep.contains("good") || sleep.contains("excellent") || sleep.contains("adequate")) {
                    factors.add("good sleep");
                }
            }
        }
        if (surveyResponse.getFamilyHistory() != null) {
            String familyHistory = surveyResponse.getFamilyHistory().toLowerCase().trim();
            if (familyHistory.contains("diabetes") || familyHistory.contains("heart") || 
                familyHistory.contains("cancer") || familyHistory.contains("stroke")) {
                factors.add("family history");
            }
        }
        if (surveyResponse.getChronicConditions() != null) {
            String chronicConditions = surveyResponse.getChronicConditions().toLowerCase().trim();
            if (!chronicConditions.isEmpty() && !chronicConditions.contains("none") && 
                !chronicConditions.contains("no")) {
                factors.add("chronic conditions");
            }
        }

        if (surveyResponse.getAge() != null) {
            if (surveyResponse.getAge() >= 65) {
                factors.add("advanced age");
            } else if (surveyResponse.getAge() >= 45) {
                factors.add("middle age");
            }
        }
        
        return factors.stream().distinct().toList();
    }
    
    private double calculateConfidence(List<String> factors, SurveyResponse response) {
        // Base confidence on how many fields we could extract factors from
        int totalPossibleFactors = 0;
        int extractedFactors = 0;

        if (response.getSmoker() != null) {
            totalPossibleFactors++;
            if (factors.contains("smoking")) extractedFactors++;
        }
        
        if (response.getExercise() != null && !response.getExercise().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("exercise"))) extractedFactors++;
        }
        
        if (response.getDiet() != null && !response.getDiet().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("diet"))) extractedFactors++;
        }
        
        if (response.getAlcohol() != null && !response.getAlcohol().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("alcohol"))) extractedFactors++;
        }
        
        if (response.getStress() != null && !response.getStress().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("stress"))) extractedFactors++;
        }
        
        if (response.getSleep() != null && !response.getSleep().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("sleep"))) extractedFactors++;
        }
        
        if (response.getFamilyHistory() != null && !response.getFamilyHistory().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.contains("family history")) extractedFactors++;
        }
        
        if (response.getChronicConditions() != null && !response.getChronicConditions().trim().isEmpty()) {
            totalPossibleFactors++;
            if (factors.contains("chronic conditions")) extractedFactors++;
        }
        
        if (response.getAge() != null) {
            totalPossibleFactors++;
            if (factors.stream().anyMatch(f -> f.contains("age"))) extractedFactors++;
        }
        
        return totalPossibleFactors > 0 ? (double) extractedFactors / totalPossibleFactors : 0.0;
    }
}
