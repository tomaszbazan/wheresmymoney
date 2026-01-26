class Transfer {
  final String id;
  final String sourceAccountId;
  final String targetAccountId;
  final double sourceAmount;
  final String sourceCurrency;
  final double targetAmount;
  final String targetCurrency;
  final double exchangeRate;
  final String? description;
  final DateTime createdAt;

  Transfer({
    required this.id,
    required this.sourceAccountId,
    required this.targetAccountId,
    required this.sourceAmount,
    required this.sourceCurrency,
    required this.targetAmount,
    required this.targetCurrency,
    required this.exchangeRate,
    this.description,
    required this.createdAt,
  });

  factory Transfer.fromJson(Map<String, dynamic> json) {
    return Transfer(
      id: json['id'] as String,
      sourceAccountId: json['sourceAccountId'] as String,
      targetAccountId: json['targetAccountId'] as String,
      sourceAmount: (json['sourceAmount'] as num).toDouble(),
      sourceCurrency: json['sourceCurrency'] as String,
      targetAmount: (json['targetAmount'] as num).toDouble(),
      targetCurrency: json['targetCurrency'] as String,
      exchangeRate: (json['exchangeRate'] as num).toDouble(),
      description: json['description'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}
