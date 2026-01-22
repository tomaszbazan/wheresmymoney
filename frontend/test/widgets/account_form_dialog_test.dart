import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/widgets/account_form_dialog.dart';

void main() {
  group('AccountFormDialog', () {
    testWidgets('displays form with all fields', (tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: AccountFormDialog(onSave: (_, __, ___) {}))));

      expect(find.text('Dodaj konto'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
      expect(find.text('Typ konta:'), findsOneWidget);
      expect(find.text('Waluta:'), findsOneWidget);
      expect(find.text('Anuluj'), findsOneWidget);
      expect(find.text('Dodaj'), findsOneWidget);
    });

    testWidgets('calls onSave with correct values when Dodaj pressed', (tester) async {
      String? savedName;
      String? savedType;
      String? savedCurrency;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AccountFormDialog(
              onSave: (name, type, currency) {
                savedName = name;
                savedType = type;
                savedCurrency = currency;
              },
            ),
          ),
        ),
      );

      await tester.enterText(find.byType(TextField), 'Test Account');
      await tester.tap(find.text('Dodaj'));
      await tester.pumpAndSettle();

      expect(savedName, 'Test Account');
      expect(savedType, 'Rachunek bieżący');
      expect(savedCurrency, 'PLN');
    });

    testWidgets('does not call onSave when account name is empty', (tester) async {
      var saveCalled = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AccountFormDialog(
              onSave: (_, __, ___) {
                saveCalled = true;
              },
            ),
          ),
        ),
      );

      await tester.tap(find.text('Dodaj'));
      await tester.pumpAndSettle();

      expect(saveCalled, false);
    });

    testWidgets('allows changing account type', (tester) async {
      String? savedType;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AccountFormDialog(
              onSave: (_, type, __) {
                savedType = type;
              },
            ),
          ),
        ),
      );

      await tester.enterText(find.byType(TextField), 'Test Account');

      await tester.tap(find.text('Rachunek bieżący'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Gotówka').last);
      await tester.pumpAndSettle();

      await tester.tap(find.text('Dodaj'));
      await tester.pumpAndSettle();

      expect(savedType, 'Gotówka');
    });

    testWidgets('allows changing currency', (tester) async {
      String? savedCurrency;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: AccountFormDialog(
              onSave: (_, __, currency) {
                savedCurrency = currency;
              },
            ),
          ),
        ),
      );

      await tester.enterText(find.byType(TextField), 'Test Account');

      await tester.tap(find.text('PLN'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('USD').last);
      await tester.pumpAndSettle();

      await tester.tap(find.text('Dodaj'));
      await tester.pumpAndSettle();

      expect(savedCurrency, 'USD');
    });
  });
}
