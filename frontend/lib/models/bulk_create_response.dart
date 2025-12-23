class BulkCreateResponse {
  final int savedCount;
  final int duplicateCount;
  final List<String> savedTransactionIds;

  BulkCreateResponse({required this.savedCount, required this.duplicateCount, required this.savedTransactionIds});

  factory BulkCreateResponse.fromJson(Map<String, dynamic> json) {
    return BulkCreateResponse(
      savedCount: json['savedCount'] as int,
      duplicateCount: json['duplicateCount'] as int,
      savedTransactionIds: (json['savedTransactionIds'] as List).map((id) => id.toString()).toList(),
    );
  }
}
