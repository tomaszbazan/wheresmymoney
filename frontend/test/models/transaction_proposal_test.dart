import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';

void main() {
  group('TransactionProposal', () {
    group('isSuggestedByAi', () {
      test('should be true when categoryId is present in JSON', () {
        final json = {
          'transactionDate': '2025-01-15T10:00:00.000Z',
          'description': 'Test transaction',
          'amount': 100.0,
          'currency': 'PLN',
          'type': 'EXPENSE',
          'categoryId': 'category-123',
        };

        final proposal = TransactionProposal.fromJson(json);

        expect(proposal.isSuggestedByAi, isTrue);
        expect(proposal.categoryId, equals('category-123'));
      });

      test('should be false when categoryId is null in JSON', () {
        final json = {'transactionDate': '2025-01-15T10:00:00.000Z', 'description': 'Test transaction', 'amount': 100.0, 'currency': 'PLN', 'type': 'EXPENSE'};

        final proposal = TransactionProposal.fromJson(json);

        expect(proposal.isSuggestedByAi, isFalse);
        expect(proposal.categoryId, isNull);
      });

      test('should be false when categoryId is explicitly null in JSON', () {
        final json = {'transactionDate': '2025-01-15T10:00:00.000Z', 'description': 'Test transaction', 'amount': 100.0, 'currency': 'PLN', 'type': 'EXPENSE', 'categoryId': null};

        final proposal = TransactionProposal.fromJson(json);

        expect(proposal.isSuggestedByAi, isFalse);
        expect(proposal.categoryId, isNull);
      });

      test('should persist after category change via copyWith', () {
        final original = TransactionProposal(
          transactionDate: DateTime(2025, 1, 15),
          description: 'Test transaction',
          amount: 100.0,
          currency: 'PLN',
          type: TransactionType.expense,
          categoryId: 'category-123',
          isSuggestedByAi: true,
        );

        final updated = original.copyWith(categoryId: 'new-category-456');

        expect(updated.isSuggestedByAi, isTrue);
        expect(updated.categoryId, equals('new-category-456'));
      });

      test('should be preserved in toJson', () {
        final proposal = TransactionProposal(
          transactionDate: DateTime(2025, 1, 15),
          description: 'Test transaction',
          amount: 100.0,
          currency: 'PLN',
          type: TransactionType.expense,
          categoryId: 'category-123',
          isSuggestedByAi: true,
        );

        final json = proposal.toJson();

        expect(json['categoryId'], equals('category-123'));
      });

      test('should handle income type correctly', () {
        final json = {
          'transactionDate': '2025-01-15T10:00:00.000Z',
          'description': 'Salary',
          'amount': 5000.0,
          'currency': 'PLN',
          'type': 'INCOME',
          'categoryId': 'income-category-1',
        };

        final proposal = TransactionProposal.fromJson(json);

        expect(proposal.isSuggestedByAi, isTrue);
        expect(proposal.type, equals(TransactionType.income));
        expect(proposal.categoryId, equals('income-category-1'));
      });
    });
  });
}
