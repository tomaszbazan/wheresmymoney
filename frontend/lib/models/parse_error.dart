import 'parse_error_type.dart';

class ParseError {
  final ErrorType type;
  final int lineNumber;
  final String details;

  const ParseError({required this.type, required this.lineNumber, required this.details});

  factory ParseError.fromJson(Map<String, dynamic> json) {
    return ParseError(type: ErrorType.fromJson(json['type'] as String?), lineNumber: json['lineNumber'] as int, details: json['details'] as String);
  }

  Map<String, dynamic> toJson() {
    return {'type': type.name, 'lineNumber': lineNumber, 'details': details};
  }
}
