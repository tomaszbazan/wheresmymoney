import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/widgets/account_list_item.dart';

void main() {
  group('AccountListItem', () {
    testWidgets('displays account information correctly', (tester) async {
      final account = Account(id: '123', name: 'Test Account', balance: 1000.50, type: 'Rachunek bieżący', currency: 'PLN');

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountListItem(account: account, onDeleteRequest: () {}))));

      expect(find.text('Test Account (PLN)'), findsOneWidget);
      expect(find.text('Rachunek bieżący'), findsOneWidget);
      expect(find.text('1000.50 PLN'), findsOneWidget);
    });

    testWidgets('displays negative balance in red', (tester) async {
      final account = Account(id: '123', name: 'Debt Account', balance: -500.00, type: 'Kredytowa', currency: 'USD');

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountListItem(account: account, onDeleteRequest: () {}))));

      final balanceText = tester.widget<Text>(find.text('-500.00 USD'));
      expect(balanceText.style?.color, Colors.red);
    });

    testWidgets('calls onDeleteRequest when delete button pressed', (tester) async {
      final account = Account(id: '123', name: 'Test Account', balance: 100.00, type: 'Gotówka', currency: 'PLN');

      var deleteRequestCalled = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AccountListItem(
              account: account,
              onDeleteRequest: () {
                deleteRequestCalled = true;
              },
            ),
          ),
        ),
      );

      await tester.tap(find.byIcon(Icons.delete_outline));
      await tester.pumpAndSettle();

      expect(deleteRequestCalled, true);
    });

    testWidgets('shows dismissible background with delete icon', (tester) async {
      final account = Account(id: '123', name: 'Test Account', balance: 100.00, type: 'Gotówka', currency: 'PLN');

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountListItem(account: account, onDeleteRequest: () {}, onDismissed: () {}))));

      expect(find.byType(Dismissible), findsOneWidget);
    });
  });
}
