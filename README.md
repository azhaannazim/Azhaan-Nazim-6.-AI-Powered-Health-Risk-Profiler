# AI-Powered Health Risk Profiler

A Spring Boot service that analyzes lifestyle survey responses (typed or scanned forms) and generates structured health risk profiles with actionable recommendations.

## Features

- **OCR/Text Parsing**: Ingest survey forms (text/image) and parse key fields
- **Factor Extraction**: Convert answers into health risk factors
- **Risk Classification**: Compute risk level with scoring logic
- **Recommendations**: Generate actionable, non-diagnostic guidance
- **Input Validation**: Handle noisy inputs and missing answers with guardrails

## API Endpoints

### Complete Health Profiling Workflow

#### 1. Analyze Text Input
```http
POST /api/health-profile/analyze-text
Content-Type: application/json

{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar"
}
```

#### 2. Analyze Image Upload
```http
POST /api/health-profile/analyze-image
Content-Type: multipart/form-data

file: [image file]
```

#### 3. Analyze JSON Input
```http
POST /api/health-profile/analyze-json
Content-Type: application/json

{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar",
  "alcohol": "moderate",
  "stress": "high",
  "sleep": "poor"
}
```

### Individual Step Endpoints

#### Step 1: OCR/Text Parsing
```http
POST /api/health-profile/parse-text
Content-Type: application/json

"Age: 42\nSmoker: yes\nExercise: rarely\nDiet: high sugar"
```

#### Step 2: Factor Extraction
```http
POST /api/health-profile/extract-factors
Content-Type: application/json

{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar"
}
```

#### Step 3: Risk Classification
```http
POST /api/health-profile/classify-risk?factors=smoking,poor diet,low exercise
Content-Type: application/json

{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar"
}
```

#### Step 4: Generate Recommendations
```http
POST /api/health-profile/generate-recommendations?factors=smoking,poor diet,low exercise&riskLevel=high
Content-Type: application/json

{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar"
}
```

### Health Check
```http
GET /api/health-profile/health
```

## Response Examples

### Complete Analysis Response
```json
{
  "parseResult": {
    "answers": {
      "age": 42,
      "smoker": true,
      "exercise": "rarely",
      "diet": "high sugar"
    },
    "missingFields": [],
    "confidence": 0.92,
    "status": "ok"
  },
  "factorResult": {
    "factors": ["smoking", "poor diet", "low exercise"],
    "confidence": 0.88
  },
  "riskResult": {
    "risk_level": "high",
    "score": 78,
    "rationale": ["smoking", "high sugar diet", "low activity"]
  },
  "recommendationResult": {
    "risk_level": "high",
    "factors": ["smoking", "poor diet", "low exercise"],
    "recommendations": [
      "Quit smoking immediately",
      "Reduce sugar and processed food intake",
      "Start with 30 minutes of daily walking",
      "Consult with a healthcare provider immediately"
    ],
    "status": "ok"
  },
  "overallStatus": "success",
  "message": "Health profile analysis completed successfully"
}
```

### Incomplete Data Response
```json
{
  "parseResult": {
    "answers": {
      "age": 42
    },
    "missingFields": ["smoker", "exercise", "diet"],
    "confidence": 0.25,
    "status": "incomplete_profile",
    "reason": ">50% fields missing"
  },
  "overallStatus": "incomplete",
  "message": "Survey data is incomplete - >50% fields missing"
}
```

## Risk Levels

- **High Risk (70-100)**: Multiple risk factors present requiring immediate attention
- **Moderate Risk (40-69)**: Some risk factors present, lifestyle changes recommended
- **Low Risk (20-39)**: Few risk factors, maintain current healthy habits
- **Minimal Risk (0-19)**: Excellent health profile, continue current practices

## Supported Survey Fields

- `age` (required): Age in years (18-120)
- `smoker` (required): Boolean indicating smoking status
- `exercise` (required): Exercise frequency/type
- `diet` (required): Diet description
- `alcohol`: Alcohol consumption level
- `stress`: Stress level
- `sleep`: Sleep quality
- `familyHistory`: Family medical history
- `chronicConditions`: Existing chronic conditions

## Input Formats

### JSON Input
```json
{
  "age": 42,
  "smoker": true,
  "exercise": "rarely",
  "diet": "high sugar"
}
```

### Structured Text Input
```
Age: 42
Smoker: yes
Exercise: rarely
Diet: high sugar
```

### Image Input
- Supported formats: JPEG, PNG, GIF, BMP
- Maximum file size: 10MB
- OCR processing for text extraction

## Error Handling

The service includes comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Invalid input data or validation errors
- `413 Payload Too Large`: File size exceeds limit
- `500 Internal Server Error`: Unexpected server errors

## Running the Application

1. **Prerequisites**:
   - Java 21+
   - Maven 3.6+

2. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the API**:
   - Base URL: `http://localhost:8080`
   - Health Check: `http://localhost:8080/api/health-profile/health`

## Testing

Run the test suite:
```bash
mvn test
```

## Dependencies

- Spring Boot 3.5.6
- Tess4J (OCR)
- Jackson (JSON processing)
- Spring Validation
- Lombok

## Notes

- This is a non-diagnostic health assessment tool
- Recommendations are for informational purposes only
- Always consult healthcare professionals for medical advice
- OCR functionality requires Tesseract installation for production use
