class ParseError {
  final int lineNumber;
  final String message;

  const ParseError({required this.lineNumber, required this.message});

  factory ParseError.fromJson(Map<String, dynamic> json) {
    return ParseError(lineNumber: json['lineNumber'] as int, message: json['message'] as String);
  }

  Map<String, dynamic> toJson() {
    return {'lineNumber': lineNumber, 'message': message};
  }
}
