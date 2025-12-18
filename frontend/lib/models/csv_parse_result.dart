import 'package:frontend/models/parse_error.dart';
import 'package:frontend/models/transaction_proposal.dart';

class CsvParseResult {
  final int totalRows;
  final int successCount;
  final int errorCount;
  final List<TransactionProposal> proposals;
  final List<ParseError> errors;

  const CsvParseResult({required this.totalRows, required this.successCount, required this.errorCount, required this.proposals, required this.errors});

  factory CsvParseResult.fromJson(Map<String, dynamic> json) {
    return CsvParseResult(
      totalRows: json['totalRows'] as int,
      successCount: json['successCount'] as int,
      errorCount: json['errorCount'] as int,
      proposals: (json['proposals'] as List<dynamic>).map((e) => TransactionProposal.fromJson(e as Map<String, dynamic>)).toList(),
      errors: (json['errors'] as List<dynamic>).map((e) => ParseError.fromJson(e as Map<String, dynamic>)).toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalRows': totalRows,
      'successCount': successCount,
      'errorCount': errorCount,
      'proposals': proposals.map((e) => e.toJson()).toList(),
      'errors': errors.map((e) => e.toJson()).toList(),
    };
  }

  bool get hasErrors => errorCount > 0;

  bool get hasProposals => successCount > 0;
}
