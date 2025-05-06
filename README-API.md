# WheresMyMoney API Documentation

This document provides information on how to use the API requests file to query the WheresMyMoney application.

## Getting Started

1. Open the `api-requests.http` file in IntelliJ IDEA:
   - The file uses IntelliJ's HTTP Client format
   - Variables are defined at the top of the file (@host, @accountId, @expenseId)
   - You can modify these variables as needed

2. Start the WheresMyMoney application:
   ```
   ./gradlew bootRun
   ```

3. Execute requests directly from IntelliJ:
   - Click the green "Run" button next to each request
   - Or place your cursor on a request and press Alt+Enter, then select "Run"
   - Results will appear in the "Response" panel

4. Working with variables:
   - Update the @accountId and @expenseId variables at the top of the file
   - Or create an HTTP Client environment file for different environments

## Available Endpoints

### Accounts

#### Get All Accounts
- **Method**: GET
- **URL**: http://localhost:8080/api/accounts
- **Description**: Returns a list of all accounts

#### Create Account
- **Method**: POST
- **URL**: http://localhost:8080/api/accounts
- **Headers**: Content-Type: application/json
- **Body**:
  ```json
  {
    "name": "My Checking Account"
  }
  ```
- **Description**: Creates a new account with the specified name

### Expenses

#### Get All Expenses
- **Method**: GET
- **URL**: http://localhost:8080/api/expenses
- **Description**: Returns a list of all expenses

#### Get Expense by ID
- **Method**: GET
- **URL**: http://localhost:8080/api/expenses/{expenseId}
- **Description**: Returns a specific expense by ID
- **Note**: Replace {expenseId} with the actual UUID of the expense

#### Get Expenses by Account ID
- **Method**: GET
- **URL**: http://localhost:8080/api/expenses/account/{accountId}
- **Description**: Returns all expenses for a specific account
- **Note**: Replace {accountId} with the actual UUID of the account

#### Create Expense
- **Method**: POST
- **URL**: http://localhost:8080/api/expenses
- **Headers**: Content-Type: application/json
- **Body**:
  ```json
  {
    "accountId": "{accountId}",
    "amount": 100.50,
    "description": "Grocery shopping",
    "date": "2023-05-15T14:30:00"
  }
  ```
- **Description**: Creates a new expense
- **Note**: Replace {accountId} with the actual UUID of the account

#### Update Expense
- **Method**: PUT
- **URL**: http://localhost:8080/api/expenses/{expenseId}
- **Headers**: Content-Type: application/json
- **Body**:
  ```json
  {
    "amount": 120.75,
    "description": "Updated grocery shopping",
    "date": "2023-05-15T15:45:00"
  }
  ```
- **Description**: Updates an existing expense
- **Note**: Replace {expenseId} with the actual UUID of the expense

#### Delete Expense
- **Method**: DELETE
- **URL**: http://localhost:8080/api/expenses/{expenseId}
- **Description**: Deletes an expense
- **Note**: Replace {expenseId} with the actual UUID of the expense

## Example Workflow

1. Create an account:
   - Execute the "Create Account" request
   - Note the returned account ID

2. Update the @accountId variable:
   - At the top of the file, replace the value of @accountId with the ID from step 1
   - For example: `@accountId = 123e4567-e89b-12d3-a456-426614174000`

3. Create an expense for that account:
   - Execute the "Create Expense" request
   - Note the returned expense ID

4. Update the @expenseId variable:
   - At the top of the file, replace the value of @expenseId with the ID from step 3
   - For example: `@expenseId = 123e4567-e89b-12d3-a456-426614174001`

5. Get all expenses for the account:
   - Execute the "Get Expenses by Account ID" request

6. Update the expense:
   - Execute the "Update Expense" request

7. Delete the expense:
   - Execute the "Delete Expense" request
