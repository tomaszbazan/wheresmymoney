import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:frontend/services/transaction_staging_service.dart';

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

    group('saveAll', () {
      test('saves all proposals via transaction service', () async {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense, categoryId: 'category-1'),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        final mockService = MockTransactionService();
        service.loadFromCsv(result);

        await service.saveAll('account-1', mockService);

        expect(mockService.createdTransactions.length, 1);
        expect(service.proposals.length, 0);
      });

      test('clears staging after successful save', () async {
        final proposals = [
          TransactionProposal(transactionDate: DateTime(2024, 1, 1), description: 'Test', amount: 100.0, currency: 'PLN', type: TransactionType.expense, categoryId: 'category-1'),
        ];

        final result = CsvParseResult(totalRows: 1, successCount: 1, errorCount: 0, proposals: proposals, errors: []);

        final mockService = MockTransactionService();
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

        final mockService = MockTransactionService(shouldFail: true);
        service.loadFromCsv(result);

        expect(() => service.saveAll('account-1', mockService), throwsException);
      });
    });
  });
}

class MockTransactionService implements TransactionServiceInterface {
  final List<Transaction> createdTransactions = [];
  final bool shouldFail;

  MockTransactionService({this.shouldFail = false});

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime date,
    required TransactionType type,
    required String categoryId,
    required String currency,
  }) async {
    if (shouldFail) {
      throw Exception('Failed to create transaction');
    }

    final transaction = Transaction(
      id: 'test-id',
      accountId: accountId,
      amount: amount,
      description: description,
      type: type,
      categoryId: categoryId,
      categoryName: null,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );

    createdTransactions.add(transaction);
    return transaction;
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {}

  @override
  Future<List<Transaction>> getTransactions() async {
    return [];
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    return [];
  }

  @override
  Future<Transaction> updateTransaction({required String id, required double amount, required String description, required String categoryId, required String currency}) async {
    throw UnimplementedError();
  }
}
