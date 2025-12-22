# TASK-0054: CategorySuggestionService Implementation Plan

## Overview
Create `CategorySuggestionService` to orchestrate AI-based transaction categorization. The service will fetch categories from the repository, build prompts using `CategorizationPromptBuilder`, call Gemini API via `GeminiClient`, and parse responses using `GeminiResponseParser`.

## Architecture Decisions

Based on clarification with the user:
- **Service type**: Synchronous (blocking) - waits for Gemini response before returning
- **Failure handling**: Return null suggestions on failure after retries (aligns with TASK-0056)
- **Category filtering**: Filter by transaction type (INCOME/EXPENSE) before sending to AI

## Dependencies

Existing components:
- `CategorySuggestion` (domain model) - backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorySuggestion.java:8
- `CategorizationPromptBuilder` - backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorizationPromptBuilder.java:14
- `GeminiResponseParser` - backend/src/main/java/pl/btsoftware/backend/csvimport/domain/GeminiResponseParser.java:18
- `GeminiClient` - backend/src/main/java/pl/btsoftware/backend/ai/infrastructure/client/GeminiClient.java:18
- `CategoryRepository` - backend/src/main/java/pl/btsoftware/backend/category/domain/CategoryRepository.java:10
- `TransactionProposal` - backend/src/main/java/pl/btsoftware/backend/csvimport/domain/TransactionProposal.java:12
- `Category` - backend/src/main/java/pl/btsoftware/backend/category/domain/Category.java:18

## Implementation Steps

### 1. Create CategorySuggestionService Test
**File**: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/CategorySuggestionServiceTest.java`

Test cases:
1. `shouldReturnSuggestionsForValidTransactions` - Happy path with multiple transactions
2. `shouldFilterCategoriesByTransactionType` - Verify only matching category types are sent to AI
3. `shouldReturnNullSuggestionsWhenGeminiFails` - Verify null return on Gemini failure
4. `shouldThrowExceptionWhenTransactionListIsEmpty` - Validate input
5. `shouldThrowExceptionWhenGroupIdIsNull` - Validate input
6. `shouldHandleTransactionsWithMixedTypes` - Both INCOME and EXPENSE in same batch
7. `shouldReturnEmptyListWhenNoCategoriesExist` - Edge case handling

Test setup:
- Use in-memory `CategoryRepository` implementation
- Mock `GeminiClient` using Mockito
- Use real `CategorizationPromptBuilder` and `GeminiResponseParser`

### 2. Implement CategorySuggestionService
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorySuggestionService.java`

Service structure:
```java
@Service
@RequiredArgsConstructor
public class CategorySuggestionService {
    private final CategoryRepository categoryRepository;
    private final GeminiClient geminiClient;
    private final CategorizationPromptBuilder promptBuilder;
    private final GeminiResponseParser responseParser;

    public List<CategorySuggestion> suggestCategories(
        List<TransactionProposal> transactions,
        GroupId groupId
    ) {
        // Implementation
    }
}
```

Implementation logic:
1. **Validate inputs**: Check transactions not empty, groupId not null
2. **Group transactions by type**: Separate INCOME and EXPENSE transactions
3. **Fetch categories per type**:
   - Use `categoryRepository.findByType(CategoryType.INCOME, groupId)` for income
   - Use `categoryRepository.findByType(CategoryType.EXPENSE, groupId)` for expense
4. **Handle empty categories**: Return empty list if no categories available
5. **Build prompts**: Create separate prompts for each transaction type group
6. **Call Gemini**: Use `geminiClient.generateContent()` for each prompt
7. **Parse responses**: Use `responseParser.parse()` to convert JSON to suggestions
8. **Merge results**: Combine suggestions from both transaction types
9. **Handle failures**: Catch exceptions, log warning, return null on failure

### 3. Add Error Handling
**Considerations**:
- Wrap Gemini calls in try-catch to handle `GeminiClientException`
- Return `null` instead of throwing exception on AI failure (as per TASK-0056)
- Log failures with sufficient context for debugging
- Preserve transaction processing flow even when AI fails

### 4. Integration Points
**Service will be used by**:
- `CsvParseService` (TASK-0055) - after CSV parsing, before presenting to user

**Dependencies needed**:
- `@Service` annotation for Spring component scanning
- Constructor injection via `@RequiredArgsConstructor`
- SLF4J logging via `@Slf4j`

## File Structure

```
backend/src/main/java/pl/btsoftware/backend/csvimport/domain/
  └── CategorySuggestionService.java (NEW)

backend/src/test/java/pl/btsoftware/backend/csvimport/domain/
  └── CategorySuggestionServiceTest.java (NEW)
```

## Testing Strategy

**Unit Tests** (7 test cases):
- Cover happy path with valid data
- Test input validation (null/empty checks)
- Verify category filtering by transaction type
- Test error handling (Gemini failure scenarios)
- Test edge cases (empty categories, mixed transaction types)

**No integration tests required** - service orchestrates existing components that are already tested

## Success Criteria

- All unit tests pass
- Service correctly filters categories by transaction type
- Gemini failures return null without breaking the flow
- Code follows project guidelines (small functions, pure logic, strong typing)
- No backward compatibility code needed
- Checkstyle and SpotBugs pass

## Notes

- GeminiClient is already annotated with `@Retryable` - no need to add retry logic in service
- GeminiClient is async (`@Async`) but returns `CompletableFuture` - need to call `.get()` or `.join()` to make it synchronous
- Transaction type is determined by the `type` field in `TransactionProposal`, not by amount sign
- CategorizationPromptBuilder already handles building transaction list with indexes
- Service should not modify transaction proposals or categories
