import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/screens/transaction_page.dart';
import 'package:frontend/widgets/no_accounts_dialog.dart';
import 'package:frontend/widgets/no_categories_dialog.dart';
import 'package:frontend/widgets/transaction_form.dart';

import '../mocks/in_memory_account_service.dart';
import '../mocks/in_memory_category_service.dart';
import '../mocks/in_memory_transaction_service.dart';
import '../test_setup.dart';

void main() {
  group('TransactionsPage Validation', () {
    late InMemoryAccountService accountService;
    late InMemoryCategoryService categoryService;
    late InMemoryTransactionService transactionService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      accountService = InMemoryAccountService();
      categoryService = InMemoryCategoryService();
      transactionService = InMemoryTransactionService();
    });

    tearDown(() {
      accountService.clear();
      categoryService.clear();
      transactionService.clear();
    });

    testWidgets('should show NoCategoriesDialog when adding transaction without categories', (WidgetTester tester) async {
      // Given: No categories
      accountService.addAccount('Test Account');

      await tester.pumpWidget(
        MaterialApp(
          home: TransactionsPage(type: TransactionType.expense, accountService: accountService, categoryService: categoryService, transactionService: transactionService),
        ),
      );
      await tester.pumpAndSettle();

      // When: Open Add Menu and click Add Manually
      final fab = find.byType(FloatingActionButton);
      await tester.tap(fab);
      await tester.pumpAndSettle();

      final addManually = find.text('Dodaj ręcznie');
      await tester.tap(addManually);
      await tester.pumpAndSettle();

      // Then: NoCategoriesDialog is shown
      expect(find.byType(NoCategoriesDialog), findsOneWidget);
      expect(find.byType(TransactionForm), findsNothing);
    });

    testWidgets('should show NoAccountsDialog when adding transaction without accounts', (WidgetTester tester) async {
      // Given: Categories exist but NO accounts
      categoryService.addCategory('Food', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(
          home: TransactionsPage(
            type: TransactionType.expense,
            accountService: accountService, // Empty
            categoryService: categoryService,
            transactionService: transactionService,
          ),
        ),
      );
      await tester.pumpAndSettle();

      // When: Open Add Menu and click Add Manually
      final fab = find.byType(FloatingActionButton);
      await tester.tap(fab);
      await tester.pumpAndSettle();

      final addManually = find.text('Dodaj ręcznie');
      await tester.tap(addManually);
      await tester.pumpAndSettle();

      // Then: NoAccountsDialog is shown
      expect(find.byType(NoAccountsDialog), findsOneWidget);
      expect(find.byType(TransactionForm), findsNothing);
    });

    testWidgets('should show TransactionForm when accounts and categories exist', (WidgetTester tester) async {
      // Given: Accounts and Categories exist
      accountService.addAccount('Test Account');
      categoryService.addCategory('Food', type: CategoryType.expense);

      await tester.pumpWidget(
        MaterialApp(
          home: TransactionsPage(type: TransactionType.expense, accountService: accountService, categoryService: categoryService, transactionService: transactionService),
        ),
      );
      await tester.pumpAndSettle();

      // When: Open Add Menu and click Add Manually
      final fab = find.byType(FloatingActionButton);
      await tester.tap(fab);
      await tester.pumpAndSettle();

      final addManually = find.text('Dodaj ręcznie');
      await tester.tap(addManually);
      await tester.pumpAndSettle();

      // Then: TransactionForm is shown
      expect(find.byType(TransactionForm), findsOneWidget);
      expect(find.byType(NoAccountsDialog), findsNothing);
      expect(find.byType(NoCategoriesDialog), findsNothing);
    });
  });
}
