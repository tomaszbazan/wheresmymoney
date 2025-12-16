import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/widgets/searchable_category_dropdown.dart';

import '../mocks/in_memory_category_service.dart';
import '../test_setup.dart';

void main() {
  group('SearchableCategoryDropdown', () {
    late InMemoryCategoryService categoryService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      categoryService = InMemoryCategoryService();
    });

    tearDown(() {
      categoryService.clear();
    });

    testWidgets('should render with loading indicator initially', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should display categories after loading', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Transport', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      expect(find.byType(CircularProgressIndicator), findsNothing);
      expect(find.byType(Autocomplete<String>), findsOneWidget);
    });

    testWidgets('should filter categories by transaction type', (WidgetTester tester) async {
      final expenseCategory = await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Salary', type: CategoryType.income);

      String? selectedCategoryId;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: SearchableCategoryDropdown(
              transactionType: CategoryType.expense,
              onChanged: (categoryId) {
                selectedCategoryId = categoryId;
              },
              categoryService: categoryService,
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.tap(textField);
      await tester.pumpAndSettle();

      expect(find.text('Food'), findsOneWidget);
      expect(find.text('Salary'), findsNothing);

      await tester.tap(find.text('Food'));
      await tester.pumpAndSettle();

      expect(selectedCategoryId, equals(expenseCategory.id));
    });

    testWidgets('should handle search input and filter results', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Transport', type: CategoryType.expense);
      await categoryService.addCategory('Fuel', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.enterText(textField, 'fu');
      await tester.pumpAndSettle();

      expect(find.text('Fuel'), findsOneWidget);
      expect(find.text('Food'), findsNothing);
      expect(find.text('Transport'), findsNothing);
    });

    testWidgets('should display hierarchy with proper indentation', (WidgetTester tester) async {
      final parentCategory = await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Groceries', type: CategoryType.expense, parentId: parentCategory.id);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.tap(textField);
      await tester.pumpAndSettle();

      expect(find.text('Food'), findsOneWidget);
      expect(find.text('Groceries'), findsOneWidget);
    });

    testWidgets('should call onChanged when category selected', (WidgetTester tester) async {
      final category = await categoryService.addCategory('Food', type: CategoryType.expense);
      String? selectedCategoryId;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: SearchableCategoryDropdown(
              transactionType: CategoryType.expense,
              onChanged: (categoryId) {
                selectedCategoryId = categoryId;
              },
              categoryService: categoryService,
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.tap(textField);
      await tester.pumpAndSettle();

      await tester.tap(find.text('Food'));
      await tester.pumpAndSettle();

      expect(selectedCategoryId, equals(category.id));
    });

    testWidgets('should respect enabled state', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService, enabled: false)),
        ),
      );

      await tester.pumpAndSettle();

      final textField = tester.widget<TextFormField>(find.byType(TextFormField));
      expect(textField.enabled, isFalse);
    });

    testWidgets('should validate input when validator provided', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Form(
              child: SearchableCategoryDropdown(
                transactionType: CategoryType.expense,
                onChanged: (categoryId) {},
                categoryService: categoryService,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please select a category';
                  }
                  return null;
                },
              ),
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      final formState = tester.state<FormState>(find.byType(Form));
      final isValid = formState.validate();

      await tester.pumpAndSettle();

      expect(isValid, isFalse);
      expect(find.text('Please select a category'), findsOneWidget);
    });

    testWidgets('should handle empty category list', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      expect(find.byType(CircularProgressIndicator), findsNothing);
      expect(find.byType(Autocomplete<String>), findsOneWidget);
    });

    testWidgets('should handle service errors gracefully', (WidgetTester tester) async {
      categoryService.setApiError(Exception('Failed to load categories'));

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      expect(find.text('Nie udało się załadować kategorii'), findsOneWidget);
      expect(find.text('Spróbuj ponownie'), findsOneWidget);
    });

    testWidgets('should show color indicators for categories', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense, color: '#FF5722');

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.tap(textField);
      await tester.pumpAndSettle();

      final containers = tester.widgetList<Container>(find.byType(Container));
      final colorIndicators = containers.where((container) {
        final decoration = container.decoration;
        return decoration is BoxDecoration && decoration.shape == BoxShape.circle;
      });

      expect(colorIndicators.isNotEmpty, isTrue);
    });

    testWidgets('search should be case-insensitive', (WidgetTester tester) async {
      await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Transport', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: SearchableCategoryDropdown(transactionType: CategoryType.expense, onChanged: (categoryId) {}, categoryService: categoryService))),
      );

      await tester.pumpAndSettle();

      final textField = find.byType(TextFormField);
      await tester.enterText(textField, 'FOOD');
      await tester.pumpAndSettle();

      expect(find.text('Food'), findsOneWidget);
      expect(find.text('Transport'), findsNothing);
    });
  });
}
