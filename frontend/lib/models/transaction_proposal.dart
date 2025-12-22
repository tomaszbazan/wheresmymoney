import 'package:frontend/models/transaction_type.dart';

class TransactionProposal {
  final DateTime transactionDate;
  final String description;
  final double amount;
  final String currency;
  final TransactionType type;
  final String? categoryId;
  final bool isSuggestedByAi;

  const TransactionProposal({
    required this.transactionDate,
    required this.description,
    required this.amount,
    required this.currency,
    required this.type,
    this.categoryId,
    this.isSuggestedByAi = false,
  });

  factory TransactionProposal.fromJson(Map<String, dynamic> json) {
    final categoryId = json['categoryId'] as String?;
    return TransactionProposal(
      transactionDate: DateTime.parse(json['transactionDate'] as String),
      description: json['description'] as String,
      amount: (json['amount'] as num).toDouble(),
      currency: json['currency'] as String,
      type: json['type'] as String == 'INCOME' ? TransactionType.income : TransactionType.expense,
      categoryId: categoryId,
      isSuggestedByAi: categoryId != null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'transactionDate': transactionDate.toIso8601String(),
      'description': description,
      'amount': amount,
      'currency': currency,
      'type': type == TransactionType.income ? 'INCOME' : 'EXPENSE',
      if (categoryId != null) 'categoryId': categoryId,
    };
  }

  TransactionProposal copyWith({
    DateTime? transactionDate,
    String? description,
    double? amount,
    String? currency,
    TransactionType? type,
    String? categoryId,
    bool? isSuggestedByAi,
  }) {
    return TransactionProposal(
      transactionDate: transactionDate ?? this.transactionDate,
      description: description ?? this.description,
      amount: amount ?? this.amount,
      currency: currency ?? this.currency,
      type: type ?? this.type,
      categoryId: categoryId ?? this.categoryId,
      isSuggestedByAi: isSuggestedByAi ?? this.isSuggestedByAi,
    );
  }
}
