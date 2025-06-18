# Account Management - Frontend Acceptance Criteria

## AC1: Account Creation Form Structure
**Given** I am on the accounts management page
**When** I click "Add Account" button
**Then** I should see a form with the following fields:
- Account name (text input, required)
- Currency (dropdown with ISO currency codes: USD, EUR, PLN, etc.)
- Initial balance (number input, optional, defaults to 0)
- Create button (disabled until name is provided)
- Cancel button

## AC2: Account Creation Validation
**When** I enter invalid account name
**Then** I should see validation errors:
- Empty name: "Account name is required"
- Name > 100 characters: "Account name must be 100 characters or less"
- Invalid characters: "Account name can only contain letters, numbers, spaces, hyphens, and apostrophes"

**When** I try to create account with duplicate name
**Then** I should see error: "Account with this name already exists"

## AC3: Account Creation Success Flow
**When** I submit valid account data
**Then** the account should be created and I should be redirected to accounts list

## AC4: Accounts List View
**Given** I am on the accounts page
**When** the page loads
**Then** I should see:
- List of all active accounts
- Each account showing: name, currency, current balance
- "Add Account" button
- Edit and Delete buttons for each account (if applicable)

**When** there are no accounts
**Then** I should see empty state message: "No accounts found. Create your first account to get started."

## AC5: Account Details View
**Given** I am viewing the accounts list
**When** I click on an account
**Then** I should see account details:
- Account name
- Currency
- Current balance
- Created date
- Last updated date
- Edit button
- Delete button (if account can be deleted)

## AC6: Account Editing
**Given** I am viewing account details
**When** I click "Edit" button
**Then** I should see edit form with:
- Account name (pre-filled, editable)
- Currency (pre-filled, editable with warning about balance reset)
- Update button
- Cancel button

**When** I change the currency
**Then** I should see confirmation dialog: "Changing currency will reset balance to zero. Do you want to continue?"

**When** I confirm currency change
**Then** the account currency should be updated and balance reset to 0

**When** I update account name with valid data
**Then** the account should be updated and I should see success message

**When** I try to update with invalid name
**Then** I should see same validation errors as in creation

## AC7: Account Deletion
**Given** I am viewing account details
**When** the account has zero balance and no transactions
**Then** Delete button should be enabled

**When** the account has non-zero balance or transactions
**Then** Delete button should be disabled with tooltip: "Cannot delete account with balance or transaction history"

**When** I click enabled Delete button
**Then** I should see confirmation dialog: "Are you sure you want to delete this account? This action cannot be undone."

**When** I confirm deletion
**Then** the account should be deleted and I should be redirected to accounts list with success message

## AC8: Error Handling
**When** any API call fails
**Then** I should see appropriate error message:
- Network error: "Unable to connect to server. Please check your connection."
- Server error: "Something went wrong. Please try again later."
- Validation error: Display specific validation message from server

**When** an error occurs during form submission
**Then** the form should remain filled with user data and show error message

## AC9: Loading States
**When** any data is being loaded
**Then** I should see loading indicator

**When** form is being submitted
**Then** submit button should show loading state and be disabled

## AC10: Responsive Design
**Given** I am using the app on mobile device
**When** I view any account screen
**Then** the interface should be fully responsive and usable on mobile

## AC11: Navigation
**Given** I am on any account screen
**When** I use browser back button or app navigation
**Then** I should navigate correctly without losing unsaved data (with confirmation if needed)

## AC12: Data Validation
**When** I enter data in any form field
**Then** validation should occur in real-time where appropriate

**When** I submit a form
**Then** all validation should be checked before API call

**When** server returns validation errors
**Then** errors should be displayed next to relevant form fields

## Technical Requirements
- All currency amounts should be displayed with appropriate formatting for the currency
- Account names should be trimmed of leading/trailing whitespace
- Form validation should prevent submission of invalid data
- All user actions should provide appropriate feedback
- Loading states should be implemented for all async operations
- Error states should be user-friendly and actionable