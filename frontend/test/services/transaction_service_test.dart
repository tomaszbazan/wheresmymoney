import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:http/http.dart' as http;
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';

import 'transaction_service_test.mocks.dart';

@GenerateMocks([http.Client])
void main() {
  group('TransactionService HTTP Error Handling', () {
    late TransactionService transactionService;
    late MockClient mockClient;

    setUp(() {
      mockClient = MockClient();
      transactionService = TransactionService(httpClient: mockClient);
    });

    test(
      'should throw HttpException with 400 status for bad request on getTransactions',
      () async {
        when(
          mockClient.get(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Bad Request', 400));

        expect(
          () => transactionService.getTransactions(),
          throwsA(
            isA<HttpException>()
                .having((e) => e.statusCode, 'statusCode', equals(400))
                .having(
                  (e) => e.userFriendlyMessage,
                  'userFriendlyMessage',
                  equals('Nieprawidłowe dane w żądaniu'),
                ),
          ),
        );
      },
    );

    test(
      'should throw HttpException with 404 status for not found on getTransactionsByAccountId',
      () async {
        when(
          mockClient.get(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Not Found', 404));

        expect(
          () =>
              transactionService.getTransactionsByAccountId('test-account-id'),
          throwsA(
            isA<HttpException>()
                .having((e) => e.statusCode, 'statusCode', equals(404))
                .having(
                  (e) => e.userFriendlyMessage,
                  'userFriendlyMessage',
                  equals('Zasób nie został znaleziony'),
                ),
          ),
        );
      },
    );

    test(
      'should throw HttpException with 422 status for validation error on createTransaction',
      () async {
        when(
          mockClient.post(
            any,
            headers: anyNamed('headers'),
            body: anyNamed('body'),
          ),
        ).thenAnswer((_) async => http.Response('Validation Error', 422));

        expect(
          () => transactionService.createTransaction(
            accountId: 'test-account',
            amount: 100.0,
            description: 'Test transaction',
            date: DateTime.now(),
            type: 'INCOME',
            category: 'Test',
            currency: 'PLN',
          ),
          throwsA(
            isA<HttpException>()
                .having((e) => e.statusCode, 'statusCode', equals(422))
                .having(
                  (e) => e.userFriendlyMessage,
                  'userFriendlyMessage',
                  equals('Błąd walidacji danych'),
                ),
          ),
        );
      },
    );

    test(
      'should throw HttpException with 409 status for conflict on updateTransaction',
      () async {
        when(
          mockClient.put(
            any,
            headers: anyNamed('headers'),
            body: anyNamed('body'),
          ),
        ).thenAnswer((_) async => http.Response('Conflict', 409));

        expect(
          () => transactionService.updateTransaction(
            id: 'test-id',
            amount: 100.0,
            description: 'Updated transaction',
            category: 'Updated',
            currency: 'PLN',
          ),
          throwsA(
            isA<HttpException>()
                .having((e) => e.statusCode, 'statusCode', equals(409))
                .having(
                  (e) => e.userFriendlyMessage,
                  'userFriendlyMessage',
                  equals('Konflikt danych'),
                ),
          ),
        );
      },
    );

    test(
      'should throw HttpException with 500 status for server error on deleteTransaction',
      () async {
        when(
          mockClient.delete(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Internal Server Error', 500));

        expect(
          () => transactionService.deleteTransaction('test-id'),
          throwsA(
            isA<HttpException>()
                .having((e) => e.statusCode, 'statusCode', equals(500))
                .having(
                  (e) => e.userFriendlyMessage,
                  'userFriendlyMessage',
                  equals('Błąd serwera'),
                ),
          ),
        );
      },
    );
  });
}
