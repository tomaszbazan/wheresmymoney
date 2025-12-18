# Implementation Plan: TASK-0040 - mBank CSV Parser

## Task Description
Implement parser for mBank CSV file format to enable bulk transaction imports.

## Context Analysis

### Current System State
- Transaction domain model: immutable record with TransactionId, AccountId, Money, TransactionType, description, CategoryId, AuditInfo
- Money value object: BigDecimal with Currency (2 decimal places, HALF_UP rounding)
- TransactionType enum: INCOME, EXPENSE
- TransactionService: handles single transaction creation with validation (currency match, category existence, account ownership)
- No existing file upload or CSV processing functionality
- DDD architecture: domain/, application/, infrastructure/ separation

### Key Constraints
- Currency convention: positive for income, negative for expenses
- UUIDs generated on backend
- Transactions scoped to GroupId (multi-tenancy)
- Must validate: account exists, currency matches, category exists and matches type
- At least one category must exist before transaction creation
- Strong typing: use domain types rather than primitives

### Import Workflow
This task implements the first phase of CSV import - parsing and proposal generation. The complete workflow is:

1. **File Upload** (this task - TASK-0040): User uploads CSV via REST controller
2. **Parsing** (this task - TASK-0040): CSV parsed into transaction proposals (not persisted)
3. **Category Assignment** (future - TASK-0050-0055): AI assigns categories to proposals
   - For now: category field can be null in proposals
   - mBank category stored as hint for AI in future tasks
4. **Return Proposals** (this task - TASK-0040): Parsed proposals returned to user for review
5. **User Review & Edit** (future - TASK-0063, TASK-0064): Frontend displays staging list, user can edit
6. **Save to Database** (future - TASK-0065): After user acceptance, transactions persisted and account balance updated

**Important**: This task does NOT persist transactions to database. It only parses CSV and returns proposals.

## Implementation Steps

### 1. Research mBank CSV Format
**Goal**: Understand exact structure of mBank CSV exports

**Sample File**: `backend/src/test/resources/mbank_transaction_list.csv`

**Format Specification** (based on sample file):
- **Encoding**: UTF-8 with BOM (﻿)
- **Delimiter**: Semicolon (;)
- **Structure**:
  - Lines 1-23: Header information (bank details, client name, date range, account info)
  - Line 24: Summary line with currency and totals
  - Line 27: Column headers starting with # symbol: `#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;`
  - Line 28+: Transaction data rows
- **Date Format**: yyyy-MM-dd (e.g., 2025-12-17)
- **Amount Format**:
  - Space as thousand separator
  - Comma as decimal separator
  - Currency suffix (e.g., "1 100,00 PLN" or "-239,22 PLN")
  - Positive values for income
  - Negative values for expenses (already matches our convention!)
- **Columns**:
  1. `#Data operacji` - Transaction date
  2. `#Opis operacji` - Description (may contain quotes, multiline text)
  3. `#Rachunek` - Account name (e.g., "mKonto Intensive 1234 ... 3456")
  4. `#Kategoria` - Category from mBank (e.g., "Wpływy - inne", "Zdrowie i uroda")
  5. `#Kwota` - Amount with currency

**Test First**: N/A (research phase)

### 2. Create Transaction Proposal Domain Models
**Goal**: Represent parsed CSV rows as transaction proposals (not persisted yet)

**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/`

**Files**:
- `TransactionProposal.java` - immutable record containing:
  - LocalDate transactionDate
  - String description (max 200 chars)
  - BigDecimal amount (positive for income, negative for expenses)
  - Currency currency (extracted from amount string, e.g., PLN)
  - TransactionType type (INCOME or EXPENSE, derived from amount sign)
  - String mbankCategory (from mBank CSV, e.g., "Wpływy - inne" - stored as hint for AI)
  - CategoryId categoryId (nullable - will be assigned by AI in future tasks)

**Test First**:
- Create `TransactionProposalTest.java`
- Test: valid object creation with null category
- Test: description length validation
- Test: type derived correctly from amount sign
- Expected: tests fail (class doesn't exist yet)

### 3. Implement mBank CSV Parser
**Goal**: Parse mBank CSV format into List<TransactionProposal>

**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/`

**Files**:
- `MbankCsvParser.java` - pure function taking InputStream, returning List<TransactionProposal>
- `CsvParsingException.java` (domain exception)

**Implementation Details**:
- Use Apache Commons CSV or similar library
- Handle UTF-8 with BOM encoding
- Skip lines 1-27 (header section until column definition line)
- Start parsing from line 28+ (after `#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;`)
- Parse semicolon-delimited fields
- Handle Polish number format: space as thousand separator, comma as decimal separator
- Extract currency from amount string (e.g., "1 100,00 PLN" → 1100.00 + PLN)
- Date parsing: yyyy-MM-dd format
- Handle quoted descriptions that may contain semicolons
- Determine TransactionType: positive amount = INCOME, negative = EXPENSE
- No need to negate amounts (mBank format already matches our convention!)
- Set categoryId to null (will be assigned by AI in future TASK-0050-0055)
- Store mBank category name in mbankCategory field as hint for AI
- Skip empty rows
- Collect parsing errors with line numbers

**Test First**:
- Create `MbankCsvParserTest.java`
- Use sample file `backend/src/test/resources/mbank_transaction_list.csv` for tests
- Test: parse valid CSV with income and expense rows (use sample file)
- Test: handle missing required field
- Test: handle invalid date format
- Test: handle invalid amount format
- Test: handle empty file
- Test: handle file with header only
- Test: skip empty rows
- Test: preserve line numbers for error reporting
- Test: extract currency correctly (PLN from "1 100,00 PLN")
- Test: parse Polish number format (space + comma separators)
- Expected: all tests fail (parser doesn't exist)

### 4. Create CSV Parse Result Domain Model
**Goal**: Represent parsing operation outcome with proposals

**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/domain/`

**Files**:
- `CsvParseResult.java` - record containing:
  - List<TransactionProposal> proposals (successfully parsed transactions)
  - List<ParseError> errors (with line number, reason)
  - int totalRows
  - int successCount
  - int errorCount
- `ParseError.java` - record containing line number and error message

**Test First**:
- Create `CsvParseResultTest.java`
- Test: calculate success rate
- Test: aggregate error types
- Test: handle mixed successful and failed parses
- Expected: tests fail

### 5. Implement CSV Parse Service
**Goal**: Orchestrate CSV parsing and return proposals (no persistence)

**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/application/`

**Files**:
- `CsvParseService.java` - application service
- `ParseCsvCommand.java` - command object with:
  - InputStream csvFile
  - UserId userId (from JWT)
  - AccountId accountId (for validation only)

**Implementation Details**:
- Inject: MbankCsvParser, AccountModuleFacade
- Validate account exists and belongs to user's group
- Validate account currency matches CSV currency
- Parse CSV using MbankCsvParser
- Return CsvParseResult with:
  - List of TransactionProposal (with categoryId = null)
  - Parse errors (if any)
  - Statistics (total, success, error counts)
- **Important**: Does NOT persist transactions - only parses and returns proposals

**Test First**:
- Create `CsvParseServiceTest.java`
- Use InMemoryAccountRepository
- Test: successful parsing of all rows into proposals
- Test: proposals have null categoryId
- Test: proposals contain mbankCategory for AI hint
- Test: reject when account doesn't exist
- Test: reject when account doesn't belong to user's group
- Test: partial parse with some invalid rows (returns both proposals and errors)
- Expected: all tests fail

### 6. Create REST API Endpoint
**Goal**: Expose CSV parsing via HTTP (returns proposals, no persistence)

**Location**: `backend/src/main/java/pl/btsoftware/backend/csvimport/infrastructure/api/`

**Files**:
- `CsvParseController.java` - REST controller
- `ParseCsvRequest.java` - multipart request with:
  - MultipartFile csvFile
  - UUID accountId
- `CsvParseResultView.java` - response view containing:
  - List<TransactionProposalView> proposals
  - List<ParseErrorView> errors
  - int totalRows, successCount, errorCount
- `TransactionProposalView.java` - DTO for proposal with all fields including mbankCategory

**Implementation Details**:
- POST /api/transactions/parse
- Validate: file not empty, file type CSV, max file size
- Extract UserId from SecurityContext (JWT)
- Delegate to CsvParseService
- Return 200 OK with CsvParseResult (proposals + errors)
- Return 400 Bad Request for validation errors
- **Important**: Does NOT save transactions - only returns proposals for user review
- **Future**: Proposals will be sent to AI (TASK-0050-0055) and then saved (TASK-0065)

**Test First**:
- Create `CsvParseControllerTest.java` (system test)
- Use @SystemTest annotation (Testcontainers)
- Use sample file `backend/src/test/resources/mbank_transaction_list.csv`
- Test: upload valid CSV and verify proposals returned (NOT persisted)
- Test: verify proposals have null categoryId
- Test: verify proposals contain mbankCategory
- Test: verify no transactions created in database
- Test: reject empty file
- Test: reject non-CSV file
- Test: reject unauthorized access (wrong group)
- Expected: tests fail

### 7. Integration Testing
**Goal**: Verify end-to-end parsing flow (no persistence)

**Actions**:
- Create system test with real PostgreSQL (Testcontainers)
- Create test account via setup
- Upload sample mBank CSV (`backend/src/test/resources/mbank_transaction_list.csv`)
- Verify 9 transaction proposals returned from sample file
- Verify proposals have correct data (date, amount, type, currency)
- Verify proposals have null categoryId
- Verify proposals contain mbankCategory from mBank CSV
- Verify NO transactions persisted to database
- Verify account balance unchanged (no transactions created yet)
- Verify error handling for invalid rows

**Test First**:
- Create `CsvParseIntegrationTest.java`
- Test: complete CSV parsing workflow (upload → parse → return proposals)
- Test: verify no persistence occurs
- Test: verify group isolation (can't parse CSV for another group's account)
- Expected: tests fail

### 8. Error Handling and Validation
**Goal**: Comprehensive error reporting

**Actions**:
- Review GlobalExceptionHandler for CSV-specific exceptions
- Ensure CsvParsingException mapped to appropriate HTTP status
- Add validation for file size limits
- Add validation for maximum rows per import

**Test First**:
- Test: file too large
- Test: too many rows
- Test: malformed CSV structure
- Expected: tests fail

### 9. Documentation
**Goal**: Enable future maintainers and API consumers

**Actions**:
- Document mBank CSV format in docs/TASK-0040/mbank-format.md:
  - Include example rows from sample file
  - Describe field formats and parsing rules
  - Document known format variations (if any)
  - Reference sample file location
- Add API documentation to CsvParseController (OpenAPI annotations if used)
- Document the parsing workflow and clarify that this task does NOT persist transactions
- Update CLAUDE.md if needed with CSV import module structure
- Document that proposals are returned for user review, with future AI categorization and persistence

## Definition of Done

- [ ] All tests written first and fail initially
- [ ] All tests pass (90%+ coverage)
- [ ] MbankCsvParser parses valid CSV correctly into TransactionProposal objects
- [ ] CsvParseService returns proposals (does NOT persist to database)
- [ ] Proposals have null categoryId (reserved for AI in future tasks)
- [ ] Proposals contain mbankCategory as hint for AI
- [ ] REST endpoint accepts multipart CSV upload and returns proposals
- [ ] Partial parsing succeeds (continue on individual row errors)
- [ ] Error reporting includes line numbers and reasons
- [ ] NO transactions persisted to database (verified in tests)
- [ ] Account balance unchanged (no persistence in this task)
- [ ] Group isolation verified (can't parse CSV for other group's account)
- [ ] No backward-compatible code
- [ ] No unnecessary comments
- [ ] Files < 200 lines where possible
- [ ] Functions < 10 lines where possible
- [ ] Integration test passes with real database (verifies no persistence)

## Dependencies

**New Libraries** (if needed):
- Apache Commons CSV or OpenCSV for parsing

**Existing Dependencies**:
- AccountModuleFacade (for account validation)
- UsersModuleFacade (for user/group validation)

**NOT Used** (persistence deferred to future tasks):
- TransactionService (will be used in TASK-0065 for saving proposals)
- TransactionModuleFacade
- CategoryModuleFacade

## Risks and Considerations

1. **mBank Format Variability**: CSV format may change between export types, accounts, or over time
   - Current implementation: Based on "Lista operacji" export format (sample file in test resources)
   - Mitigation: Make parser configurable, add format version detection, support multiple formats if needed in future

2. **Large File Handling**: CSV with thousands of rows may cause memory issues
   - Mitigation: Stream processing, chunked imports (future enhancement)

3. **Transaction Deduplication**: Not needed in this task (parsing phase only)
   - Deduplication will be handled in TASK-0062 before persistence

4. **Category Assignment**: Categories not assigned in this task
   - Proposals have null categoryId (will be assigned by AI in TASK-0050-0055)
   - mBank category stored as hint for future AI categorization
   - Actual persistence with categories handled in TASK-0065

5. **Stateless Parsing**: Proposals not stored anywhere
   - Frontend must keep proposals in memory/state (TASK-0043)
   - User can review and edit before sending to save endpoint (TASK-0065)

6. **Date/Time Zones**: mBank may export dates without time zone
   - Mitigation: Assume local time zone (Europe/Warsaw), convert to OffsetDateTime

## Next Steps (Future Tasks)

**Immediate workflow continuation**:
- TASK-0041: CSV file validation (format detection)
- TASK-0042: CSV upload UI in Flutter
- TASK-0043: In-memory transaction processing (frontend holds proposals)
- TASK-0050-0055: AI categorization (assign categories to proposals)
- TASK-0063: Staging list UI (display proposals for user review)
- TASK-0064: Transaction edit in staging (user can modify proposals)
- TASK-0065: Bulk transaction save (persist approved proposals to database)
- TASK-0062: Transaction deduplication (before save)

**Complete Import Workflow**:
1. TASK-0040 ✓: Parse CSV → return proposals
2. TASK-0050-0055: AI assigns categories to proposals
3. TASK-0063-0064: User reviews and edits proposals in staging UI
4. TASK-0065: Save approved proposals to database (with balance update)

## Implementation Order Summary

1. Research mBank format (sample file analysis)
2. Domain models (TransactionProposal, CsvParseResult, ParseError)
3. Parser unit tests → Parser implementation (MbankCsvParser)
4. Service unit tests → Service implementation (CsvParseService)
5. Controller system tests → Controller implementation (CsvParseController)
6. Integration tests (verify no persistence)
7. Documentation (including workflow explanation)

## Estimated Scope

- **Unit tests**: ~12-15 test cases
- **Integration tests**: ~4-5 test cases
- **New classes**: ~10 classes (domain models, parser, service, controller, views)
- **Total lines**: ~600-800 lines (including tests) - simpler than originally planned (no persistence)
- **Coverage target**: 95%+

**Note**: Scope reduced compared to original plan because this task only handles parsing, not persistence.