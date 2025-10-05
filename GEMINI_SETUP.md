# Gemini API Setup Guide

This application now uses Google's Gemini AI API to provide intelligent health risk analysis instead of hardcoded answers.

## Prerequisites

1. A Google Cloud account
2. Access to Google AI Studio or Google Cloud Console

## Getting Your Gemini API Key

### Method 1: Google AI Studio (Recommended)
1. Go to [Google AI Studio](https://aistudio.google.com/)
2. Sign in with your Google account
3. Click on "Get API Key" in the left sidebar
4. Create a new API key or use an existing one
5. Copy the API key

### Method 2: Google Cloud Console
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Generative AI API
4. Go to "APIs & Services" > "Credentials"
5. Create credentials > API Key
6. Copy the API key

## Setting Up the API Key

### Option 1: Environment Variable (Recommended)
Set the `GEMINI_API_KEY` environment variable:

```bash
# Linux/Mac
export GEMINI_API_KEY="your-api-key-here"

# Windows
set GEMINI_API_KEY=your-api-key-here
```

### Option 2: Application Properties
Edit `src/main/resources/application.properties` and replace the placeholder:

```properties
gemini.api.key=your-actual-api-key-here
```

**⚠️ Security Warning:** Never commit your actual API key to version control. Use environment variables or secure configuration management.

## Running the Application

1. Make sure your API key is configured
2. Build the application: `mvn clean install`
3. Run the application: `mvn spring-boot:run`

## Features Enhanced with Gemini AI

### 1. Intelligent Factor Extraction
- AI analyzes survey responses to identify health risk factors
- More accurate than hardcoded rule-based extraction
- Understands context and nuance in responses

### 2. Smart Risk Classification
- AI determines risk levels based on comprehensive analysis
- Considers multiple factors and their interactions
- More sophisticated than simple scoring algorithms

### 3. Personalized Recommendations
- AI generates tailored health recommendations
- Considers individual circumstances and risk factors
- More relevant and actionable than generic advice

### 4. Enhanced Text Parsing
- AI improves OCR text parsing accuracy
- Better understanding of unstructured text
- Handles various input formats and languages

## Fallback Behavior

The application includes robust fallback mechanisms:
- If Gemini API is unavailable, the system falls back to rule-based processing
- Ensures the application continues to work even without AI
- Maintains backward compatibility

## API Usage and Costs

- Gemini API has usage limits and potential costs
- Monitor your usage in Google AI Studio or Cloud Console
- Consider implementing rate limiting for production use

## Troubleshooting

### Common Issues

1. **API Key Not Working**
   - Verify the key is correctly set
   - Check if the Generative AI API is enabled
   - Ensure the key has proper permissions

2. **Rate Limiting**
   - Implement request throttling
   - Consider caching responses
   - Monitor API usage quotas

3. **Network Issues**
   - Check internet connectivity
   - Verify firewall settings
   - Test API connectivity separately

### Logs
Check application logs for detailed error messages:
```bash
tail -f logs/application.log
```

## Testing

Test the AI integration with sample data:

```bash
curl -X POST http://localhost:8080/api/health-profile/analyze-json \
  -H "Content-Type: application/json" \
  -d '{
    "age": 35,
    "smoker": false,
    "exercise": "moderate",
    "diet": "balanced",
    "alcohol": "occasional",
    "stress": "moderate",
    "sleep": "good",
    "familyHistory": "diabetes",
    "chronicConditions": "none"
  }'
```

## Production Considerations

1. **Security**: Use secure configuration management
2. **Monitoring**: Implement API usage monitoring
3. **Caching**: Cache AI responses to reduce API calls
4. **Rate Limiting**: Implement proper rate limiting
5. **Error Handling**: Robust error handling and fallbacks
6. **Testing**: Comprehensive testing of AI integration
