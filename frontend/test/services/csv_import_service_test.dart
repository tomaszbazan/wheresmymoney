import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/http_exception.dart';
import 'package:frontend/services/csv_import_service.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

import '../mocks/fake_auth_service.dart';

void main() {
  group('CsvImportService', () {
    late FakeAuthService fakeAuthService;
    late Uint8List testFileBytes;
    const testFileName = 'test.csv';

    setUp(() {
      fakeAuthService = FakeAuthService();
      testFileBytes = Uint8List.fromList('test data'.codeUnits);
    });

    test('uploadCsv sends multipart POST request to /transactions/import', () async {
      final mockClient = MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/transactions/import');
        expect(request.headers['Authorization'], 'Bearer fake-jwt-token');

        return http.Response(jsonEncode({'proposals': <Map<String, dynamic>>[], 'errors': <Map<String, dynamic>>[], 'totalRows': 0, 'successCount': 0, 'errorCount': 0}), 200);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      await csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123');
    });

    test('uploadCsv returns CsvParseResult with proposals', () async {
      final mockClient = MockClient((request) async {
        final responseBody = jsonEncode({
          'proposals': [
            {'transactionDate': '2024-01-15T00:00:00Z', 'description': 'Test transaction', 'amount': 100.50, 'currency': 'PLN', 'type': 'EXPENSE', 'categoryId': 'cat-123'},
          ],
          'errors': <Map<String, dynamic>>[],
          'totalRows': 1,
          'successCount': 1,
          'errorCount': 0,
        });

        return http.Response(responseBody, 200);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      final result = await csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123');

      expect(result.totalRows, 1);
      expect(result.successCount, 1);
      expect(result.errorCount, 0);
      expect(result.proposals, hasLength(1));
      expect(result.proposals.first.description, 'Test transaction');
      expect(result.proposals.first.amount, 100.50);
      expect(result.errors, isEmpty);
    });

    test('uploadCsv returns CsvParseResult with errors', () async {
      final mockClient = MockClient((request) async {
        final responseBody = jsonEncode({
          'proposals': <Map<String, dynamic>>[],
          'errors': <Map<String, dynamic>>[
            {'lineNumber': 5, 'type': 'INVALID_DATE_FORMAT', 'details': 'Date format is incorrect'},
            {'lineNumber': 12, 'type': 'INVALID_CSV_FORMAT', 'details': 'CSV structure is invalid'},
          ],
          'totalRows': 2,
          'successCount': 0,
          'errorCount': 2,
        });

        return http.Response(responseBody, 200);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      final result = await csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123');

      expect(result.totalRows, 2);
      expect(result.successCount, 0);
      expect(result.errorCount, 2);
      expect(result.errors, hasLength(2));
      expect(result.errors.first.lineNumber, 5);
      expect(result.errors.first.type.name, 'invalidDateFormat');
      expect(result.proposals, isEmpty);
    });

    test('uploadCsv throws HttpException on 400 error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Invalid CSV format', 400);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 400)));
    });

    test('uploadCsv throws HttpException on 401 error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Unauthorized', 401);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 401)));
    });

    test('uploadCsv throws HttpException on 500 error', () async {
      final mockClient = MockClient((request) async {
        return http.Response('Internal Server Error', 500);
      });

      final csvImportService = CsvImportService(authService: fakeAuthService, httpClient: mockClient);

      expect(() => csvImportService.uploadCsv(testFileBytes, testFileName, 'account-123'), throwsA(isA<HttpException>().having((e) => e.statusCode, 'statusCode', 500)));
    });
  });
}
