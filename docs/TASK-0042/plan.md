# TASK-0042: CSV Upload UI - Implementation Plan

## Task Overview
Create Flutter file upload component with progress indicator for mBank CSV import functionality.

## Current State Analysis

### Completed Prerequisites
- ✅ TASK-0040: mBank CSV parser implemented in backend
- ✅ TASK-0041: CSV file validation (28-line header, column validation, size limits)
- ✅ Backend API endpoint: `POST /api/transactions/import`

### Backend API Details
**Endpoint:** `/api/transactions/import`
- **Method:** POST (multipart/form-data)
- **Parameters:**
  - `csvFile`: MultipartFile (max 10MB, must have mBank format)
  - `accountId`: UUID (required for transaction association)
- **Response:**
  ```json
  {
    "proposals": [...],
    "errors": [...],
    "totalRows": 10,
    "successCount": 8,
    "errorCount": 2
  }
  ```
- **Error Codes:** 400 (validation), 401 (auth), 500 (server error)

### Frontend Gaps Identified
1. ❌ No file picker dependency
2. ❌ No multipart upload support in `http_client.dart`
3. ❌ No CSV upload UI component
4. ❌ No staging/preview screen (future TASK-0063)

## Implementation Strategy

### Phase 1: Infrastructure Setup
1. Add `file_picker` dependency to `pubspec.yaml`
2. Extend `ApiClient` with multipart POST method
3. Create `CsvImportService` for handling CSV upload logic

### Phase 2: UI Component Development
4. Create `CsvUploadDialog` widget with:
   - File picker button
   - Account selector dropdown
   - Progress indicator
   - Error display
   - Upload button
5. Integrate dialog into `TransactionsPage`

### Phase 3: Integration & Testing
6. Add CSV import action to transaction pages (expense/income)
7. Handle success/error responses
8. Write widget tests
9. Perform manual end-to-end testing

## Detailed Implementation Steps

### Step 1: Add Dependencies
**File:** `frontend/pubspec.yaml`
```yaml
dependencies:
  file_picker: ^6.1.1  # Latest stable version for file selection
```

### Step 2: Extend HTTP Client
**File:** `frontend/lib/services/http_client.dart`

Add multipart POST method:
```dart
Future<Map<String, dynamic>> postMultipart(
  String path,
  Map<String, String> fields,
  Map<String, MultipartFile> files,
) async
```

**Implementation Notes:**
- Use `http.MultipartRequest` class
- Add JWT token to headers
- Handle file size validation
- Parse JSON response

### Step 3: Create CSV Import Service
**File:** `frontend/lib/services/csv_import_service.dart`

**Responsibilities:**
- Upload CSV file with account ID
- Parse API response into domain models
- Handle errors and validation messages

**Key Methods:**
```dart
Future<CsvParseResult> uploadCsv(File file, String accountId)
```

**Domain Models Needed:**
- `CsvParseResult`: totalRows, successCount, errorCount, proposals, errors
- `TransactionProposal`: transactionDate, description, amount, currency, type, categoryId
- `ParseError`: lineNumber, message

### Step 4: Create Upload Dialog Widget
**File:** `frontend/lib/widgets/csv_upload_dialog.dart`

**Component Structure:**
```
Dialog
├── Title: "Import Transactions from CSV"
├── File Selection Section
│   ├── Current file name display
│   └── "Choose File" button
├── Account Selection Dropdown
│   └── List of user accounts
├── Progress Indicator (conditional)
│   └── LinearProgressIndicator or CircularProgressIndicator
├── Error Display (conditional)
│   └── List of parse errors with line numbers
└── Actions
    ├── "Cancel" button
    └── "Upload" button (disabled until file + account selected)
```

**State Management:**
- Use `StatefulWidget`
- Track: selectedFile, selectedAccount, isUploading, uploadProgress, errors

**User Flow:**
1. User clicks "Choose File" → file picker opens
2. User selects CSV file → file name displayed
3. User selects account from dropdown
4. User clicks "Upload" → progress indicator shown
5. On success → close dialog, show snackbar, navigate to staging (future)
6. On error → display validation errors with line numbers

### Step 5: Add CSV Import Action
**File:** `frontend/lib/screens/transaction_page.dart`

**Integration Points:**
1. Add FloatingActionButton with menu (add manual / import CSV)
2. Or add "Import CSV" button to AppBar actions
3. Show `CsvUploadDialog` on button click
4. Refresh transaction list after successful import (future)

**Recommended Approach:** FloatingActionButton with speed dial menu
- Primary action: Add manual transaction
- Secondary action: Import from CSV

### Step 6: Error Handling Strategy

**Client-Side Validation:**
- File size check (< 10MB)
- File extension check (.csv)
- Account selection required

**Server-Side Error Display:**
- Show parse errors in dialog with line numbers
- Display validation messages clearly
- Differentiate between:
  - File format errors (show in alert)
  - Row-level parse errors (show in list)

**Error Message Examples:**
```
❌ File is too large (max 10MB)
❌ Invalid file format - must be mBank CSV
❌ Line 5: Invalid date format
❌ Line 12: Missing required column
```

### Step 7: Progress Indicator Design

**Upload Progress:**
- Use `LinearProgressIndicator` (indeterminate mode)
- Show during: file upload → parsing → response
- Disable "Upload" button during upload
- Show status text: "Uploading...", "Parsing...", "Processing..."

**Future Enhancement (TASK-0043):**
- Track actual upload progress percentage
- Show file upload vs server processing separately

## Testing Strategy

### Unit Tests
1. `CsvImportService` tests:
   - Successful upload with valid response
   - Error handling for network failures
   - Error handling for validation failures
   - Response parsing

### Widget Tests
1. `CsvUploadDialog` tests:
   - Initial state rendering
   - File picker interaction
   - Account selection
   - Upload button state (enabled/disabled)
   - Error display
   - Progress indicator visibility

### Integration Tests
1. End-to-end flow:
   - Open dialog → select file → select account → upload
   - Verify API call with correct parameters
   - Verify error handling

### Manual Testing Checklist
- [ ] File picker opens on button click
- [ ] File name displays after selection
- [ ] Account dropdown populates with user accounts
- [ ] Upload button disabled until file + account selected
- [ ] Progress indicator shows during upload
- [ ] Success response closes dialog and shows snackbar
- [ ] Parse errors display with line numbers
- [ ] Validation errors display clearly
- [ ] Cancel button works at any stage
- [ ] Large file (>10MB) rejected with error message

## File Structure

```
frontend/
├── lib/
│   ├── models/
│   │   ├── csv_parse_result.dart        # NEW
│   │   ├── transaction_proposal.dart    # NEW
│   │   └── parse_error.dart             # NEW
│   ├── services/
│   │   ├── http_client.dart             # MODIFY (add multipart POST)
│   │   └── csv_import_service.dart      # NEW
│   ├── screens/
│   │   └── transaction_page.dart        # MODIFY (add import action)
│   └── widgets/
│       └── csv_upload_dialog.dart       # NEW
├── pubspec.yaml                          # MODIFY (add file_picker)
└── test/
    ├── services/
    │   └── csv_import_service_test.dart # NEW
    └── widgets/
        └── csv_upload_dialog_test.dart  # NEW
```

## Dependencies on Future Tasks

**This task enables:**
- TASK-0043: In-memory transaction processing
- TASK-0063: Staging list UI
- TASK-0064: Transaction edit in staging
- TASK-0065: Bulk transaction save

**Current limitations:**
- No staging screen yet → after upload, user sees success message only
- No transaction preview before save
- No AI categorization integration (TASK-0050+)

**Temporary behavior after successful upload:**
- Show success snackbar with count: "Successfully parsed X transactions"
- Close dialog
- User waits for staging screen implementation

## Open Questions

Before implementation, please clarify:

1. **Upload Action Placement:**
   - Option A: FloatingActionButton with speed dial menu (Add Manual / Import CSV)
   - Option B: Separate "Import" button in AppBar actions
   - Option C: Add to existing transaction list as a card/button
   - **Recommendation:** Option A (consistent with current "Add" pattern)

2. **Account Selection Required?**
   - Backend requires `accountId` parameter
   - Should we pre-select if user has only one account?
   - Should we allow changing account per transaction later in staging?
   - **Assumption:** Required on upload, can be changed later in staging

3. **Post-Upload Behavior:**
   - Option A: Show success message, stay on transaction page
   - Option B: Navigate to staging screen immediately (requires TASK-0063)
   - Option C: Show preview in expanded dialog
   - **Recommendation:** Option A for now (TASK-0063 will add navigation)

4. **File Picker Mode:**
   - Single file only (mBank CSV)
   - Or allow multiple CSV files to be uploaded sequentially?
   - **Assumption:** Single file per upload

5. **Error Display Detail Level:**
   - Show all parse errors in dialog (could be many)?
   - Show first 5 errors + "and X more" summary?
   - Show errors in expandable section?
   - **Recommendation:** Show first 10 errors + summary if more

## Success Criteria

✅ User can select CSV file via file picker
✅ User can select account from dropdown
✅ Upload button disabled until both file and account selected
✅ Progress indicator shown during upload
✅ Success response shows snackbar with parsed transaction count
✅ Parse errors displayed with line numbers
✅ Validation errors displayed clearly
✅ File size validation (max 10MB)
✅ Dialog can be cancelled at any stage
✅ All tests passing (unit + widget)
✅ Code follows project guidelines (DDD, pure functions, small files)

## Estimated Complexity

**Total Story Points:** 5
- Infrastructure setup: 1 point
- Service layer: 1 point
- UI component: 2 points
- Testing: 1 point

**Risk Areas:**
- File picker platform compatibility (web vs mobile)
- Multipart upload implementation
- Large file handling (memory constraints)
- Error message UX (too many errors overwhelming user)

## Next Steps After Completion

1. Mark TASK-0042 as complete in backlog
2. Proceed with TASK-0043: In-memory transaction processing
3. Then TASK-0063: Staging list UI
4. Integrate with TASK-0050+: AI categorization
