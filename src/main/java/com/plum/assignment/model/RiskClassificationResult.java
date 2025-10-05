package com.plum.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskClassificationResult {
    
    @JsonProperty("risk_level")
    private String riskLevel;
    
    private Integer score;
    private List<String> rationale;
    
}
