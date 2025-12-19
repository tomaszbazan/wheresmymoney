# TASK-0044: Import Error Handling - Implementation Plan

## Overview
Implement comprehensive error handling for CSV import functionality with user-friendly error messages displayed in a modal dialog. The implementation will:
- Define error types on backend (`ErrorType` enum)
- Map error types to Polish user-friendly messages on frontend
- Display errors in modal dialog immediately upon detection
- Block entire imports when errors occur
- Handle both invalid file formats and invalid data types

This approach ensures proper separation of concerns: backend handles error classification, frontend handles presentation and localization.

## Current State Analysis

### Backend
- **Exception Types**:
  - `CsvParsingException` (base exception)
  - `CsvValidationException` (extends CsvParsingException)
- **Validation Points**:
  - File upload validation in `TransactionsImportController`:
    - Empty file check
    - File size validation (max 10MB)
    - Content type validation (CSV only)
  - CSV structure validation in `MbankCsvParser`:
    - Empty file check
    - Minimum line count (28 lines)
    - Column header validation (mBank format)
  - Data parsing in `MbankCsvParser`:
    - Date format validation
    - Amount format validation
    - Currency validation
    - Currency mismatch with account

### Frontend
- **Current Implementation** (`csv_upload_dialog.dart`):
  - Basic error display in red container
  - Shows up to 10 errors with line numbers
  - Generic exception handling with raw error message
  - Error state management with `_errors` list

### Gap Analysis
Current implementation lacks:
1. User-friendly error messages (currently shows technical details)
2. Proper distinction between validation errors (file format) and parsing errors (data types)
3. Clear categorization of error types for users
4. Comprehensive error handling for all failure scenarios

## Requirements (Based on User Feedback)

### Error Types to Handle
1. **Invalid file format**: Non-CSV files
2. **Invalid data types**: Unparseable date/amount/currency values

### Error Display
- **Location**: Modal dialog (already implemented in `CsvUploadDialog`)
- **Behavior**: Show immediately when detected during upload

### Error Handling Strategy
- **Block entire import**: Reject the CSV file when errors occur
- **User action**: Require user to fix issues before retrying

### Error Message Style
- **User-friendly only**: Simple, non-technical messages
- No technical stack traces or implementation details

## Implementation Plan

### Phase 1: Backend - Error Type Classification

#### 1.1 Create Error Type Enum
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/ErrorType.java`

Create enum with error type classification:
- `INVALID_FILE_TYPE` - File is not CSV
- `FILE_TOO_LARGE` - File exceeds size limit
- `EMPTY_FILE` - File has no content
- `INVALID_CSV_FORMAT` - Not mBank format (missing headers, too few lines)
- `INVALID_DATE_FORMAT` - Cannot parse date value
- `INVALID_AMOUNT_FORMAT` - Cannot parse amount value
- `INVALID_CURRENCY` - Unsupported currency code
- `CURRENCY_MISMATCH` - Transaction currency doesn't match account currency
- `UNKNOWN_ERROR` - Generic fallback for unexpected errors

**Tests**:
- `ErrorTypeTest.java`: Verify enum values exist

#### 1.2 Update ParseError Domain Model
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/ParseError.java`

Enhance `ParseError` record to include:
- `ErrorType type` - Error classification
- `int lineNumber` - Line number where error occurred (0 for file-level errors)
- `String details` - Optional technical details (nullable)

Change from:
```java
public record ParseError(int lineNumber, String message)
```

To:
```java
public record ParseError(ErrorType type, int lineNumber, String details)
```

**Tests**:
- `ParseErrorTest.java`: Update existing tests for new structure
  - Test creation with all parameters
  - Test creation with null details
  - Test validation

#### 1.3 Update MbankCsvParser Error Creation
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvParser.java`

Update error creation in:
- `parseDate()`: Create ParseError with `INVALID_DATE_FORMAT` type
- `parseAmount()`: Create ParseError with `INVALID_AMOUNT_FORMAT` type
- `extractCurrency()`: Create ParseError with `INVALID_CURRENCY` type
- Currency mismatch check: Create ParseError with `CURRENCY_MISMATCH` type
- `validate()` methods: Create ParseError with `INVALID_CSV_FORMAT` type

**Tests**:
- `MbankCsvParserTest.java`: Update tests to verify error types
  - Test invalid date returns INVALID_DATE_FORMAT
  - Test invalid amount returns INVALID_AMOUNT_FORMAT
  - Test invalid currency returns INVALID_CURRENCY
  - Test currency mismatch returns CURRENCY_MISMATCH
  - Test invalid format returns INVALID_CSV_FORMAT

#### 1.4 Update Controller Exception Handling
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/TransactionsImportController.java`

Keep existing `validateFile()` validation but update exception messages to use technical identifiers:
- Empty file → throw with "EMPTY_FILE"
- File too large → throw with "FILE_TOO_LARGE"
- Invalid content type → throw with "INVALID_FILE_TYPE"

**Tests**:
- `TransactionsImportControllerTest.java`: Update tests to verify exception messages contain error type identifiers

#### 1.5 Create Global Exception Handler
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/CsvImportExceptionHandler.java`

Create `@RestControllerAdvice` to handle:
- `CsvValidationException`: Return 400 with error type in response body
- `CsvParsingException`: Return 400 with error type in response body
- `IllegalArgumentException`: Parse message to extract error type, return 400

Response structure:
```json
{
  "errorType": "INVALID_FILE_TYPE",
  "message": "Technical description"
}
```

**Tests**:
- `CsvImportExceptionHandlerTest.java`: Test exception handling responses
  - Test CsvValidationException returns correct error type
  - Test CsvParsingException returns correct error type
  - Test IllegalArgumentException returns correct error type
  - Test HTTP 400 status code

### Phase 2: Frontend - Error Type Mapping and Display

#### 2.1 Create Error Type Model
**File**: `frontend/lib/models/parse_error_type.dart`

Create enum matching backend error types:
```dart
enum ErrorType {
  invalidFileType,
  fileTooLarge,
  emptyFile,
  invalidCsvFormat,
  invalidDateFormat,
  invalidAmountFormat,
  invalidCurrency,
  currencyMismatch,
  unknownError;

  static ErrorType fromJson(String value) {
    // Map backend UPPER_SNAKE_CASE to Dart camelCase
  }
}
```

**Tests**:
- `test/models/parse_error_type_test.dart`: Test JSON serialization
  - Test fromJson maps INVALID_FILE_TYPE to invalidFileType
  - Test fromJson maps all backend error types correctly
  - Test fromJson handles unknown types (returns unknownError)

#### 2.2 Update ParseError Model
**File**: `frontend/lib/models/parse_error.dart`

Update `ParseError` model to include error type:
```dart
class ParseError {
  final ErrorType type;
  final int lineNumber;
  final String? details;

  // Add fromJson to parse backend response
}
```

**Tests**:
- `test/models/parse_error_test.dart`: Update tests
  - Test JSON deserialization with error type
  - Test handles null details

#### 2.3 Create Error Message Localization
**File**: `frontend/lib/utils/error_messages.dart`

Create utility class to map error types to Polish messages:
```dart
class ErrorMessages {
  static String getMessage(ErrorType type, int lineNumber) {
    // Return user-friendly Polish message based on error type
  }

  static String getHint(ErrorType type) {
    // Return helpful hint for fixing the error
  }
}
```

Messages to include:
- `INVALID_FILE_TYPE` → "Nieprawidłowy format pliku. Dozwolone są tylko pliki CSV."
- `FILE_TOO_LARGE` → "Plik jest za duży. Maksymalny rozmiar to 10 MB."
- `EMPTY_FILE` → "Plik jest pusty."
- `INVALID_CSV_FORMAT` → "Plik nie zawiera wymaganego formatu mBank."
- `INVALID_DATE_FORMAT` → "Nieprawidłowy format daty w wierszu {lineNumber}."
- `INVALID_AMOUNT_FORMAT` → "Nieprawidłowa kwota w wierszu {lineNumber}."
- `INVALID_CURRENCY` → "Nieobsługiwana waluta w wierszu {lineNumber}."
- `CURRENCY_MISMATCH` → "Waluta w wierszu {lineNumber} nie pasuje do waluty konta."
- `UNKNOWN_ERROR` → "Wystąpił nieoczekiwany błąd."

**Tests**:
- `test/utils/error_messages_test.dart`: Test message generation
  - Test each error type returns correct Polish message
  - Test line number is inserted correctly
  - Test hints are provided for common errors

#### 2.4 Update Error Display Component
**File**: `frontend/lib/widgets/csv_upload_dialog.dart`

Enhance error display section (lines 132-152):
- Use `ErrorMessages.getMessage()` to display errors
- Group errors by type (format errors vs data errors)
- Show clear error categories
- Show helpful hints using `ErrorMessages.getHint()`
- Improve visual hierarchy

**Tests**:
- `test/widgets/csv_upload_dialog_golden_test.dart`: Add golden tests for error states
  - Test: Error display with format errors (INVALID_CSV_FORMAT)
  - Test: Error display with data parsing errors (INVALID_DATE_FORMAT)
  - Test: Error display with mixed errors
  - Test: Error display with hints

#### 2.5 Update Error Handling Logic
**File**: `frontend/lib/widgets/csv_upload_dialog.dart`

Update `_uploadFile()` method (lines 38-72):
- Parse HTTP error responses to extract error type
- Handle file-level errors from exception handler
- Handle network errors gracefully

Update `_handleUploadResult()` method (lines 74-95):
- Use error types for grouping logic
- Map error types to user-friendly messages
- Show error count summary by category
- Add actionable suggestions based on error types

**Tests**:
- `test/widgets/csv_upload_dialog_test.dart`: Add widget tests
  - Test: Parse error types from HTTP 400 response
  - Test: Handle file-level errors (INVALID_FILE_TYPE, etc.)
  - Test: Handle row-level errors (INVALID_DATE_FORMAT, etc.)
  - Test: Group errors by type correctly
  - Test: Show correct error counts
  - Test: Clear errors on retry
  - Test: Display Polish messages for all error types

### Phase 3: Integration and Testing

#### 3.1 Integration Tests
**File**: `backend/src/test/java/pl/btsoftware/backend/csvimport/infrastructure/api/TransactionsImportControllerTest.java`

Add integration tests:
- Test end-to-end error handling for invalid file format
- Test end-to-end error handling for invalid data types
- Verify HTTP status codes and response structure

#### 3.2 Frontend Integration Tests
**File**: `frontend/test/widgets/csv_upload_dialog_test.dart`

Add integration tests:
- Test upload with invalid CSV format
- Test upload with invalid data
- Test error display and retry flow

#### 3.3 Manual Testing Scenarios
Create test CSV files:
1. Invalid format (JSON file with .csv extension)
2. Invalid date format
3. Invalid amount format
4. Invalid currency
5. Missing column headers
6. Empty file
7. File too large

### Phase 4: Documentation

#### 4.1 Update API Documentation
Document error responses in API spec:
- Error response structure
- HTTP status codes
- Common error scenarios

#### 4.2 User Documentation
Create user guide section:
- Common import errors
- How to fix them
- CSV format requirements

## Error Type Mapping

### Backend Error Types → Frontend Messages

| Backend Error Type | Frontend Enum | Polish Message | Helpful Hint (Optional) |
|-------------------|---------------|----------------|------------------------|
| `INVALID_FILE_TYPE` | `invalidFileType` | "Nieprawidłowy format pliku. Dozwolone są tylko pliki CSV." | "Upewnij się, że plik ma rozszerzenie .csv" |
| `FILE_TOO_LARGE` | `fileTooLarge` | "Plik jest za duży. Maksymalny rozmiar to 10 MB." | "Spróbuj zaimportować mniejszy zakres dat" |
| `EMPTY_FILE` | `emptyFile` | "Plik jest pusty." | "Sprawdź czy wybrałeś właściwy plik" |
| `INVALID_CSV_FORMAT` | `invalidCsvFormat` | "Plik nie zawiera wymaganego formatu mBank." | "Upewnij się, że plik został wyeksportowany z mBanku" |
| `INVALID_DATE_FORMAT` | `invalidDateFormat` | "Nieprawidłowy format daty w wierszu {lineNumber}." | "Sprawdź poprawność daty w pliku CSV" |
| `INVALID_AMOUNT_FORMAT` | `invalidAmountFormat` | "Nieprawidłowa kwota w wierszu {lineNumber}." | "Sprawdź czy kwota zawiera poprawną wartość liczbową" |
| `INVALID_CURRENCY` | `invalidCurrency` | "Nieobsługiwana waluta w wierszu {lineNumber}." | "Obsługiwane waluty: PLN, EUR, USD, GBP" |
| `CURRENCY_MISMATCH` | `currencyMismatch` | "Waluta w wierszu {lineNumber} nie pasuje do waluty konta." | "Wybierz konto w tej samej walucie co transakcje" |
| `UNKNOWN_ERROR` | `unknownError` | "Wystąpił nieoczekiwany błąd." | "Spróbuj ponownie lub skontaktuj się z pomocą techniczną" |

### Error Grouping Strategy

Errors will be grouped into two categories for display:
1. **File-level errors** (stop processing immediately):
   - `INVALID_FILE_TYPE`
   - `FILE_TOO_LARGE`
   - `EMPTY_FILE`
   - `INVALID_CSV_FORMAT`

2. **Row-level errors** (collected during parsing):
   - `INVALID_DATE_FORMAT`
   - `INVALID_AMOUNT_FORMAT`
   - `INVALID_CURRENCY`
   - `CURRENCY_MISMATCH`

## Implementation Order

1. **Backend error type classification** (Phase 1.1-1.5)
   - Start with test-first approach
   - Create `ErrorType` enum with error type constants
   - Update `ParseError` domain model to include error type
   - Update `MbankCsvParser` to create errors with types
   - Update `TransactionsImportController` validation
   - Create global exception handler with error type support
   - Verify all tests pass

2. **Frontend error type handling** (Phase 2.1-2.5)
   - Create `ErrorType` enum (Dart) matching backend types
   - Update `ParseError` model to include error type
   - Create `ErrorMessages` utility for type-to-message mapping
   - Add unit tests for error message generation
   - Update `csv_upload_dialog.dart` to use error types
   - Add golden tests for error display variations
   - Verify all tests pass

3. **Integration testing** (Phase 3.1-3.3)
   - Backend integration tests for error types in API responses
   - Frontend integration tests for error type parsing and display
   - Manual testing with test CSV files
   - Verify error types flow correctly from backend to frontend

4. **Documentation** (Phase 4.1-4.2)
   - Document error type enum in API spec
   - Document error response structure
   - Create user guide with common errors and fixes

## Success Criteria

- [ ] All file format errors show user-friendly messages
- [ ] All data parsing errors show user-friendly messages with line numbers
- [ ] Errors are displayed in a modal dialog immediately upon detection
- [ ] Invalid CSV files block entire import
- [ ] Users can retry upload after fixing errors
- [ ] Error messages are clear and actionable (Polish language)
- [ ] No technical stack traces visible to users
- [ ] Test coverage remains above 90%
- [ ] Golden tests pass for all error display states
- [ ] Integration tests pass for all error scenarios

## Files to Modify

### Backend
- ✅ Create: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/ErrorType.java`
- ✅ Update: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/ParseError.java`
- ✅ Update: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/ParseErrorView.java`
- ✅ Update: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvParser.java`
- ✅ Update: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/TransactionsImportController.java`
- ✅ Create: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/CsvImportExceptionHandler.java`
- ✅ Create: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/ErrorResponse.java`
- ✅ Create: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/ErrorTypeTest.java`
- ✅ Update: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/ParseErrorTest.java`
- ✅ Update: `backend/src/test/java/pl/btsoftware/backend/csvimport/application/MbankCsvParserTest.java`
- ✅ Update: `backend/src/test/java/pl/btsoftware/backend/csvimport/infrastructure/api/TransactionsImportControllerTest.java`
- ✅ Create: `backend/src/test/java/pl/btsoftware/backend/csvimport/infrastructure/api/CsvImportExceptionHandlerTest.java`

### Frontend
- ✅ Create: `frontend/lib/models/parse_error_type.dart`
- ✅ Update: `frontend/lib/models/parse_error.dart`
- ✅ Create: `frontend/lib/utils/error_messages.dart`
- ✅ Update: `frontend/lib/widgets/csv_upload_dialog.dart`
- ✅ Create: `frontend/test/models/parse_error_type_test.dart`
- ✅ Update: `frontend/test/models/parse_error_test.dart`
- ✅ Create: `frontend/test/utils/error_messages_test.dart`
- ✅ Update: `frontend/test/widgets/csv_upload_dialog_golden_test.dart`
- ✅ Create: `frontend/test/widgets/csv_upload_dialog_test.dart`

## Risk Assessment

### Low Risk
- Error message changes (backward compatible with existing ParseError structure)
- Frontend display updates (isolated to CsvUploadDialog widget)

### Medium Risk
- Exception handler changes (ensure doesn't affect other endpoints)
- Error grouping logic (ensure all error types are handled)

### Mitigation Strategies
- Comprehensive test coverage (unit + integration)
- Golden tests for UI consistency
- Test with various invalid CSV files
- Verify error handling doesn't break happy path

## Notes
- Follow test-first development approach
- **Backend**: Return error types only (no localization)
- **Frontend**: Translate error types to Polish messages (separation of concerns)
- Backend error types use `UPPER_SNAKE_CASE` naming convention
- Frontend error types use `camelCase` naming convention (Dart style)
- Maintain API contract: backend returns error type, frontend handles presentation
- Ensure all tests pass before moving to next phase
- Use golden tests for consistent error display UI
- Error messages are user-facing and must be clear, actionable, and in Polish
