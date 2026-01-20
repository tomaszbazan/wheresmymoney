import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/widgets/category_form.dart';
import 'package:frontend/widgets/searchable_category_dropdown.dart';

import '../mocks/in_memory_category_service.dart';
import '../test_setup.dart';

void main() {
  group('CategoryForm', () {
    late InMemoryCategoryService categoryService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      categoryService = InMemoryCategoryService();
    });

    tearDown(() {
      categoryService.clear();
    });

    testWidgets('should allow unchecking main category checkbox and show parent dropdown', (WidgetTester tester) async {
      await categoryService.addCategory('Parent Category', type: CategoryType.expense);

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryForm(defaultType: CategoryType.expense, categoryService: categoryService, onSaved: (_) {}))));

      await tester.pumpAndSettle();

      final mainCategoryCheckbox = find.byType(Checkbox);
      expect(mainCategoryCheckbox, findsOneWidget);

      final checkboxWidget = tester.widget<Checkbox>(mainCategoryCheckbox);
      expect(checkboxWidget.value, isTrue);

      expect(find.byType(SearchableCategoryDropdown), findsNothing);

      await tester.tap(mainCategoryCheckbox);
      await tester.pumpAndSettle();

      final updatedCheckbox = tester.widget<Checkbox>(mainCategoryCheckbox);
      expect(updatedCheckbox.value, isFalse);

      expect(find.byType(SearchableCategoryDropdown), findsOneWidget);
    });

    testWidgets('should allow checking main category checkbox and hide parent dropdown', (WidgetTester tester) async {
      await categoryService.addCategory('Parent Category', type: CategoryType.expense);

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryForm(defaultType: CategoryType.expense, categoryService: categoryService, onSaved: (_) {}))));

      await tester.pumpAndSettle();

      final mainCategoryCheckbox = find.byType(Checkbox);

      await tester.tap(mainCategoryCheckbox);
      await tester.pumpAndSettle();

      expect(find.byType(SearchableCategoryDropdown), findsOneWidget);

      final dropdown = tester.widget<SearchableCategoryDropdown>(find.byType(SearchableCategoryDropdown));
      expect(dropdown.enabled, isTrue);

      await tester.tap(mainCategoryCheckbox);
      await tester.pumpAndSettle();

      final updatedCheckbox = tester.widget<Checkbox>(mainCategoryCheckbox);
      expect(updatedCheckbox.value, isTrue);
    });
  });
}
