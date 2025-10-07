# Use Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/tessdata

COPY target/*.jar /app/assignment-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENV GEMINI_API_KEY=${GEMINI_API_KEY}

ENTRYPOINT ["java", "-jar", "/app/assignment-0.0.1-SNAPSHOT.jar"]
