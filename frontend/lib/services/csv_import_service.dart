import 'dart:io';

import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/services/auth_service.dart';
import 'package:frontend/services/http_client.dart';
import 'package:http/http.dart' as http;

class CsvImportService {
  final ApiClient _apiClient;

  CsvImportService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);

  Future<CsvParseResult> uploadCsv(File file, String accountId) async {
    final bytes = await file.readAsBytes();
    final multipartFile = http.MultipartFile.fromBytes('csvFile', bytes, filename: file.path.split('/').last);

    return _apiClient.postMultipart('/api/transactions/import', {'accountId': accountId}, {'csvFile': multipartFile}, CsvParseResult.fromJson);
  }
}
