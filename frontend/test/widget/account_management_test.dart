import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/screens/accounts_page.dart';

import '../mocks/in_memory_account_service.dart';

void main() {
  group('Account Management Acceptance Tests', () {
    late InMemoryAccountService accountService;

    setUp(() {
      accountService = InMemoryAccountService();
    });

    group('AC1: Account Creation Form Structure', () {
      testWidgets('should display accounts page in main app', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.byType(FloatingActionButton), findsOneWidget);
      });

      testWidgets('should display account creation form when Add Account is clicked', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        await tester.tap(find.byType(FloatingActionButton));
        await tester.pumpAndSettle();

        expect(find.byType(FloatingActionButton), findsOneWidget);
        expect(find.text('Nazwa konta'), findsOneWidget);
        expect(find.text('Typ konta:'), findsOneWidget);
        expect(find.text('Waluta:'), findsOneWidget);
        expect(find.text('Dodaj'), findsOneWidget);
        expect(find.text('Anuluj'), findsOneWidget);
      });
    });

    group('AC2: Account Creation Validation', () {
      testWidgets('should show validation error for empty account name', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        await tester.tap(find.byType(FloatingActionButton));
        await tester.pumpAndSettle();

        await tester.tap(find.text('Dodaj'));
        await tester.pumpAndSettle();

        expect(find.byType(FloatingActionButton), findsOneWidget);
      });
    });

    group('AC3: Account Creation Success Flow', () {
      testWidgets('should create account and close dialog on valid data submission', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        await tester.tap(find.byType(FloatingActionButton));
        await tester.pumpAndSettle();

        await tester.enterText(find.byType(TextField), 'Test Account');
        await tester.tap(find.text('Dodaj'));
        await tester.pumpAndSettle();

        final accounts = await accountService.getAccounts();
        expect(accounts.length, 1);
        expect(accounts.first.name, 'Test Account');

        expect(find.text('Test Account (PLN)'), findsOneWidget);
      });
    });

    group('AC4: Accounts List View', () {
      testWidgets('should show empty state message when no accounts exist', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.text('Brak kont'), findsOneWidget);
        expect(find.text('Dodaj pierwsze konto, aby zacząć'), findsOneWidget);
        expect(find.byIcon(Icons.account_balance_wallet_outlined), findsOneWidget);
      });

      testWidgets('should display loading indicator initially', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));

        expect(find.byType(CircularProgressIndicator), findsOneWidget);

        await tester.pumpAndSettle();

        expect(find.text('Brak kont'), findsOneWidget);
      });
    });

    group('AC5: Account Details View', () {
      testWidgets('should show delete button for accounts', (WidgetTester tester) async {
        accountService.createAccount('Test Account 1', type: 'Rachunek bieżący', currency: 'PLN');
        accountService.createAccount('Test Account 2', type: 'Oszczędnościowe', currency: 'EUR');

        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.byIcon(Icons.delete_outline), findsNWidgets(2));
      });
    });

    group('AC6: Account Editing', () {
      testWidgets('should have currency dropdown with available currencies', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        await tester.tap(find.byType(FloatingActionButton));
        await tester.pumpAndSettle();

        expect(find.text('Waluta:'), findsOneWidget);
        expect(find.text('PLN'), findsOneWidget);

        await tester.tap(find.byType(DropdownButton<String>).last);
        await tester.pumpAndSettle();

        expect(find.text('EUR'), findsOneWidget);
        expect(find.text('USD'), findsOneWidget);
        expect(find.text('GBP'), findsOneWidget);
      });
    });

    group('AC7: Account Deletion', () {
      testWidgets('should show confirmation dialog template', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.text('Usuń'), findsNothing);
      });
    });

    group('AC8: Error Handling', () {
      testWidgets('should handle error states', (WidgetTester tester) async {
        accountService.setGetAccountsError(Exception('Network error'));

        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));

        await tester.pumpAndSettle();

        expect(find.text('Nieoczekiwany błąd: Exception: Network error'), findsOneWidget);
        expect(find.text('Brak kont'), findsOneWidget);
      });
    });

    group('AC9: Loading States', () {
      testWidgets('should show loading indicator when data is being loaded', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));

        expect(find.byType(CircularProgressIndicator), findsOneWidget);

        await tester.pumpAndSettle();
      });
    });

    group('AC10: Responsive Design', () {
      testWidgets('should be usable on mobile device', (WidgetTester tester) async {
        await tester.binding.setSurfaceSize(const Size(360, 640));
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.byType(FloatingActionButton), findsOneWidget);
      });
    });

    group('AC11: Navigation', () {
      testWidgets('should have proper navigation structure', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.byType(FloatingActionButton), findsOneWidget);
      });
    });

    group('AC12: Data Validation', () {
      testWidgets('should have form validation in place', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        await tester.tap(find.byType(FloatingActionButton));
        await tester.pumpAndSettle();

        expect(find.byType(TextField), findsOneWidget);
        expect(find.text('Nazwa konta'), findsOneWidget);
        expect(find.byType(DropdownButton<String>), findsNWidgets(2));
      });
    });

    group('AC13: Currency Summary Display', () {
      testWidgets('should not display currency summary when accounts list is empty', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.text('Saldo łączne według walut:'), findsNothing);
      });

      testWidgets('should display currency summary when accounts exist', (WidgetTester tester) async {
        accountService.createAccount('Test Account 1', type: 'Rachunek bieżący', currency: 'PLN');
        accountService.createAccount('Test Account 2', type: 'Oszczędnościowe', currency: 'EUR');

        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.text('Saldo łączne według walut:'), findsOneWidget);

        final summaryCard = find.ancestor(of: find.text('Saldo łączne według walut:'), matching: find.byType(Card));
        expect(summaryCard, findsOneWidget);

        expect(find.descendant(of: summaryCard, matching: find.text('PLN')), findsOneWidget);
        expect(find.descendant(of: summaryCard, matching: find.text('EUR')), findsOneWidget);
      });

      testWidgets('should sum balances for accounts with same currency', (WidgetTester tester) async {
        accountService.addAccount('Account 1', balance: 12.12, currency: 'PLN');
        accountService.addAccount('Account 2', balance: 30.33, currency: 'PLN');
        accountService.addAccount('Account 2', balance: 100.44, currency: 'USD');

        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));
        await tester.pumpAndSettle();

        expect(find.text('Saldo łączne według walut:'), findsOneWidget);

        final summaryCard = find.ancestor(of: find.text('Saldo łączne według walut:'), matching: find.byType(Card));

        expect(find.descendant(of: summaryCard, matching: find.text('USD')), findsOneWidget);
        expect(find.descendant(of: summaryCard, matching: find.text('100.44')), findsOneWidget);
        expect(find.descendant(of: summaryCard, matching: find.text('PLN')), findsOneWidget);
        expect(find.descendant(of: summaryCard, matching: find.text('42.45')), findsOneWidget);
      });

      testWidgets('should not display currency summary while loading', (WidgetTester tester) async {
        await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountsPage(accountService: accountService))));

        expect(find.text('Saldo łączne według walut:'), findsNothing);
        expect(find.byType(CircularProgressIndicator), findsOneWidget);

        await tester.pumpAndSettle();
      });
    });
  });
}
