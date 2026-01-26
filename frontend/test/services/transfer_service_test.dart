import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/services/transfer_service.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('RestTransferService HTTP', () {
    late FakeAuthService fakeAuthService;

    setUp(() {
      fakeAuthService = FakeAuthService();
    });

    test('getTransfers sends GET request to /transfers', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/transfers');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');
        expect(request.headers['Content-Type'], 'application/json');

        final responseBody = jsonEncode({
          'transfers': [
            {
              'id': 'transfer-1',
              'sourceAccountId': 'account-1',
              'targetAccountId': 'account-2',
              'sourceAmount': 100.0,
              'sourceCurrency': 'PLN',
              'targetAmount': 100.0,
              'targetCurrency': 'PLN',
              'exchangeRate': 1.0,
              'description': 'Test transfer',
              'createdAt': '2024-01-01T00:00:00Z',
            },
          ],
        });

        return http.Response(responseBody, 200);
      });

      final transferService = RestTransferService(authService: fakeAuthService, httpClient: mockClient);

      final transfers = await transferService.getTransfers();

      expect(transfers, hasLength(1));
      expect(transfers.first.id, 'transfer-1');
      expect(transfers.first.sourceAccountId, 'account-1');
      expect(transfers.first.sourceAmount, 100.0);
    });

    test('createTransfer sends POST request with correct body for same currency', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/transfers');

        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['sourceAccountId'], 'account-1');
        expect(body['targetAccountId'], 'account-2');
        expect(body['sourceAmount'], 100.0);
        expect(body.containsKey('targetAmount'), false);
        expect(body['description'], 'Description');

        return http.Response(
          jsonEncode({
            'id': 'transfer-1',
            'sourceAccountId': 'account-1',
            'targetAccountId': 'account-2',
            'sourceAmount': 100.0,
            'sourceCurrency': 'PLN',
            'targetAmount': 100.0,
            'targetCurrency': 'PLN',
            'exchangeRate': 1.0,
            'description': 'Description',
            'createdAt': '2024-01-01T00:00:00Z',
          }),
          201,
        );
      });

      final transferService = RestTransferService(authService: fakeAuthService, httpClient: mockClient);

      await transferService.createTransfer(sourceAccountId: 'account-1', targetAccountId: 'account-2', sourceAmount: 100.0, description: 'Description');
    });

    test('createTransfer sends correct body for different currencies', () async {
      final mockClient = MockClient((request) async {
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['sourceAmount'], 100.0);
        expect(body['targetAmount'], 85.0);

        return http.Response(
          jsonEncode({
            'id': 'transfer-2',
            'sourceAccountId': 'account-1',
            'targetAccountId': 'account-3',
            'sourceAmount': 100.0,
            'sourceCurrency': 'PLN',
            'targetAmount': 85.0,
            'targetCurrency': 'EUR',
            'exchangeRate': 0.85,
            'createdAt': '2024-01-01T00:00:00Z',
          }),
          201,
        );
      });

      final transferService = RestTransferService(authService: fakeAuthService, httpClient: mockClient);

      await transferService.createTransfer(sourceAccountId: 'account-1', targetAccountId: 'account-3', sourceAmount: 100.0, targetAmount: 85.0);
    });

    test('createTransfer throws HttpException on error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Bad Request', 400);
      });

      final transferService = RestTransferService(authService: fakeAuthService, httpClient: mockClient);

      expect(
        () => transferService.createTransfer(sourceAccountId: 'acc1', targetAccountId: 'acc2', sourceAmount: 100),
        throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });
}
