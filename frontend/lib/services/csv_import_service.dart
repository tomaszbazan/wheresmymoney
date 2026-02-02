import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/services/auth_service.dart';
import 'package:frontend/services/http_client.dart';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';

abstract class CsvImportService {
  Future<CsvParseResult> uploadCsv(List<int> bytes, String filename, String accountId);
}

class RestCsvImportService implements CsvImportService {
  final ApiClient _apiClient;

  RestCsvImportService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);

  @override
  Future<CsvParseResult> uploadCsv(List<int> bytes, String filename, String accountId) async {
    final multipartFile = http.MultipartFile.fromBytes('csvFile', bytes, filename: filename, contentType: MediaType('text', 'csv'));

    return _apiClient.postMultipart('/transactions/import', {'accountId': accountId}, {'csvFile': multipartFile}, CsvParseResult.fromJson);
  }
}
