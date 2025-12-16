import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';

void main() {
  group('HttpException', () {
    test('should create exception with status code and message', () {
      const exception = HttpException(404, 'Not Found');

      expect(exception.statusCode, equals(404));
      expect(exception.message, equals('Not Found'));
      expect(exception.toString(), equals('HttpException: 404 - Not Found'));
    });

    test('should identify 4XX status codes correctly', () {
      const badRequest = HttpException(400, 'Bad Request');
      const notFound = HttpException(404, 'Not Found');
      const serverError = HttpException(500, 'Server Error');
      const success = HttpException(200, 'OK');

      expect(badRequest.isClientError, isTrue);
      expect(notFound.isClientError, isTrue);
      expect(serverError.isClientError, isFalse);
      expect(success.isClientError, isFalse);
    });

    test('should provide user-friendly messages for common 4XX errors', () {
      const badRequest = HttpException(400, '');
      const unauthorized = HttpException(401, '');
      const notFound = HttpException(404, '');
      const conflict = HttpException(409, '');
      const unprocessable = HttpException(422, '');

      expect(badRequest.userFriendlyMessage, equals('Nieprawidłowe dane w żądaniu'));
      expect(unauthorized.userFriendlyMessage, equals('Brak autoryzacji'));
      expect(notFound.userFriendlyMessage, equals('Zasób nie został znaleziony'));
      expect(conflict.userFriendlyMessage, equals('Konflikt danych'));
      expect(unprocessable.userFriendlyMessage, equals('Błąd walidacji danych'));
    });

    test('should return generic message for unknown 4XX errors', () {
      const unknownError = HttpException(418, '');

      expect(unknownError.userFriendlyMessage, equals('Błąd klienta: 418'));
    });

    test('should return server error message for 5XX errors', () {
      const serverError = HttpException(500, '');

      expect(serverError.userFriendlyMessage, equals('Błąd serwera'));
    });

    test('should return network error message for connection errors', () {
      const networkError = HttpException(0, 'Connection refused');

      expect(networkError.userFriendlyMessage, equals('Błąd połączenia z serwerem'));
    });
  });
}
