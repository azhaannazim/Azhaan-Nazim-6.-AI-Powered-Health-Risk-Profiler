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
public class OcrParseResult {
    
    private SurveyResponse answers;
    private List<String> missingFields;
    private Double confidence;
    
    @JsonProperty("status")
    private String status;
    
    private String reason;
    
    public boolean isComplete() {
        return "ok".equals(status) && (missingFields == null || missingFields.isEmpty());
    }
    
    public boolean isIncomplete() {
        return "incomplete_profile".equals(status);
    }
}
