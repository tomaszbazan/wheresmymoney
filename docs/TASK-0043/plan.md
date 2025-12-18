# TASK-0043: In-memory transaction processing

## Overview
Process parsed transactions in browser RAM before saving them to the database. This creates a staging area where users can review, modify categories, and selectively remove transactions before committing them.

## Requirements Summary
Based on clarifications:
- Display parsed transactions from CSV in a staging list
- Allow category assignment/modification for each transaction
- Enable selective removal of unwanted transactions
- Support bulk save of approved transactions to backend
- Manual category assignment only (AI integration deferred)
- No duplicate detection in this phase

## Technical Approach

### 1. State Management (Frontend)
Create a staging state that holds parsed transactions in memory:
- Use `TransactionProposal` model (already exists from TASK-0042)
- Store list of proposals in component state or provider
- Track modifications separately from original CSV data
- Maintain selection/removal state

### 2. Staging List UI Component
New widget: `TransactionStagingList`
- Display all parsed transactions in a scrollable list/table
- Show transaction details: date, description, amount
- Provide category dropdown for each transaction (reuse `SearchableCategoryDropdown`)
- Include remove button for each transaction
- Show summary: total count, total amount, selected count

### 3. Bulk Actions
- "Save All" button - sends all remaining transactions to backend
- "Clear All" button - clears staging area
- Category bulk assignment (optional enhancement)

### 4. Backend Integration
- Use existing transaction creation endpoint (`POST /api/transactions`)
- Send transactions one-by-one or implement batch endpoint
- Handle partial failures gracefully

### 5. Navigation Flow
CSV Upload Dialog → Parse → Staging List → Save → Transaction List

## Implementation Steps

### Step 1: Create State Management for Staging
**File**: `frontend/lib/services/transaction_staging_service.dart`
- Create service to manage in-memory transaction proposals
- Methods: `loadFromCsv()`, `updateCategory()`, `removeTransaction()`, `saveAll()`, `clear()`
- Use ChangeNotifier or similar pattern for reactivity

**Tests**: `frontend/test/services/transaction_staging_service_test.dart`
- Test loading proposals from CSV result
- Test category updates persist in memory
- Test transaction removal
- Test clear operation
- Test state notifications

### Step 2: Create Staging List Widget
**File**: `frontend/lib/widgets/transaction_staging_list.dart`
- Display list of `TransactionProposal` objects
- Each row shows: date, description, amount, category dropdown, remove button
- Use `SearchableCategoryDropdown` for category selection
- Responsive layout (table on desktop, cards on mobile)

**Tests**: `frontend/test/widgets/transaction_staging_list_test.dart`
- Test rendering proposals
- Test category selection updates proposal
- Test remove button removes transaction
- Test empty state display

### Step 3: Implement Bulk Save Operation
**Update**: `frontend/lib/services/transaction_staging_service.dart`
- Method `saveAll()` calls transaction service for each proposal
- Convert `TransactionProposal` to transaction create DTO
- Handle errors and return results
- Clear staging on successful save

**Tests**: Update `frontend/test/services/transaction_staging_service_test.dart`
- Test successful bulk save
- Test partial failure handling
- Test staging cleared after save
- Test error propagation

### Step 4: Create Staging Screen/Dialog
**File**: `frontend/lib/screens/transaction_staging_screen.dart` OR
        `frontend/lib/widgets/transaction_staging_dialog.dart`
- Full screen view or modal dialog containing staging list
- Action buttons: Save All, Clear, Cancel
- Summary section showing totals
- Loading states during save operation

**Tests**: `frontend/test/screens/transaction_staging_screen_test.dart` (or dialog test)
- Test initial render with proposals
- Test Save All triggers save operation
- Test Clear confirms and clears staging
- Test Cancel navigation
- Test loading states

### Step 5: Integrate with CSV Upload Flow
**Update**: `frontend/lib/widgets/csv_upload_dialog.dart`
- After successful parse, navigate to staging screen/dialog
- Pass `CsvParseResult.proposals` to staging component
- Handle navigation back on cancel

**Tests**: Update `frontend/test/widgets/csv_upload_dialog_test.dart`
- Test navigation to staging after parse
- Test passing proposals to staging

### Step 6: Add Validation
**Update**: Staging service and UI
- Require category selection before save (or use default "To be clarified")
- Validate all required fields present
- Show validation errors in UI

**Tests**: Add validation tests
- Test save blocked when categories missing
- Test validation error display

## Data Flow
```
CSV File Upload
    ↓
CSV Parser (CsvImportService)
    ↓
CsvParseResult with TransactionProposal[]
    ↓
TransactionStagingService.loadFromCsv()
    ↓
TransactionStagingList Widget
    ↓ (user edits categories, removes unwanted)
TransactionStagingService.saveAll()
    ↓
Transaction Service (POST /api/transactions per proposal)
    ↓
Navigate to Transaction List
```

## Models Required
All already exist:
- `TransactionProposal` (frontend/lib/models/transaction_proposal.dart)
- `ParseError` (frontend/lib/models/parse_error.dart)
- `CsvParseResult` (frontend/lib/models/csv_parse_result.dart)

## Testing Strategy
Following project guidelines (70% unit, 20% integration, 10% acceptance):
- Unit tests for staging service logic
- Widget tests for UI components
- Integration tests for save flow
- Target: 95%+ coverage

## Dependencies
Existing:
- `SearchableCategoryDropdown` (TASK-0035)
- `CsvImportService` (TASK-0040, TASK-0041)
- Transaction service (TASK-0061)
- Category service

New:
- None (use existing Flutter packages)

## Future Enhancements (Not in this task)
- AI category suggestions (TASK-0055)
- Duplicate detection warnings (TASK-0062, TASK-0070)
- Bulk category assignment
- Transaction edit in staging (TASK-0064 - currently limited to category only)
- Data loss warning on navigation (TASK-0094)

## Acceptance Criteria
- [ ] User can upload CSV and see parsed transactions in staging area
- [ ] User can assign category to each transaction using searchable dropdown
- [ ] User can remove unwanted transactions from staging
- [ ] User can save all staged transactions to database with one action
- [ ] Staging area cleared after successful save
- [ ] User redirected to transaction list after save
- [ ] All tests pass with 95%+ coverage
- [ ] No data persists in memory after save or cancel
