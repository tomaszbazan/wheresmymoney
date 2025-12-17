# TASK-0036: Empty Category List Blocking - Implementation Plan

## Task Description

Disable transaction operations when no categories are defined to prevent users from creating transactions without proper
categorization.

## Problem Statement

Currently, the system allows users to attempt creating or editing transactions even when no categories exist. This leads
to:

- Poor user experience (form errors without clear guidance)
- Potential data integrity issues
- Confusion about why transactions cannot be saved

## Current State Analysis

### Frontend

- **TransactionsPage**: Entry point for add/edit/delete operations via FloatingActionButton and list callbacks
- **TransactionForm**: Requires category selection via validator, but assumes categories exist
- **SearchableCategoryDropdown**: Already handles empty category list with error state, but doesn't prevent form display
- Categories fetched via `CategoryService.getCategoriesByType(CategoryType type)`

### Backend

- **TransactionController**: Exposes POST/PUT endpoints for create/update operations
- **TransactionService**: Validates currency, description length, account existence
- **Missing**: No validation that categories exist before allowing transaction operations
- **Missing**: No validation that the provided categoryId is valid

## Solution Design

### Principle: Fail Fast

Block transaction operations as early as possible:

1. Backend validation (security boundary)
2. Frontend validation (user experience)

### Backend Implementation

#### 1. Create New Exception

**File**: `backend/src/main/java/pl/btsoftware/backend/category/domain/NoCategoriesAvailableException.java`

```java
public class NoCategoriesAvailableException extends BusinessException {
    public NoCategoriesAvailableException(CategoryType type) {
        super("No categories of type " + type + " are available. Please create categories first.");
    }
}
```

#### 2. Extend CategoryModuleFacade

**File**: `backend/src/main/java/pl/btsoftware/backend/category/application/CategoryModuleFacade.java`

Add method:

```java
public boolean hasCategories(CategoryType type, UUID groupId)
```

Implementation will check if any categories exist for the given type and group.

#### 3. Update CategoryService

**File**: `backend/src/main/java/pl/btsoftware/backend/category/application/CategoryService.java`

Implement the check using existing repository:

```java
public boolean hasCategories(CategoryType type, UUID groupId) {
    return !categoryRepository.findByGroupId(groupId)
            .stream()
            .filter(c -> c.type().equals(type))
            .toList()
            .isEmpty();
}
```

#### 4. Update TransactionService

**File**: `backend/src/main/java/pl/btsoftware/backend/transaction/application/TransactionService.java`

Add validation in both `createTransaction()` and `updateTransaction()`:

```java
private void validateCategoriesExist(TransactionType type, UUID groupId) {
    CategoryType categoryType = type == TransactionType.INCOME
            ? CategoryType.INCOME
            : CategoryType.EXPENSE;

    if (!categoryModuleFacade.hasCategories(categoryType, groupId)) {
        throw new NoCategoriesAvailableException(categoryType);
    }
}
```

Call this before transaction creation/update.

#### 5. Update GlobalExceptionHandler (Optional)

**File**: `backend/src/main/java/pl/btsoftware/backend/account/infrastructure/api/GlobalExceptionHandler.java`

Add specific handler if needed (otherwise BusinessException will catch it):

```java

@ExceptionHandler(NoCategoriesAvailableException.class)
public ResponseEntity<ErrorResponse> handleNoCategoriesAvailable(NoCategoriesAvailableException e) {
    return ResponseEntity
            .status(HttpStatus.PRECONDITION_FAILED)
            .body(new ErrorResponse(e.getMessage()));
}
```

### Frontend Implementation

#### 1. Update TransactionsPage

**File**: `frontend/lib/screens/transaction_page.dart`

Before showing add/edit dialogs:

1. Check if categories exist for the selected transaction type
2. If no categories exist, show informative dialog instead of form
3. Guide user to create categories first

Add helper method:

```dart
Future<bool> _hasCategoriesForType(TransactionType type) async {
  try {
    final categoryType = type == TransactionType.income
        ? CategoryType.income
        : CategoryType.expense;
    final categories = await _categoryService.getCategoriesByType(categoryType);
    return categories.isNotEmpty;
  } catch (e) {
    return false;
  }
}
```

Modify `_showAddTransactionDialog()` and `_showEditTransactionDialog()` to check first.

#### 2. Create Warning Dialog

**File**: `frontend/lib/widgets/no_categories_dialog.dart` (new file)

Simple dialog that:

- Explains no categories exist
- Provides navigation to category management page
- Has clear CTA button

#### 3. Enhance SearchableCategoryDropdown (Optional)

**File**: `frontend/lib/widgets/searchable_category_dropdown.dart`

Update empty state message to be more actionable:

- Change generic error to "No categories available. Please create categories in the Category Management section."

## Testing Strategy

### Backend Tests

#### Unit Tests (70%)

1. **CategoryServiceTest**
    - Test `hasCategories()` returns true when categories exist
    - Test `hasCategories()` returns false when no categories exist
    - Test `hasCategories()` filters by type correctly
    - Test `hasCategories()` filters by groupId correctly

2. **TransactionServiceTest**
    - Test `createTransaction()` throws `NoCategoriesAvailableException` when no categories exist
    - Test `createTransaction()` succeeds when categories exist
    - Test `updateTransaction()` throws `NoCategoriesAvailableException` when no categories exist
    - Test `updateTransaction()` succeeds when categories exist
    - Test validation checks correct category type (income vs expense)

#### Integration Tests (20%)

3. **TransactionControllerTest**
    - Test POST `/api/transactions` returns 412 when no categories exist
    - Test POST `/api/transactions` returns 201 when categories exist
    - Test PUT `/api/transactions/{id}` returns 412 when no categories exist
    - Test PUT `/api/transactions/{id}` returns 200 when categories exist

### Frontend Tests

#### Widget Tests (70%)

4. **TransactionsPageTest**
    - Test shows warning dialog instead of form when no categories exist
    - Test shows transaction form when categories exist
    - Test edit dialog checks categories before displaying
    - Test warning dialog navigates to category page

5. **NoCategoriesDialogTest**
    - Test dialog displays correct message
    - Test dialog has navigation button
    - Test button triggers correct navigation

#### Integration Tests (20%)

6. **TransactionFlowTest**
    - Test full flow: attempt to add transaction → see warning → navigate to categories → add category → return →
      successfully add transaction
    - Test error handling when backend returns 412

## Implementation Steps

### Phase 1: Backend (Test-First)

1. Write failing test: `CategoryServiceTest.hasCategories_returnsFalse_whenNoCategoriesExist()`
2. Implement `CategoryService.hasCategories()` → test passes
3. Write failing test: `TransactionServiceTest.createTransaction_throwsException_whenNoCategoriesExist()`
4. Create `NoCategoriesAvailableException` and add validation → test passes
5. Write failing test: `TransactionServiceTest.updateTransaction_throwsException_whenNoCategoriesExist()`
6. Add validation to update method → test passes
7. Write integration tests for controller endpoints
8. Update `CategoryModuleFacade` to expose new method
9. Run all tests to ensure nothing breaks

### Phase 2: Frontend (Test-First)

1. Write failing test: `TransactionsPageTest.showsWarningDialog_whenNoCategoriesExist()`
2. Implement category check in `TransactionsPage` → test passes
3. Write failing test: `NoCategoriesDialogTest.displaysCorrectMessage()`
4. Create `NoCategoriesDialog` widget → test passes
5. Write integration test for full user flow
6. Update `SearchableCategoryDropdown` error message (optional)
7. Run all tests

### Phase 3: Manual Testing

1. Start with fresh database (no categories)
2. Attempt to create transaction → verify warning appears
3. Navigate to category management via dialog
4. Create income and expense categories
5. Return to transactions → verify can now add transactions
6. Delete all categories
7. Attempt to edit existing transaction → verify warning appears

## Edge Cases to Consider

1. **User deletes last category**: After transaction creation, user deletes all categories. Existing transactions
   remain, but new ones cannot be created.
2. **Type mismatch**: User has only INCOME categories but tries to create EXPENSE transaction.
3. **Race condition**: User has categories page open in another tab, deletes last category while transaction form is
   open.
4. **Backend validation fails but frontend passes**: Backend must be source of truth.
5. **Import functionality**: When CSV import is implemented, same validation must apply.

## Success Criteria

- [ ] Backend throws `NoCategoriesAvailableException` when attempting to create transaction without categories
- [ ] Backend throws exception for both create and update operations
- [ ] Frontend shows warning dialog instead of transaction form when no categories exist
- [ ] Warning dialog provides clear path to category creation
- [ ] All tests pass (unit, integration, widget)
- [ ] Test coverage remains above 90%
- [ ] Manual testing confirms expected behavior
- [ ] No regression in existing transaction operations

## Files to Modify

### Backend

- `backend/src/main/java/pl/btsoftware/backend/category/domain/NoCategoriesAvailableException.java` (new)
- `backend/src/main/java/pl/btsoftware/backend/category/application/CategoryService.java`
- `backend/src/main/java/pl/btsoftware/backend/category/application/CategoryModuleFacade.java`
- `backend/src/main/java/pl/btsoftware/backend/transaction/application/TransactionService.java`
- `backend/src/main/java/pl/btsoftware/backend/account/infrastructure/api/GlobalExceptionHandler.java` (optional)

### Frontend

- `frontend/lib/screens/transaction_page.dart`
- `frontend/lib/widgets/no_categories_dialog.dart` (new)
- `frontend/lib/widgets/searchable_category_dropdown.dart` (optional enhancement)

### Tests

- `backend/src/test/java/pl/btsoftware/backend/category/application/CategoryServiceTest.java`
- `backend/src/test/java/pl/btsoftware/backend/transaction/application/TransactionServiceTest.java`
- `backend/src/test/java/pl/btsoftware/backend/transaction/infrastructure/api/TransactionControllerTest.java`
- `frontend/test/screens/transaction_page_test.dart` (new or modify existing)
- `frontend/test/widgets/no_categories_dialog_test.dart` (new)

## Estimated Complexity

**Medium** - Requires changes across multiple layers (domain, service, controller, UI) but logic is straightforward.

## Dependencies

- None - this task is independent and doesn't block or depend on other backlog items

## Future Enhancements

- Real-time validation: Detect when last category is deleted and disable transaction operations immediately
- Category type hints: Show specific message about which category type is missing (income vs expense)
- Bulk validation: When import is implemented, validate before processing entire batch
