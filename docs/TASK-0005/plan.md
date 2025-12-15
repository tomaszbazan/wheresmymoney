# TASK-0005: Golden Test Implementation Plan

## Overview
Configure the first golden test for the Flutter frontend using Alchemist to establish visual regression testing infrastructure. This will enable snapshot-based UI testing to catch unintended visual changes.

### Why Alchemist?
Alchemist is chosen over Flutter's built-in golden testing for several key advantages:
- **Cross-platform consistency**: Eliminates font rendering issues across macOS, Linux, and CI environments
- **Simpler API**: More intuitive test structure with `GoldenTestScenario` and `GoldenTestGroup`
- **Better CI/CD support**: Platform-specific golden directories and automatic failure artifacts
- **No manual font configuration**: Built-in font loading eliminates environment-specific failures
- **Industry proven**: Maintained by Betterment and used in production Flutter applications

## Context
- **Current State**: No golden tests exist in the frontend codebase
- **Testing Framework**: Alchemist - Advanced golden testing library for Flutter
- **Existing Tests**: 17 Dart test files including widget tests, but no golden tests
- **Test Directory**: `/frontend/test/`

## Goals
1. Set up golden test infrastructure in the Flutter frontend
2. Create the first golden test as a reference implementation
3. Establish patterns for future golden test development
4. Integrate with CI/CD pipeline for automated validation

## Implementation Steps

### Step 0: Add Alchemist Dependency
**Action**: Add Alchemist package to `pubspec.yaml`

**File to Update**: `/frontend/pubspec.yaml`

```yaml
dev_dependencies:
  alchemist: ^0.7.0
  flutter_test:
    sdk: flutter
```

**Why Alchemist**:
- Consistent font rendering across platforms (eliminates font rendering issues)
- Better CI/CD integration with explicit platform support
- Simplified golden test API
- Built-in utilities for common test scenarios
- Automatic golden file organization

### Step 1: Create Golden Test Directory Structure
**Action**: Create directory structure for golden test files and baseline images

```
frontend/
├── test/
│   └── goldens/                        # New directory for golden tests
│       └── transaction_list_golden_test.dart
└── golden_test_output/                 # Alchemist auto-generated directory
    └── goldens/
        └── ci/                         # CI-specific goldens
            └── transaction_list/
                ├── empty_state.png
                ├── with_transactions.png
                └── loading_state.png
```

**Files to Create**:
- `/frontend/test/goldens/` directory

**Important Notes**:
- Alchemist automatically creates `golden_test_output/` directory when tests run
- Commit `golden_test_output/goldens/ci/` to repository (baseline images)
- Add `golden_test_output/failures/` to `.gitignore` (temporary failure artifacts)

### Step 2: Choose Target Widget for First Golden Test
**Decision**: Use `TransactionListWidget` as the first golden test target

**Rationale**:
- Already has widget test (`transaction_list_test.dart`)
- Visual component with multiple states (empty, loading, populated)
- Core feature of the application
- Good reference for future golden tests

**Widget Location**: `/frontend/lib/screens/transactions/transaction_list.dart`

### Step 3: Write Failing Golden Test (RED)
**Action**: Create `transaction_list_golden_test.dart` following TDD red-green cycle

**Test Structure**:
```dart
import 'package:alchemist/alchemist.dart';
import 'package:flutter/material.dart';
import 'package:wheresmymoney/screens/transactions/transaction_list.dart';

void main() {
  group('TransactionList Golden Tests', () {
    goldenTest(
      'renders empty state correctly',
      fileName: 'transaction_list',
      builder: () => GoldenTestGroup(
        scenarioConstraints: const BoxConstraints(maxWidth: 400),
        children: [
          GoldenTestScenario(
            name: 'empty_state',
            child: MaterialApp(
              home: Scaffold(
                body: TransactionListWidget(transactions: []),
              ),
            ),
          ),
        ],
      ),
    );

    goldenTest(
      'renders transaction list with data',
      fileName: 'transaction_list',
      builder: () => GoldenTestGroup(
        scenarioConstraints: const BoxConstraints(maxWidth: 400),
        children: [
          GoldenTestScenario(
            name: 'with_transactions',
            child: MaterialApp(
              home: Scaffold(
                body: TransactionListWidget(
                  transactions: [
                    // Test data using existing test patterns
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  });
}
```

**Expected Result**: Test fails because golden files don't exist yet

**Key Alchemist Features Used**:
- `goldenTest()`: Main test wrapper for golden tests
- `GoldenTestGroup`: Groups multiple scenarios together
- `GoldenTestScenario`: Individual test case with name
- `scenarioConstraints`: Controls widget size for consistent rendering
- `fileName`: Organizes goldens by feature

### Step 4: Generate Golden Files (GREEN)
**Action**: Run Alchemist's golden file generation command

**Commands**:
```bash
cd frontend
flutter test --update-goldens --tags=golden test/goldens/transaction_list_golden_test.dart
```

**Expected Outcome**:
- Baseline images created in `golden_test_output/goldens/ci/transaction_list/`
- Tests pass after golden files are generated
- Alchemist automatically handles font loading and rendering consistency

**Note**: Alchemist uses `--tags=golden` to specifically target golden tests

### Step 5: Verify Golden Test
**Action**: Run golden tests without update flag to verify they pass

**Commands**:
```bash
cd frontend
flutter test --tags=golden test/goldens/transaction_list_golden_test.dart
```

**Validation**:
- All tests pass
- No visual differences detected
- Baseline images in `golden_test_output/goldens/ci/` are committed to repository

**Alternative - Run All Golden Tests**:
```bash
cd frontend
flutter test --tags=golden
```

### Step 6: Document Golden Test Patterns
**Action**: Create documentation for golden test best practices

**File to Create**: `/frontend/test/goldens/README.md`

**Content**:
- How to run golden tests with Alchemist (`flutter test --tags=golden`)
- How to update golden files when intentional UI changes occur (`--update-goldens`)
- Naming conventions for golden files (use `fileName` and scenario `name`)
- When to add new golden tests (new UI components, critical user flows)
- Alchemist-specific features:
  - Using `GoldenTestScenario` for multiple states
  - Setting `scenarioConstraints` for consistent sizing
  - Organizing tests with `fileName` parameter
  - Reviewing failure artifacts in `golden_test_output/failures/`

### Step 7: Integrate with Build Pipeline
**Action**: Update CI/CD configuration to run golden tests with Alchemist

**Files to Update**:
- `.github/workflows/frontend.yml` (if exists)
- Add golden test step: `flutter test --tags=golden`

**CI/CD Configuration Example**:
```yaml
- name: Run Golden Tests
  run: |
    cd frontend
    flutter test --tags=golden --update-goldens

- name: Upload Golden Test Failures
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: golden-test-failures
    path: frontend/golden_test_output/failures/
```

**Alchemist CI Advantages**:
- Consistent font rendering across CI environments (built-in)
- No need for custom font loading scripts
- Automatic failure artifact generation
- Platform-specific golden support (ci/ directory for CI runs)

## Testing Strategy

### Test Coverage
- **Unit Tests**: Not applicable (golden tests are widget-level)
- **Widget Tests**: 100% (this IS a widget test using golden files)
- **Integration Tests**: Not applicable

### Test Scenarios for First Golden Test
1. **Empty State**: No transactions to display
2. **Populated State**: List with multiple transactions
3. **Loading State** (optional): Shimmer or loading indicator

### Test Data Requirements
- Use existing test patterns from `transaction_list_test.dart`
- Create minimal, representative transaction data
- Avoid dynamic data (dates, random values) that would cause golden file changes

## Dependencies
- **Alchemist** (^0.7.0): Advanced golden testing library
  - Provides consistent font rendering
  - Simplifies golden test creation
  - Better CI/CD integration
  - Automatic golden file management
- **flutter_test**: Flutter SDK (already in project)

## Success Criteria
- [ ] Alchemist dependency added to pubspec.yaml
- [ ] Golden test directory structure created
- [ ] First golden test file created using Alchemist API
- [ ] Test fails initially (RED phase)
- [ ] Golden files generated successfully in `golden_test_output/`
- [ ] Tests pass after golden generation (GREEN phase)
- [ ] Documentation created
- [ ] CI/CD integration completed
- [ ] Golden files in `golden_test_output/goldens/ci/` committed to repository

## Risks and Considerations

### Font Rendering Differences
**Risk**: Golden tests may fail on different machines due to font rendering
**Mitigation**: ✅ **Alchemist solves this** - Built-in consistent font loading across all platforms

### Image Size
**Risk**: Golden files add to repository size
**Mitigation**: Keep golden tests focused on critical UI components only

### False Positives
**Risk**: Insignificant pixel differences causing test failures
**Mitigation**: Alchemist provides better pixel-perfect rendering consistency, reducing false positives

### Platform Differences
**Risk**: macOS vs Linux rendering differences
**Mitigation**: ✅ **Alchemist solves this** - Uses `ci/` directory for CI-specific goldens with consistent rendering

### Learning Curve
**Risk**: Team needs to learn Alchemist API
**Mitigation**: Alchemist has simpler API than raw Flutter golden tests, with better documentation

## Follow-up Tasks
After completing TASK-0005:
1. Add golden tests for other critical widgets (Category tree, Transaction form)
2. Establish golden test review process in PR workflow
3. Create helper utilities for common golden test setup using Alchemist
4. Explore Alchemist's advanced features (themes, text scaling, device configurations)

## References
- **Alchemist Documentation**: https://pub.dev/packages/alchemist
- **Alchemist GitHub**: https://github.com/Betterment/alchemist
- Flutter Golden Test Documentation: https://docs.flutter.dev/cookbook/testing/widget/golden-files
- Existing Widget Test: `/frontend/test/widget/transaction_list_test.dart`
- Test Setup Utilities: `/frontend/test/test_setup.dart`

## Estimated Complexity
**Low**: Alchemist simplifies golden test setup significantly. Built-in font rendering and platform consistency features eliminate most configuration complexity.

## Project Compliance

### Test-First Development
- ✅ Create test file first
- ✅ Verify test fails (no golden files)
- ✅ Generate golden files
- ✅ Verify test passes

### Code Quality
- No conditional logic in test code
- Small test functions (<10 lines per test)
- Pure widget rendering without side effects
- Follow existing test patterns from `transaction_list_test.dart`

### Coverage Target
- This establishes the foundation for visual regression testing
- Contributes to overall test coverage goal of 90%+
