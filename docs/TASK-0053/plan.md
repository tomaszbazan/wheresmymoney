# TASK-0053: GeminiResponseParser Implementation Plan

## Task Description
Parse Gemini JSON response and map to CategoryId for each transaction.

## Context
The Gemini API integration is already implemented with:
- `GeminiClient` that makes async API calls with retry logic
- `CategorizationPromptBuilder` that builds JSON prompts with transactions and category tree
- `CategorySuggestion` domain model (transactionId, categoryId, confidence)
- Gemini returns JSON array: `[{"transactionId": 0, "categoryId": "uuid", "confidence": 0.95}]`

## Implementation Requirements

### Error Handling Strategy
- **Malformed JSON**: Throw `GeminiClientException` to trigger retry mechanism (up to 3 attempts)
- **Invalid categoryId**: Skip suggestion, log warning, continue processing valid suggestions
- **Invalid transactionId**: Skip suggestion with out-of-range index, log warning
- **Validation failures**: Continue processing, return partial results with valid suggestions only

### Architecture Decision
- Parser will be a **domain service** in `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/`
- Contains business logic for mapping Gemini responses to `CategorySuggestion` domain objects
- Name: `GeminiResponseParser`

## Implementation Steps

### Step 1: Create GeminiResponseParser Test (RED)
**File**: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/GeminiResponseParserTest.java`

Test scenarios to implement:
1. **Happy path**: Parse valid JSON array with multiple suggestions
2. **Null categoryId**: Handle suggestions where categoryId is null (no suitable category)
3. **Empty array**: Parse empty JSON array `[]`
4. **Malformed JSON**: Throw `GeminiClientException` for invalid JSON syntax
5. **Missing required fields**: Throw exception for missing transactionId/confidence
6. **Invalid confidence range**: Skip suggestions with confidence < 0.0 or > 1.0
7. **Invalid transactionId index**: Skip suggestions with negative or out-of-range indices
8. **Invalid UUID format**: Skip suggestions with malformed categoryId strings
9. **Mixed valid/invalid**: Return only valid suggestions, skip invalid ones
10. **Null/empty input**: Handle edge cases gracefully

Test structure:
```java
@Test
void shouldParseValidJsonArrayToListOfCategorySuggestions() {
    // given: valid JSON response with 2 suggestions
    // when: parse
    // then: returns list of 2 CategorySuggestion objects
}

@Test
void shouldHandleNullCategoryIdInSuggestion() {
    // given: JSON with null categoryId
    // when: parse
    // then: returns CategorySuggestion with null categoryId
}

@Test
void shouldThrowExceptionForMalformedJson() {
    // given: invalid JSON syntax
    // when: parse
    // then: throws GeminiClientException
}

@Test
void shouldSkipSuggestionWithInvalidConfidenceRange() {
    // given: JSON with confidence = 1.5
    // when: parse with transactionCount=1
    // then: returns empty list, logs warning
}

@Test
void shouldSkipSuggestionWithNegativeTransactionId() {
    // given: JSON with transactionId = -1
    // when: parse with transactionCount=1
    // then: returns empty list, logs warning
}

@Test
void shouldSkipSuggestionWithTransactionIdOutOfRange() {
    // given: JSON with transactionId = 10
    // when: parse with transactionCount=5
    // then: returns empty list, logs warning
}

@Test
void shouldSkipSuggestionWithInvalidUuidFormat() {
    // given: JSON with categoryId = "not-a-uuid"
    // when: parse with transactionCount=1
    // then: returns empty list, logs warning
}

@Test
void shouldReturnOnlyValidSuggestionsWhenMixedValidInvalid() {
    // given: JSON array with 3 suggestions (1 valid, 2 invalid)
    // when: parse with transactionCount=3
    // then: returns list with 1 valid CategorySuggestion
}

@Test
void shouldReturnEmptyListForEmptyJsonArray() {
    // given: JSON = "[]"
    // when: parse
    // then: returns empty list
}

@Test
void shouldThrowExceptionForNullInput() {
    // given: jsonResponse = null
    // when: parse
    // then: throws IllegalArgumentException
}

@Test
void shouldThrowExceptionForBlankInput() {
    // given: jsonResponse = "   "
    // when: parse
    // then: throws IllegalArgumentException
}
```

**Dependencies needed**:
- `CategoryId` from `pl.btsoftware.backend.shared`
- `TransactionId` from `pl.btsoftware.backend.shared`
- `CategorySuggestion` from domain
- `GeminiClientException` from `pl.btsoftware.backend.ai.infrastructure.client`
- JSON parsing library (Jackson or similar - check existing backend dependencies)

**Validation rules**:
- `transactionId` must be >= 0 and < transactionCount
- `confidence` must be between 0.0 and 1.0 (enforced by CategorySuggestion constructor)
- `categoryId` must be valid UUID format (or null)
- JSON must be valid array syntax

### Step 2: Implement GeminiResponseParser (GREEN)
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/GeminiResponseParser.java`

Implementation approach:
```java
public class GeminiResponseParser {
    private static final Logger log = LoggerFactory.getLogger(GeminiResponseParser.class);

    public List<CategorySuggestion> parse(String jsonResponse, int transactionCount) {
        // 1. Validate input (null/blank check)
        // 2. Parse JSON array using Jackson ObjectMapper
        // 3. For each JSON object:
        //    - Validate transactionId index
        //    - Parse categoryId (handle null, validate UUID format)
        //    - Extract confidence value
        //    - Create CategorySuggestion (may throw on invalid confidence)
        //    - Skip invalid suggestions, log warnings
        // 4. Return list of valid CategorySuggestion objects
    }
}
```

**Error handling logic**:
- Malformed JSON → throw `GeminiClientException("Failed to parse Gemini response", cause)`
- Invalid transactionId → log.warn, skip suggestion, continue
- Invalid UUID format → log.warn, skip suggestion, continue
- Invalid confidence → caught from CategorySuggestion constructor, log.warn, skip
- Missing required fields → log.warn, skip suggestion, continue

**Return value**:
- List of valid `CategorySuggestion` objects
- Empty list if no valid suggestions found
- Never returns null

### Step 3: Integration Testing Considerations
The parser itself doesn't need integration tests (it's pure parsing logic), but consider:
- Add test case in `GeminiClientTest` that verifies the full flow: prompt → API call → parse response
- This would be part of TASK-0054 (CategorySuggestionService)

### Step 4: Update Backlog
Mark TASK-0053 as completed in `docs/backlog.md`:
```markdown
| TASK-0053 | GeminiResponseParser | Parse Gemini JSON response and map to CategoryId for each transaction | [x] |
```

## Dependencies

### Existing Code
- ✅ `CategorySuggestion` domain model (already implemented)
- ✅ `TransactionId` shared type (already exists)
- ✅ `CategoryId` shared type (already exists)
- ✅ `GeminiClientException` (already exists)

### External Libraries
- Jackson JSON library (check `build.gradle` - likely already included via Spring Boot)
- SLF4J for logging (already included)

### Domain Rules to Enforce
1. TransactionId validation: `0 <= transactionId < transactionCount`
2. Confidence validation: `0.0 <= confidence <= 1.0` (enforced by CategorySuggestion)
3. CategoryId: valid UUID format or null
4. Skip invalid suggestions, never fail entire batch

## Testing Strategy

### Unit Tests (100% coverage target)
- All edge cases covered in Step 1
- Focus on validation logic and error handling
- No mocks needed (pure function, no external dependencies)

### Test Data Examples
```json
// Valid response
[
  {"transactionId": 0, "categoryId": "123e4567-e89b-12d3-a456-426614174000", "confidence": 0.95},
  {"transactionId": 1, "categoryId": null, "confidence": 0.3}
]

// Invalid confidence
[{"transactionId": 0, "categoryId": "123e4567-e89b-12d3-a456-426614174000", "confidence": 1.5}]

// Invalid UUID
[{"transactionId": 0, "categoryId": "not-a-uuid", "confidence": 0.8}]

// Out of range index
[{"transactionId": 999, "categoryId": "123e4567-e89b-12d3-a456-426614174000", "confidence": 0.9}]

// Malformed JSON
"[{invalid json syntax"
```

## Success Criteria

1. ✅ All tests pass (green)
2. ✅ Parser handles all error cases gracefully
3. ✅ Invalid suggestions are skipped with warnings logged
4. ✅ Malformed JSON triggers retry mechanism
5. ✅ Code follows project guidelines:
   - Small functions (< 10 lines)
   - No conditional logic in tests
   - Pure function (no side effects except logging)
   - Strong typing with domain types
6. ✅ Test coverage > 95%
7. ✅ Checkstyle and SpotBugs pass

## Next Steps (Future Tasks)
After TASK-0053 is complete:
- **TASK-0054**: Implement `CategorySuggestionService` that orchestrates the full flow
- **TASK-0055**: Integrate `CategorySuggestionService` into `CsvParseService`
- **TASK-0056**: Add error handling for AI failures in the service layer

## Notes
- CategoryId validation (checking if it exists in DB) is NOT part of this parser
- That validation will be done in TASK-0054 (CategorySuggestionService)
- Parser focuses solely on JSON structure validation and mapping to domain objects
- Logging framework: Use `@Slf4j` Lombok annotation for logger
