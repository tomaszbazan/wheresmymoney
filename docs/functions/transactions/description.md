# Transaction Management Function

## Overview

The Transaction module manages financial movements within accounts in the personal finance system. It provides
functionality to record, track, and categorize money flowing in and out of accounts.

## Core Functionality

### Transaction Types

- **Income Transactions (Positive)**: Money flowing into accounts
    - Salary, bonuses, investment returns, gifts received
    - Represented as positive amounts
- **Expense Transactions (Negative)**: Money flowing out of accounts
    - Purchases, bills, fees, transfers out
    - Represented as negative amounts

### Transaction Recording

- **Purpose**: Record financial movements to maintain accurate account balances
- **Input**: Account ID, amount, description, date, category (optional)
- **Validation**:
    - Account must exist and be active
    - Description is optional and must be 1-200 characters
    - Category must exist if provided
- **Output**: Created transaction with generated UUID
- **Business Rules**:
    - Transaction amounts follow currency convention: positive for income, negative for expenses
    - Account balance is automatically updated upon transaction creation
    - UUIDs are generated on backend

### Transaction Modification

- **Purpose**: Correct errors in recorded transactions
- **Modifiable Fields**:
    - Description (subject to same validation as creation)
    - Amount (with balance recalculation)
    - Date (with validation rules)
    - Category (optional)
- **Validation**:
    - Same validation rules as creation
    - Transaction must exist
    - Amount change requires balance recalculation
- **Business Rules**:
    - Modifying amount triggers automatic account balance adjustment
    - Date modifications cannot result in future dates
    - Currency must match account currency

### Transaction Removal

- **Purpose**: Remove incorrectly recorded transactions
- **Prerequisites**:
    - Transaction must exist
    - User must have permission to delete transactions
- **Process**:
    - Validate transaction exists
    - Reverse balance impact on associated account
    - Soft delete transaction (mark as inactive)
    - Maintain audit trail for historical data
- **Business Rules**:
    - Deleting transaction automatically adjusts account balance
    - Deleted transactions remain in system for reporting but are not visible in UI
    - Balance adjustment is reverse of original transaction amount

### Transaction Querying

- **List Account Transactions**: Return all transactions for specific account
- **List All Transactions**: Return all transactions across all accounts with pagination and filtering
- **Get Transaction by ID**: Return specific transaction details
- **Search Transactions**: Find transactions by description, amount range, or date range
- **Transaction Summary**: Return aggregated transaction information by period

## Domain Model

### Transaction Entity

```
Transaction:
- id: TransactionId (UUID)
- accountId: AccountId (UUID reference)
- amount: Money (positive for income, negative for expenses)
- type: TransactionType (INCOME or EXPENSE)
- description: String (1-200 chars)
- when: LocalDate
- category: String (optional)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- isDeleted: Boolean
```

### Value Objects

- **TransactionId**: UUID wrapper for type safety
- **Money**: Amount + Currency combination
- **TransactionType**: Enum (INCOME, EXPENSE) derived from amount sign

## API Endpoints

### REST API Design

- `POST /accounts/{accountId}/transactions` - Create new transaction
- `GET /accounts/{accountId}/transactions` - List account transactions
- `GET /transactions` - List all transactions across all accounts
- `GET /transactions/{id}` - Get specific transaction
- `PUT /transactions/{id}` - Update transaction
- `DELETE /transactions/{id}` - Remove transaction

### Request/Response Models

- **CreateTransactionRequest**: amount, description, date (optional), category (optional)
- **UpdateTransactionRequest**: amount, description, date, category (optional)
- **TransactionView**: id, accountId, amount, description, date, category, createdAt, updatedAt
- **TransactionsView**: List of TransactionView objects with pagination

## Error Handling

### Business Exceptions

- **TransactionAmountZeroException**: When amount is zero
- **TransactionDescriptionEmptyException**: When description is null or empty
- **TransactionDescriptionTooLongException**: When description exceeds 200 characters
- **TransactionDateFutureException**: When date is in the future
- **TransactionNotFoundException**: When transaction ID doesn't exist
- **AccountNotFoundException**: When associated account doesn't exist
- **CurrencyMismatchException**: When transaction currency doesn't match account currency

### HTTP Status Codes

- 201: Transaction created successfully
- 200: Transaction retrieved/updated successfully
- 400: Invalid request data
- 404: Transaction or account not found
- 422: Business rule violation (e.g., zero amount, future date)

## Testing Strategy

### Unit Tests (70%)

- Transaction domain object validation
- Amount sign validation (positive/negative)
- Business rule enforcement
- Value object behavior
- Exception scenarios

### Integration Tests (20%)

- Repository operations
- Service layer interactions
- Account balance updates
- Database constraints
- API endpoint behavior

### Acceptance Tests (10%)

- Complete transaction workflows
- Account balance consistency
- Cross-module interactions
- End-to-end transaction scenarios

## Implementation Notes

### Repository Pattern

- TransactionRepository interface with domain methods
- JpaTransactionRepository for PostgreSQL persistence
- InMemoryTransactionRepository for testing

### Service Layer

- TransactionService handles business logic
- Validates business rules before persistence
- Coordinates balance updates with AccountService
- Ensures transaction-account consistency

### Infrastructure

- TransactionController handles HTTP requests/responses
- TransactionEntity for JPA persistence
- View models for API responses

### Balance Management

- Account balance automatically updated on transaction create/update/delete
- Atomic operations ensure data consistency
- Balance calculation includes all active transactions

### Test-First Development

1. Write failing test for new functionality
2. Implement minimal code to make test pass
3. Refactor while keeping tests green
4. Ensure 95%+ test coverage for critical paths