# TASK-0062: Transaction Deduplication - Implementation Plan

## Goal
Implement a hash-based transaction deduplication mechanism to prevent re-importing the same transactions from CSV files.

## Functional Requirements
- **Deduplication timing**: During CSV import (staging) + before database save (verification)
- **Hash fields**: `accountId`, `amount`, `currency`, `description`, `transactionDate`, `type`
- **Storage**: `transaction_hash` field in Transaction entity (database is clean - no data migration needed)
- **User feedback**: Automatic skipping of duplicates with notification (e.g., "5 duplicates skipped")

## Solution Architecture

### 1. Domain Layer - Hash Calculation
Mechanism for calculating a deterministic hash for transactions.

**TransactionHash (Value Object)**
```java
public record TransactionHash(String value) {
    public TransactionHash {
        if (value == null || !value.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException("Invalid transaction hash");
        }
    }
}
```

**TransactionHashCalculator (Domain Service)**
```java
public class TransactionHashCalculator {
    String calculateHash(
        AccountId accountId,
        Money amount,
        String description,
        LocalDate transactionDate,
        TransactionType type
    );
}
```

Hash will use SHA-256 from concatenation of:
- accountId.value()
- amount.value() + amount.currency()
- description.trim().toLowerCase()
- transactionDate.toString()
- type.name()

### 2. Domain Model - Transaction Record
Add `transactionHash` field to the domain model.

**Transaction.java Modification**
```java
public record Transaction(
    TransactionId id,
    AccountId accountId,
    Money amount,
    TransactionType type,
    String description,
    CategoryId categoryId,
    TransactionHash transactionHash,  // NEW FIELD
    AuditInfo createdInfo,
    AuditInfo updatedInfo,
    Tombstone tombstone
)
```

### 3. Persistence Layer - Database Schema
Add `transaction_hash` column and index for fast duplicate lookup.

**Flyway Migration: V6__add_transaction_hash.sql**
```sql
ALTER TABLE transaction
    ADD COLUMN transaction_hash VARCHAR(64) NOT NULL;

CREATE INDEX idx_transaction_account_hash
    ON transaction (account_id, transaction_hash);
```

**TransactionEntity**
```java
@Entity
@Table(name = "transaction")
public class TransactionEntity {
    // ... existing fields

    @Column(name = "transaction_hash", nullable = false, length = 64)
    private String transactionHash;

    // getters/setters
}
```

### 4. Repository - Duplicate Detection Query
Extend `TransactionRepository` with a hash lookup method.

**TransactionRepository Interface**
```java
public interface TransactionRepository {
    // ... existing methods

    Optional<Transaction> findByAccountIdAndHash(
        AccountId accountId,
        TransactionHash hash,
        GroupId groupId
    );
}
```

**JpaTransactionRepository Implementation**
```java
@Override
public Optional<Transaction> findByAccountIdAndHash(
    AccountId accountId,
    TransactionHash hash,
    GroupId groupId
) {
    return transactionJpaRepository
        .findByAccountIdAndTransactionHashAndIsDeletedFalse(
            accountId.value(),
            hash.value()
        )
        .filter(entity -> entity.getCreatedByGroup().equals(groupId.value()))
        .map(transactionMapper::toDomain);
}
```

**TransactionJpaRepository**
```java
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByAccountIdAndTransactionHashAndIsDeletedFalse(
        UUID accountId,
        String transactionHash
    );
}
```

### 5. Application Layer - Bulk Import Service
New service for atomic import of multiple transactions with deduplication.

**BulkTransactionImportService**
```java
@Service
public class BulkTransactionImportService {

    record BulkImportCommand(
        UserId userId,
        AccountId accountId,
        List<TransactionProposalDto> proposals
    );

    record BulkImportResult(
        int saved,
        int skipped,
        List<UUID> savedTransactionIds,
        List<DuplicateInfo> duplicates
    );

    record DuplicateInfo(
        int proposalIndex,
        String description,
        BigDecimal amount,
        LocalDate date
    );

    @Transactional
    BulkImportResult importTransactions(BulkImportCommand command) {
        // 1. Validate user and account
        // 2. For each proposal:
        //    - Calculate hash
        //    - Check if duplicate (repository.findByAccountIdAndHash)
        //    - If not duplicate: create Transaction and store
        //    - If duplicate: add to skipped list
        // 3. Return BulkImportResult
    }
}
```

### 6. API Layer - Bulk Import Endpoint
New endpoint for bulk import of transactions from staging.

**POST /api/transactions/bulk/import**

Request:
```json
{
    "accountId": "uuid",
    "proposals": [
        {
            "transactionDate": "2024-01-15",
            "description": "Zakupy Biedronka",
            "amount": -45.50,
            "currency": "PLN",
            "type": "EXPENSE",
            "categoryId": "uuid"
        }
    ]
}
```

Response:
```json
{
    "saved": 8,
    "skipped": 2,
    "savedTransactionIds": ["uuid1", "uuid2", ...],
    "duplicates": [
        {
            "proposalIndex": 3,
            "description": "Zakupy Biedronka",
            "amount": -45.50,
            "date": "2024-01-15"
        }
    ]
}
```

**BulkTransactionImportController**
```java
@PostMapping("/api/transactions/bulk/import")
public ResponseEntity<BulkImportResultView> bulkImport(
    @RequestBody BulkImportRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // Map request -> command
    // Call bulkTransactionImportService.importTransactions()
    // Return result
}
```

### 7. Frontend - Bulk Save Integration
Modify `TransactionStagingService` to use the new bulk endpoint.

**transaction_staging_service.dart**
```dart
class TransactionStagingService {
    Future<BulkSaveResult> saveAll(String accountId, TransactionService transactionService) async {
        final result = await transactionService.bulkImport(
            accountId: accountId,
            proposals: _proposals,
        );

        // Clear staging
        _proposals.clear();
        notifyListeners();

        return result;
    }
}
```

**transaction_service.dart**
```dart
Future<BulkSaveResult> bulkImport({
    required String accountId,
    required List<TransactionProposal> proposals,
}) async {
    final response = await http.post(
        Uri.parse('$baseUrl/transactions/bulk/import'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
            'accountId': accountId,
            'proposals': proposals.map((p) => p.toJson()).toList(),
        }),
    );

    // Parse BulkImportResultView
    return BulkSaveResult.fromJson(jsonDecode(response.body));
}
```

### 8. Frontend - User Notification
Display duplicate information after bulk save.

**transaction_staging_screen.dart**
```dart
Future<void> _saveTransactions() async {
    final result = await _stagingService.saveAll(accountId, transactionService);

    ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
            content: Text(
                'Saved: ${result.saved}, '
                'Skipped duplicates: ${result.skipped}'
            ),
            backgroundColor: result.skipped > 0 ? Colors.orange : Colors.green,
        ),
    );

    // Optional: show dialog with list of skipped duplicates
    if (result.skipped > 0) {
        _showDuplicatesDialog(result.duplicates);
    }
}
```

## Implementation Order (Test-First)

### Step 1: Domain - TransactionHash Value Object
**Test**: `TransactionHashTest.java`
- Invalid hash format → exception
- Valid hash → object created
- Equality semantics

**Implementation**: `TransactionHash.java`

### Step 2: Domain - TransactionHashCalculator
**Test**: `TransactionHashCalculatorTest.java`
- Same input → same hash (deterministic)
- Different description case → same hash (normalization)
- Different amount → different hash
- Different date → different hash
- Different account → different hash

**Implementation**: `TransactionHashCalculator.java`
- SHA-256 based hash calculation
- Input normalization (trim, lowercase description)

### Step 3: Domain - Transaction Record Modification
**Test**: `TransactionTest.java` (update existing tests)
- Create transaction with hash field
- Verify hash is stored correctly

**Implementation**: Update `Transaction.java` record

### Step 4: Persistence - Database Migration
**Test**: Verify migration can be applied and rolled back
**Implementation**:
- Create migration file `V6__add_transaction_hash.sql`
- Run migration locally with Flyway
- Verify column and index created in PostgreSQL

### Step 5: Persistence - TransactionEntity Update
**Test**: JPA integration test
- Save TransactionEntity with hash
- Query by accountId and hash

**Implementation**:
- Update `TransactionEntity.java`
- Update `TransactionMapper.java`

### Step 6: Repository - findByAccountIdAndHash
**Test**: `JpaTransactionRepositoryTest.java`
- Save transaction with hash
- Find by accountId + hash → found
- Find with non-existing hash → empty
- Find respects group isolation

**Implementation**:
- Add method to `TransactionRepository` interface
- Implement in `JpaTransactionRepository`
- Add query method to `TransactionJpaRepository`

### Step 7: Service - BulkTransactionImportService
**Test**: `BulkTransactionImportServiceTest.java`
- Import unique transactions → all saved
- Import duplicates → skipped with info
- Import mix (unique + duplicates) → correct counts
- User doesn't belong to account → error
- Invalid category → error

**Implementation**: `BulkTransactionImportService.java`
- Validation logic
- Hash calculation per proposal
- Duplicate detection
- Atomic save

### Step 8: API - BulkTransactionImportController
**Test**: Integration test (MockMvc)
- POST /api/transactions/bulk/import → 200 OK
- Response contains saved/skipped counts
- Unauthorized user → 401

**Implementation**:
- `BulkTransactionImportController.java`
- `BulkImportRequest.java` (DTO)
- `BulkImportResultView.java` (DTO)

### Step 9: Frontend - TransactionService.bulkImport
**Test**: `transaction_service_test.dart` (unit test with mock HTTP)
- Successful bulk import → result parsed
- HTTP error → exception thrown

**Implementation**: Update `transaction_service.dart`

### Step 10: Frontend - TransactionStagingService.saveAll
**Test**: `transaction_staging_service_test.dart`
- saveAll calls bulkImport
- Staging cleared after save

**Implementation**: Update `transaction_staging_service.dart`

### Step 11: Frontend - UI Notification
**Test**: Golden test update
- Save with duplicates → snackbar visible

**Implementation**: Update `transaction_staging_screen.dart`

### Step 12: End-to-End System Test
**Test**: `TransactionDeduplicationSystemTest.java`
- Import CSV → proposals with hashes
- Save all → duplicates detected
- Re-import same CSV → all skipped as duplicates

**Implementation**: System test covering full flow

## Potential Problems and Mitigation

### Problem 1: Hash Collision
**Mitigation**: SHA-256 provides virtually zero probability of collision for transaction space

### Problem 2: Description Normalization
**Risk**: Different whitespace or case might be intentional
**Mitigation**: Only `trim()` and `toLowerCase()` - minimal normalization

### Problem 3: Performance for Large Imports
**Mitigation**:
- Index on (account_id, transaction_hash)
- Bulk query optimization
- Limit 1000 transactions per request

### Problem 4: Race Condition during Concurrent Import
**Mitigation**: `@Transactional` ensures atomic operations. Optional: add unique constraint on (account_id, transaction_hash) in migration if strict enforcement needed at DB level

### Problem 5: Category Change Should Not Create Duplicate
**Solution**: Hash does not contain categoryId - aligned with requirements

## Acceptance Criteria

1. ✅ Transactions with identical: date, amount, description, account → treated as duplicates
2. ✅ Duplicates detected during staging (before showing to user)
3. ✅ Duplicates detected before database save (safety)
4. ✅ Hash stored in `transaction_hash` field in database
5. ✅ Bulk import endpoint `/api/transactions/bulk/import` works atomically
6. ✅ Frontend receives info on how many transactions were saved and skipped
7. ✅ User sees notification about skipped duplicates
8. ✅ Category change does not create duplicate (category not in hash)
9. ✅ Deduplication respects group isolation (duplicate only within group)
10. ✅ Test coverage ≥ 90%

## Estimation of Related Tasks

After completing TASK-0062:
- **TASK-0063**: Staging list UI - requires bulk endpoint (from TASK-0062)
- **TASK-0064**: Transaction edit in staging - UI enhancement
- **TASK-0065**: Bulk transaction save - COMPLETED by TASK-0062 (bulk endpoint)
- **TASK-0070**: Duplicate detection notification - COMPLETED by TASK-0062 (UI feedback)

## Files to Create/Modify

### Backend - New Files
1. `backend/src/main/java/pl/btsoftware/backend/transaction/domain/TransactionHash.java`
2. `backend/src/main/java/pl/btsoftware/backend/transaction/domain/TransactionHashCalculator.java`
3. `backend/src/main/java/pl/btsoftware/backend/transaction/application/BulkTransactionImportService.java`
4. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/api/BulkTransactionImportController.java`
5. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/api/dto/BulkImportRequest.java`
6. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/api/dto/BulkImportResultView.java`
7. `backend/src/main/resources/db/migration/V6__add_transaction_hash.sql`
8. `backend/src/test/java/pl/btsoftware/backend/transaction/domain/TransactionHashTest.java`
9. `backend/src/test/java/pl/btsoftware/backend/transaction/domain/TransactionHashCalculatorTest.java`
10. `backend/src/test/java/pl/btsoftware/backend/transaction/application/BulkTransactionImportServiceTest.java`
11. `backend/src/systemTest/java/pl/btsoftware/backend/transaction/TransactionDeduplicationSystemTest.java`

### Backend - Modified Files
1. `backend/src/main/java/pl/btsoftware/backend/transaction/domain/Transaction.java`
2. `backend/src/main/java/pl/btsoftware/backend/transaction/domain/TransactionRepository.java`
3. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/persistance/TransactionEntity.java`
4. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/persistance/TransactionJpaRepository.java`
5. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/persistance/JpaTransactionRepository.java`
6. `backend/src/main/java/pl/btsoftware/backend/transaction/infrastructure/persistance/TransactionMapper.java`
7. `backend/src/test/java/pl/btsoftware/backend/transaction/infrastructure/persistance/JpaTransactionRepositoryTest.java`

### Frontend - New Files
1. `frontend/lib/models/bulk_save_result.dart`
2. `frontend/test/models/bulk_save_result_test.dart`

### Frontend - Modified Files
1. `frontend/lib/services/transaction_service.dart`
2. `frontend/lib/services/transaction_staging_service.dart`
3. `frontend/lib/screens/transaction_staging_screen.dart`
4. `frontend/test/services/transaction_service_test.dart`
5. `frontend/test/services/transaction_staging_service_test.dart`
6. `frontend/test/screens/goldens/*/transaction_staging_screen_with_transactions.png` (golden update)

## Definition of Done
- [ ] All unit tests written (test-first) and passing
- [ ] All integration tests passing
- [ ] System test covering full deduplication flow passing
- [ ] Database migration executed and tested
- [ ] Bulk import endpoint works according to spec
- [ ] Frontend displays duplicate notifications
- [ ] Coverage ≥ 90%
- [ ] No regression in existing tests
- [ ] Golden tests updated
- [ ] Code review passed
- [ ] API documentation updated (Swagger/OpenAPI)
