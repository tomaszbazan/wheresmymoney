import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/services/account_service.dart';
import 'package:http/http.dart' as http;
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';

import 'account_service_test.mocks.dart';

@GenerateMocks([http.Client])
void main() {
  group('AccountService HTTP Error Handling', () {
    late AccountService accountService;
    late MockClient mockClient;

    setUp(() {
      mockClient = MockClient();
      accountService = AccountService(httpClient: mockClient);
    });

    test(
      'should throw HttpException with 400 status for bad request on getAccounts',
      () async {
        when(
          mockClient.get(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Bad Request', 400));

        expect(
          () => accountService.getAccounts(),
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
      'should throw HttpException with 404 status for not found on createAccount',
      () async {
        when(
          mockClient.post(
            any,
            headers: anyNamed('headers'),
            body: anyNamed('body'),
          ),
        ).thenAnswer((_) async => http.Response('Not Found', 404));

        expect(
          () => accountService.createAccount('Test Account'),
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
      'should throw HttpException with 422 status for validation error on createAccount',
      () async {
        when(
          mockClient.post(
            any,
            headers: anyNamed('headers'),
            body: anyNamed('body'),
          ),
        ).thenAnswer((_) async => http.Response('Validation Error', 422));

        expect(
          () => accountService.createAccount(''),
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
      'should throw HttpException with 409 status for conflict on deleteAccount',
      () async {
        when(
          mockClient.delete(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Conflict', 409));

        expect(
          () => accountService.deleteAccount('test-id'),
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
      'should throw HttpException with 500 status for server error',
      () async {
        when(
          mockClient.get(any, headers: anyNamed('headers')),
        ).thenAnswer((_) async => http.Response('Internal Server Error', 500));

        expect(
          () => accountService.getAccounts(),
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
