import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';

import 'package:frontend/widgets/transaction_form.dart';

import '../mocks/in_memory_transaction_service.dart';
import '../test_setup.dart';

void main() {
  group('TransactionForm Bill Mode', () {
    late List<Account> testAccounts;
    late InMemoryTransactionService transactionService;

    setUp(() async {
      await TestSetup.initializeSupabase();
      transactionService = InMemoryTransactionService();
      testAccounts = [Account(id: '1', name: 'PLN Account', balance: 1000.0, currency: 'PLN')];
    });

    testWidgets('should allow switching to Bill Mode', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: TransactionForm(accounts: testAccounts, onSaved: (_) {}, transactionService: transactionService))));

      // Verify "Prosty" (Simple) is selected by default
      expect(find.byType(ListTile), findsAtLeastNWidgets(2));

      // Tap "Rachunek" (Bill)
      await tester.tap(find.text('Rachunek'));
      await tester.pumpAndSettle();

      // Verify Bill Mode elements are present
      expect(find.text('Dodaj pozycję'), findsOneWidget);
      expect(find.text('Pozycja 1'), findsOneWidget);
      expect(find.text('Suma: 0.00 PLN'), findsOneWidget);
    });

    testWidgets('should calculate sum correctly in Bill Mode', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: TransactionForm(accounts: testAccounts, onSaved: (_) {}, transactionService: transactionService))));

      // Switch to Bill Mode
      await tester.tap(find.text('Rachunek'));
      await tester.pumpAndSettle();

      // Enter amount in first item
      await tester.enterText(find.widgetWithText(TextFormField, 'Kwota'), '100');
      await tester.pumpAndSettle(); // trigger listener

      expect(find.text('Suma: 100.00 PLN'), findsOneWidget);

      // Add another item
      await tester.tap(find.text('Dodaj pozycję'));
      await tester.pumpAndSettle();

      expect(find.text('Pozycja 2'), findsOneWidget);

      // Enter amount in second item (need to find the second amount field)
      // Since there are two 'Kwota' fields now, we need to be specific.
      final amountFields = find.widgetWithText(TextFormField, 'Kwota');
      expect(amountFields, findsNWidgets(2));

      await tester.enterText(amountFields.at(1), '50.50');
      await tester.pumpAndSettle();

      expect(find.text('Suma: 150.50 PLN'), findsOneWidget);
    });
  });
}
