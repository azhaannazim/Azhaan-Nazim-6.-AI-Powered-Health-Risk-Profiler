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
public class FactorExtractionResult {
    
    private List<String> factors;
    private Double confidence;
    
}
