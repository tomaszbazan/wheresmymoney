import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/services/csv_import_service.dart';
import 'package:frontend/widgets/csv_upload_dialog.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('CsvUploadDialog', () {
    late CsvImportService csvImportService;
    late List<Account> accounts;

    setUp(() {
      csvImportService = CsvImportService(authService: FakeAuthService());
      accounts = [
        Account(id: '1', name: 'Test Account 1', balance: 1000.0, currency: 'PLN', type: 'Checking', createdAt: DateTime.now(), updatedAt: DateTime.now()),
        Account(id: '2', name: 'Test Account 2', balance: 500.0, currency: 'EUR', type: 'Savings', createdAt: DateTime.now(), updatedAt: DateTime.now()),
      ];
    });

    testWidgets('should render initial state correctly', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CsvUploadDialog(csvImportService: csvImportService, accounts: accounts))));

      expect(find.text('Importuj transakcje z CSV'), findsOneWidget);
      expect(find.text('Wybierz plik CSV'), findsOneWidget);
      expect(find.byType(DropdownButtonFormField<String>), findsOneWidget);
      expect(find.text('Anuluj'), findsOneWidget);
      expect(find.text('Prześlij'), findsOneWidget);

      final uploadButton = tester.widget<ElevatedButton>(find.widgetWithText(ElevatedButton, 'Prześlij'));
      expect(uploadButton.onPressed, isNull);

      expect(find.byType(LinearProgressIndicator), findsNothing);
      expect(find.text('Błędy'), findsNothing);
    });

    testWidgets('should populate account dropdown with accounts', (WidgetTester tester) async {
      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CsvUploadDialog(csvImportService: csvImportService, accounts: accounts))));

      await tester.tap(find.byType(DropdownButtonFormField<String>));
      await tester.pumpAndSettle();

      expect(find.text('Test Account 1').hitTestable(), findsOneWidget);
      expect(find.text('Test Account 2').hitTestable(), findsOneWidget);
    });
  });
}
