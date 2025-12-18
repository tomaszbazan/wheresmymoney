import 'package:alchemist/alchemist.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/services/csv_import_service.dart';
import 'package:frontend/widgets/csv_upload_dialog.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('CsvUploadDialog Golden Tests', () {
    final mockAccounts = [Account(id: 'account-1', name: 'Konto główne', balance: 1000.0), Account(id: 'account-2', name: 'Oszczędności', balance: 5000.0)];

    goldenTest(
      'renders complete dialog',
      fileName: 'csv_upload_dialog_complete',
      builder: () {
        return GoldenTestGroup(
          children: [GoldenTestScenario(name: 'initial state', child: CsvUploadDialog(csvImportService: CsvImportService(authService: FakeAuthService()), accounts: mockAccounts))],
        );
      },
    );
  });
}
