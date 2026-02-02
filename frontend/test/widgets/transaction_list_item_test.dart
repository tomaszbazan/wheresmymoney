import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/money.dart';
import 'package:frontend/models/transaction/bill_item.dart';
import 'package:frontend/models/transaction/bill_item_category.dart';
import 'package:frontend/models/transaction/transaction.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/widgets/transaction_list_item.dart';

void main() {
  final now = DateTime.now();

  Transaction createTransaction({required List<BillItem> billItems, required TransactionType type}) {
    return Transaction(
      id: '1',
      accountId: 'acc1',
      amount: Money(value: 100.0, currency: 'PLN'),
      type: type,
      billItems: billItems,
      createdAt: now,
      updatedAt: now,
      transactionDate: now,
    );
  }

  const category = BillItemCategory(id: 'cat1', name: 'Food');

  group('TransactionListItem', () {
    testWidgets('displays single item transaction correctly', (tester) async {
      final billItem = BillItem(id: '1', category: category, amount: Money(value: 100.0, currency: 'PLN'), description: 'Lunch');
      final transaction = createTransaction(billItems: [billItem], type: TransactionType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: TransactionListItem(transaction: transaction, accountName: 'Main Account', accountCurrency: 'PLN', onEdit: (_) {}, onDelete: (_) {}))),
      );

      expect(find.text('Lunch'), findsOneWidget);
      expect(find.text('Konto: Main Account (PLN)'), findsOneWidget);
      expect(find.text('Kategoria: Food'), findsOneWidget);
      expect(find.text('100.00 PLN'), findsOneWidget);
      expect(find.byIcon(Icons.arrow_downward), findsOneWidget);
    });

    testWidgets('displays multi-item transaction correctly (collapsed)', (tester) async {
      final billItems = [
        BillItem(id: '1', category: category, amount: Money(value: 50.0, currency: 'PLN'), description: 'Lunch'),
        BillItem(id: '2', category: category, amount: Money(value: 50.0, currency: 'PLN'), description: 'Dinner'),
      ];
      final transaction = createTransaction(billItems: billItems, type: TransactionType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: TransactionListItem(transaction: transaction, accountName: 'Main Account', accountCurrency: 'PLN', onEdit: (_) {}, onDelete: (_) {}))),
      );

      expect(find.text('Lunch (+1 więcej)'), findsOneWidget);
      expect(find.text('Kategoria: Wiele kategorii'), findsOneWidget);
      expect(find.byIcon(Icons.expand_more), findsOneWidget);
      // Ensure details are not visible yet
      expect(find.text('Dinner'), findsNothing);
    });

    testWidgets('expands multi-item transaction on tap', (tester) async {
      final billItems = [
        BillItem(id: '1', category: category, amount: Money(value: 50.0, currency: 'PLN'), description: 'Lunch'),
        BillItem(id: '2', category: category, amount: Money(value: 50.0, currency: 'PLN'), description: 'Dinner'),
      ];
      final transaction = createTransaction(billItems: billItems, type: TransactionType.expense);

      await tester.pumpWidget(
        MaterialApp(home: Scaffold(body: TransactionListItem(transaction: transaction, accountName: 'Main Account', accountCurrency: 'PLN', onEdit: (_) {}, onDelete: (_) {}))),
      );

      // Tap to expand
      await tester.tap(find.byType(ListTile));
      await tester.pumpAndSettle();

      expect(find.byIcon(Icons.expand_less), findsOneWidget);
      expect(find.byType(Divider), findsWidgets);

      // Verify RichText content for 'Dinner'
      expect(find.byWidgetPredicate((widget) => widget is RichText && (widget.text as TextSpan).toPlainText().contains('Kategoria: Food, Opis: Dinner')), findsOneWidget);

      // Verify RichText content for 'Lunch' (first item)
      expect(find.byWidgetPredicate((widget) => widget is RichText && (widget.text as TextSpan).toPlainText().contains('Kategoria: Food, Opis: Lunch')), findsOneWidget);
    });

    testWidgets('calls onEdit when edit selected', (tester) async {
      final billItem = BillItem(id: '1', category: category, amount: Money(value: 100.0, currency: 'PLN'), description: 'Lunch');
      final transaction = createTransaction(billItems: [billItem], type: TransactionType.expense);
      var edited = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(body: TransactionListItem(transaction: transaction, accountName: 'Main Account', accountCurrency: 'PLN', onEdit: (_) => edited = true, onDelete: (_) {})),
        ),
      );

      await tester.tap(find.byType(PopupMenuButton<String>));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Edytuj'));
      await tester.pumpAndSettle();

      expect(edited, isTrue);
    });
  });
}
