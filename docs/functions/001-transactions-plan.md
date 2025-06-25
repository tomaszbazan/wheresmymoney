# Transaction Module Implementation Plan

## Overview

This plan outlines the backend implementation of the Transaction module for the WheresMyMoney personal finance system.
The module will be implemented following DDD principles, maintaining consistency with the existing Account module
structure.

## Project Structure Analysis

Based on the existing codebase:

- Current structure follows DDD with `domain/`, `application/`, and `infrastructure/` layers
- Existing `Money` value object can be reused for transaction amounts
- Account module serves as template for transaction module structure
- Testing infrastructure with `@IntegrationTest` and Testcontainers is already set up

## Implementation Plan

### Phase 1: Domain Layer Foundation

1. **Create Transaction domain package structure**
    - `backend/src/main/java/pl/btsoftware/backend/transaction/domain/`
    - `backend/src/main/java/pl/btsoftware/backend/transaction/domain/error/`

2. **Implement core domain objects**
    - `TransactionId` - UUID wrapper for type safety
    - `TransactionType` - Enum (INCOME, EXPENSE)
    - `Transaction` - Aggregate root with business logic
    - `TransactionRepository` - Domain repository interface

3. **Create domain exceptions**
    - `TransactionAmountZeroException`
    - `TransactionDescriptionEmptyException`
    - `TransactionDescriptionTooLongException`
    - `TransactionDateFutureException`
    - `TransactionNotFoundException`

4. **Write domain unit tests (TDD approach)**
    - Transaction creation validation
    - Amount sign validation (positive/negative)
    - Description length and content validation
    - Date validation (no future dates)
    - TransactionType derivation from amount

### Phase 2: Repository Layer

1. **Create repository interfaces and implementations**
    - `TransactionRepository` - Domain interface
    - `InMemoryTransactionRepository` - For testing
    - `JpaTransactionRepository` - PostgreSQL implementation

2. **Create persistence entities**
    - `TransactionEntity` - JPA entity
    - `TransactionJpaRepository` - Spring Data repository

3. **Database migration**
    - Create Flyway migration for transactions table
    - Add foreign key constraint to accounts table
    - Add indexes for performance

4. **Write repository tests**
    - CRUD operations testing
    - Query methods testing
    - Account relationship testing

### Phase 3: Application Layer

1. **Create application service**
    - `TransactionService` - Business orchestration
    - Transaction creation with account balance update
    - Transaction modification with balance recalculation
    - Transaction deletion with balance adjustment

2. **Implement balance management**
    - Coordinate with existing `AccountService`
    - Ensure atomic operations for consistency
    - Handle currency validation

3. **Write application service tests**
    - Transaction lifecycle operations
    - Balance update scenarios
    - Error handling and rollback scenarios

### Phase 4: Infrastructure Layer

1. **Create REST API controllers**
    - `TransactionController` - HTTP endpoints
    - Request/Response DTOs:
        - `CreateTransactionRequest`
        - `UpdateTransactionRequest`
        - `TransactionView`
        - `TransactionsView`

2. **Implement API endpoints**
    - `POST /accounts/{accountId}/transactions` - Create transaction
    - `GET /accounts/{accountId}/transactions` - List account transactions
    - `GET /transactions` - List all transactions
    - `GET /transactions/{id}` - Get specific transaction
    - `PUT /transactions/{id}` - Update transaction
    - `DELETE /transactions/{id}` - Remove transaction

3. **Create module configuration**
    - `TransactionModuleConfiguration` - Bean configuration
    - Integration with existing `AccountModuleConfiguration`

4. **Write API integration tests**
    - Full HTTP request/response testing
    - Error handling and status codes
    - Account integration scenarios

### Phase 5: Module Integration

1. **Update Account module**
    - Modify `Account` domain to use `Transaction` instead of `Expense`
    - Update `AccountService` to work with transactions
    - Migrate existing expense data structure

2. **Create module facade**
    - `TransactionModuleFacade` - Public API
    - Integration points with Account module

3. **Write integration tests**
    - Cross-module interaction testing
    - End-to-end transaction workflows
    - Data consistency validation

## Technical Specifications

### Transaction Entity Model

```java
Transaction:
- id: TransactionId (UUID)
- accountId: AccountId (FK to accounts)
- amount: Money (existing value object)
- type: TransactionType (derived from amount sign)
- description: String (optional, 1-200 chars)
- when: LocalDate (defaults to current date)
- category: String (optional)
- createdAt: OffsetDateTime
- updatedAt: OffsetDateTime
- isDeleted: Boolean (soft delete)
```

### Database Schema

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    description VARCHAR(200),
    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
    category VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

### Business Rules Implementation

1. **Amount Validation**: Enforce non-zero amounts
2. **Currency Consistency**: Transaction currency must match account currency
3. **Balance Updates**: Automatic account balance recalculation
4. **Type Derivation**: TransactionType derived from amount sign (positive = INCOME, negative = EXPENSE)
5. **Soft Delete**: Maintain audit trail while hiding from UI

### Testing Strategy

- **Unit Tests (70%)**: Domain logic, validation rules, value objects
- **Integration Tests (20%)**: Repository operations, service interactions, database constraints
- **Acceptance Tests (10%)**: Full workflows, API endpoints, cross-module interactions
- **Coverage Target**: 95%+ for transaction module

## Migration Strategy

1. **Phase 1-4**: Build transaction module alongside existing expense functionality
2. **Phase 5**: Migrate expense data to transaction structure
3. **Cleanup**: Remove deprecated expense-related code after successful migration

## Dependencies

- Existing `Money` value object (reuse)
- Existing `Account` and `AccountId` domain objects
- Existing testing infrastructure
- Spring Data JPA and Hibernate
- PostgreSQL with Flyway migrations

## Success Criteria

- All tests passing (95%+ coverage)
- RESTful API fully functional
- Account balance consistency maintained
- Soft delete functionality working
- Performance acceptable for expected load
- Code follows existing patterns and conventions