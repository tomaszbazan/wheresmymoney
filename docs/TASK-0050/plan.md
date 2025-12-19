# TASK-0050: Gemini API Integration - Implementation Plan

## Overview
Configure Gemini 3.0 Flash Preview API client in the backend to enable AI-based transaction categorization.

## Configuration Details
- **API Key Storage**: Environment variable
- **Model**: gemini-3-flash-preview
- **Error Handling**: Comprehensive with retry logic and graceful degradation
- **Prerequisites**: API key already available

## Implementation Steps

### 1. Add Gemini API Dependencies
**Files to modify:**
- `backend/build.gradle`

**Actions:**
- Add Google Generative AI SDK dependency
- Verify compatibility with existing Spring Boot version

**Tests:**
- No direct tests; verify build succeeds with new dependencies

### 2. Create Configuration Class
**Files to create:**
- `backend/src/main/java/pl/btsoftware/backend/ai/infrastructure/config/GeminiConfig.java`

**Actions:**
- Create `@Configuration` class for Gemini API client
- Read API key from environment variable `GEMINI_API_KEY`
- Configure model name as `gemini-3-flash-preview`
- Set up connection timeouts and retry policies
- Validate API key presence on startup

**Tests:**
- Unit test: `GeminiConfigTest.java`
  - Test configuration bean creation
  - Test API key validation (fails when missing)
  - Test timeout configuration

### 3. Create Gemini Client Wrapper
**Files to create:**
- `backend/src/main/java/pl/btsoftware/backend/ai/infrastructure/client/GeminiClient.java`
- `backend/src/main/java/pl/btsoftware/backend/ai/infrastructure/client/GeminiClientException.java`

**Actions:**
- Create wrapper class around Google Generative AI SDK
- Implement method `generateContent(String prompt)` for basic text generation
- Add comprehensive error handling:
  - API rate limiting (429 errors) with exponential backoff
  - Network timeouts with retry logic (max 3 retries)
  - Invalid API key errors
  - Model overload errors
- Add logging for API calls and errors
- Create custom exception `GeminiClientException` for API-specific errors

**Tests:**
- Unit test: `GeminiClientTest.java`
  - Test successful API call (mock Gemini SDK)
  - Test retry logic on transient failures
  - Test exception handling for permanent failures
  - Test timeout behavior
  - Test rate limiting handling

### 4. Add Logging and Monitoring
**Files to modify:**
- `backend/src/main/resources/logback-spring.xml` (if exists) or create new

**Actions:**
- Configure dedicated logger for `pl.btsoftware.backend.ai` package
- Log API request/response metadata (not full content for privacy)
- Log retry attempts and failures

**Tests:**
- Manual verification of logs during development

## Package Structure
```
backend/src/main/java/pl/btsoftware/backend/ai/
├── infrastructure/
│   ├── config/
│   │   └── GeminiConfig.java
│   ├── client/
│   │   ├── GeminiClient.java
│   │   └── GeminiClientException.java
│   └── api/
│       └── GeminiHealthController.java
```

## Test Coverage Requirements
- **Unit Tests**: 95%+ coverage for client and config classes
- **Integration Tests**: Health check endpoint verification
- **Error Scenarios**: All exception paths tested

## Dependencies to Add
```gradle
// Google Generative AI SDK
implementation 'com.google.ai.generativelanguage:generativelanguage:0.7.1'
implementation 'io.grpc:grpc-okhttp:1.59.0'
```

## Environment Variables
```bash
GEMINI_API_KEY=<your-api-key-here>
```

## Definition of Done
- [ ] Gemini API client configured and working
- [ ] Environment variable setup documented
- [ ] Comprehensive error handling implemented
- [ ] Health check endpoint accessible
- [ ] All tests passing (unit + integration)
- [ ] Test coverage ≥ 95% for new code
- [ ] Logging configured for API interactions
- [ ] Code reviewed and follows project standards
- [ ] Documentation complete

## Risk Mitigation
1. **API Key Security**: Never commit API key to version control
2. **Rate Limiting**: Implement exponential backoff to avoid API bans
3. **Cost Control**: Monitor API usage; consider request caching for repeated prompts
4. **Fallback Strategy**: Application should work without AI (mark as "To be clarified")

## Next Steps (Future Tasks)
After completing this task:
- TASK-0051: Create category suggestion endpoint using this client
- TASK-0052: Send transaction descriptions to Gemini for categorization
