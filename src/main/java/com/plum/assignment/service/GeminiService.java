package com.plum.assignment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plum.assignment.model.SurveyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {
    
    private final WebClient webClient;
    private final String modelName;
    private final ObjectMapper objectMapper;
    
    public GeminiService(@Value("${gemini.api.key}") String apiKey,
                        @Value("${gemini.model.name}") String modelName) {
        this.modelName = modelName;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey).build();
    }

    public List<String> extractHealthFactors(SurveyResponse surveyResponse) {
        try {
            String prompt = buildFactorExtractionPrompt(surveyResponse);
            String response = callGeminiAPI(prompt);
            return parseFactorList(response);
        } catch (Exception e) {
            log.error("Error extracting factors with Gemini: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    public String classifyHealthRisk(SurveyResponse surveyResponse, List<String> factors) {
        try {
            String prompt = buildRiskClassificationPrompt(surveyResponse, factors);
            String response = callGeminiAPI(prompt);
            return parseRiskLevel(response);
        } catch (Exception e) {
            log.error("Error classifying risk with Gemini: {}", e.getMessage(), e);
            return "unknown"; // Return unknown on error
        }
    }

    public List<String> generateHealthRecommendations(SurveyResponse surveyResponse, 
                                                     List<String> factors, 
                                                     String riskLevel) {
        try {
            String prompt = buildRecommendationPrompt(surveyResponse, factors, riskLevel);
            String response = callGeminiAPI(prompt);
            return parseRecommendationList(response);
        } catch (Exception e) {
            log.error("Error generating recommendations with Gemini: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    public SurveyResponse parseStructuredTextWithAI(String textInput) {
        try {
            String prompt = buildTextParsingPrompt(textInput);
            String response = callGeminiAPI(prompt);
            return parseSurveyResponse(response);
        } catch (Exception e) {
            log.error("Error parsing text with Gemini: {}", e.getMessage(), e);
            return new SurveyResponse(); // Return empty response on error
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            
            parts.put("text", prompt);
            contents.put("parts", List.of(parts));
            requestBody.put("contents", List.of(contents));
            
            String response = webClient.post()
                    .uri("/models/{model}:generateContent", modelName)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                JsonNode candidates = jsonNode.get("candidates");
                if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).get("content");
                    if (content != null) {
                        JsonNode partsNode = content.get("parts");
                        if (partsNode != null && partsNode.isArray() && partsNode.size() > 0) {
                            return partsNode.get(0).get("text").asText();
                        }
                    }
                }
            }
            
            throw new RuntimeException("Failed to parse response from Gemini API");
            
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String buildFactorExtractionPrompt(SurveyResponse surveyResponse) {
        return String.format("""
            You are a medical AI assistant. Analyze the following health survey data and extract relevant health risk factors.
            
            Survey Data:
            - Age: %s
            - Smoker: %s
            - Exercise: %s
            - Diet: %s
            - Alcohol: %s
            - Stress: %s
            - Sleep: %s
            - Family History: %s
            - Chronic Conditions: %s
            
            Extract and list the health risk factors. Consider factors like:
            - Smoking status
            - Exercise level (sedentary, moderate, active)
            - Diet quality (poor, fair, good, excellent)
            - Alcohol consumption (none, moderate, excessive)
            - Stress levels (low, moderate, high)
            - Sleep quality (poor, fair, good, excellent)
            - Family history of diseases
            - Chronic conditions
            - Age-related factors
            
            Return only a comma-separated list of factors. Example: "smoking, sedentary lifestyle, poor diet, high stress"
            """,
            surveyResponse.getAge(),
            surveyResponse.getSmoker(),
            surveyResponse.getExercise(),
            surveyResponse.getDiet(),
            surveyResponse.getAlcohol(),
            surveyResponse.getStress(),
            surveyResponse.getSleep(),
            surveyResponse.getFamilyHistory(),
            surveyResponse.getChronicConditions()
        );
    }

    private String buildRiskClassificationPrompt(SurveyResponse surveyResponse, List<String> factors) {
        return String.format("""
            You are a medical AI assistant. Classify the health risk level based on the survey data and extracted factors.
            
            Survey Data:
            - Age: %s
            - Smoker: %s
            - Exercise: %s
            - Diet: %s
            - Alcohol: %s
            - Stress: %s
            - Sleep: %s
            - Family History: %s
            - Chronic Conditions: %s
            
            Extracted Factors: %s
            
            Classify the risk level as one of: minimal, low, moderate, high
            
            Consider:
            - Multiple severe risk factors = high risk
            - Some moderate risk factors = moderate risk
            - Few minor risk factors = low risk
            - No significant risk factors = minimal risk
            
            Return only the risk level: minimal, low, moderate, or high
            """,
            surveyResponse.getAge(),
            surveyResponse.getSmoker(),
            surveyResponse.getExercise(),
            surveyResponse.getDiet(),
            surveyResponse.getAlcohol(),
            surveyResponse.getStress(),
            surveyResponse.getSleep(),
            surveyResponse.getFamilyHistory(),
            surveyResponse.getChronicConditions(),
            String.join(", ", factors)
        );
    }

    private String buildRecommendationPrompt(SurveyResponse surveyResponse, 
                                           List<String> factors, 
                                           String riskLevel) {
        return String.format("""
            You are a medical AI assistant. Generate personalized health recommendations based on the survey data, factors, and risk level.
            
            Survey Data:
            - Age: %s
            - Smoker: %s
            - Exercise: %s
            - Diet: %s
            - Alcohol: %s
            - Stress: %s
            - Sleep: %s
            - Family History: %s
            - Chronic Conditions: %s
            
            Risk Factors: %s
            Risk Level: %s
            
            Generate 5-8 specific, actionable health recommendations. Focus on:
            - Lifestyle modifications
            - Preventive measures
            - Medical consultations if needed
            - Age-appropriate recommendations
            
            Return each recommendation on a new line, starting with a number (1., 2., etc.)
            """,
            surveyResponse.getAge(),
            surveyResponse.getSmoker(),
            surveyResponse.getExercise(),
            surveyResponse.getDiet(),
            surveyResponse.getAlcohol(),
            surveyResponse.getStress(),
            surveyResponse.getSleep(),
            surveyResponse.getFamilyHistory(),
            surveyResponse.getChronicConditions(),
            String.join(", ", factors),
            riskLevel
        );
    }

    private String buildTextParsingPrompt(String textInput) {
        return String.format("""
            You are a medical AI assistant. Parse the following text and extract health survey information.
            
            Text: "%s"
            
            Extract the following fields and return as JSON:
            {
                "age": number or null,
                "smoker": true/false or null,
                "exercise": "description" or null,
                "diet": "description" or null,
                "alcohol": "description" or null,
                "stress": "description" or null,
                "sleep": "description" or null,
                "familyHistory": "description" or null,
                "chronicConditions": "description" or null
            }
            
            If a field is not mentioned or unclear, set it to null. Return only valid JSON.
            """, textInput);
    }

    private List<String> parseFactorList(String response) {
        return List.of(response.trim().split(",\\s*"))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String parseRiskLevel(String response) {
        String level = response.trim().toLowerCase();
        if (level.contains("minimal")) return "minimal";
        if (level.contains("low")) return "low";
        if (level.contains("moderate")) return "moderate";
        if (level.contains("high")) return "high";
        return "unknown";
    }
    
    /**
     * Parse recommendation list from AI response
     */
    private List<String> parseRecommendationList(String response) {
        return response.lines()
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isEmpty())
                .toList();
    }
    
    /**
     * Parse survey response from AI JSON response
     */
    private SurveyResponse parseSurveyResponse(String response) {
        try {
            // Simple JSON parsing - in production, use a proper JSON parser
            SurveyResponse surveyResponse = new SurveyResponse();
            
            // Extract age
            if (response.contains("\"age\":")) {
                String ageStr = extractJsonValue(response, "age");
                if (ageStr != null && !ageStr.equals("null")) {
                    surveyResponse.setAge(Integer.parseInt(ageStr));
                }
            }
            
            // Extract smoker
            if (response.contains("\"smoker\":")) {
                String smokerStr = extractJsonValue(response, "smoker");
                if (smokerStr != null && !smokerStr.equals("null")) {
                    surveyResponse.setSmoker(Boolean.parseBoolean(smokerStr));
                }
            }
            
            // Extract other fields
            surveyResponse.setExercise(extractJsonValue(response, "exercise"));
            surveyResponse.setDiet(extractJsonValue(response, "diet"));
            surveyResponse.setAlcohol(extractJsonValue(response, "alcohol"));
            surveyResponse.setStress(extractJsonValue(response, "stress"));
            surveyResponse.setSleep(extractJsonValue(response, "sleep"));
            surveyResponse.setFamilyHistory(extractJsonValue(response, "familyHistory"));
            surveyResponse.setChronicConditions(extractJsonValue(response, "chronicConditions"));
            
            return surveyResponse;
        } catch (Exception e) {
            log.error("Error parsing survey response: {}", e.getMessage());
            return new SurveyResponse();
        }
    }
    
    /**
     * Extract value from JSON string
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            
            // Try for non-string values
            pattern = "\"" + key + "\"\\s*:\\s*([^,\\}]*)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(json);
            if (m.find()) {
                return m.group(1).trim();
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
