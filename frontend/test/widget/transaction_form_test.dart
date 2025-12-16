import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/widgets/transaction_form.dart';

import '../mocks/in_memory_transaction_service.dart';
import '../test_setup.dart';

void main() {
  group('TransactionForm Amount Normalization', () {
    test('should normalize amount 100 to 100.00', () {
      expect(TransactionForm.normalizeAmount('100'), equals('100.00'));
    });

    test('should normalize amount 100,00 to 100.00', () {
      expect(TransactionForm.normalizeAmount('100,00'), equals('100.00'));
    });

    test('should normalize amount 100,5 to 100.50', () {
      expect(TransactionForm.normalizeAmount('100,5'), equals('100.50'));
    });

    test('should normalize amount 100.5 to 100.50', () {
      expect(TransactionForm.normalizeAmount('100.5'), equals('100.50'));
    });

    test('should keep amount 100.00 as 100.00', () {
      expect(TransactionForm.normalizeAmount('100.00'), equals('100.00'));
    });

    test('should handle whitespace in amount', () {
      expect(TransactionForm.normalizeAmount(' 100 '), equals('100.00'));
    });
  });

  group('TransactionForm Currency Change', () {
    late List<Account> testAccounts;
    late InMemoryTransactionService transactionService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      transactionService = InMemoryTransactionService();
      testAccounts = [
        Account(id: '1', name: 'PLN Account', balance: 1000.0, currency: 'PLN'),
        Account(id: '2', name: 'USD Account', balance: 500.0, currency: 'USD'),
        Account(id: '3', name: 'EUR Account', balance: 200.0, currency: 'EUR'),
      ];
    });

    testWidgets('should show PLN currency by default for first account', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: TransactionForm(accounts: testAccounts, onSaved: (_) {}, transactionService: transactionService))));

      expect(find.text('PLN '), findsOneWidget);
    });

    testWidgets('should change currency when different account is selected', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: TransactionForm(accounts: testAccounts, onSaved: (_) {}, transactionService: transactionService))));

      await tester.tap(find.text('PLN Account (PLN)'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('USD Account (USD)'));
      await tester.pumpAndSettle();

      expect(find.text('USD '), findsOneWidget);
    });

    testWidgets('should show EUR currency when EUR account is selected', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: TransactionForm(accounts: testAccounts, onSaved: (_) {}, transactionService: transactionService))));

      await tester.tap(find.text('PLN Account (PLN)'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('EUR Account (EUR)'));
      await tester.pumpAndSettle();

      expect(find.text('EUR '), findsOneWidget);
    });
  });
}
