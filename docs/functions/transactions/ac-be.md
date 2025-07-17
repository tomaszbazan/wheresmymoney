# Acceptance Criteria - Transaction Management Backend Module

## 1. Transaction Creation

### AC-1.1: Create Income Transaction

**Given** an account with currency "PLN"
**When** I create a transaction with:

- amount: 1000.12
- description: "Salary payment"
- date: 2024-01-15
- type: "Income"
- category: "Salary"
  **Then** the system should:
- Create transaction with generated UUID
- Set amount 1000.12
- Set type as INCOME
- Update account balance by +1000.12

**Test Coverage:** `TransactionServiceTest.shouldCreateIncomeTransaction()`

### AC-1.2: Create Expense Transaction

**Given** an account with currency "PLN"
**When** I create a transaction with:

- amount: 250.50
- description: "Grocery shopping"
- date: 2024-01-16
- type: "Expense"
- category: "Food"
  **Then** the system should:
- Create transaction with generated UUID
- Set amount 250.50
- Set type as EXPENSE
- Update account balance by -250.50

**Test Coverage:** `TransactionServiceTest.shouldCreateExpenseTransaction()`

## 2. Transaction Creation Validation

### AC-2.1: Reject Transaction for Non-existent Account

**Given** a non-existent account
**When** I create a transaction for this account
**Then** the system should:

- Return 404 Not Found
- Return error message: "Account not found"
- Not create any transaction

**Test Coverage:** `TransactionServiceTest.shouldRejectTransactionForNonexistentAccount()`

### AC-2.2: Reject Transaction with Invalid Description Length

**Given** an existing active account
**When** I create a transaction with description longer than 200 characters
**Then** the system should:

- Return 400 Bad Request
- Return error message: "Description must be between 1 and 200 characters"
- Not create any transaction

### AC-2.3: Reject Transaction with Currency Mismatch

**Given** an existing account with currency "PLN"
**When** I create a transaction with currency "USD"
**Then** the system should:

- Return 422 Unprocessable Entity
- Return error message: "Transaction currency must match account currency"
- Not create any transaction

## 3. Transaction Retrieval

### AC-3.1: Get Transaction by ID

**Given** an existing transaction with ID "660e8400-e29b-41d4-a716-446655440000"
**When** I request GET /transactions/{id}
**Then** the system should:

- Return 200 OK
- Return transaction details including: id, accountId, amount, description, date, category, createdAt, updatedAt

### AC-3.2: Get Non-existent Transaction

**Given** a non-existent transaction ID
**When** I request GET /transactions/{id}
**Then** the system should:

- Return 404 Not Found
- Return error message: "Transaction not found"

### AC-3.3: List Account Transactions

**Given** an account with multiple transactions
**When** I request GET /accounts/{accountId}/transactions
**Then** the system should:

- Return 200 OK
- Return list of all transactions for the account
- Include pagination metadata
- Order by date descending

### AC-3.4: List All Transactions

**Given** multiple accounts with transactions
**When** I request GET /transactions
**Then** the system should:

- Return 200 OK
- Return list of all transactions across all accounts
- Include pagination metadata
- Order by date descending

## 4. Transaction Update

### AC-4.1: Update Transaction Amount

**Given** an existing transaction with amount 500.00
**When** I update the transaction amount to 750.00
**Then** the system should:

- Update transaction amount to 750.00
- Recalculate account balance (adjust by +250.00)
- Update updatedAt timestamp
- Return 200 OK with updated transaction

### AC-4.2: Update Transaction Description

**Given** an existing transaction
**When** I update the transaction description to "Updated description"
**Then** the system should:

- Update transaction description
- Keep account balance unchanged
- Update updatedAt timestamp
- Return 200 OK with updated transaction

### AC-4.3: Update Transaction Date

**Given** an existing transaction with date 2024-01-15
**When** I update the transaction date to 2024-01-10
**Then** the system should:

- Update transaction date to 2024-01-10
- Keep account balance unchanged
- Update updatedAt timestamp
- Return 200 OK with updated transaction

### AC-4.4: Update Transaction Category

**Given** an existing transaction with category "Food"
**When** I update the transaction category to "Entertainment"
**Then** the system should:

- Update transaction category
- Keep account balance unchanged
- Update updatedAt timestamp
- Return 200 OK with updated transaction

## 5. Transaction Update Validation

### AC-5.1: Reject Update with Zero Amount

**Given** an existing transaction
**When** I update the transaction amount to 0.00
**Then** the system should:

- Return 422 Unprocessable Entity
- Return error message: "Transaction amount cannot be zero"
- Not update the transaction

### AC-5.2: Reject Update with Future Date

**Given** an existing transaction
**When** I update the transaction date to future date
**Then** the system should:

- Return 422 Unprocessable Entity
- Return error message: "Transaction date cannot be in the future"
- Not update the transaction

### AC-5.3: Reject Update for Non-existent Transaction

**Given** a non-existent transaction ID
**When** I attempt to update the transaction
**Then** the system should:

- Return 404 Not Found
- Return error message: "Transaction not found"

## 6. Transaction Deletion

### AC-6.1: Delete Transaction

**Given** an existing transaction with amount -100.00
**When** I delete the transaction
**Then** the system should:

- Mark transaction as deleted (soft delete)
- Reverse balance impact on account (+100.00)
- Return 200 OK
- Transaction should not appear in normal queries

### AC-6.2: Delete Non-existent Transaction

**Given** a non-existent transaction ID
**When** I attempt to delete the transaction
**Then** the system should:

- Return 404 Not Found
- Return error message: "Transaction not found"

### AC-6.3: Verify Soft Delete Behavior

**Given** a deleted transaction
**When** I query for the transaction by ID
**Then** the system should:

- Return 404 Not Found
- Transaction should remain in database with isDeleted=true
- Transaction should not appear in transaction lists

## 7. Balance Management

### AC-7.1: Account Balance Update on Creation

**Given** an account with balance 1000.00
**When** I create a transaction with amount -200.00
**Then** the account balance should be updated to 800.00

### AC-7.2: Account Balance Update on Modification

**Given** an account with balance 1000.00 and existing transaction of -200.00
**When** I update the transaction amount to -300.00
**Then** the account balance should be updated to 900.00

### AC-7.3: Account Balance Update on Deletion

**Given** an account with balance 800.00 and existing transaction of -200.00
**When** I delete the transaction
**Then** the account balance should be updated to 1000.00

## 8. Error Handling

### AC-8.1: Database Connection Error

**Given** database connection is unavailable
**When** I perform any transaction operation
**Then** the system should:

- Return 500 Internal Server Error
- Return error message: "Service temporarily unavailable"
- Not modify any data

### AC-8.2: Concurrent Transaction Modification

**Given** a transaction being modified by another user
**When** I attempt to modify the same transaction
**Then** the system should:

- Handle concurrent modification gracefully
- Return appropriate error response
- Maintain data consistency

## 9. API Response Format

### AC-9.1: Transaction Response Structure

**Given** any successful transaction operation
**When** the system returns transaction data
**Then** the response should include:

- id (UUID)
- accountId (UUID)
- amount (decimal with currency)
- type (INCOME or EXPENSE)
- description (string)
- date (ISO date format)
- category (string or null)
- createdAt (ISO datetime format)
- updatedAt (ISO datetime format)

### AC-9.2: Error Response Structure

**Given** any failed transaction operation
**When** the system returns an error
**Then** the response should include:

- error code
- error message
- timestamp
- request path