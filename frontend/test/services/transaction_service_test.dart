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
                'amount': {'value': 100.0, 'currency': 'PLN'},
                'type': 'INCOME',
                'bill': {
                  'items': [
                    {
                      'category': {'id': 'cat-1', 'name': 'Salary'},
                      'amount': {'value': 100.0, 'currency': 'PLN'},
                      'description': 'Salary',
                    },
                  ],
                },
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
      expect(transactionPage.transactions.first.amount.value, 100.0);
      expect(transactionPage.transactions.first.amount.currency, 'PLN');
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

    test('createTransaction sends POST with correct body format', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/transactions');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['accountId'], 'acc-1');
        expect(body['type'], 'INCOME');
        expect(body['transactionDate'], isNotNull);
        expect(body['bill']['billItems'], hasLength(1));
        expect(body['bill']['billItems'][0]['categoryId'], 'cat-1');
        expect(body['bill']['billItems'][0]['amount']['value'], 100.0);
        expect(body['bill']['billItems'][0]['amount']['currency'], 'PLN');
        expect(body['bill']['billItems'][0]['description'], 'Test transaction');

        return http.Response(
          jsonEncode({
            'id': 'new-trans-id',
            'accountId': 'acc-1',
            'amount': {'value': 100.0, 'currency': 'PLN'},
            'type': 'INCOME',
            'bill': {
              'items': [
                {
                  'category': {'id': 'cat-1', 'name': 'Category'},
                  'amount': {'value': 100.0, 'currency': 'PLN'},
                  'description': 'Test transaction',
                },
              ],
            },
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
        transactionDate: DateTime(2024, 1, 1),
        type: TransactionType.income,
        billItems: [
          {'categoryId': 'cat-1', 'amount': 100.0, 'description': 'Test transaction'},
        ],
        currency: 'pln',
      );

      expect(transaction.id, 'new-trans-id');
      expect(transaction.description, 'Test transaction');
    });

    test('createTransaction converts type and currency to uppercase', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['type'], 'EXPENSE');
        expect(body['bill']['billItems'][0]['amount']['currency'], 'USD');

        return http.Response(
          jsonEncode({
            'id': 'trans-id',
            'accountId': 'acc-1',
            'amount': {'value': 50.0, 'currency': 'USD'},
            'type': 'EXPENSE',
            'bill': {
              'items': [
                {
                  'category': {'id': 'cat-1', 'name': 'Category'},
                  'amount': {'value': 50.0, 'currency': 'USD'},
                  'description': 'Coffee',
                },
              ],
            },
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
        transactionDate: DateTime(2024, 1, 1),
        type: TransactionType.expense,
        billItems: [
          {'categoryId': 'cat-1', 'amount': 50.0, 'description': 'Coffee'},
        ],
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
          transactionDate: DateTime.now(),
          type: TransactionType.income,
          billItems: [
            {'categoryId': 'cat-1', 'amount': 100.0, 'description': 'Test'},
          ],
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
        expect(body['bill']['billItems'], hasLength(1));
        expect(body['bill']['billItems'][0]['amount']['value'], 150.0);
        expect(body['bill']['billItems'][0]['amount']['currency'], 'EUR');
        expect(body['bill']['billItems'][0]['description'], 'Updated description');
        expect(body['bill']['billItems'][0]['categoryId'], 'cat-2');

        return http.Response(
          jsonEncode({
            'id': 'trans-123',
            'accountId': 'acc-1',
            'amount': {'value': 150.0, 'currency': 'EUR'},
            'type': 'INCOME',
            'bill': {
              'items': [
                {
                  'category': {'id': 'cat-2', 'name': 'Category'},
                  'amount': {'value': 150.0, 'currency': 'EUR'},
                  'description': 'Updated description',
                },
              ],
            },
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-02T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      final transaction = await transactionService.updateTransaction(
        id: 'trans-123',
        billItems: [
          {'categoryId': 'cat-2', 'amount': 150.0, 'description': 'Updated description'},
        ],
        currency: 'eur',
      );

      expect(transaction.id, 'trans-123');
      expect(transaction.description, 'Updated description');
    });

    test('updateTransaction converts currency to uppercase', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['bill']['billItems'][0]['amount']['currency'], 'GBP');

        return http.Response(
          jsonEncode({
            'id': 'trans-id',
            'accountId': 'acc-1',
            'amount': {'value': 100.0, 'currency': 'GBP'},
            'type': 'INCOME',
            'bill': {
              'items': [
                {
                  'category': {'id': 'cat-1', 'name': 'Category'},
                  'amount': {'value': 100.0, 'currency': 'GBP'},
                  'description': 'Test',
                },
              ],
            },
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-01T00:00:00Z',
            'transactionDate': '2024-01-01',
          }),
          200,
        );
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      await transactionService.updateTransaction(
        id: 'trans-id',
        billItems: [
          {'categoryId': 'cat-1', 'amount': 100.0, 'description': 'Test'},
        ],
        currency: 'gbp',
      );
    });

    test('updateTransaction throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Not Found', 404);
      });

      final transactionService = RestTransactionService(authService: fakeAuthService, httpClient: mockClient);

      expect(
        () => transactionService.updateTransaction(
          id: 'non-existent',
          billItems: [
            {'categoryId': 'cat-1', 'amount': 100.0, 'description': 'Test'},
          ],
          currency: 'pln',
        ),
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
