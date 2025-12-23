# TASK-0064: Transaction Edit in Staging - Implementation Plan

## Goal
Allow users to edit transaction details (category, description, date, amount) in the staging list before saving them to the database.

## Current State Analysis
The current implementation already supports:
- **Category editing**: Users can change categories via `SearchableCategoryDropdown` (line 119-128 in `transaction_staging_list.dart`)
- **Transaction deletion**: Users can remove transactions from staging (line 137-142)
- **Category update service method**: `TransactionStagingService.updateCategory()` exists

Missing functionality:
- Editing description
- Editing transaction date
- Editing amount
- UI for accessing edit mode (inline vs dialog)

## Functional Requirements
1. User can edit the following fields for staged transactions:
   - Description (text field)
   - Transaction date (date picker)
   - Amount (number input)
   - Category (already implemented)
2. Changes are reflected immediately in the staging list
3. Edited transactions maintain their AI suggestion indicator
4. Validation ensures:
   - Amount is not zero
   - Description is not empty
   - Date is valid
5. User can cancel edits without saving changes

## Design Decision Required

### Option 1: Inline Editing (Expandable Card)
- Each transaction card can expand to show edit fields
- Click "Edit" button → card expands with editable fields
- "Save" and "Cancel" buttons within the expanded card
- **Pros**: No modal dialogs, quick editing
- **Cons**: May clutter the list, harder to implement validation feedback

### Option 2: Edit Dialog (Recommended)
- Click "Edit" icon on transaction card
- Opens a dialog with editable fields
- "Save" and "Cancel" buttons in dialog
- **Pros**: Clean UI, clear validation feedback, familiar pattern
- **Cons**: Extra click required, dialog overhead

**Recommendation**: Option 2 (Edit Dialog) - more consistent with existing patterns and better UX for validation.

## Solution Architecture

### 1. Domain Model - No Changes Required
`TransactionProposal` already has all necessary fields and a `copyWith()` method for immutable updates.

### 2. Service Layer - Add Edit Methods

**TransactionStagingService** (`transaction_staging_service.dart`)

Add methods for editing each field:

```dart
void updateDescription(int index, String description) {
  if (description.trim().isEmpty) {
    throw ArgumentError('Description cannot be empty');
  }
  _proposals[index] = _proposals[index].copyWith(description: description.trim());
  notifyListeners();
}

void updateTransactionDate(int index, DateTime date) {
  _proposals[index] = _proposals[index].copyWith(transactionDate: date);
  notifyListeners();
}

void updateAmount(int index, double amount) {
  if (amount == 0) {
    throw ArgumentError('Amount cannot be zero');
  }
  _proposals[index] = _proposals[index].copyWith(amount: amount);
  notifyListeners();
}

void updateTransaction(int index, {
  String? description,
  DateTime? transactionDate,
  double? amount,
  String? categoryId,
}) {
  var updated = _proposals[index];

  if (description != null) {
    if (description.trim().isEmpty) {
      throw ArgumentError('Description cannot be empty');
    }
    updated = updated.copyWith(description: description.trim());
  }

  if (transactionDate != null) {
    updated = updated.copyWith(transactionDate: transactionDate);
  }

  if (amount != null) {
    if (amount == 0) {
      throw ArgumentError('Amount cannot be zero');
    }
    updated = updated.copyWith(amount: amount);
  }

  if (categoryId != null) {
    updated = updated.copyWith(categoryId: categoryId);
  }

  _proposals[index] = updated;
  notifyListeners();
}
```

### 3. UI Layer - Edit Dialog

**New Widget: `TransactionEditDialog`** (`frontend/lib/widgets/transaction_edit_dialog.dart`)

```dart
import 'package:flutter/material.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:intl/intl.dart';

class TransactionEditDialog extends StatefulWidget {
  final TransactionProposal proposal;

  const TransactionEditDialog({
    super.key,
    required this.proposal,
  });

  @override
  State<TransactionEditDialog> createState() => _TransactionEditDialogState();
}

class _TransactionEditDialogState extends State<TransactionEditDialog> {
  late final TextEditingController _descriptionController;
  late final TextEditingController _amountController;
  late DateTime _selectedDate;
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    _descriptionController = TextEditingController(text: widget.proposal.description);
    _amountController = TextEditingController(
      text: widget.proposal.amount.abs().toStringAsFixed(2),
    );
    _selectedDate = widget.proposal.transactionDate;
  }

  @override
  void dispose() {
    _descriptionController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  Future<void> _selectDate(BuildContext context) async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime.now(),
    );

    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  void _save() {
    if (_formKey.currentState!.validate()) {
      final amount = double.tryParse(_amountController.text);
      if (amount == null || amount == 0) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Kwota musi być liczbą różną od zera')),
        );
        return;
      }

      Navigator.of(context).pop({
        'description': _descriptionController.text.trim(),
        'transactionDate': _selectedDate,
        'amount': widget.proposal.type == TransactionType.expense ? -amount.abs() : amount.abs(),
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Edytuj transakcję'),
      content: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextFormField(
                controller: _descriptionController,
                decoration: const InputDecoration(
                  labelText: 'Opis',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Opis nie może być pusty';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _amountController,
                decoration: InputDecoration(
                  labelText: 'Kwota (${widget.proposal.currency})',
                  border: const OutlineInputBorder(),
                ),
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Kwota nie może być pusta';
                  }
                  final amount = double.tryParse(value);
                  if (amount == null) {
                    return 'Nieprawidłowa kwota';
                  }
                  if (amount == 0) {
                    return 'Kwota nie może być zerem';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              InkWell(
                onTap: () => _selectDate(context),
                child: InputDecorator(
                  decoration: const InputDecoration(
                    labelText: 'Data',
                    border: OutlineInputBorder(),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(DateFormat('yyyy-MM-dd').format(_selectedDate)),
                      const Icon(Icons.calendar_today),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Anuluj'),
        ),
        ElevatedButton(
          onPressed: _save,
          child: const Text('Zapisz'),
        ),
      ],
    );
  }
}
```

### 4. Update TransactionStagingList Widget

**Modifications to `transaction_staging_list.dart`**

Add edit button next to delete button:

```dart
Row(
  children: [
    IconButton(
      icon: const Icon(Icons.edit),
      onPressed: () => _editTransaction(context, proposal, index),
      tooltip: 'Edytuj',
    ),
    IconButton(
      icon: const Icon(Icons.delete),
      onPressed: () {
        widget.stagingService.removeTransaction(index);
      },
      tooltip: 'Usuń',
    ),
  ],
)
```

Add edit handler method:

```dart
Future<void> _editTransaction(
  BuildContext context,
  TransactionProposal proposal,
  int index,
) async {
  final result = await showDialog<Map<String, dynamic>>(
    context: context,
    builder: (context) => TransactionEditDialog(proposal: proposal),
  );

  if (result != null) {
    widget.stagingService.updateTransaction(
      index,
      description: result['description'] as String?,
      transactionDate: result['transactionDate'] as DateTime?,
      amount: result['amount'] as double?,
    );
  }
}
```

## Implementation Order (Test-First)

### Step 1: Service Layer - Update Methods
**Test**: `transaction_staging_service_test.dart`
- Update description with valid value → proposal updated
- Update description with empty value → ArgumentError
- Update transaction date → proposal updated
- Update amount with valid value → proposal updated
- Update amount with zero → ArgumentError
- Update multiple fields via `updateTransaction()` → all fields updated
- Update maintains AI suggestion flag

**Implementation**: Add methods to `TransactionStagingService`

### Step 2: Edit Dialog Widget
**Test**: `transaction_edit_dialog_test.dart` (widget test)
- Dialog displays current values in fields
- Valid form submission → returns updated values
- Empty description validation → shows error
- Zero amount validation → shows error
- Invalid amount (non-numeric) → shows error
- Date picker opens and updates date
- Cancel button closes dialog without returning values

**Test**: Golden test
- Dialog appearance with all fields populated

**Implementation**: Create `TransactionEditDialog` widget

### Step 3: Integration in TransactionStagingList
**Test**: `transaction_staging_list_test.dart` (widget test)
- Edit button visible on each transaction card
- Clicking edit opens dialog
- Saving dialog updates transaction in staging service
- Canceling dialog leaves transaction unchanged

**Test**: Update golden test
- Edit button visible in transaction card

**Implementation**:
- Add edit button to transaction card
- Add `_editTransaction()` method
- Import `TransactionEditDialog`

### Step 4: End-to-End Flow Test
**Test**: Golden test update for `transaction_staging_screen.dart`
- Full flow: CSV import → edit transaction → verify changes → save

**Implementation**: Verify integration works end-to-end

## Files to Create

### Frontend - New Files
1. `frontend/lib/widgets/transaction_edit_dialog.dart`
2. `frontend/test/widgets/transaction_edit_dialog_test.dart`
3. `frontend/test/widgets/goldens/ci/transaction_edit_dialog.png`
4. `frontend/test/widgets/goldens/linux/transaction_edit_dialog.png`

### Frontend - Modified Files
1. `frontend/lib/services/transaction_staging_service.dart`
2. `frontend/lib/widgets/transaction_staging_list.dart`
3. `frontend/test/services/transaction_staging_service_test.dart`
4. `frontend/test/widgets/transaction_staging_list_test.dart` (if exists, otherwise create)
5. `frontend/test/screens/goldens/ci/transaction_staging_screen_with_transactions.png`
6. `frontend/test/screens/goldens/linux/transaction_staging_screen_with_transactions.png`

## Validation Rules

1. **Description**:
   - Cannot be empty or whitespace only
   - Leading/trailing whitespace is trimmed

2. **Amount**:
   - Must be a valid number
   - Cannot be zero
   - Sign is automatically applied based on transaction type

3. **Date**:
   - Must be a valid date
   - Cannot be in the future
   - Minimum date: 2000-01-01

## Edge Cases & Considerations

### 1. Amount Sign Handling
- User enters positive amount always
- Service applies sign based on `TransactionType`:
  - EXPENSE → negative amount
  - INCOME → positive amount

### 2. AI Suggestion Flag Preservation
- Editing a transaction does NOT clear the `isSuggestedByAi` flag
- This maintains transparency about which fields were originally AI-suggested

### 3. Date Picker Constraints
- User cannot select future dates
- Maximum date: `DateTime.now()`
- Minimum date: `DateTime(2000)` (configurable)

### 4. Concurrent Edits (Not Applicable)
- Since staging is local to browser, no concurrent edit conflicts
- Each user has their own staging area

## Acceptance Criteria

1. ✅ User can click "Edit" button on any transaction in staging list
2. ✅ Edit dialog opens with pre-populated fields (description, amount, date)
3. ✅ User can modify description, amount, and date
4. ✅ Validation prevents:
   - Empty description
   - Zero amount
   - Invalid amount (non-numeric)
   - Future dates
5. ✅ Clicking "Save" updates the transaction in staging
6. ✅ Clicking "Cancel" closes dialog without changes
7. ✅ Updated transaction reflects changes in the list immediately
8. ✅ AI suggestion indicator remains visible after edit
9. ✅ Test coverage ≥ 90%
10. ✅ Golden tests capture edit button and dialog UI

## User Experience Flow

```
User Flow:
1. CSV imported → Staging list displayed
2. User sees incorrect description on transaction #3
3. User clicks "Edit" icon on transaction #3
4. Dialog opens with current values
5. User corrects description
6. User clicks "Save"
7. Dialog closes
8. Transaction #3 now shows corrected description
9. User clicks "Zapisz wszystkie" to save to database
```

## Definition of Done
- [ ] `TransactionStagingService.updateTransaction()` implemented with validation
- [ ] `TransactionEditDialog` widget created with form validation
- [ ] Edit button added to transaction cards in `TransactionStagingList`
- [ ] All unit tests for service methods passing
- [ ] All widget tests for edit dialog passing
- [ ] All widget tests for transaction list integration passing
- [ ] Golden tests updated and passing
- [ ] Code formatted with `dart format`
- [ ] No linting errors from `flutter analyze`
- [ ] Coverage ≥ 90%
- [ ] Manual testing completed on web platform
