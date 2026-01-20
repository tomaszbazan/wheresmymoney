import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/screens/categories_page.dart';
import 'package:frontend/widgets/category_form.dart';

import '../mocks/in_memory_category_service.dart';
import '../test_setup.dart';

void main() {
  group('CategoriesPage', () {
    late InMemoryCategoryService categoryService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      categoryService = InMemoryCategoryService();
    });

    tearDown(() {
      categoryService.clear();
    });

    testWidgets('should pass income type to CategoryForm when adding income category', (WidgetTester tester) async {
      final originalOnError = FlutterError.onError;
      FlutterError.onError = (details) {};

      await tester.pumpWidget(MaterialApp(home: CategoriesPage(transactionType: CategoryType.income, categoryService: categoryService)));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      final categoryForm = tester.widget<CategoryForm>(find.byType(CategoryForm));

      FlutterError.onError = originalOnError;

      expect(categoryForm.defaultType, equals(CategoryType.income));
    });

    testWidgets('should pass expense type to CategoryForm when adding expense category', (WidgetTester tester) async {
      final originalOnError = FlutterError.onError;
      FlutterError.onError = (details) {};

      await tester.pumpWidget(MaterialApp(home: CategoriesPage(transactionType: CategoryType.expense, categoryService: categoryService)));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      final categoryForm = tester.widget<CategoryForm>(find.byType(CategoryForm));

      FlutterError.onError = originalOnError;

      expect(categoryForm.defaultType, equals(CategoryType.expense));
    });
  });
}
