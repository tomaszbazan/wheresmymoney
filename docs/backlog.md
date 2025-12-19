# Backlog - Where's My Money?!?

## Project Setup

| ID        | Task                            | Description                                                                                              | Done |
|-----------|---------------------------------|----------------------------------------------------------------------------------------------------------|------|
| TASK-0001 | Backend project initialization  | Initialize Spring Boot project with required dependencies (Spring Web, Spring Data JPA, Spring Security) | [x]  |
| TASK-0002 | Frontend project initialization | Initialize Flutter web project with basic structure and navigation                                       | [x]  |
| TASK-0003 | Database setup                  | Configure PostgreSQL connection, create database schema and migrations                                   | [x]  |
| TASK-0004 | CI/CD pipeline setup            | Configure build, test and deployment pipelines for frontend and backend                                  | [x]  |
| TASK-0005 | Golden test                     | Configure first golden test                                                                              | [x]  |

## Authentication and User Management

| ID        | Task                                   | Description                                                                   | Done |
|-----------|----------------------------------------|-------------------------------------------------------------------------------|------|
| TASK-0010 | External identity provider integration | Integrate external authentication provider for user login/registration        | [x]  |
| TASK-0011 | User registration flow                 | Implement user registration with automatic group creation                     | [x]  |
| TASK-0012 | User login flow                        | Implement login mechanism with credential verification and session management | [x]  |
| TASK-0013 | Login/registration UI                  | Create Flutter login and registration screens with form validation            | [x]  |
| TASK-0014 | Authentication error handling          | Implement proper error messages for failed login/registration attempts        | [x]  |

## Group Management

| ID        | Task                           | Description                                                                   | Done |
|-----------|--------------------------------|-------------------------------------------------------------------------------|------|
| TASK-0020 | Group domain model             | Create Group entity with owner relationship and unique identifier             | [x]  |
| TASK-0021 | Automatic group creation       | Create new group automatically when user registers                            | [x]  |
| TASK-0022 | Invitation code generation     | Implement unique invitation code generation for groups                        | [x]  |
| TASK-0023 | Join group via invitation code | Allow users to join existing group using invitation code                      | [x]  |
| TASK-0024 | Group settings UI              | Create Flutter screen for managing group settings and invitation codes - TODO | [x]  |
| TASK-0025 | Group membership validation    | Ensure user belongs to only one group at a time                               | [x]  |

## Category Management

| ID        | Task                         | Description                                                           | Done |
|-----------|------------------------------|-----------------------------------------------------------------------|------|
| TASK-0030 | Category domain model        | Create Category entity with tree structure support (up to 5 levels)   | [x]  |
| TASK-0031 | Category CRUD API            | Implement REST endpoints for category create, read, update, delete    | [x]  |
| TASK-0032 | Category tree validation     | Validate category depth (max 5 levels) and unique names within parent | [x]  |
| TASK-0033 | Category deletion validation | Block deletion of categories with assigned transactions               | [x]  |
| TASK-0034 | Category management UI       | Create Flutter screen for managing category tree                      | [x]  |
| TASK-0035 | Searchable category dropdown | Implement searchable dropdown component for category selection        | [x]  |
| TASK-0036 | Empty category list blocking | Disable transaction operations when no categories are defined         | [x]  |

## CSV Import and Parsing

| ID        | Task                             | Description                                                  | Done |
|-----------|----------------------------------|--------------------------------------------------------------|------|
| TASK-0040 | mBank CSV parser                 | Implement parser for mBank CSV file format                   | [x]  |
| TASK-0041 | CSV file validation              | Validate uploaded file structure matches mBank format        | [x]  |
| TASK-0042 | CSV upload UI                    | Create Flutter file upload component with progress indicator | [x]  |
| TASK-0043 | In-memory transaction processing | Process parsed transactions in browser RAM before saving     | [x]  |
| TASK-0044 | Import error handling            | Handle and display parsing errors to user                    | [x]  |

## AI Categorization

| ID        | Task                                  | Description                                                                                  | Done |
|-----------|---------------------------------------|----------------------------------------------------------------------------------------------|------|
| TASK-0050 | Gemini API integration                | Configure Gemini 3.0 Flash API client in backend                                             | [x]  |
| TASK-0051 | CategorySuggestion domain model       | Create domain model for AI category suggestions (transactionIndex, CategoryId, confidence)   | [x]  |
| TASK-0052 | CategorizationPromptBuilder           | Build JSON prompt with transactions and category tree for bulk categorization                | [ ]  |
| TASK-0053 | GeminiResponseParser                  | Parse Gemini JSON response and map to CategoryId for each transaction                        | [ ]  |
| TASK-0054 | CategorySuggestionService             | Orchestrate AI categorization: fetch categories, build prompt, call Gemini, parse response   | [ ]  |
| TASK-0055 | Integration in CsvParseService        | Integrate CategorySuggestionService into CSV import flow after parsing                       | [ ]  |
| TASK-0056 | Error handling for AI failures        | Handle Gemini errors with retry (using @Retryable) and return null categories on failure     | [ ]  |
| TASK-0057 | Support INCOME and EXPENSE categories | Ensure AI suggests categories for both transaction types based on amount sign                | [ ]  |
| TASK-0058 | AI categorization UI integration      | Display AI-suggested categories in import preview with manual edit capability                | [ ]  |

## Transaction Management

| ID        | Task                             | Description                                                                          | Done |
|-----------|----------------------------------|--------------------------------------------------------------------------------------|------|
| TASK-0060 | Transaction domain model         | Create Transaction entity with required fields (date, amount, category, description) | [x]  |
| TASK-0061 | Transaction CRUD API             | Implement REST endpoints for transaction operations                                  | [x]  |
| TASK-0062 | Transaction deduplication        | Implement hash-based deduplication mechanism                                         | [ ]  |
| TASK-0063 | Staging list UI                  | Create Flutter screen for reviewing imported transactions before save                | [ ]  |
| TASK-0064 | Transaction edit in staging      | Allow editing category, description, date, amount before saving                      | [ ]  |
| TASK-0065 | Bulk transaction save            | Save all approved transactions from staging to database                              | [ ]  |
| TASK-0066 | Manual transaction form          | Create form for adding single transactions manually                                  | [x]  |
| TASK-0067 | Transaction list view            | Display saved transactions with filtering and sorting                                | [x]  |
| TASK-0068 | Transaction edit form            | Allow editing saved transactions with deduplication warning                          | [x]  |
| TASK-0069 | Transaction deletion             | Implement hard delete for transactions                                               | [x]  |
| TASK-0070 | Duplicate detection notification | Inform user about skipped duplicate transactions during import                       | [ ]  |

## Data Security and Validation

| ID        | Task                          | Description                                            | Done |
|-----------|-------------------------------|--------------------------------------------------------|------|
| TASK-0080 | Group data isolation          | Ensure users can only access data within their group   | [x]  |
| TASK-0081 | Input sanitization            | Sanitize all user inputs to prevent injection attacks  | [ ]  |
| TASK-0082 | Bank account number filtering | Ensure bank account numbers are not stored in database | [ ]  |
| TASK-0083 | API authorization             | Implement proper authorization checks on all endpoints | [ ]  |

## UI/UX Enhancements

| ID        | Task                  | Description                                               | Done |
|-----------|-----------------------|-----------------------------------------------------------|------|
| TASK-0090 | Main dashboard        | Create main application view after login                  | [x]  |
| TASK-0091 | Navigation structure  | Implement app navigation between main sections            | [x]  |
| TASK-0092 | Loading states        | Add loading indicators for async operations               | [x]  |
| TASK-0093 | Error message display | Implement consistent error message presentation           | [x]  |
| TASK-0094 | Data loss warning     | Warn user about unsaved staging data when navigating away | [ ]  |
