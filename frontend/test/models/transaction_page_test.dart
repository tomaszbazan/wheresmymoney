import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/transaction_page.dart';

void main() {
  group('TransactionPage', () {
    test('should parse from JSON correctly', () {
      // Given
      final json = {
        'transactions': [
          {
            'id': '123e4567-e89b-12d3-a456-426614174000',
            'accountId': '123e4567-e89b-12d3-a456-426614174001',
            'amount': 100.50,
            'type': 'INCOME',
            'description': 'Test transaction',
            'category': {'id': '123e4567-e89b-12d3-a456-426614174002', 'name': 'Salary'},
            'transactionDate': '2024-01-15',
            'createdAt': '2024-01-15T10:00:00Z',
            'updatedAt': '2024-01-15T10:00:00Z',
          },
        ],
        'page': 0,
        'size': 20,
        'totalElements': 1,
        'totalPages': 1,
      };

      // When
      final transactionPage = TransactionPage.fromJson(json);

      // Then
      expect(transactionPage.transactions.length, 1);
      expect(transactionPage.page, 0);
      expect(transactionPage.size, 20);
      expect(transactionPage.totalElements, 1);
      expect(transactionPage.totalPages, 1);
      expect(transactionPage.transactions[0].description, 'Test transaction');
    });

    test('hasMore returns true when more pages exist', () {
      // Given
      final transactionPage = TransactionPage(transactions: [], page: 0, size: 20, totalElements: 50, totalPages: 3);

      // When & Then
      expect(transactionPage.hasMore, true);
    });

    test('hasMore returns false on last page', () {
      // Given
      final transactionPage = TransactionPage(transactions: [], page: 2, size: 20, totalElements: 50, totalPages: 3);

      // When & Then
      expect(transactionPage.hasMore, false);
    });

    test('hasMore returns false when no pages', () {
      // Given
      final transactionPage = TransactionPage(transactions: [], page: 0, size: 20, totalElements: 0, totalPages: 0);

      // When & Then
      expect(transactionPage.hasMore, false);
    });
  });
}
