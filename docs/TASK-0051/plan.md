# Implementation Plan TASK-0051: CategorySuggestion domain model

## Task Objective
Create a domain model `CategorySuggestion` as a Value Object (DTO) to represent AI-generated category suggestions for transactions during CSV import.

## Functional Requirements
- Model will be used in-memory only (will not be persisted in the database)
- Model will contain three fields:
  - `transactionId` - transaction UUID (using TransactionId instead of index)
  - `categoryId` - suggested category (may be null if AI doesn't return a suggestion)
  - `confidence` - AI confidence level in the range 0.0-1.0 (according to Gemini API documentation)
- Model should be immutable
- Model should validate input data

## Project Context Analysis

### Existing Structures
- **TransactionId** (`shared/TransactionId.java`): Value Object wrapping UUID
- **CategoryId** (`shared/CategoryId.java`): Value Object wrapping UUID
- **TransactionProposal** (`csvimport/domain/TransactionProposal.java`): Record used to represent transactions before saving
- **Money** (`shared/Money.java`): Example of Value Object with validation and helper methods

### Project Patterns
- Using `record` for immutable Value Objects
- Validation in compact constructor
- Using `requireNonNull` for required fields
- Static factory methods (optional)

## Implementation Details

### 1. File Structure
**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorySuggestion.java`

**Location Rationale**:
- Module `csvimport` - category suggestions are used during CSV import
- Package `domain` - this is a domain model (Value Object)

### 2. Code Structure

```java
package pl.btsoftware.backend.csvimport.domain;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;

import static java.util.Objects.requireNonNull;

public record CategorySuggestion(
    TransactionId transactionId,
    CategoryId categoryId,
    double confidence
) {
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 1.0;

    public CategorySuggestion {
        requireNonNull(transactionId, "Transaction ID cannot be null");

        if (confidence < MIN_CONFIDENCE || confidence > MAX_CONFIDENCE) {
            throw new IllegalArgumentException(
                "Confidence must be between " + MIN_CONFIDENCE + " and " + MAX_CONFIDENCE
            );
        }
    }
}
```

### 3. Key Design Decisions

#### 3.1 Field `transactionId`
- **Type**: `TransactionId` (not nullable)
- **Rationale**:
  - According to user's answer, we use transaction UUID
  - We use existing Value Object `TransactionId` for consistency with project architecture
  - Required field - each suggestion must be associated with a specific transaction

#### 3.2 Field `categoryId`
- **Type**: `CategoryId` (nullable)
- **Rationale**:
  - AI may not return a suggestion for some transactions
  - We use existing Value Object `CategoryId`
  - Null means no suggestion from AI

#### 3.3 Field `confidence`
- **Type**: `double` (primitive)
- **Range**: 0.0 - 1.0
- **Rationale**:
  - According to Gemini API documentation (source: https://medium.com/google-cloud/building-a-vision-powered-infrastructure-detection-agent-with-gemini-3-8fc2f1067082)
  - Format 0.0-1.0 is the standard in ML/AI (0% - 100%)
  - Primitive double instead of BigDecimal for simplicity and performance

#### 3.4 Validation
- `transactionId` - required (null check)
- `categoryId` - optional (may be null)
- `confidence` - range validation 0.0-1.0

#### 3.5 No Additional Metadata
- According to user's answer, we don't add fields like `reasoning` or `explanation`
- Model contains only minimal required fields

## TDD Implementation

### Step 1: Write Tests (RED)
**Location**: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/CategorySuggestionTest.java`

Tests to create:
1. `shouldCreateCategorySuggestionWithValidData()` - create with valid data
2. `shouldCreateCategorySuggestionWithNullCategoryId()` - create with null categoryId
3. `shouldRejectNullTransactionId()` - reject null transactionId
4. `shouldRejectConfidenceBelowZero()` - reject confidence < 0.0
5. `shouldRejectConfidenceAboveOne()` - reject confidence > 1.0
6. `shouldAcceptMinimumConfidence()` - accept confidence = 0.0
7. `shouldAcceptMaximumConfidence()` - accept confidence = 1.0
8. `shouldAcceptMidRangeConfidence()` - accept confidence = 0.5

### Step 2: Implementation (GREEN)
Create `CategorySuggestion` class according to the structure described in section 2.

### Step 3: Refactoring (if needed)
- Check compliance with project standards
- Run checkstyle and spotbugs
- Ensure code is clean and readable

## Acceptance Criteria
- [ ] `CategorySuggestion` class is created as a `record`
- [ ] Contains three fields: `transactionId`, `categoryId`, `confidence`
- [ ] Validation in compact constructor:
  - `transactionId` cannot be null
  - `confidence` must be in range 0.0-1.0
  - `categoryId` may be null
- [ ] All unit tests pass (8 tests)
- [ ] Test code coverage is 100%
- [ ] Code passes checkstyle and spotbugs
- [ ] Class is not persisted (no JPA annotations)

## Integration with the Rest of the System
`CategorySuggestion` model will be used in:
- **TASK-0053**: `GeminiResponseParser` - parsing AI response to list of `CategorySuggestion`
- **TASK-0054**: `CategorySuggestionService` - returning list of `CategorySuggestion`
- **TASK-0055**: `CsvParseService` - using suggestions to enrich `TransactionProposal`
- **TASK-0058**: Frontend - displaying suggestions in UI

## Related Tasks
- **TASK-0050** ✅ - Gemini API integration (completed)
- **TASK-0052** ⏳ - CategorizationPromptBuilder (next)
- **TASK-0053** ⏳ - GeminiResponseParser (next)
- **TASK-0054** ⏳ - CategorySuggestionService (next)

## Sources
- [Building a Vision-Powered Infrastructure Detection Agent with Gemini 3](https://medium.com/google-cloud/building-a-vision-powered-infrastructure-detection-agent-with-gemini-3-8fc2f1067082) - confidence score format example
- [Gemini 3 Flash Documentation](https://blog.google/products/gemini/gemini-3-flash/) - model information
- [Structured Outputs | Gemini API](https://ai.google.dev/gemini-api/docs/structured-output) - structured JSON output

## Estimated Implementation Time
- Writing tests: ~15 minutes
- Class implementation: ~5 minutes
- Refactoring and verification: ~10 minutes
- **Total**: ~30 minutes
