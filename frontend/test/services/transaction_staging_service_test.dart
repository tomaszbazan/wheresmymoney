import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_staging_service.dart';

import '../mocks/in_memory_transaction_service.dart';

void main() {
  late TransactionStagingService service;

  setUp(() {
    service = TransactionStagingService();
  });

  group('TransactionStagingService', () {
    group('loadFromCsv', () {
      test('loads proposals from CSV result', () {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test transaction', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        expect(service.proposals.length, 1);
        expect(service.proposals.first.description, 'Test transaction');
      });

      test('replaces existing proposals', () {
        final firstProposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'First', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final firstResult = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: firstProposals, errors: []);

        service.loadFromCsv(firstResult);
        expect(service.proposals.length, 1);

        final secondProposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 2), description: 'Second', amount: 200.0, currency: 'PLN', type: TransactionType.expense)];

        final secondResult = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: secondProposals, errors: []);

        service.loadFromCsv(secondResult);
        expect(service.proposals.length, 1);
        expect(service.proposals.first.description, 'Second');
      });
    });

    group('updateCategory', () {
      test('updates category for specific proposal', () {
        final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);
        service.updateCategory(0, 'category-123');

        expect(service.proposals.first.categoryId, 'category-123');
      });

      test('preserves isSuggestedByAi flag when updating category', () {
        final proposals = [
          TransactionProposal(
            transactionDate: DateTime(2024, 1, 1),
            description: 'Test',
            amount: 100.0,
            currency: 'PLN',
            type: TransactionType.expense,
            categoryId: 'ai-suggested-category',
            isSuggestedByAi: true,
          ),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);
        service.updateCategory(0, 'manually-selected-category');

        expect(service.proposals.first.categoryId, 'manually-selected-category');
        expect(service.proposals.first.isSuggestedByAi, true);
      });

      test('throws when index out of range', () {
        expect(() => service.updateCategory(0, 'category-123'), throwsRangeError);
      });
    });

    group('removeTransaction', () {
      test('removes transaction at specific index', () {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'First', amount: 100.0, currency: 'PLN', type: TransactionType.expense),
          TransactionProposal(transactionDate: DateTime(2024, 1, 2), description: 'Second', amount: 200.0, currency: 'PLN', type: TransactionType.expense),
        ];

        final result = CsvParseResult(totalRows: 2, successCount: 2, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);
        service.removeTransaction(0);

        expect(service.proposals.length, 1);
        expect(service.proposals.first.description, 'Second');
      });

      test('throws when index out of range', () {
        expect(() => service.removeTransaction(0), throwsRangeError);
      });
    });

    group('clear', () {
      test('clears all proposals', () {
        final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);
        expect(service.proposals.length, 1);

        service.clear();
        expect(service.proposals.length, 0);
      });
    });

    group('notifications', () {
      test('notifies listeners on loadFromCsv', () {
        var notified = false;
        service.addListener(() {
          notified = true;
        });

        final result = CsvParseResult(totalRows: 0, successCount: 0, errorCount: 0, proposals: [], errors: []);

        service.loadFromCsv(result);
        expect(notified, true);
      });

      test('notifies listeners on updateCategory', () {
        final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        var notified = false;
        service.addListener(() {
          notified = true;
        });

        service.updateCategory(0, 'category-123');
        expect(notified, true);
      });

      test('notifies listeners on removeTransaction', () {
        final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        var notified = false;
        service.addListener(() {
          notified = true;
        });

        service.removeTransaction(0);
        expect(notified, true);
      });

      test('notifies listeners on clear', () {
        final proposals = [TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense)];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        var notified = false;
        service.addListener(() {
          notified = true;
        });

        service.clear();
        expect(notified, true);
      });
    });

    group('hasIncompleteCategorization', () {
      test('returns true when any proposal has null categoryId', () {
        final proposals = [
          TransactionProposal(
            transactionDate: DateTime(2024, 1, 1),
            description: 'Test 1',
            amount: 100.0,
            currency: 'PLN',
            type: TransactionType.expense,
            categoryId: 'category-1',
          ),
          TransactionProposal(transactionDate: DateTime(2024, 1, 2), description: 'Test 2', amount: 200.0, currency: 'PLN', type: TransactionType.expense),
        ];

        final result = CsvParseResult(totalRows: 2, successCount: 2, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        expect(service.hasIncompleteCategorization(), true);
      });

      test('returns false when all proposals have categoryId', () {
        final proposals = [
          TransactionProposal(
            transactionDate: DateTime(2024, 1, 1),
            description: 'Test 1',
            amount: 100.0,
            currency: 'PLN',
            type: TransactionType.expense,
            categoryId: 'category-1',
          ),
          TransactionProposal(
            transactionDate: DateTime(2024, 1, 2),
            description: 'Test 2',
            amount: 200.0,
            currency: 'PLN',
            type: TransactionType.expense,
            categoryId: 'category-2',
          ),
        ];

        final result = CsvParseResult(totalRows: 2, successCount: 2, errorCount: 0, proposals: proposals, errors: []);

        service.loadFromCsv(result);

        expect(service.hasIncompleteCategorization(), false);
      });

      test('returns false when proposals list is empty', () {
        expect(service.hasIncompleteCategorization(), false);
      });
    });

    group('saveAll', () {
      test('saves all proposals via transaction service', () async {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense, categoryId: 'category-1'),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        final mockService = InMemoryTransactionService();
        service.loadFromCsv(result);

        await service.saveAll('account-1', mockService);

        final transactions = await mockService.getTransactions(page: 0, size: 10);

        expect(transactions.totalElements, 1);
        expect(service.proposals.length, 0);
      });

      test('clears staging after successful save', () async {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense, categoryId: 'category-1'),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        final mockService = InMemoryTransactionService();
        service.loadFromCsv(result);
        expect(service.proposals.length, 1);

        await service.saveAll('account-1', mockService);

        expect(service.proposals.length, 0);
      });

      test('handles save errors gracefully', () async {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense, categoryId: 'category-1'),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        final mockService = InMemoryTransactionService();
        mockService.setApiError(Exception('Failed to create transaction'));
        service.loadFromCsv(result);

        expect(() => service.saveAll('account-1', mockService), throwsException);
      });
    });
  });
}
