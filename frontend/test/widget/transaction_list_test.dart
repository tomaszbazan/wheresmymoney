import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/models/transaction.dart';
import 'package:frontend/widgets/transaction_list.dart';

void main() {
  group('TransactionList Currency Display', () {
    late List<Account> testAccounts;
    late List<Transaction> testTransactions;

    setUp(() {
      testAccounts = [
        Account(id: '1', name: 'PLN Account', balance: 1000.0, currency: 'PLN'),
        Account(id: '2', name: 'USD Account', balance: 500.0, currency: 'USD'),
        Account(id: '3', name: 'EUR Account', balance: 200.0, currency: 'EUR'),
      ];

      testTransactions = [
        Transaction(
          id: 't1',
          accountId: '1',
          amount: 100.0,
          type: 'INCOME',
          description: 'PLN Transaction',
          categoryId: 'salary-id',
          categoryName: 'Salary',
          createdAt: DateTime(2024, 1, 1),
          updatedAt: DateTime(2024, 1, 1),
        ),
        Transaction(
          id: 't2',
          accountId: '2',
          amount: -50.0,
          type: 'EXPENSE',
          description: 'USD Transaction',
          categoryId: 'food-id',
          categoryName: 'Food',
          createdAt: DateTime(2024, 1, 2),
          updatedAt: DateTime(2024, 1, 2),
        ),
        Transaction(
          id: 't3',
          accountId: '3',
          amount: 75.0,
          type: 'INCOME',
          description: 'EUR Transaction',
          categoryId: 'freelance-id',
          categoryName: 'Freelance',
          createdAt: DateTime(2024, 1, 3),
          updatedAt: DateTime(2024, 1, 3),
        ),
      ];
    });

    testWidgets('should display correct currency for each transaction', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              transactions: testTransactions,
              accounts: testAccounts,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('100.00 PLN'), findsOneWidget);
      expect(find.text('50.00 USD'), findsOneWidget);
      expect(find.text('75.00 EUR'), findsOneWidget);
    });

    testWidgets('should fallback to PLN for account without currency', (
      WidgetTester tester,
    ) async {
      final accountWithoutCurrency = Account(
        id: '4',
        name: 'No Currency Account',
        balance: 300.0,
      );

      final transactionWithNoCurrency = Transaction(
        id: 't4',
        accountId: '4',
        amount: 200.0,
        type: 'INCOME',
        description: 'No Currency Transaction',
        categoryId: 'bonus-id',
        categoryName: 'Bonus',
        createdAt: DateTime(2024, 1, 4),
        updatedAt: DateTime(2024, 1, 4),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              transactions: [transactionWithNoCurrency],
              accounts: [accountWithoutCurrency],
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('200.00 PLN'), findsOneWidget);
    });

    testWidgets('should fallback to PLN for unknown account', (
      WidgetTester tester,
    ) async {
      final orphanTransaction = Transaction(
        id: 't5',
        accountId: 'unknown-account-id',
        amount: 150.0,
        type: 'INCOME',
        description: 'Orphan Transaction',
        categoryId: 'unknown-id',
        categoryName: 'Unknown',
        createdAt: DateTime(2024, 1, 5),
        updatedAt: DateTime(2024, 1, 5),
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              transactions: [orphanTransaction],
              accounts: testAccounts,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('150.00 PLN'), findsOneWidget);
    });

    testWidgets('should show empty state when no transactions', (
      WidgetTester tester,
    ) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              transactions: [],
              accounts: testAccounts,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );

      expect(find.text('Brak transakcji'), findsOneWidget);
      expect(
        find.text('Dodaj pierwszą transakcję klikając przycisk +'),
        findsOneWidget,
      );
    });
  });
}
