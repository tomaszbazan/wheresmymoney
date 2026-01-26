import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('TransactionService HTTP', () {
    late FakeAuthService fakeAuthService;

    setUp(() {
      fakeAuthService = FakeAuthService();
    });

    test('getTransactions sends GET request to /transactions with page params', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/transactions');
        expect(request.url.queryParameters['page'], '0');
        expect(request.url.queryParameters['size'], '20');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');
        expect(request.headers['Content-Type'], 'application/json');

        return http.Response(
          jsonEncode({
            'transactions': [
              {
                'id': 'trans-1',
                'accountId': 'acc-1',
                'amount': 100.0,
                'type': 'INCOME',
                'description': 'Salary',
                'category': 'cat-1',
                'createdAt': '2024-01-01T00:00:00Z',
                'updatedAt': '2024-01-01T00:00:00Z',
                'transactionDate': '2024-01-01',
              },
            ],
            'page': 0,
            'size': 20,
            'totalElements': 1,
            'totalPages': 1,
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transactionPage = await transactionService.getTransactions(page: 0, size: 20);

      expect(transactionPage.transactions, hasLength(1));
      expect(transactionPage.transactions.first.id, 'trans-1');
      expect(transactionPage.transactions.first.amount, 100.0);
      expect(transactionPage.transactions.first.type, TransactionType.income);
      expect(transactionPage.page, 0);
      expect(transactionPage.size, 20);
      expect(transactionPage.totalElements, 1);
      expect(transactionPage.totalPages, 1);
    });

    test('getTransactions returns empty page', () async {
      final mockClient = MockClient((request) async {
        return http.Response(jsonEncode({'transactions': <Map<String, dynamic>>[], 'page': 0, 'size': 20, 'totalElements': 0, 'totalPages': 0}), 200);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transactionPage = await transactionService.getTransactions(page: 0, size: 20);

      expect(transactionPage.transactions, isEmpty);
      expect(transactionPage.totalElements, 0);
      expect(transactionPage.totalPages, 0);
      expect(transactionPage.hasMore, false);
    });

    test('getTransactions throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Server Error', 500);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => transactionService.getTransactions(page: 0, size: 20), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 500)));
    });

    test('getTransactionsByAccountId sends GET to correct endpoint', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/accounts/acc-123/transactions');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        return http.Response(
          jsonEncode({
            'transactions': [
              {
                'id': 'trans-1',
                'accountId': 'acc-123',
                'amount': 50.0,
                'type': 'EXPENSE',
                'description': 'Groceries',
                'category': 'cat-2',
                'createdAt': '2024-01-01T00:00:00Z',
                'updatedAt': '2024-01-01T00:00:00Z',
                'transactionDate': '2024-01-01',
              },
            ],
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transactions = await transactionService.getTransactionsByAccountId('acc-123');

      expect(transactions, hasLength(1));
      expect(transactions.first.accountId, 'acc-123');
    });

    test('createTransaction sends POST with correct body format', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/transactions');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['accountId'], 'acc-1');
        expect(body['amount']['value'], 100.0);
        expect(body['amount']['currency'], 'PLN');
        expect(body['description'], 'Test transaction');
        expect(body['type'], 'INCOME');
        expect(body['categoryId'], 'cat-1');
        expect(body['transactionDate'], isNotNull);

        return http.Response(
          jsonEncode({
            'id': 'new-trans-id',
            'accountId': 'acc-1',
            'amount': 100.0,
            'type': 'INCOME',
            'description': 'Test transaction',
            'category': 'cat-1',
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-01T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          201,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transaction = await transactionService.createTransaction(
        accountId: 'acc-1',
        amount: 100.0,
        description: 'Test transaction',
        transactionDate: DateTime(2024, 1, 1),
        type: TransactionType.income,
        categoryId: 'cat-1',
        currency: 'pln',
      );

      expect(transaction.id, 'new-trans-id');
      expect(transaction.description, 'Test transaction');
    });

    test('createTransaction converts type and currency to uppercase', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['type'], 'EXPENSE');
        expect(body['amount']['currency'], 'USD');

        return http.Response(
          jsonEncode({
            'id': 'trans-id',
            'accountId': 'acc-1',
            'amount': 50.0,
            'type': 'EXPENSE',
            'description': 'Coffee',
            'category': 'cat-1',
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-01T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          201,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      await transactionService.createTransaction(
        accountId: 'acc-1',
        amount: 50.0,
        description: 'Coffee',
        transactionDate: DateTime(2024, 1, 1),
        type: TransactionType.expense,
        categoryId: 'cat-1',
        currency: 'usd',
      );
    });

    test('createTransaction throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Validation Error', 422);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      expect(
        () => transactionService.createTransaction(
          accountId: 'acc-1',
          amount: 100.0,
          description: 'Test',
          transactionDate: DateTime.now(),
          type: TransactionType.income,
          categoryId: 'cat-1',
          currency: 'pln',
        ),
        throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 422)),
      );
    });

    test('updateTransaction sends PUT with correct endpoint and body', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'PUT');
        expect(request.url.path, '/api/transactions/trans-123');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['amount']['value'], 150.0);
        expect(body['amount']['currency'], 'EUR');
        expect(body['description'], 'Updated description');
        expect(body['categoryId'], 'cat-2');

        return http.Response(
          jsonEncode({
            'id': 'trans-123',
            'accountId': 'acc-1',
            'amount': 150.0,
            'type': 'INCOME',
            'description': 'Updated description',
            'category': 'cat-2',
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-02T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transaction = await transactionService.updateTransaction(id: 'trans-123', amount: 150.0, description: 'Updated description', categoryId: 'cat-2', currency: 'eur');

      expect(transaction.id, 'trans-123');
      expect(transaction.description, 'Updated description');
    });

    test('updateTransaction converts currency to uppercase', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['amount']['currency'], 'GBP');

        return http.Response(
          jsonEncode({
            'id': 'trans-id',
            'accountId': 'acc-1',
            'amount': 100.0,
            'type': 'INCOME',
            'description': 'Test',
            'category': 'cat-1',
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-01T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      await transactionService.updateTransaction(id: 'trans-id', amount: 100.0, description: 'Test', categoryId: 'cat-1', currency: 'gbp');
    });

    test('updateTransaction throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Not Found', 404);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      expect(
        () => transactionService.updateTransaction(id: 'non-existent', amount: 100.0, description: 'Test', categoryId: 'cat-1', currency: 'pln'),
        throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });

    test('deleteTransaction sends DELETE to correct endpoint', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'DELETE');
        expect(request.url.path, '/api/transactions/trans-to-delete');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        return http.Response('', 204);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      await transactionService.deleteTransaction('trans-to-delete');
    });

    test('deleteTransaction accepts 200 status code', () async {
      final mockClient = MockClient((request) async {
        return http.Response('', 200);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      await transactionService.deleteTransaction('any-id');
    });

    test('deleteTransaction throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Forbidden', 403);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => transactionService.deleteTransaction('forbidden-id'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 403)));
    });
  });
}
