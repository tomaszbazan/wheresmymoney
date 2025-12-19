# TASK-0052: CategorizationPromptBuilder - Implementation Plan

## Goal
Create a builder that prepares a JSON prompt for Gemini API containing transactions and category tree for automatic categorization.

## Functional Requirements

### Input
- List of `TransactionProposal` with indices
- Hierarchical category tree `Category`

### Output
JSON-formatted prompt containing:
1. **System instructions** - AI instructions for categorization
2. **Transactions** - list of transactions to categorize:
   - `transactionId` - transaction id
   - `description` - transaction description
   - `type` - transaction type (INCOME/EXPENSE)
3. **Categories** - hierarchical category structure:
   - `id` - CategoryId
   - `name` - category name
   - `type` - category type (INCOME/EXPENSE)
   - `children` - list of subcategories (recursively)
4. **Expected response format** - expected response format:
   ```json
   [
     {
       "transactionId": 0,
       "categoryId": "uuid",
       "confidence": 0.95
     }
   ]
   ```

## Code Structure

### 1. Domain Model: `CategorizationPrompt`
**Location:** `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorizationPrompt.java`

```java
public record CategorizationPrompt(String jsonPrompt) {
    public CategorizationPrompt {
        requireNonNull(jsonPrompt, "JSON prompt cannot be null");
        if (jsonPrompt.isBlank()) {
            throw new IllegalArgumentException("JSON prompt cannot be blank");
        }
    }
}
```

**Tests:** `CategorizationPromptTest.java`
- Test: should reject null JSON prompt
- Test: should reject blank JSON prompt
- Test: should accept valid JSON prompt

### 2. Domain Service: `CategorizationPromptBuilder`
**Location:** `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CategorizationPromptBuilder.java`

Public methods:
- `CategorizationPrompt build(List<TransactionProposal> transactions, List<Category> categories)`

Private methods:
- `String buildSystemInstructions()` - AI instructions
- `List<TransactionForPrompt> buildTransactions(List<TransactionProposal> transactions)` - transaction transformation
- `List<CategoryNode> buildCategoryTree(List<Category> categories)` - category hierarchy construction
- `String toJson(Object prompt)` - JSON serialization

**Internal records for JSON structure:**
```java
private record PromptStructure(
    String systemInstructions,
    List<TransactionForPrompt> transactions,
    List<CategoryNode> categories,
    String expectedResponseFormat
) {}

private record TransactionForPrompt(
    int transactionId,
    String description,
    TransactionType type
) {}

private record CategoryNode(
    CategoryId id,
    String name,
    CategoryType type,
    List<CategoryNode> children
) {}
```

**Tests:** `CategorizationPromptBuilderTest.java`
- Test: should build valid prompt for single transaction and flat categories
- Test: should build valid prompt for multiple transactions
- Test: should build hierarchical category structure correctly
- Test: should include only INCOME categories for INCOME transactions context
- Test: should include only EXPENSE categories for EXPENSE transactions context
- Test: should include both INCOME and EXPENSE categories when mixed transactions
- Test: should assign correct indices to transactions
- Test: should reject empty transaction list
- Test: should reject empty category list
- Test: should include system instructions in prompt
- Test: should include expected response format in prompt
- Test: should escape special characters in descriptions
- Test: should handle null category parentId for root categories

## System Instructions Content

```
You are a financial transaction categorization AI. Your task is to suggest appropriate categories for transactions based on their description and type.

Rules:
1. Match transaction description to the most specific category available
2. Consider transaction type (INCOME/EXPENSE) when selecting categories
3. Only suggest categories that match the transaction type
4. Provide confidence score between 0.0 and 1.0
5. If uncertain, choose the most general matching category with lower confidence
6. Return null categoryId if no suitable category exists
7. Use the exact categoryId from the provided category tree

Response format:
Return a JSON array of objects, one for each transaction, with the following structure:
[
  {
    "transactionId": <transactionId>,
    "categoryId": "<uuid>",
    "confidence": <0.0-1.0>
  }
]
```

## Expected Response Format (in prompt)

```json
[
  {
    "transactionId": 0,
    "categoryId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "confidence": 0.95
  }
]
```

## Implementation Steps (TDD)

### Step 1: Domain Model `CategorizationPrompt`
1. Write test: should reject null JSON prompt
2. Implement constructor with null validation
3. Write test: should reject blank JSON prompt
4. Add blank string validation
5. Write test: should accept valid JSON prompt
6. Run tests - all should be green

### Step 2: Builder - basic structure
1. Write test: should reject empty transaction list
2. Implement `build()` method with empty transaction list validation
3. Write test: should reject empty category list
4. Add empty category list validation
5. Run tests

### Step 3: Builder - transaction construction
1. Write test: should build valid prompt for single transaction and flat categories
2. Implement `buildTransactions()` - transformation to TransactionForPrompt
3. Implement `toJson()` - serialization (can use Jackson ObjectMapper)
4. Write test: should assign correct indices to transactions
5. Write test: should escape special characters in descriptions
6. Run tests

### Step 4: Builder - category construction
1. Write test: should build hierarchical category structure correctly
2. Implement `buildCategoryTree()` - recursive tree construction
3. Write test: should handle null category parentId for root categories
4. Write test: should include only INCOME categories for INCOME transactions context
5. Write test: should include only EXPENSE categories for EXPENSE transactions context
6. Write test: should include both INCOME and EXPENSE categories when mixed transactions
7. Run tests

### Step 5: Builder - system instructions
1. Write test: should include system instructions in prompt
2. Implement `buildSystemInstructions()` - returns static instruction text
3. Write test: should include expected response format in prompt
4. Run tests

### Step 6: Integration
1. Write test: should build valid prompt for multiple transactions
2. Implement full `build()` method combining all components
3. Run all tests
4. Verify generated JSON correctness

## Additional Notes

### Dependencies
- Jackson for JSON serialization (already available in Spring Boot)
- Lombok for constructors

### Edge Cases
- Empty transaction descriptions (null or empty string)
- Categories without parent (root categories)
- Very long transaction descriptions (truncation to MAX_DESCRIPTION_LENGTH already in TransactionProposal)
- Special characters in descriptions and category names (JSON escaping)

### Performance
- For small number of transactions (<100) and categories (<1000) no issues expected
- If list becomes larger, pagination of Gemini requests may be needed

### Security
- List size validation (transactions, categories) to prevent DoS via large prompts
- JSON escaping to prevent injection

## Example Output

```json
{
  "systemInstructions": "You are a financial transaction categorization AI...",
  "transactions": [
    {
      "transactionId": 0,
      "description": "McDonald's Downtown",
      "type": "EXPENSE"
    },
    {
      "transactionId": 1,
      "description": "Salary payment",
      "type": "INCOME"
    }
  ],
  "categories": [
    {
      "id": "cat-1-uuid",
      "name": "Food",
      "type": "EXPENSE",
      "children": [
        {
          "id": "cat-2-uuid",
          "name": "Restaurants",
          "type": "EXPENSE",
          "children": [
            {
              "id": "cat-3-uuid",
              "name": "Fast Food",
              "type": "EXPENSE",
              "children": []
            }
          ]
        }
      ]
    },
    {
      "id": "cat-4-uuid",
      "name": "Income",
      "type": "INCOME",
      "children": [
        {
          "id": "cat-5-uuid",
          "name": "Salary",
          "type": "INCOME",
          "children": []
        }
      ]
    }
  ],
  "expectedResponseFormat": "[{\"transactionId\": 0, \"categoryId\": \"uuid\", \"confidence\": 0.95}]"
}
```

## Related Tasks
- **TASK-0053**: GeminiResponseParser - will parse response in the format defined in this prompt
- **TASK-0054**: CategorySuggestionService - will use this builder to create prompts
