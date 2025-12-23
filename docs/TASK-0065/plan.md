# TASK-0065: Bulk Transaction Save - Implementation Plan

## Task Description
Save all approved transactions from staging to database with atomic behavior, deduplication handling, and user feedback on skipped duplicates.

## User Requirements (from clarification)
1. Show notification about skipped duplicates during save
2. Atomic transaction - all-or-nothing (excluding duplicates)
3. Clear staging and redirect to transaction list after successful save

## Current State Analysis

### Backend
- **Hash-based deduplication infrastructure exists** but is not actively used
  - `TransactionHashCalculator` calculates SHA-256 from: accountId, amount, currency, normalized description, date, type
  - `TransactionRepository.findByAccountIdAndHash()` method available
  - Database column `transaction_hash` (64 chars, NOT NULL) exists
- **Single transaction save** via `POST /api/transactions` works
- **No bulk save endpoint** exists - current staging saves transactions one-by-one

### Frontend
- `TransactionStagingService.saveAll()` saves transactions sequentially (not atomic)
- Staging state managed in-memory via `List<TransactionProposal>`
- No duplicate detection feedback to user

## Implementation Strategy

### Backend Changes

#### 1. Create Bulk Save Endpoint
**File**: `/backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/api/TransactionController.java`

Add new endpoint:
```java
@PostMapping("/api/transactions/bulk")
public BulkCreateTransactionResponse bulkCreateTransaction(
    @RequestBody BulkCreateTransactionRequest request,
    @AuthenticationPrincipal UserDetails userDetails
)
```

#### 2. Create DTOs
**Files to create**:
- `/backend/.../transaction/infrastructure/api/BulkCreateTransactionRequest.java`
  ```java
  record BulkCreateTransactionRequest(
      UUID accountId,
      List<CreateTransactionRequest> transactions
  )
  ```

- `/backend/.../transaction/infrastructure/api/BulkCreateTransactionResponse.java`
  ```java
  record BulkCreateTransactionResponse(
      int savedCount,
      int duplicateCount,
      List<UUID> savedTransactionIds
  )
  ```

#### 3. Add Deduplication to TransactionService
**File**: `/backend/src/main/java/pl/btsoftware/backend/transaction/application/TransactionService.java`

Modify `createTransaction()`:
- Add duplicate check before save
- Throw `DuplicateTransactionException` when duplicate detected

Create new method:
```java
@Transactional
public BulkCreateResult bulkCreateTransactions(
    List<CreateTransactionCommand> commands,
    UserDetails userDetails
)
```

Logic:
1. Calculate hashes for all transactions
2. Query for existing duplicates in single batch query
3. Filter out duplicates
4. Save all non-duplicate transactions in single transaction
5. Update account balance once
6. Return result with saved count and duplicate count

#### 4. Create Domain Exception
**File to create**: `/backend/.../transaction/domain/DuplicateTransactionException.java`
```java
public class DuplicateTransactionException extends RuntimeException
```

#### 5. Optimize Repository for Batch Duplicate Check
**File**: `/backend/.../transaction/domain/TransactionRepository.java`

Add method:
```java
List<TransactionHash> findExistingHashes(
    AccountId accountId,
    List<TransactionHash> hashes,
    GroupId groupId
)
```

**File**: `/backend/.../transaction/infrastructure/persistance/TransactionJpaRepository.java`

Add JPA query method:
```java
@Query("SELECT t.transactionHash FROM TransactionEntity t WHERE ...")
List<String> findExistingHashesByAccountIdAndHashesInAndCreatedByGroup(...)
```

#### 6. Update TransactionModuleFacade
**File**: `/backend/.../transaction/application/TransactionModuleFacade.java`

Add:
```java
public BulkCreateResult bulkCreateTransactions(
    List<CreateTransactionCommand> commands,
    UserDetails userDetails
)
```

### Frontend Changes

#### 7. Update TransactionService
**File**: `/frontend/lib/services/transaction_service.dart`

Add method:
```dart
Future<BulkCreateResponse> bulkCreateTransactions(
  String accountId,
  List<CreateTransactionRequest> transactions
)
```

Create DTO model:
```dart
class BulkCreateResponse {
  final int savedCount;
  final int duplicateCount;
  final List<String> savedTransactionIds;
}
```

#### 8. Update TransactionStagingService
**File**: `/frontend/lib/services/transaction_staging_service.dart`

Replace `saveAll()` implementation:
- Call new bulk endpoint instead of sequential saves
- Return `BulkCreateResponse` for feedback

#### 9. Update UI for Duplicate Notification
**File**: `/frontend/lib/screens/transaction_staging_screen.dart`

Modify `_saveAll()`:
- Display success message with duplicate count if any
- Example: "Saved 45 transactions. 3 duplicates were skipped."
- Clear staging and navigate to transaction list

### Testing Strategy

#### Backend Tests

**Unit Tests** (`/backend/src/test/java/.../TransactionServiceTest.java`):
1. Single transaction duplicate detection (new)
2. Bulk save with no duplicates
3. Bulk save with some duplicates
4. Bulk save with all duplicates
5. Atomic behavior - verify rollback on error

**Integration Tests** (`/backend/src/systemTest/java/.../TransactionServiceTest.java`):
1. Bulk save persists to database correctly
2. Account balance updated correctly after bulk save
3. Duplicate hash detection works end-to-end
4. Transaction rollback on mid-batch error

**API Tests** (create `/backend/src/systemTest/java/.../TransactionControllerTest.java`):
1. POST `/api/transactions/bulk` returns correct response
2. Validates authentication
3. Validates group isolation

#### Frontend Tests

**Service Tests** (`/frontend/test/services/transaction_staging_service_test.dart`):
1. `saveAll()` calls bulk endpoint
2. Staging cleared after successful save
3. Returns duplicate count correctly

**Widget Tests** (`/frontend/test/screens/transaction_staging_screen_test.dart`):
1. Success message shows duplicate count
2. Navigation to transaction list after save
3. Error handling displays correctly

## Implementation Steps (Test-First)

### Phase 1: Backend Deduplication (Single Transaction)
1. Write failing test: single duplicate transaction rejected
2. Implement duplicate check in `TransactionService.createTransaction()`
3. Verify test passes
4. Write failing test: non-duplicate transaction saved
5. Verify existing logic passes

### Phase 2: Backend Bulk Save Infrastructure
1. Write failing test: bulk save with no duplicates saves all
2. Create DTOs: `BulkCreateTransactionRequest`, `BulkCreateTransactionResponse`
3. Create `BulkCreateResult` domain object
4. Implement `TransactionService.bulkCreateTransactions()`
5. Verify test passes

### Phase 3: Backend Batch Duplicate Detection
1. Write failing test: bulk save with duplicates skips duplicates
2. Add `findExistingHashes()` to repository
3. Implement batch duplicate filtering in `bulkCreateTransactions()`
4. Verify test passes

### Phase 4: Backend Atomic Transaction
1. Write failing test: mid-batch error rolls back all saves
2. Ensure `@Transactional` on `bulkCreateTransactions()`
3. Verify rollback behavior
4. Verify test passes

### Phase 5: Backend API Endpoint
1. Write failing API test: POST `/api/transactions/bulk`
2. Implement `TransactionController.bulkCreateTransaction()`
3. Wire to `TransactionModuleFacade`
4. Verify test passes

### Phase 6: Frontend Service
1. Write failing test: `TransactionService.bulkCreateTransactions()` calls endpoint
2. Implement method with DTO parsing
3. Verify test passes

### Phase 7: Frontend Staging Service
1. Write failing test: `saveAll()` uses bulk endpoint
2. Modify implementation
3. Verify staging cleared after save
4. Verify test passes

### Phase 8: Frontend UI
1. Write failing test: duplicate notification shown
2. Update `_saveAll()` to display message
3. Verify navigation to transaction list
4. Verify test passes

### Phase 9: Integration Testing
1. Test end-to-end flow with real CSV import
2. Verify duplicate detection works with hash calculation
3. Verify account balance update
4. Manual testing of full user flow

## Files to Create

### Backend
- `/backend/.../transaction/infrastructure/api/BulkCreateTransactionRequest.java`
- `/backend/.../transaction/infrastructure/api/BulkCreateTransactionResponse.java`
- `/backend/.../transaction/domain/DuplicateTransactionException.java`
- `/backend/.../transaction/application/BulkCreateResult.java`
- `/backend/src/systemTest/java/.../TransactionControllerTest.java` (if doesn't exist)

### Frontend
- No new files - modify existing services and screens

## Files to Modify

### Backend
- `/backend/.../transaction/infrastructure/api/TransactionController.java`
- `/backend/.../transaction/application/TransactionService.java`
- `/backend/.../transaction/application/TransactionModuleFacade.java`
- `/backend/.../transaction/domain/TransactionRepository.java`
- `/backend/.../transaction/infrastructure/persistance/JpaTransactionRepository.java`
- `/backend/.../transaction/infrastructure/persistance/TransactionJpaRepository.java`
- `/backend/src/test/java/.../TransactionServiceTest.java`
- `/backend/src/systemTest/java/.../TransactionServiceTest.java`

### Frontend
- `/frontend/lib/services/transaction_service.dart`
- `/frontend/lib/services/transaction_staging_service.dart`
- `/frontend/lib/screens/transaction_staging_screen.dart`
- `/frontend/test/services/transaction_staging_service_test.dart`
- `/frontend/test/screens/transaction_staging_screen_test.dart` (create if doesn't exist)

## Success Criteria
1. All tests pass (unit, integration, widget)
2. Bulk save is atomic - all transactions saved or none
3. Duplicates are detected and skipped based on hash
4. User sees notification: "Saved X transactions. Y duplicates were skipped."
5. Staging cleared and user redirected after save
6. Account balance updated correctly
7. No backward compatibility code needed
8. Code coverage remains above 90%

## Non-Goals
- Support for partial saves (rejected in favor of atomic)
- Persistence of staging data to device storage
- Undo functionality for saved transactions
- Custom duplicate detection rules (using existing hash mechanism)

## Risks and Mitigations
- **Risk**: Large batch saves (1000+ transactions) may timeout
  - **Mitigation**: Add pagination/chunking if needed in future task
- **Risk**: Hash collision (extremely unlikely with SHA-256)
  - **Mitigation**: Accept risk - probability negligible
- **Risk**: Race condition with concurrent saves
  - **Mitigation**: Database transaction isolation handles this

## Dependencies
- TASK-0062 (Transaction deduplication) - **COMPLETED** ✓
- TASK-0063 (Staging list UI) - **COMPLETED** ✓
- TASK-0064 (Transaction edit in staging) - **COMPLETED** ✓

## Follow-up Tasks
- TASK-0070: Duplicate detection notification (separate task for import flow)
