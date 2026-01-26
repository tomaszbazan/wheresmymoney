import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/widgets/transfer_dialog.dart';

import '../mocks/in_memory_transfer_service.dart';

void main() {
  late InMemoryTransferService transferService;

  final accounts = [
    {'id': '1', 'name': 'Konto PLN', 'currency': 'PLN', 'balance': 1000.0},
    {'id': '2', 'name': 'Konto EUR', 'currency': 'EUR', 'balance': 200.0},
    {'id': '3', 'name': 'Konto PLN 2', 'currency': 'PLN', 'balance': 500.0},
  ];

  setUp(() {
    transferService = InMemoryTransferService();
    transferService.setAccountCurrency('1', 'PLN');
    transferService.setAccountCurrency('2', 'EUR');
    transferService.setAccountCurrency('3', 'PLN');
  });

  Widget createDialog({Map<String, dynamic>? sourceAccount}) {
    return MaterialApp(home: Scaffold(body: TransferDialog(accounts: accounts, transferService: transferService, sourceAccount: sourceAccount, onSuccess: () {})));
  }

  testWidgets('should display form with all fields', (tester) async {
    await tester.pumpWidget(createDialog());

    expect(find.text('Z konta'), findsOneWidget);
    expect(find.text('Na konto'), findsOneWidget);
    expect(find.text('Kwota źródłowa'), findsOneWidget);
  });

  testWidgets('should pre-select source account when provided', (tester) async {
    await tester.pumpWidget(createDialog(sourceAccount: accounts[0]));

    expect(find.text('Konto PLN (PLN)'), findsOneWidget);
  });

  testWidgets('should show target amount field for different currencies', (tester) async {
    await tester.pumpWidget(createDialog(sourceAccount: accounts[0]));

    // Open target account dropdown
    await tester.tap(find.byType(DropdownButtonFormField<String>).last);
    await tester.pumpAndSettle();

    // Select EUR account
    await tester.tap(find.text('Konto EUR (EUR)').last);
    await tester.pumpAndSettle();

    expect(find.text('Kwota docelowa'), findsOneWidget);
  });

  testWidgets('should call createTransfer on submit', (tester) async {
    await tester.pumpWidget(createDialog(sourceAccount: accounts[0]));

    // Select target account (same currency)
    await tester.tap(find.byType(DropdownButtonFormField<String>).last);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Konto PLN 2 (PLN)').last);
    await tester.pumpAndSettle();

    // Enter amount
    await tester.enterText(find.widgetWithText(TextFormField, 'Kwota źródłowa'), '100');
    await tester.pump();

    // Submit
    await tester.tap(find.text('Przelej'));
    await tester.pump();

    final transfers = await transferService.getTransfers();
    expect(transfers.length, 1);
    expect(transfers[0].sourceAccountId, '1');
    expect(transfers[0].targetAccountId, '3');
    expect(transfers[0].sourceAmount, 100);
    expect(transfers[0].sourceCurrency, 'PLN');
    expect(transfers[0].targetCurrency, 'PLN');
  });
}
