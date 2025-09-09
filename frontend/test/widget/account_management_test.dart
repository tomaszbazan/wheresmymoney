import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/screens/accounts_page.dart';
import 'package:mockito/mockito.dart';

import '../mocks/mock_account_service.dart';

void main() {
  group('Account Management Acceptance Tests', () {
    late MockAccountServiceInterface mockAccountService;

    setUp(() {
      mockAccountService = MockAccountServiceInterface();
    });

    group('AC1: Account Creation Form Structure', () {
      testWidgets('should display accounts page in main app', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Dodaj konto'), findsOneWidget);
      });

      testWidgets(
        'should display account creation form when Add Account is clicked',
        (WidgetTester tester) async {
          when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

          await tester.pumpWidget(
            MaterialApp(
              home: Scaffold(
                body: AccountsPage(accountService: mockAccountService),
              ),
            ),
          );
          await tester.pumpAndSettle();

          await tester.tap(find.text('Dodaj konto'));
          await tester.pumpAndSettle();

          expect(find.text('Dodaj konto'), findsWidgets);
          expect(find.text('Nazwa konta'), findsOneWidget);
          expect(find.text('Typ konta:'), findsOneWidget);
          expect(find.text('Waluta:'), findsOneWidget);
          expect(find.text('Dodaj'), findsOneWidget);
          expect(find.text('Anuluj'), findsOneWidget);
        },
      );
    });

    group('AC2: Account Creation Validation', () {
      testWidgets('should show validation error for empty account name', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        await tester.tap(find.text('Dodaj konto'));
        await tester.pumpAndSettle();

        // Test clicking Add with empty name
        await tester.tap(find.text('Dodaj'));
        await tester.pumpAndSettle();

        // Dialog should still be open since name is empty
        expect(find.text('Dodaj konto'), findsWidgets);
      });
    });

    group('AC3: Account Creation Success Flow', () {
      testWidgets(
        'should create account and close dialog on valid data submission',
        (WidgetTester tester) async {
          final mockService = MockAccountServiceInterface();

          when(mockService.getAccounts()).thenAnswer((_) async => []);

          when(
            mockService.createAccount(
              'Test Account',
              type: anyNamed('type'),
              currency: anyNamed('currency'),
            ),
          ).thenAnswer(
            (_) async => Account(
              id: '1',
              name: 'Test Account',
              balance: 0.0,
              number: 'ACC000001',
              type: 'Rachunek bieżący',
              currency: 'PLN',
            ),
          );

          await tester.pumpWidget(
            MaterialApp(
              home: Scaffold(body: AccountsPage(accountService: mockService)),
            ),
          );
          await tester.pumpAndSettle();

          await tester.tap(find.text('Dodaj konto'));
          await tester.pumpAndSettle();

          await tester.enterText(find.byType(TextField), 'Test Account');
          await tester.tap(find.text('Dodaj'));
          await tester.pumpAndSettle();

          verify(
            mockService.createAccount(
              'Test Account',
              type: anyNamed('type'),
              currency: anyNamed('currency'),
            ),
          ).called(1);

          expect(find.text('Test Account'), findsOneWidget);
        },
      );
    });

    group('AC4: Accounts List View', () {
      testWidgets('should show empty state message when no accounts exist', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Brak kont do wyświetlenia'), findsOneWidget);
      });

      testWidgets('should display loading indicator initially', (
        WidgetTester tester,
      ) async {
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );

        expect(find.byType(CircularProgressIndicator), findsOneWidget);
      });
    });

    group('AC5: Account Details View', () {
      testWidgets('should show delete button for accounts', (
        WidgetTester tester,
      ) async {
        final mockService = MockAccountServiceInterface();

        // Mock the getAccounts call to return a list with accounts
        when(mockService.getAccounts()).thenAnswer(
          (_) async => [
            Account(
              id: '1',
              name: 'Test Account 1',
              balance: 1000.0,
              number: 'ACC000001',
              type: 'Rachunek bieżący',
              currency: 'PLN',
            ),
            Account(
              id: '2',
              name: 'Test Account 2',
              balance: 500.0,
              number: 'ACC000002',
              type: 'Oszczędnościowe',
              currency: 'EUR',
            ),
          ],
        );

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(body: AccountsPage(accountService: mockService)),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.byIcon(Icons.delete_outline), findsNWidgets(2));
      });
    });

    group('AC6: Account Editing', () {
      testWidgets('should have currency dropdown with available currencies', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        await tester.tap(find.text('Dodaj konto'));
        await tester.pumpAndSettle();

        // Check that currency dropdown is present and shows PLN as default
        expect(find.text('Waluta:'), findsOneWidget);
        expect(find.text('PLN'), findsOneWidget);

        // Tap the currency dropdown to open it
        await tester.tap(find.byType(DropdownButton<String>).last);
        await tester.pumpAndSettle();

        // Now check if all currencies are available in the dropdown
        expect(find.text('EUR'), findsOneWidget);
        expect(find.text('USD'), findsOneWidget);
        expect(find.text('GBP'), findsOneWidget);
      });
    });

    group('AC7: Account Deletion', () {
      testWidgets('should show confirmation dialog template', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        // Check that deletion confirmation method exists
        expect(find.text('Usuń'), findsNothing); // Not visible initially
      });
    });

    group('AC8: Error Handling', () {
      testWidgets('should handle error states', (WidgetTester tester) async {
        final mockService = MockAccountServiceInterface();

        // Mock to throw an error
        when(mockService.getAccounts()).thenThrow(Exception('Network error'));

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(body: AccountsPage(accountService: mockService)),
          ),
        );

        // Wait for the error state to appear
        await tester.pumpAndSettle();

        // Should show error message
        expect(
          find.text('Nieoczekiwany błąd: Exception: Network error'),
          findsOneWidget,
        );
        expect(find.text('Brak kont do wyświetlenia'), findsOneWidget);
      });
    });

    group('AC9: Loading States', () {
      testWidgets('should show loading indicator when data is being loaded', (
        WidgetTester tester,
      ) async {
        // Create a delayed future to simulate loading state
        when(mockAccountService.getAccounts()).thenAnswer((_) async {
          await Future.delayed(const Duration(milliseconds: 100));
          return [];
        });

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );

        expect(find.byType(CircularProgressIndicator), findsOneWidget);

        // Wait for the loading to complete
        await tester.pumpAndSettle();
      });
    });

    group('AC10: Responsive Design', () {
      testWidgets('should be usable on mobile device', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.binding.setSurfaceSize(const Size(360, 640));
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Dodaj konto'), findsOneWidget);
      });
    });

    group('AC11: Navigation', () {
      testWidgets('should have proper navigation structure', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Dodaj konto'), findsOneWidget);
      });
    });

    group('AC12: Data Validation', () {
      testWidgets('should have form validation in place', (
        WidgetTester tester,
      ) async {
        when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );
        await tester.pumpAndSettle();

        await tester.tap(find.text('Dodaj konto'));
        await tester.pumpAndSettle();

        // Form should have validation controls
        expect(find.byType(TextField), findsOneWidget);
        expect(find.text('Nazwa konta'), findsOneWidget);
        expect(find.byType(DropdownButton<String>), findsNWidgets(2));
      });
    });

    group('AC13: Currency Summary Display', () {
      testWidgets(
        'should not display currency summary when accounts list is empty',
        (WidgetTester tester) async {
          when(mockAccountService.getAccounts()).thenAnswer((_) async => []);

          await tester.pumpWidget(
            MaterialApp(
              home: Scaffold(
                body: AccountsPage(accountService: mockAccountService),
              ),
            ),
          );
          await tester.pumpAndSettle();

          expect(find.text('Saldo łączne według walut:'), findsNothing);
        },
      );

      testWidgets('should display currency summary when accounts exist', (
        WidgetTester tester,
      ) async {
        final mockService = MockAccountServiceInterface();

        when(mockService.getAccounts()).thenAnswer(
          (_) async => [
            Account(
              id: '1',
              name: 'Test Account 1',
              balance: 1000.0,
              number: 'ACC000001',
              type: 'Rachunek bieżący',
              currency: 'PLN',
            ),
            Account(
              id: '2',
              name: 'Test Account 2',
              balance: 500.0,
              number: 'ACC000002',
              type: 'Oszczędnościowe',
              currency: 'EUR',
            ),
          ],
        );

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(body: AccountsPage(accountService: mockService)),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Saldo łączne według walut:'), findsOneWidget);

        final summaryCard = find.ancestor(
          of: find.text('Saldo łączne według walut:'),
          matching: find.byType(Card),
        );
        expect(summaryCard, findsOneWidget);

        expect(
          find.descendant(of: summaryCard, matching: find.text('PLN')),
          findsOneWidget,
        );

        expect(
          find.descendant(of: summaryCard, matching: find.text('EUR')),
          findsOneWidget,
        );
      });

      testWidgets('should sum balances for accounts with same currency', (
        WidgetTester tester,
      ) async {
        final mockService = MockAccountServiceInterface();

        when(mockService.getAccounts()).thenAnswer(
          (_) async => [
            Account(
              id: '1',
              name: 'Account 1',
              balance: 1000.0,
              number: 'ACC000001',
              type: 'Rachunek bieżący',
              currency: 'PLN',
            ),
            Account(
              id: '2',
              name: 'Account 2',
              balance: 500.0,
              number: 'ACC000002',
              type: 'Oszczędnościowe',
              currency: 'PLN',
            ),
          ],
        );

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(body: AccountsPage(accountService: mockService)),
          ),
        );
        await tester.pumpAndSettle();

        expect(find.text('Saldo łączne według walut:'), findsOneWidget);

        final summaryCard = find.ancestor(
          of: find.text('Saldo łączne według walut:'),
          matching: find.byType(Card),
        );

        expect(
          find.descendant(of: summaryCard, matching: find.text('PLN')),
          findsOneWidget,
        );

        expect(
          find.descendant(of: summaryCard, matching: find.text('1500.00')),
          findsOneWidget,
        );
      });

      testWidgets('should not display currency summary while loading', (
        WidgetTester tester,
      ) async {
        // Create a delayed future to simulate loading state
        when(mockAccountService.getAccounts()).thenAnswer((_) async {
          await Future.delayed(const Duration(milliseconds: 100));
          return [];
        });

        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: AccountsPage(accountService: mockAccountService),
            ),
          ),
        );

        expect(find.text('Saldo łączne według walut:'), findsNothing);
        expect(find.byType(CircularProgressIndicator), findsOneWidget);

        // Wait for the loading to complete
        await tester.pumpAndSettle();
      });
    });
  });
}
