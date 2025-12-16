# TASK-0033: Category deletion validation

## Description
Block deletion of categories with assigned transactions

## Current State Analysis

### Existing Implementation
- `CategoryService.deleteCategory()` (backend/src/main/java/pl/btsoftware/backend/category/application/CategoryService.java:63-74) performs soft deletion using tombstone pattern
- No validation exists to check if category has associated transactions before deletion
- `Transaction` domain model (backend/src/main/java/pl/btsoftware/backend/transaction/domain/Transaction.java:11-106) contains `CategoryId` field linking transactions to categories
- `TransactionRepository` interface provides methods to query transactions but no method exists to check if transactions exist for a given category

### Gap Analysis
The system currently allows deletion of categories that have transactions assigned to them, which could lead to:
- Data integrity issues
- Orphaned transaction records
- Loss of categorization information
- Difficulty in reporting and analysis

## Implementation Plan

### 1. Create Domain Exception
**File**: `backend/src/main/java/pl/btsoftware/backend/category/domain/error/CategoryHasTransactionsException.java`
- Create new exception extending `RuntimeException`
- Exception should indicate that category cannot be deleted due to existing transactions

### 2. Extend TransactionRepository Interface
**File**: `backend/src/main/java/pl/btsoftware/backend/transaction/domain/TransactionRepository.java`
- Add method `boolean existsByCategoryId(CategoryId categoryId, GroupId groupId)`
- This method will check if any transactions (only active) exist for given category

### 3. Implement Repository Method in Concrete Implementations

**File**: `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/persistance/JpaTransactionRepository.java`
- Implement `existsByCategoryId()` using JPA repository query
- Query should check only active transactions

**File**: `backend/src/test/java/pl/btsoftware/backend/transaction/infrastructure/persistance/InMemoryTransactionRepository.java`
- Implement `existsByCategoryId()` for in-memory repository
- Used for unit testing purposes

### 4. Create TransactionModuleFacade Method
**File**: `backend/src/main/java/pl/btsoftware/backend/transaction/TransactionModuleFacade.java`
- Add public method `boolean categoryHasTransactions(CategoryId categoryId, GroupId groupId)`
- This exposes transaction existence check to other modules (following modular monolith pattern)

### 5. Update CategoryService with Validation
**File**: `backend/src/main/java/pl/btsoftware/backend/category/application/CategoryService.java`
- Inject `TransactionModuleFacade` as dependency
- In `deleteCategory()` method (line 63), add validation before deletion:
  - Check if category has transactions using facade
  - Throw `CategoryHasTransactionsException` if transactions exist
  - Proceed with deletion only if no transactions exist

### 6. Update CategoryModuleConfiguration
**File**: `backend/src/main/java/pl/btsoftware/backend/category/infrastructure/configuration/CategoryModuleConfiguration.java`
- Update `CategoryService` bean creation to include `TransactionModuleFacade` dependency

### 7. Update CategoryController Error Handling
**File**: `backend/src/main/java/pl/btsoftware/backend/category/infrastructure/api/CategoryController.java`
- Exception handling likely already exists via global exception handler
- Verify that `CategoryHasTransactionsException` is properly mapped to appropriate HTTP status (409 Conflict or 400 Bad Request)

## Testing Strategy

### Unit Tests

**File**: `backend/src/test/java/pl/btsoftware/backend/category/application/CategoryServiceTest.java`

Add test cases:
1. `shouldThrowCategoryHasTransactionsExceptionWhenDeletingCategoryWithActiveTransactions()`
   - Create category
   - Create transaction with that category
   - Attempt to delete category
   - Verify exception is thrown

2. `shouldThrowCategoryHasTransactionsExceptionWhenDeletingCategoryWithDeletedTransactions()`
   - Create category
   - Create transaction with that category
   - Delete the transaction (soft delete)
   - Attempt to delete category
   - Verify exception is thrown (even deleted transactions should block category deletion)

3. `shouldAllowDeletingCategoryWithoutTransactions()`
   - Create category
   - Delete category without creating any transactions
   - Verify deletion succeeds

### Integration Tests

**File**: `backend/src/systemTest/java/pl/btsoftware/backend/transaction/infrastructure/persistance/JpaTransactionRepositoryTest.java`

Add test cases:
1. `shouldReturnTrueWhenTransactionsExistForCategory()`
   - Verify `existsByCategoryId()` returns true when transactions exist

2. `shouldReturnFalseWhenNoTransactionsExistForCategory()`
   - Verify `existsByCategoryId()` returns false when no transactions exist

### Controller/API Tests

**File**: `backend/src/test/java/pl/btsoftware/backend/category/infrastructure/api/CategoryControllerTest.java`

Add test case:
1. `shouldReturn409ConflictWhenDeletingCategoryWithTransactions()`
   - Test DELETE endpoint returns proper HTTP status when category has transactions

## Implementation Order

Following TDD approach:

1. Write failing unit test: `shouldThrowCategoryHasTransactionsExceptionWhenDeletingCategoryWithActiveTransactions()`
2. Create `CategoryHasTransactionsException`
3. Extend `TransactionRepository` interface with `existsByCategoryId()` method
4. Implement method in `InMemoryTransactionRepository` (for tests)
5. Update `TransactionModuleFacade` with `categoryHasTransactions()` method
6. Update `CategoryService` with validation logic
7. Update `CategoryModuleConfiguration` to inject dependency
8. Run test - should pass
9. Write additional unit tests and repeat cycle
10. Implement `existsByCategoryId()` in `JpaTransactionRepository`
11. Write and run integration tests
12. Write and run controller tests
13. Verify all tests pass

## Acceptance Criteria

- [ ] Unit tests written and passing (minimum 3 test cases)
- [ ] Integration tests written and passing
- [ ] API endpoint returns appropriate HTTP status code when attempting to delete category with transactions
- [ ] Categories without transactions can still be deleted successfully
- [ ] Both active and soft-deleted transactions prevent category deletion
- [ ] Code follows project guidelines (no mocks in tests, small functions, strong typing)
- [ ] Test coverage for category module remains above 90%
- [ ] Code passes checkstyle and spotbugs validation

## Dependencies

- Depends on existing Transaction module
- Follows modular monolith pattern with module facades
- Uses soft delete (tombstone) pattern consistent with existing implementation

## Notes

- This implementation blocks deletion at the service layer, preventing data integrity issues
- Consider future enhancement: cascade soft-delete of category to transactions (out of scope for this task)
- Frontend should display appropriate error message when deletion fails (separate task)
