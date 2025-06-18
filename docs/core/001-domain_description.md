# Domain Description for Personal Finance Management System

## Core Domain

Personal finance management system focused on zero-based budgeting methodology where every dollar has a purpose and is allocated to specific categories.

## Domain Entities

### Budget
- **Purpose**: Monthly financial plan using zero-based budgeting approach
- **Key Attributes**: month/year, total income, category allocations
- **Business Rules**: 
  - Income must equal total category allocations
  - Categories shouldn't be over-allocated
  - Monthly scope with rollover capabilities

### Transaction
- **Purpose**: Record of financial movement (income/expense)
- **Key Attributes**: amount, date, description, account, budget category assignment
- **Business Rules**:
  - Positive amounts represent income
  - Negative amounts represent expenses
  - Must be assigned to a budget category
  - Cannot exceed category allocation

### Account
- **Purpose**: Banking/financial institution account representation
- **Key Attributes**: name, type, balance, currency
- **Business Rules**:
  - Balance calculated from transaction history
  - Supports transaction import/sync
  - Multiple currency support

### Goal
- **Purpose**: Financial objective with progress tracking
- **Key Attributes**: target amount, deadline, current progress, priority
- **Business Rules**:
  - Must have a realistic timeline
  - Progress tracked through dedicated transactions
  - Can be linked to budget categories

### Portfolio
- **Purpose**: Investment tracking and allocation management
- **Key Attributes**: holdings, target allocation, performance metrics
- **Business Rules**:
  - Tracks asset allocation percentages
  - Monitors performance against benchmarks
  - Rebalancing recommendations

## Domain Services

### Budget Management
- Category allocation validation
- Rollover calculations
- Budget vs actual reporting

### Transaction Processing
- Category assignment
- Balance calculations
- Import reconciliation

### Goal Tracking
- Progress calculation
- Timeline adjustment
- Achievement notifications

## Ubiquitous Language

- **Allocation**: Amount assigned to budget category
- **Rollover**: Unused budget amount carried to next period
- **Reconciliation**: Matching imported transactions with manual entries
- **Zero-based**: Budgeting method where income minus allocations equals zero
- **Category**: Budget subdivision for specific expense types
- **Overspend**: Expense exceeding category allocation