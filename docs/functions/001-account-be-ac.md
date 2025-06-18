# Account Management - Acceptance Criteria

## Feature: Account Creation

### Scenario: Create account with valid data
**Given** I want to create a new account  
**When** I provide a valid account name, currency, and optional initial balance  
**Then** the account should be created with a generated UUID  
**And** the account should be stored with the provided details  
**And** I should receive a 201 Created response  

### Scenario: Create account with minimal data
**Given** I want to create a new account  
**When** I provide only account name and currency  
**Then** the account should be created with zero initial balance  
**And** I should receive a 201 Created response  

### Scenario: Reject account creation with empty name
**Given** I want to create a new account  
**When** I provide an empty or null account name  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Account name is required"  

### Scenario: Reject account creation with invalid name length
**Given** I want to create a new account  
**When** I provide an account name longer than 100 characters  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Account name too long"  

### Scenario: Reject account creation with invalid characters
**Given** I want to create a new account  
**When** I provide an account name with special characters other than spaces, hyphens, or apostrophes  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Invalid characters in account name"  

### Scenario: Reject account creation with invalid currency
**Given** I want to create a new account  
**When** I provide an invalid currency code  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Invalid currency code"  

### Scenario: Reject duplicate account names
**Given** an account with name "Checking Account" already exists  
**When** I try to create another account with the same name  
**Then** the account should not be created  
**And** I should receive a 409 Conflict response  
**And** the error message should indicate "Account name already exists"  

## Feature: Account Retrieval

### Scenario: List all active accounts
**Given** I have multiple active accounts in the system  
**When** I request all accounts  
**Then** I should receive a list of all active accounts  
**And** each account should include id, name, currency, balance, createdAt, and updatedAt  
**And** inactive accounts should not be included  
**And** I should receive a 200 OK response  

### Scenario: Get specific account by ID
**Given** an account exists with a specific ID  
**When** I request the account by its ID  
**Then** I should receive the account details  
**And** the response should include id, name, currency, balance, createdAt, and updatedAt  
**And** I should receive a 200 OK response  

### Scenario: Get non-existent account
**Given** no account exists with a specific ID  
**When** I request the account by that ID  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"  

## Feature: Account Modification

### Scenario: Update account name
**Given** an account exists  
**When** I update the account name with valid data  
**Then** the account name should be updated  
**And** the updatedAt timestamp should be refreshed  
**And** I should receive a 200 OK response  

### Scenario: Update account currency
**Given** an account exists  
**When** I update the account currency  
**Then** the account currency should be updated  
**And** the account balance should be reset to zero  
**And** the updatedAt timestamp should be refreshed  
**And** I should receive a 200 OK response  

### Scenario: Reject update with invalid name
**Given** an account exists  
**When** I try to update with an invalid account name  
**Then** the account should not be updated  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate the validation issue  

### Scenario: Reject update with duplicate name
**Given** two accounts exist with different names  
**When** I try to update one account's name to match the other  
**Then** the account should not be updated  
**And** I should receive a 409 Conflict response  
**And** the error message should indicate "Account name already exists"  

### Scenario: Update non-existent account
**Given** no account exists with a specific ID  
**When** I try to update the account  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"  

## Feature: Account Removal

### Scenario: Delete account with zero balance
**Given** an account exists with zero balance and no transaction history  
**When** I request to delete the account  
**Then** the account should be marked as inactive  
**And** the account should no longer appear in active account lists  
**And** I should receive a 200 OK response  

### Scenario: Reject deletion of account with non-zero balance
**Given** an account exists with a non-zero balance  
**When** I request to delete the account  
**Then** the account should not be deleted  
**And** I should receive a 422 Unprocessable Entity response  
**And** the error message should indicate "Cannot delete account with non-zero balance"  

### Scenario: Reject deletion of account with transaction history
**Given** an account exists with transaction history  
**When** I request to delete the account  
**Then** the account should not be deleted  
**And** I should receive a 422 Unprocessable Entity response  
**And** the error message should indicate "Cannot delete account with transaction history"  

### Scenario: Delete non-existent account
**Given** no account exists with a specific ID  
**When** I try to delete the account  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"  

## Feature: Data Validation and Business Rules

### Scenario: Account names support valid characters
**Given** I want to create an account  
**When** I provide a name with letters, numbers, spaces, hyphens, and apostrophes  
**Then** the account should be created successfully  

### Scenario: UUID generation on backend
**Given** I create a new account  
**When** the account is saved  
**Then** a unique UUID should be generated on the backend  
**And** the UUID should be returned in the response  

### Scenario: Money object with currency
**Given** I create an account with initial balance  
**When** the account is created  
**Then** the balance should be stored as a Money object  
**And** the Money object should include both amount and currency  

### Scenario: Audit trail maintenance
**Given** I perform any account operation  
**When** the operation is completed  
**Then** appropriate timestamps should be recorded  
**And** the audit trail should be maintained for historical purposes  

## Feature: API Response Format

### Scenario: Successful account creation response
**Given** I create a valid account  
**When** the account is created successfully  
**Then** the response should include the complete account details  
**And** the response should have status 201 Created  

### Scenario: Account list response format
**Given** I request all accounts  
**When** accounts exist in the system  
**Then** the response should be an array of account objects  
**And** each account should have consistent field structure  
**And** the response should have status 200 OK  

### Scenario: Error response format
**Given** any operation fails due to validation or business rules  
**When** the error occurs  
**Then** the response should include a descriptive error message  
**And** the response should have the appropriate HTTP status code  
**And** the error format should be consistent across all endpoints  