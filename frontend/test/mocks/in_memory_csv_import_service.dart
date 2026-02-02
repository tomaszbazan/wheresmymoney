import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/services/csv_import_service.dart';

class InMemoryCsvImportService implements CsvImportService {
  @override
  Future<CsvParseResult> uploadCsv(List<int> bytes, String filename, String accountId) async {
    return const CsvParseResult(totalRows: 0, successCount: 0, errorCount: 0, proposals: [], errors: []);
  }
}
