import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_staging_service.dart';

void main() {
  group('TransactionStagingList integration', () {
    test('staging service integrates with proposals', () {
      final stagingService = TransactionStagingService();
      final proposals = [
        TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'Test transaction', amount: 100.50, currency: 'PLN', type: TransactionType.expense),
      ];

      stagingService.loadFromCsv(_createCsvResult(proposals));

      expect(stagingService.proposals.length, 1);
      expect(stagingService.proposals.first.description, 'Test transaction');
      expect(stagingService.proposals.first.amount, 100.50);
    });

    test('staging service allows removing proposals', () {
      final stagingService = TransactionStagingService();
      final proposals = [
        TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'First', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
        TransactionProposal(transactionDate: DateTime(2024, 1, 16), description: 'Second', amount: 200.0, currency: 'PLN', type: TransactionType.expense),
      ];

      stagingService.loadFromCsv(_createCsvResult(proposals));
      expect(stagingService.proposals.length, 2);

      stagingService.removeTransaction(0);
      expect(stagingService.proposals.length, 1);
      expect(stagingService.proposals.first.description, 'Second');
    });

    test('staging service handles empty proposals', () {
      final stagingService = TransactionStagingService();

      expect(stagingService.proposals.length, 0);
    });

    test('staging service allows updating categories', () {
      final stagingService = TransactionStagingService();
      final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

      stagingService.loadFromCsv(_createCsvResult(proposals));
      expect(stagingService.proposals.first.categoryId, null);

      stagingService.updateCategory(0, 'category-123');
      expect(stagingService.proposals.first.categoryId, 'category-123');
    });

    test('staging service tracks multiple proposals', () {
      final stagingService = TransactionStagingService();
      final proposals = [
        TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'Test 1', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
        TransactionProposal(transactionDate: DateTime(2024, 1, 16), description: 'Test 2', amount: 200.0, currency: 'PLN', type: TransactionType.expense),
      ];

      stagingService.loadFromCsv(_createCsvResult(proposals));

      expect(stagingService.proposals.length, 2);
      expect(stagingService.proposals[0].description, 'Test 1');
      expect(stagingService.proposals[1].description, 'Test 2');
    });
  });
}

CsvParseResult _createCsvResult(List<TransactionProposal> proposals) {
  return CsvParseResult(totalRows: proposals.length, successCount: proposals.length, errorCount: 0, proposals: proposals, errors: []);
}
