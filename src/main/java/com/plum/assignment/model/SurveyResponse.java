package com.plum.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {
    
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;
    
    @JsonProperty("smoker")
    private Boolean smoker;
    
    private String exercise;
    private String diet;
    private String alcohol;
    private String stress;
    private String sleep;
    private String familyHistory;
    private String chronicConditions;
    
    // Additional fields that might be present in surveys
    private Double weight;
    private Double height;
    private String gender;
    private String occupation;
    private String education;
}
