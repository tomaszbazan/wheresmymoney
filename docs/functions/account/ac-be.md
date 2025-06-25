# Account Management - Acceptance Criteria

## Feature: Account Creation

### Scenario: Create account with valid data
**Given** I want to create a new account  
**When** I provide a valid account name, currency, and optional initial balance
**Then** the account should be created with a generated UUID  
**And** the account should be stored with the provided details  
**And** I should receive a 201 Created response

**Test Coverage:** `AccountServiceTest.CreateAccount.shouldCreateAccountWithDifferentSupportedCurrencies()`

### Scenario: Create account with minimal data
**Given** I want to create a new account  
**When** I provide only account name and currency  
**Then** the account should be created with zero initial balance  
**And** I should receive a 201 Created response  

**Test Coverage:** `AccountServiceTest.CreateAccount.shouldCreateAccountWithMinimalData()`

### Scenario: Reject account creation with empty name
**Given** I want to create a new account  
**When** I provide an empty or null account name  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Account name is required"  

**Test Coverage:**

- `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithEmptyName()`
- `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithNullName()`
- `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithBlankName()`

### Scenario: Reject account creation with invalid name length
**Given** I want to create a new account  
**When** I provide an account name longer than 100 characters  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Account name too long"  

**Test Coverage:** `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithTooLongName()`

### Scenario: Reject account creation with invalid characters
**Given** I want to create a new account  
**When** I provide an account name with special characters other than spaces, hyphens, or apostrophes  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Invalid characters in account name"  

**Test Coverage:**

- `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithInvalidCharacters()` (newline, tab)
- `AccountServiceTest.CreateAccount.shouldCreateAccountWithValidSpecialCharacters()` (positive test)

### Scenario: Reject account creation with invalid currency
**Given** I want to create a new account  
**When** I provide an invalid currency code  
**Then** the account should not be created  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate "Invalid currency code"  

**Test Coverage:** `AccountServiceTest.CreateAccount.shouldRejectAccountCreationWithUnsupportedCurrency()`

### Scenario: Reject duplicate account names
**Given** an account with name "Checking Account" already exists  
**When** I try to create another account with the same name  
**Then** the account should not be created  
**And** I should receive a 409 Conflict response  
**And** the error message should indicate "Account name already exists"  

**Test Coverage:** `AccountServiceTest.CreateAccount.shouldRejectDuplicateAccountNames()`

## Feature: Account Retrieval

### Scenario: List all active accounts
**Given** I have multiple active accounts in the system  
**When** I request all accounts  
**Then** I should receive a list of all active accounts  
**And** each account should include id, name, currency, balance, createdAt, and updatedAt  
**And** inactive accounts should not be included  
**And** I should receive a 200 OK response  

**Test Coverage:**

- `AccountServiceTest.AccountRetrieval.shouldReturnAllActiveAccounts()`
- `AccountServiceTest.AccountRetrieval.shouldReturnEmptyListWhenNoAccountsExist()`
- `AccountServiceTest.AccountRetrieval.shouldReturnAccountsWithCompleteFieldStructure()`

### Scenario: Get specific account by ID
**Given** an account exists with a specific ID  
**When** I request the account by its ID  
**Then** I should receive the account details  
**And** the response should include id, name, currency, balance, createdAt, and updatedAt  
**And** I should receive a 200 OK response  

**Test Coverage:** `AccountServiceTest.AccountRetrieval.shouldReturnSpecificAccountById()`

### Scenario: Get non-existent account
**Given** no account exists with a specific ID  
**When** I request the account by that ID  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"  

**Test Coverage:** `AccountServiceTest.AccountRetrieval.shouldThrowExceptionWhenAccountNotFound()`

## Feature: Account Modification

### Scenario: Update account name
**Given** an account exists  
**When** I update the account name with valid data  
**Then** the account name should be updated  
**And** the updatedAt timestamp should be refreshed  
**And** I should receive a 200 OK response  

**Test Coverage:**

- `AccountServiceTest.AccountUpdate.shouldUpdateAccountName()`
- `AccountControllerTest.shouldUpdateAccount()`

### Scenario: Reject update with invalid name
**Given** an account exists  
**When** I try to update with an invalid account name  
**Then** the account should not be updated  
**And** I should receive a 400 Bad Request response  
**And** the error message should indicate the validation issue  

**Test Coverage:** `AccountServiceTest.AccountUpdate.shouldThrowExceptionWhenUpdatingAccountWithInvalidName()`

### Scenario: Reject update with duplicate name
**Given** two accounts exist with different names  
**When** I try to update one account's name to match the other  
**Then** the account should not be updated  
**And** I should receive a 409 Conflict response  
**And** the error message should indicate "Account name already exists"  

**Test Coverage:** `AccountServiceTest.AccountUpdate.shouldThrowExceptionWhenUpdatingWithDuplicateName()`

### Scenario: Update non-existent account
**Given** no account exists with a specific ID  
**When** I try to update the account  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"  

**Test Coverage:**

- `AccountServiceTest.AccountUpdate.shouldThrowExceptionWhenUpdatingNonExistentAccount()`
- `AccountControllerTest.shouldReturnNotFoundWhenUpdatingNonExistentAccount()`

## Feature: Account Removal

### Scenario: Delete account with zero balance
**Given** an account exists with zero balance and no transaction history  
**When** I request to delete the account  
**Then** the account should be marked as inactive  
**And** the account should no longer appear in active account lists  
**And** I should receive a 200 OK response  

**Test Coverage:** `AccountServiceTest.AccountDeletion.shouldDeleteAccountWithZeroBalance()`

### Scenario: Reject deletion of account with transaction history
**Given** an account exists with transaction history  
**When** I request to delete the account  
**Then** the account should not be deleted  
**And** I should receive a 422 Unprocessable Entity response  
**And** the error message should indicate "Cannot delete account with transaction history"  

**Test Coverage:** `AccountServiceTest.AccountDeletion.shouldRejectDeletionOfAccountWithTransactionHistory()`

### Scenario: Delete non-existent account
**Given** no account exists with a specific ID  
**When** I try to delete the account  
**Then** I should receive a 404 Not Found response  
**And** the error message should indicate "Account not found"

**Test Coverage:** `AccountServiceTest.AccountDeletion.shouldThrowExceptionWhenDeletingNonExistentAccount()`