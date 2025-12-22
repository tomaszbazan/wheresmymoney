# TASK-0058: AI Categorization UI Integration

## Objective
Display AI-suggested categories in import preview with manual edit capability using modal/dialog after CSV upload.

## User Requirements
- Show transaction list with AI-proposed categories in a modal after successful CSV upload
- Use inline dropdown for category editing (existing SearchableCategoryDropdown)
- Display AI icon indicator for suggested categories
- Allow user to review all transactions and fill missing categories
- Single "Save All" button to save all transactions at once

## Current State Analysis

### Backend (Completed)
- `CategorySuggestionService` returns `List<CategorySuggestion>` with `transactionId` and `categoryId`
- `CsvParseService` integrates AI categorization and populates `categoryId` in `TransactionProposalView`
- Response structure: `CsvParseResultView` contains `List<TransactionProposalView>` with nullable `categoryId`

### Frontend (Existing)
- `CsvUploadDialog` handles file upload and calls `/api/transactions/import`
- `TransactionStagingScreen` displays transaction list with category selection
- `SearchableCategoryDropdown` provides inline category editing
- `TransactionStagingService` manages in-memory transaction proposals before save

## Implementation Plan

### 1. Track AI Suggestions in Frontend Model
**Files**:
- `frontend/lib/models/transaction_proposal.dart`
- `frontend/lib/services/transaction_staging_service.dart`

**Changes**:
- Add `bool isSuggestedByAi` field to `TransactionProposal` model
- Set `isSuggestedByAi = true` when transaction is loaded with `categoryId != null` from backend
- This flag remains true even if user manually changes the category
- Update `fromJson` to set this flag based on presence of `categoryId`

**Tests**:
- Unit test: verify `isSuggestedByAi` is set correctly during deserialization
- Unit test: verify flag persists after category change

### 2. Enhance Transaction Staging List UI
**Files**:
- `frontend/lib/widgets/transaction_staging_list.dart`
- Create new: `frontend/lib/widgets/ai_category_indicator.dart`

**Changes**:
- Create `AiCategoryIndicator` widget that displays:
  - AI icon (e.g., `Icons.auto_awesome` or `Icons.psychology`)
  - Only shown when `isSuggestedByAi == true`
  - Positioned next to the category dropdown
- Update `TransactionStagingList` to show `AiCategoryIndicator` for AI-suggested categories
- Style AI-suggested rows differently (subtle background color or border)

**Tests**:
- Widget test: verify `AiCategoryIndicator` shows icon when `isSuggestedByAi` is true
- Widget test: verify indicator hidden when `isSuggestedByAi` is false
- Golden test: snapshot of transaction card with AI indicator

### 3. Add Validation for Missing Categories
**Files**:
- `frontend/lib/services/transaction_staging_service.dart`
- `frontend/lib/screens/transaction_staging_screen.dart`

**Changes**:
- Add `hasIncompleteCategorization()` method to `TransactionStagingService`
- Check if any `categoryId` is null before allowing save
- Show error snackbar when user tries to save with missing categories
- Highlight/scroll to first transaction with missing category

**Tests**:
- Unit test: verify `hasIncompleteCategorization()` logic
- Widget test: verify save button behavior when categories missing

### 4. Update Save All Flow
**Files**:
- `frontend/lib/screens/transaction_staging_screen.dart`

**Changes**:
- Modify "Save All" button to:
  - Validate all categories are assigned
  - Show confirmation dialog with count of AI-suggested vs manually-edited
  - Call existing bulk save API
- Add loading state during save operation

**Tests**:
- Widget test: verify validation before save
- Widget test: verify confirmation dialog shows correct counts
- Integration test: verify end-to-end flow from upload to save

### 5. Modal Dialog Integration
**Files**:
- `frontend/lib/widgets/csv_upload_dialog.dart`
- Create new: `frontend/lib/widgets/transaction_preview_modal.dart`

**Changes**:
- Create `TransactionPreviewModal` that wraps `TransactionStagingScreen` content
- Modify `CsvUploadDialog._handleUploadResult()` to show modal instead of navigating
- Modal should:
  - Display transaction count and AI suggestion stats
  - Include close/cancel action
  - Include "Save All" action (with validation)
  - Handle navigation after successful save

**Tests**:
- Widget test: verify modal displays correctly
- Widget test: verify modal can be closed
- Widget test: verify save action triggers correctly

## Testing Strategy

### Unit Tests (70%)
- Service validation logic for incomplete categorization
- AI indicator display logic

### Widget Tests (20%)
- `AiCategoryIndicator` rendering
- Transaction card with/without AI suggestions
- Modal dialog interactions
- Save button validation behavior

### Integration Tests (10%)
- End-to-end CSV upload → AI categorization → preview → save flow
- Error handling when AI service fails (backend returns null categories)

## Edge Cases to Handle

1. **No AI suggestions** (all `categoryId` null): User must manually categorize all
2. **Partial AI suggestions**: Some transactions have suggestions, others don't
3. **AI service failure**: Backend returns `categoryId=null` for all - graceful degradation
4. **User overrides AI**: Track which categories were manually changed (optional analytics)

## UI/UX Considerations

- Keep AI indicator subtle - don't overwhelm user
- Clear visual distinction between AI-suggested and user-selected categories
- Ensure modal is scrollable for large CSV imports (100+ transactions)
- Provide keyboard navigation for faster category assignment
- Consider adding "Skip" option to close modal without saving

## Dependencies

- TASK-0054 must be fully completed (CategorySuggestionService integrated)
- TASK-0055 must be fully completed (Integration in CsvParseService)
- Existing `SearchableCategoryDropdown` component
- Existing `TransactionStagingService` and `TransactionStagingScreen`

## Success Criteria

- [ ] AI-suggested categories display with visual indicator in preview
- [ ] User can edit any category using inline dropdown
- [ ] Validation prevents saving with missing categories
- [ ] Modal shows after upload with transaction list
- [ ] Single "Save All" button saves all transactions
- [ ] 90%+ test coverage for new/modified code
- [ ] Golden tests pass for new UI components
- [ ] No regression in existing CSV import flow

## Rollout Plan

1. Frontend model updates (add `isSuggestedByAi` tracking)
2. UI components (indicator widget)
3. Modal integration
4. Validation logic
5. Integration testing
6. Golden test updates


## Notes

- Focus on simple, clean UI with basic AI suggestion indicator
- Ensure backwards compatibility if AI service is disabled/unavailable
- AI indicator should be subtle and non-intrusive
