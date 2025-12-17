import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/services/account_service.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('RestAccountService HTTP', () {
    late FakeAuthService fakeAuthService;

    setUp(() {
      fakeAuthService = FakeAuthService();
    });

    test('getAccounts sends GET request to /accounts', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/accounts');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');
        expect(request.headers['Content-Type'], 'application/json');

        final responseBody = jsonEncode({
          'accounts': [
            {
              'id': '5d98513c-d224-43d9-a521-960af3ce9b46',
              'name': 'Test Account',
              'balance': 1000.0,
              'currency': 'PLN',
              'type': 'Checking',
              'createdAt': '2024-01-01T00:00:00Z',
              'updatedAt': '2024-01-01T00:00:00Z',
            },
          ],
        });

        return http.Response(responseBody, 200);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      final accounts = await accountService.getAccounts();

      expect(accounts, hasLength(1));
      expect(accounts.first.id, '5d98513c-d224-43d9-a521-960af3ce9b46');
      expect(accounts.first.name, 'Test Account');
      expect(accounts.first.balance, 1000.0);
      expect(accounts.first.currency, 'PLN');
      expect(accounts.first.type, 'Checking');
      expect(accounts.first.createdAt, DateTime.parse('2024-01-01T00:00:00Z'));
      expect(accounts.first.updatedAt, DateTime.parse('2024-01-01T00:00:00Z'));
    });

    test('getAccounts returns empty list when no accounts key', () async {
      final mockClient = MockClient((request) async {
        return http.Response(jsonEncode({}), 200);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      final accounts = await accountService.getAccounts();

      expect(accounts, isEmpty);
    });

    test('getAccounts throws HttpException on error status', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Unauthorized', 401);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => accountService.getAccounts(), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 401)));
    });

    test('createAccount sends POST request with correct body', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/accounts');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['name'], 'New Account');
        expect(body['type'], 'Savings');
        expect(body['currency'], 'EUR');

        return http.Response(
          jsonEncode({
            'id': '5d98513c-d224-43d9-a521-960af3ce9b46',
            'name': 'New Account',
            'balance': 0.0,
            'currency': 'EUR',
            'type': 'Savings',
            'createdAt': '2024-01-01T00:00:00Z',
            'updatedAt': '2024-01-01T00:00:00Z',
          }),
          201,
        );
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      final account = await accountService.createAccount('New Account', type: 'Savings', currency: 'EUR');

      expect(account.name, 'New Account');
      expect(account.type, 'Savings');
      expect(account.currency, 'EUR');
    });

    test('createAccount sends only name when no optional params', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body.keys, {'name'});
        expect(body['name'], 'Simple Account');

        final responseBody = jsonEncode({
          'id': '5d98513c-d224-43d9-a521-960af3ce9b46',
          'name': 'Simple Account',
          'balance': 0.0,
          'currency': 'PLN',
          'type': 'Checking',
          'createdAt': '2024-01-01T00:00:00Z',
          'updatedAt': '2024-01-01T00:00:00Z',
        });

        return http.Response(responseBody, 201);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      await accountService.createAccount('Simple Account');
    });

    test('createAccount throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Bad Request', 400);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => accountService.createAccount('Test'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 400)));
    });

    test('deleteAccount sends DELETE request to correct endpoint', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'DELETE');
        expect(request.url.path, '/api/accounts/account-to-delete');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        return http.Response('', 204);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      await accountService.deleteAccount('account-to-delete');
    });

    test('deleteAccount accepts 200 status code', () async {
      final mockClient = MockClient((request) async {
        return http.Response('', 200);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      await accountService.deleteAccount('any-id');
    });

    test('deleteAccount throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Not Found', 404);
      });

      final accountService = RestAccountService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => accountService.deleteAccount('non-existent'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 404)));
    });
  });
}
