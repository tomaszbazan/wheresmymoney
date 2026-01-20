import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/widgets/transaction_staging_list.dart';

import '../mocks/in_memory_category_service.dart';
import '../test_setup.dart';

void main() {
  group('TransactionStagingList integration', () {
    setUpAll(() async {
      await TestSetup.initializeSupabase();
    });

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

    testWidgets('should fetch categories only once for multiple transactions', (WidgetTester tester) async {
      final categoryService = InMemoryCategoryService();
      await categoryService.addCategory('Food', type: CategoryType.expense);
      await categoryService.addCategory('Transport', type: CategoryType.expense);

      final stagingService = TransactionStagingService();
      final proposals = [
        TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'Transaction 1', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
        TransactionProposal(transactionDate: DateTime(2024, 1, 16), description: 'Transaction 2', amount: 200.0, currency: 'PLN', type: TransactionType.expense),
        TransactionProposal(transactionDate: DateTime(2024, 1, 17), description: 'Transaction 3', amount: 300.0, currency: 'PLN', type: TransactionType.expense),
      ];

      stagingService.loadFromCsv(_createCsvResult(proposals));
      categoryService.resetCallCount();

      await tester.runAsync(() async {
        await tester.pumpWidget(
          MaterialApp(home: Scaffold(body: TransactionStagingList(stagingService: stagingService, accountId: 'test-account', categoryService: categoryService))),
        );

        await tester.pumpAndSettle();

        expect(categoryService.getCategoriesByTypeCallCount, equals(1), reason: 'Categories should be fetched only once for all transactions, not once per transaction');
      });
    });

    testWidgets('should show error message when loading categories fails', (WidgetTester tester) async {
      final categoryService = InMemoryCategoryService();
      categoryService.setApiError(Exception('Failed to load categories'));

      final stagingService = TransactionStagingService();
      final proposals = [
        TransactionProposal(transactionDate: DateTime(2024, 1, 15), description: 'Test transaction', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
      ];

      stagingService.loadFromCsv(_createCsvResult(proposals));

      await tester.runAsync(() async {
        await tester.pumpWidget(
          MaterialApp(home: Scaffold(body: TransactionStagingList(stagingService: stagingService, accountId: 'test-account', categoryService: categoryService))),
        );

        await tester.pumpAndSettle();

        expect(find.text('Nie udało się załadować kategorii'), findsOneWidget);
      });
    });
  });
}

CsvParseResult _createCsvResult(List<TransactionProposal> proposals) {
  return CsvParseResult(totalRows: proposals.length, successCount: proposals.length, errorCount: 0, proposals: proposals, errors: []);
}
