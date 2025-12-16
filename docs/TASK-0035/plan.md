# TASK-0035: Searchable Category Dropdown - Implementation Plan

## Overview

Implement a searchable dropdown component for category selection that supports hierarchical display, type filtering, and efficient searching across potentially large category lists.

## Current State Analysis

### Existing Implementation
- **CategorySelector widget**: Uses standard `DropdownButtonFormField` (not searchable)
- **CategoryForm**: Uses basic dropdown for parent category selection
- **Category Model**: Supports hierarchical structure with `parentId`
- **CategoryHierarchy utility**: Flattens tree structure with level information
- **Type awareness**: Categories filtered by INCOME/EXPENSE
- **Visual features**: Color indicators, hierarchical indentation

### Limitations
- No search/filter functionality
- Difficult to use with many categories
- No keyboard navigation support
- Limited accessibility features

## Implementation Approach

### Design Decisions

1. **Use Flutter's built-in Autocomplete widget** instead of external packages
   - Follows project's minimal dependency approach
   - Native Material Design support
   - Handles keyboard navigation automatically

2. **Preserve existing CategorySelector API** for backward compatibility
   - Keep same constructor parameters
   - Maintain type filtering behavior
   - Support validation

3. **Visual hierarchy representation** in dropdown
   - Indentation with visual depth indicators
   - Color dots for quick identification
   - Parent category path display for clarity

4. **Search algorithm**
   - Case-insensitive matching
   - Match against category name
   - Consider showing parent path in results

## Implementation Steps

### Phase 1: Create SearchableCategoryDropdown Widget

**File**: `frontend/lib/widgets/searchable_category_dropdown.dart`

**Requirements**:
- Use Material `Autocomplete<Category>` widget
- Accept parameters:
  - `transactionType` (String): INCOME or EXPENSE
  - `selectedCategoryId` (String?): Currently selected category
  - `onChanged` (Function(String)): Callback when selection changes
  - `validator` (Function(String?)?): Optional form validation
  - `enabled` (bool): Enable/disable state
- Load categories via CategoryService
- Build hierarchical options using CategoryHierarchy
- Implement custom options view builder for hierarchy display
- Handle loading and error states
- Support form validation

**Tests to write first** (`frontend/test/widgets/searchable_category_dropdown_test.dart`):
1. Widget renders with loading indicator initially
2. Widget displays categories after loading
3. Widget filters categories by transaction type
4. Widget handles search input and filters results
5. Widget displays hierarchy with proper indentation
6. Widget calls onChanged when category selected
7. Widget respects enabled/disabled state
8. Widget validates input when validator provided
9. Widget handles empty category list
10. Widget handles service errors gracefully
11. Widget shows color indicators for categories
12. Search is case-insensitive

### Phase 2: Create CategoryOptionItem Widget

**File**: `frontend/lib/widgets/category_option_item.dart`

**Requirements**:
- Display category with proper indentation based on level
- Show color indicator
- Display category name
- Show parent path for nested categories (optional)
- Highlight search matches (optional enhancement)

**Tests to write first** (`frontend/test/widgets/category_option_item_test.dart`):
1. Widget renders category name
2. Widget displays color indicator with correct color
3. Widget applies correct indentation for level 0, 1, 2, etc.
4. Widget displays parent path when requested

### Phase 3: Update CategorySelector to Use New Component

**File**: `frontend/lib/widgets/category_selector.dart`

**Requirements**:
- Replace DropdownButtonFormField with SearchableCategoryDropdown
- Maintain existing API (no breaking changes)
- Keep backward compatibility with existing forms

**Tests to update**:
1. Update existing CategorySelector tests
2. Ensure TransactionForm integration still works
3. Verify validation behavior unchanged

### Phase 4: Update CategoryForm Parent Selection

**File**: `frontend/lib/widgets/category_form.dart`

**Requirements**:
- Replace parent category dropdown with SearchableCategoryDropdown
- Maintain parent validation logic (exclude self and descendants)
- Keep optional "none" option behavior

**Tests to update**:
1. Update CategoryForm tests for new dropdown
2. Verify parent exclusion logic works
3. Test "none" parent option

### Phase 5: Golden Tests

**File**: `frontend/test/widgets/searchable_category_dropdown_golden_test.dart`

**Golden test scenarios**:
1. Empty state (no categories)
2. Loading state
3. Error state
4. Categories loaded - top level only
5. Categories loaded - with hierarchy (2-3 levels)
6. Search in progress with results
7. Search with no results
8. Disabled state
9. With validation error

### Phase 6: Integration Testing

**File**: `frontend/test/widgets/transaction_form_integration_test.dart`

**Test scenarios**:
1. User can search and select category in transaction form
2. Category filtering by transaction type works
3. Form validation includes category selection
4. Selected category persists across form edits

## Testing Strategy

### Unit Tests (70%)
- SearchableCategoryDropdown widget logic
- CategoryOptionItem rendering
- Search filtering algorithm
- Hierarchy flattening with CategoryHierarchy
- Type filtering logic

### Integration Tests (20%)
- CategorySelector with SearchableCategoryDropdown
- TransactionForm with updated CategorySelector
- CategoryForm with updated parent selection
- Service integration with real HTTP responses

### Acceptance Tests (10%)
- Full user flow: create transaction with category search
- Full user flow: create category with parent search
- Error handling scenarios
- Performance with large category lists (50+ categories)

## Implementation Order

1. **Write failing tests** for SearchableCategoryDropdown
2. **Implement SearchableCategoryDropdown** widget
3. **Run tests** - verify they pass (green)
4. **Write failing tests** for CategoryOptionItem
5. **Implement CategoryOptionItem** widget
6. **Run tests** - verify they pass (green)
7. **Update CategorySelector** with new component
8. **Update tests** for CategorySelector
9. **Run tests** - verify integration works
10. **Update CategoryForm** parent dropdown
11. **Update tests** for CategoryForm
12. **Run tests** - verify all changes work
13. **Create golden tests** for visual verification
14. **Run golden tests** and approve baselines
15. **Manual testing** with running application

## Technical Considerations

### Search Algorithm

```dart
List<Category> _filterCategories(List<Category> categories, String query) {
  if (query.isEmpty) return categories;

  final lowerQuery = query.toLowerCase();
  return categories.where((category) {
    return category.name.toLowerCase().contains(lowerQuery);
  }).toList();
}
```

### Hierarchy Display

```dart
Widget _buildCategoryOption(CategoryWithLevel item) {
  return Padding(
    padding: EdgeInsets.only(left: item.level * 24.0),
    child: Row(
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: Color(int.parse(item.category.color)),
            shape: BoxShape.circle,
          ),
        ),
        SizedBox(width: 8),
        Text(item.category.name),
      ],
    ),
  );
}
```

### Form Integration

```dart
SearchableCategoryDropdown(
  transactionType: _selectedType,
  selectedCategoryId: _selectedCategoryId,
  onChanged: (categoryId) {
    setState(() {
      _selectedCategoryId = categoryId;
    });
  },
  validator: (value) {
    if (value == null || value.isEmpty) {
      return 'Please select a category';
    }
    return null;
  },
)
```

## Dependencies

No new dependencies required. Using Flutter's built-in widgets:
- `Autocomplete<T>` for searchable dropdown
- `TextEditingController` for search input
- Existing Material widgets

## Acceptance Criteria

- [ ] User can type to search categories
- [ ] Search is case-insensitive
- [ ] Categories display with visual hierarchy
- [ ] Color indicators shown for each category
- [ ] Type filtering works (INCOME/EXPENSE)
- [ ] Form validation integrated
- [ ] Loading states handled gracefully
- [ ] Error states display retry option
- [ ] Keyboard navigation works
- [ ] Touch/click selection works
- [ ] Disabled state respected
- [ ] All tests pass (90%+ coverage)
- [ ] Golden tests approved
- [ ] No breaking changes to existing forms

## Edge Cases to Handle

1. Empty category list (no categories defined)
2. Single category (search not needed but available)
3. Very long category names (text overflow)
4. Deep hierarchy (5 levels)
5. Many categories (100+) - performance
6. Network errors during category loading
7. Rapid typing in search field (debouncing)
8. Search with no results
9. Special characters in category names
10. Color parsing errors

## Performance Considerations

- CategoryHierarchy already handles flattening efficiently
- Search filtering is O(n) where n = category count
- Consider lazy loading for 100+ categories (future enhancement)
- Autocomplete widget handles scroll performance

## Accessibility

- Autocomplete provides keyboard navigation
- Screen reader support via semantic labels
- Focus management handled by Flutter
- Error messages announced properly

## Future Enhancements (Out of Scope)

- Debounced search for very large lists
- Recent/favorite categories
- Category usage statistics
- Multi-select support
- Keyboard shortcuts (e.g., arrow keys)
- Search highlighting in results
- Parent path breadcrumbs in results

## Success Metrics

- Test coverage: 90%+ for new components
- No performance degradation with 50+ categories
- Zero breaking changes to existing forms
- All golden tests passing
- User can find category in < 3 seconds