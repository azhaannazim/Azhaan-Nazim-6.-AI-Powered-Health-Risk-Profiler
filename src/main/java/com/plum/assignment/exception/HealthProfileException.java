package com.plum.assignment.exception;

public class HealthProfileException extends RuntimeException {
    
    private final String errorCode;
    private final String userMessage;
    
    public HealthProfileException(String message) {
        super(message);
        this.errorCode = "HEALTH_PROFILE_ERROR";
        this.userMessage = message;
    }
    
    public HealthProfileException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "HEALTH_PROFILE_ERROR";
        this.userMessage = message;
    }
    
    public HealthProfileException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}
