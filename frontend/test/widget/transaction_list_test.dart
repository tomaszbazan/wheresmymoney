import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/models/transaction/bill_item_request.dart';
import 'package:frontend/models/transaction_filter.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/utils/date_formatter.dart';
import 'package:frontend/widgets/transaction_list.dart';

import '../mocks/in_memory_transaction_service.dart';

void main() {
  group('TransactionList Currency Display', () {
    late List<Account> testAccounts;
    late InMemoryTransactionService transactionService;

    setUp(() {
      testAccounts = [
        Account(id: '1', name: 'PLN Account', balance: 1000.0, currency: 'PLN'),
        Account(id: '2', name: 'USD Account', balance: 500.0, currency: 'USD'),
        Account(id: '3', name: 'EUR Account', balance: 200.0, currency: 'EUR'),
      ];

      transactionService = InMemoryTransactionService();
    });

    Future<void> createTestTransaction({required String accountId, required double amountVal, required TransactionType type, required DateTime date}) async {
      await transactionService.createTransaction(
        accountId: accountId,
        transactionDate: date,
        type: type,
        billItems: [BillItemRequest(amount: amountVal, categoryId: 'cat-1', description: 'Test transaction')],
      );
    }

    testWidgets('should display correct currency for each transaction', (WidgetTester tester) async {
      await createTestTransaction(accountId: '1', amountVal: 100.0, type: TransactionType.income, date: DateTime(2024, 1, 15));
      await createTestTransaction(accountId: '3', amountVal: 75.0, type: TransactionType.income, date: DateTime(2024, 1, 25));

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: testAccounts,
              filter: const TransactionFilter(types: [TransactionType.income]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('100.00 PLN'), findsOneWidget);
      expect(find.text('75.00 PLN'), findsOneWidget);
    });

    testWidgets('should display expense transactions when type is expense', (WidgetTester tester) async {
      await createTestTransaction(accountId: '2', amountVal: 50.0, type: TransactionType.expense, date: DateTime(2024, 1, 20));

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: testAccounts,
              filter: const TransactionFilter(types: [TransactionType.expense]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('50.00 PLN'), findsOneWidget);
    });

    testWidgets('should fallback to PLN for account without currency', (WidgetTester tester) async {
      final accountWithoutCurrency = Account(id: '4', name: 'No Currency Account', balance: 300.0);

      await transactionService.createTransaction(
        accountId: '4',
        transactionDate: DateTime(2024, 1, 30),
        type: TransactionType.income,
        billItems: [const BillItemRequest(amount: 200.0, categoryId: 'cat', description: 'No curr')],
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: [...testAccounts, accountWithoutCurrency],
              filter: const TransactionFilter(types: [TransactionType.income]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('200.00 PLN'), findsOneWidget);
    });

    testWidgets('should fallback to PLN for unknown account', (WidgetTester tester) async {
      await transactionService.createTransaction(
        accountId: 'unknown-account-id',
        transactionDate: DateTime(2024, 2, 1),
        type: TransactionType.income,
        billItems: [const BillItemRequest(amount: 150.0, categoryId: 'cat', description: 'Unknown account')],
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: testAccounts,
              filter: const TransactionFilter(types: [TransactionType.income]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('150.00 PLN'), findsOneWidget);
    });

    testWidgets('should show empty state when no transactions', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: testAccounts,
              filter: const TransactionFilter(types: [TransactionType.income]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Brak transakcji'), findsOneWidget);
    });

    testWidgets('should display date from transactionDate field', (WidgetTester tester) async {
      final date = DateTime(2024, 3, 15);
      await transactionService.createTransaction(
        accountId: '1',
        transactionDate: date,
        type: TransactionType.income,
        billItems: [const BillItemRequest(amount: 100.0, categoryId: 'cat', description: 'Date Test')],
      );

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: TransactionList(
              accounts: testAccounts,
              filter: const TransactionFilter(types: [TransactionType.income]),
              transactionService: transactionService,
              onEdit: (_) {},
              onDelete: (_) {},
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.textContaining(DateFormatter.format(date)), findsOneWidget);
    });
  });
}
