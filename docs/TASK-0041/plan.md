# TASK-0041: CSV File Validation - Implementation Plan

## Task Description
Validate uploaded file structure matches mBank format before parsing.

## Current State Analysis

### Existing Implementation
- **MbankCsvParser** (backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvParser.java):
  - Currently parses mBank CSV files directly
  - Handles errors during parsing (invalid dates, amounts, currencies)
  - Returns `CsvParseResult` with proposals and errors
  - Throws `CsvParsingException` only for empty files
  - No upfront validation of file structure

### mBank Format Requirements (from docs/TASK-0040/mbank-format.md)
- **Encoding**: UTF-8 with BOM
- **Delimiter**: Semicolon (`;`)
- **Structure**:
  - Lines 1-26: Header section
  - Line 27: Column headers starting with `#` (`#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;`)
  - Line 28+: Data rows (5 columns minimum)
- **Required columns** (in order):
  1. `#Data operacji` (Transaction Date)
  2. `#Opis operacji` (Description)
  3. `#Rachunek` (Account)
  4. `#Kategoria` (Category)
  5. `#Kwota` (Amount)

### Current Gap
The parser currently:
- Accepts any file and attempts to parse it
- Only validates during parsing (per-row validation)
- Throws exception only for completely empty files
- Does not validate file structure upfront

This means invalid files (wrong format, wrong bank, corrupted files) are only detected during parsing, resulting in multiple error messages instead of a clear "invalid file format" error.

## Goal
Add upfront validation to reject files that don't match mBank format before attempting to parse individual rows. This provides:
- Faster feedback to users
- Clearer error messages
- Protection against parsing non-CSV or non-mBank files

## Design Decisions

### Validation Strategy
Create a separate validator class that checks file structure before parsing:
- **MbankCsvValidator**: Validates file structure matches mBank format
- **Validation occurs**: Before `MbankCsvParser.parse()` attempts row parsing
- **Validation includes**:
  - File is not empty
  - File has minimum required lines (27+ for header + column headers)
  - Line 27 contains correct column headers with `#` prefix
  - Column headers match expected mBank format
  - Delimiter appears to be semicolon

### Integration Point
Validation will be integrated in `CsvParseService`:
```
CsvParseService.parse()
  -> MbankCsvValidator.validate() [NEW]
  -> MbankCsvParser.parse()
```

### Error Handling
- Create `CsvValidationException` extending `CsvParsingException`
- Throw `CsvValidationException` with descriptive message when validation fails
- Include validation failure reason in exception message

### Validation Rules
1. **File not empty**: File has at least 1 byte
2. **Minimum line count**: File has at least 27 lines (header) + 1 (column headers) = 28 lines
3. **Column header line exists**: Line 27 exists and is not blank
4. **Column headers match**: Line 27 contains all required headers in correct order:
   - `#Data operacji`
   - `#Opis operacji`
   - `#Rachunek`
   - `#Kategoria`
   - `#Kwota`
5. **Delimiter check**: Line 27 contains semicolons as delimiters

### Non-Goals
- Do NOT validate encoding (UTF-8 with BOM) - too complex and parser handles it
- Do NOT validate header content (lines 1-26) - not critical for parsing
- Do NOT validate data row format - handled by existing parser
- Do NOT validate date/amount formats - handled by existing parser
- Do NOT count total lines upfront - expensive for large files

## Implementation Steps

### 1. Create Domain Exception
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CsvValidationException.java`

Create exception class extending `CsvParsingException`:
```java
public class CsvValidationException extends CsvParsingException {
    public CsvValidationException(String message) {
        super(message);
    }
}
```

**Test**: `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/CsvValidationExceptionTest.java`
- Verify exception can be created with message
- Verify it extends `CsvParsingException`

### 2. Create Validator Class
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidator.java`

Create validator with single public method:
```java
public void validate(InputStream csvStream) throws CsvValidationException
```

Implementation:
- Read file into List<String> (only first 28 lines needed)
- Check minimum line count (28 lines)
- Extract line 27 (index 26)
- Validate column headers against expected format

**Test**: `backend/src/test/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidatorTest.java`

Test cases (TDD - write tests first):
1. `shouldAcceptValidMbankFile()` - valid sample file passes
2. `shouldRejectEmptyFile()` - throws exception with "empty" message
3. `shouldRejectFileTooShort()` - file with < 28 lines throws exception
4. `shouldRejectMissingColumnHeaders()` - line 27 is empty/missing
5. `shouldRejectIncorrectColumnHeaders()` - wrong header names
6. `shouldRejectMissingRequiredColumn()` - missing one of 5 required columns
7. `shouldRejectWrongColumnOrder()` - columns in wrong order
8. `shouldRejectWrongDelimiter()` - comma instead of semicolon

### 3. Integrate Validator into Service
**File**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/CsvParseService.java`

Modify `parse()` method:
```java
public CsvParseResult parse(ParseCsvCommand command) {
    try (InputStream stream = createInputStream(command)) {
        validator.validate(stream); // NEW: validate before parsing
        return parser.parse(stream);
    }
}
```

**Problem**: InputStream can only be read once!

**Solution**:
- Option A: Read file into memory (byte array), create two streams
- Option B: Make validator non-consuming (mark/reset)
- Option C: Validator reads minimal bytes, resets stream

**Chosen**: Option A - Read into byte array for files < 10MB
- Simple and reliable
- CSV files are typically small (< 1MB)
- Add size limit validation (10MB max)

Updated implementation:
```java
public CsvParseResult parse(ParseCsvCommand command) {
    byte[] fileBytes = readFileBytes(command); // NEW
    validator.validate(new ByteArrayInputStream(fileBytes)); // NEW
    return parser.parse(new ByteArrayInputStream(fileBytes));
}
```

**Test**: `backend/src/test/java/pl/btsoftware/backend/csvimport/application/CsvParseServiceTest.java`

Update existing tests:
- Existing tests should still pass (valid file still works)

Add new test cases:
1. `shouldRejectInvalidFileFormat()` - invalid file throws `CsvValidationException`
2. `shouldProvideValidationErrorMessage()` - validation error includes reason
3. `shouldValidateBeforeParsing()` - validation happens before parsing attempts

### 4. Update System Test
**File**: `backend/src/systemTest/java/pl/btsoftware/backend/csvimport/CsvParseControllerTest.java`

Add system test for validation:
1. `shouldReturn400ForInvalidFileFormat()` - POST with invalid file returns 400
2. `shouldReturnValidationErrorInResponse()` - error message explains validation failure

### 5. Update Exception Handler
**File**: `backend/src/main/java/pl/btsoftware/backend/account/infrastructure/api/GlobalExceptionHandler.java`

Add handler for `CsvValidationException`:
```java
@ExceptionHandler(CsvValidationException.class)
public ResponseEntity<ErrorResponse> handleCsvValidationException(CsvValidationException ex) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ex.getMessage()));
}
```

**Test**: Verify in system test that 400 is returned for validation errors

## Testing Strategy

### Test-First Approach
1. Write failing tests for `CsvValidationException`
2. Write failing tests for `MbankCsvValidator`
3. Implement validator to pass tests
4. Write failing tests for `CsvParseService` integration
5. Implement integration to pass tests
6. Write failing system test
7. Update exception handler to pass system test

### Test Coverage
- Unit tests for validator (8 test cases)
- Unit tests for exception (2 test cases)
- Integration tests for service (3 test cases)
- System tests for API (2 test cases)

### Test Files
Test resource files needed:
- Use existing: `mbank_transaction_list.csv`
- Create new: `invalid_headers.csv` - wrong column names
- Create new: `too_short.csv` - only 20 lines
- Create new: `wrong_delimiter.csv` - comma-separated

## Implementation Order

1. **Create exception class** (backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CsvValidationException.java)
2. **Write exception tests** (backend/src/test/java/pl/btsoftware/backend/csvimport/domain/CsvValidationExceptionTest.java)
3. **Write validator tests** (backend/src/test/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidatorTest.java) - RED
4. **Implement validator** (backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidator.java) - GREEN
5. **Update service tests** (backend/src/test/java/pl/btsoftware/backend/csvimport/application/CsvParseServiceTest.java) - RED
6. **Update service implementation** (backend/src/main/java/pl/btsoftware/backend/csvimport/application/CsvParseService.java) - GREEN
7. **Update system tests** (backend/src/systemTest/java/pl/btsoftware/backend/csvimport/CsvParseControllerTest.java) - RED
8. **Update exception handler** (backend/src/main/java/pl/btsoftware/backend/account/infrastructure/api/GlobalExceptionHandler.java) - GREEN
9. **Run all tests** - verify everything passes
10. **Run checkstyle & spotbugs** - verify code quality

## Files to Create/Modify

### New Files
1. `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/CsvValidationException.java`
2. `backend/src/main/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidator.java`
3. `backend/src/test/java/pl/btsoftware/backend/csvimport/domain/CsvValidationExceptionTest.java`
4. `backend/src/test/java/pl/btsoftware/backend/csvimport/application/MbankCsvValidatorTest.java`
5. `backend/src/test/resources/invalid_headers.csv`
6. `backend/src/test/resources/too_short.csv`
7. `backend/src/test/resources/wrong_delimiter.csv`

### Modified Files
1. `backend/src/main/java/pl/btsoftware/backend/csvimport/application/CsvParseService.java` - integrate validator
2. `backend/src/test/java/pl/btsoftware/backend/csvimport/application/CsvParseServiceTest.java` - add validation tests
3. `backend/src/systemTest/java/pl/btsoftware/backend/csvimport/CsvParseControllerTest.java` - add system tests
4. `backend/src/main/java/pl/btsoftware/backend/account/infrastructure/api/GlobalExceptionHandler.java` - add exception handler

## Success Criteria

1. All new tests pass
2. All existing tests still pass
3. Code coverage for new classes ≥ 95%
4. Checkstyle passes
5. Spotbugs passes
6. Invalid files are rejected with clear error messages before parsing
7. Valid mBank files still parse correctly
8. API returns 400 with error message for invalid files

## Notes

- Keep validator simple and focused on structure validation
- Do not over-validate - parser handles data validation
- Validation should be fast (< 10ms for typical files)
- Error messages should be user-friendly and actionable
- Follow existing code patterns from `MbankCsvParser` and `CsvParseService`
