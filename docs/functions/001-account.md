# Account Management Function

## Overview
The Account module manages banking accounts within the personal finance system. It provides functionality to track different financial accounts (checking, savings, credit cards, etc.) and their balances.

## Core Functionality

### Account Creation
- **Purpose**: Create new financial accounts to track money across different institutions
- **Input**: Account name, currency, initial balance (optional)
- **Validation**:
  - Account name is required and must be 1-100 characters
  - Account name cannot contain special characters except spaces, hyphens, and apostrophes
  - Currency must be valid ISO currency code (USD, EUR, PLN, etc.)
  - Initial balance defaults to zero if not provided
- **Output**: Created account with generated UUID
- **Business Rules**:
  - Account names must be unique within the system
  - Balance is stored as Money domain object with currency
  - UUIDs are generated on backend

### Account Modification
- **Purpose**: Update account details to reflect changes
- **Modifiable Fields**:
  - Account name (subject to same validation as creation)
  - Currency (with balance conversion rules)
- **Validation**:
  - Same name validation as creation
  - Currency changes require explicit confirmation
  - Account must exist
- **Business Rules**:
  - Currency changes reset balance to zero (user must manually adjust)
  - Modified account name must remain unique

### Account Removal
- **Purpose**: Remove accounts no longer needed
- **Prerequisites**:
  - Account must exist
  - Account balance must be zero
  - No pending transactions associated with account
- **Process**:
  - Validate account exists and meets removal criteria
  - Soft delete account (mark as inactive rather than hard delete)
  - Maintain audit trail for historical data
- **Business Rules**:
  - Cannot delete accounts with non-zero balances
  - Cannot delete accounts with transaction history
  - Deleted accounts remain in system for reporting but are not visible in UI

### Account Querying
- **List All Accounts**: Return all active accounts with current balances
- **Get Account by ID**: Return specific account details
- **Search Accounts**: Find accounts by name pattern
- **Account Summary**: Return aggregated account information

## Domain Model

### Account Entity
```
Account:
- id: AccountId (UUID)
- name: String (1-100 chars, valid characters only)
- currency: Currency (ISO code)
- balance: Money (amount + currency)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- isActive: Boolean
```

### Value Objects
- **AccountId**: UUID wrapper for type safety
- **Money**: Amount + Currency combination
- **Currency**: ISO currency code validation

## API Endpoints

### REST API Design
- `POST /accounts` - Create new account
- `GET /accounts` - List all active accounts
- `GET /accounts/{id}` - Get specific account
- `PUT /accounts/{id}` - Update account
- `DELETE /accounts/{id}` - Remove account

### Request/Response Models
- **CreateAccountRequest**: name, currency, initialBalance (optional)
- **UpdateAccountRequest**: name, currency
- **AccountView**: id, name, currency, balance, createdAt, updatedAt
- **AccountsView**: List of AccountView objects

## Error Handling

### Business Exceptions
- **AccountNameEmptyException**: When name is null or empty
- **AccountNameTooLongException**: When name exceeds 100 characters
- **AccountNameInvalidCharactersException**: When name contains invalid characters
- **AccountNotFoundException**: When account ID doesn't exist
- **InvalidCurrencyException**: When currency code is invalid
- **AccountDeletionNotAllowedException**: When account cannot be deleted due to business rules

### HTTP Status Codes
- 201: Account created successfully
- 200: Account retrieved/updated successfully
- 400: Invalid request data
- 404: Account not found
- 409: Account name already exists
- 422: Business rule violation (e.g., cannot delete account with balance)

## Testing Strategy

### Unit Tests (70%)
- Account domain object validation
- Business rule enforcement
- Value object behavior
- Exception scenarios

### Integration Tests (20%)
- Repository operations
- Service layer interactions
- Database constraints
- API endpoint behavior

### Acceptance Tests (10%)
- Complete user workflows
- Cross-module interactions
- End-to-end account management scenarios

## Implementation Notes

### Repository Pattern
- AccountRepository interface with domain methods
- JpaAccountRepository for PostgreSQL persistence
- InMemoryAccountRepository for testing

### Service Layer
- AccountService handles business logic
- Validates business rules before persistence
- Coordinates with repository layer

### Infrastructure
- AccountController handles HTTP requests/responses
- AccountEntity for JPA persistence
- View models for API responses

### Test-First Development
1. Write failing test for new functionality
2. Implement minimal code to make test pass
3. Refactor while keeping tests green
4. Ensure 95%+ test coverage for critical paths