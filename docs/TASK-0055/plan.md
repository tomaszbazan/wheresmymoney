# TASK-0055: Integration in CsvParseService

## Overview

Integrate `CategorySuggestionService` into the CSV import flow to automatically suggest categories for parsed transactions using AI (Gemini API).

## Context

### Current State
- `CsvParseService` parses CSV files and returns `TransactionProposal` objects with `categoryId = null`
- `CategorySuggestionService` is implemented and tested but not integrated into the CSV import flow
- `TransactionProposal` is an immutable record that contains category information
- AI categorization happens synchronously via Gemini API with retry logic

### User Requirements (from questions)
1. **Always automatic**: AI categorization runs automatically for every CSV import
2. **Continue on failure**: If AI fails, import proceeds with null categories (graceful degradation)
3. **Skip existing categories**: Only apply AI suggestions to transactions without a category (not applicable for CSV import since all are null initially)

## Implementation Plan

### Step 1: Modify CsvParseService to Integrate CategorySuggestionService

**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/CsvParseService.java`

**Changes**:
1. Add `CategorySuggestionService` as a dependency via constructor injection
2. After parsing CSV, call `categoryService.suggestCategories(proposals, groupId)`
3. Apply category suggestions to transaction proposals
4. Return updated `CsvParseResult` with categorized proposals

**New Flow**:
```
parse(command) {
    1. Validate user (get groupId)
    2. Validate account (get currency)
    3. Parse CSV → CsvParseResult
    4. Call AI categorization → List<CategorySuggestion>
    5. Apply suggestions to proposals → List<TransactionProposal>
    6. Return CsvParseResult with updated proposals
}
```

**Key Design Decision**: Since `TransactionProposal` is immutable, we need to create new instances with updated `categoryId` values.

**Helper Method** (new):
```java
private List<TransactionProposal> applySuggestions(
    List<TransactionProposal> proposals,
    List<CategorySuggestion> suggestions
)
```
- Maps suggestions by transaction index
- Creates new `TransactionProposal` instances with suggested `categoryId`
- Preserves original proposal if no suggestion exists

**Error Handling**:
- If `suggestCategories()` returns `null` (API failure), proceed with original proposals
- Log warning when AI categorization fails
- Never fail the entire import due to AI errors

### Step 2: Update CsvParseServiceTest

**File**: `backend/src/test/java/pl/btsoftware/backend/csvimport/application/CsvParseServiceTest.java`

**New Test Cases**:

1. **shouldApplyCategorySuggestionsWhenAvailable**
   - Given: Valid CSV + categories exist + Gemini returns suggestions
   - When: parse() is called
   - Then: Proposals have suggested categoryId populated

2. **shouldContinueWithoutCategoriesWhenAiFails**
   - Given: Valid CSV + CategorySuggestionService returns null
   - When: parse() is called
   - Then: Proposals have null categoryId (graceful degradation)

3. **shouldContinueWithoutCategoriesWhenNoCategoriesExist**
   - Given: Valid CSV + no categories in database
   - When: parse() is called
   - Then: Proposals have null categoryId

4. **shouldHandleMixedIncomeAndExpenseTransactions**
   - Given: CSV with both income and expense transactions
   - When: parse() is called
   - Then: Both types get appropriate category suggestions

**Test Setup Changes**:
- Add `CategorySuggestionService` mock to existing setup
- Mock `CategoryRepository` and `GeminiClient` for AI flow
- Update existing tests to expect AI categorization calls

**Important**: All existing test cases must continue to pass with minimal changes (just mock setup).

### Step 3: Verify Integration End-to-End

**Manual Verification Steps**:
1. Start application with PostgreSQL
2. Create test categories (via API or database)
3. Upload CSV file with transactions
4. Verify transaction proposals have suggested categories
5. Test failure scenarios (no categories, API timeout, invalid response)

**What to Check**:
- Proposals returned from `/api/csv/parse` have `categoryId` populated
- Import continues successfully even if Gemini fails
- Categories match transaction types (INCOME → INCOME categories, EXPENSE → EXPENSE categories)
- Logging shows AI categorization attempts and results

## Technical Considerations

### Performance
- AI categorization is synchronous and may add 5-10 seconds to import
- `GeminiClient` uses `CompletableFuture` but service calls `.join()` to block
- Consider showing loading indicator in frontend (handled in TASK-0058)

### Error Handling
- `CategorySuggestionService.suggestCategories()` catches all exceptions and returns `null`
- `CsvParseService` must handle null return value gracefully
- Logging at WARN level for AI failures

### Data Flow
```
Controller → CsvParseService → Parser → TransactionProposal (categoryId=null)
                ↓
          CategorySuggestionService → Gemini API
                ↓
          Apply suggestions → TransactionProposal (categoryId=suggested)
                ↓
          Return to Controller
```

### Dependencies
- `CategorySuggestionService` (already implemented)
- `CategoryRepository` (for fetching categories by type and group)
- `GeminiClient` (for AI API calls)
- All dependencies are already available via Spring DI

## Test-First Development Steps

Following the project's TDD approach:

### Red Phase
1. Write `shouldApplyCategorySuggestionsWhenAvailable` test
2. Write `shouldContinueWithoutCategoriesWhenAiFails` test
3. Run tests → expect failures (service doesn't call AI yet)

### Green Phase
1. Add `CategorySuggestionService` dependency to `CsvParseService`
2. Implement `applySuggestions()` helper method
3. Integrate AI call after parsing
4. Run tests → expect all tests to pass

### Refactor Phase
1. Extract any complex logic into small helper functions
2. Ensure methods stay under 10 lines
3. Run checkstyle and spotbugs
4. Verify test coverage

## Files to Modify

1. `backend/src/main/java/pl/btsoftware/backend/csvimport/application/CsvParseService.java`
   - Add dependency injection
   - Add AI categorization call
   - Add helper method for applying suggestions

2. `backend/src/test/java/pl/btsoftware/backend/csvimport/application/CsvParseServiceTest.java`
   - Add mock setup for CategorySuggestionService
   - Add 4 new test cases
   - Update existing tests if needed

## Success Criteria

- All unit tests pass (existing + new)
- System tests pass
- Checkstyle and spotbugs pass
- CSV import returns proposals with suggested categories
- Import gracefully degrades when AI fails
- No breaking changes to existing API contracts
- Code coverage remains above 90%

## Related Tasks

- **TASK-0054**: CategorySuggestionService (prerequisite, completed)
- **TASK-0056**: Error handling for AI failures (will build on this)
- **TASK-0057**: Support INCOME and EXPENSE categories (already handled by CategorySuggestionService)
- **TASK-0058**: AI categorization UI integration (depends on this task)

## Notes

- This task focuses on backend integration only
- Frontend changes (showing suggested categories in UI) are in TASK-0058
- The integration is transparent to the frontend - proposals just have `categoryId` populated
- Retry logic is already implemented in `GeminiClient` via `@Retryable`
