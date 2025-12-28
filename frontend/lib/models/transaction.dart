import 'package:frontend/models/transaction_type.dart';

class Transaction {
  final String id;
  final String accountId;
  final double amount;
  final TransactionType type;
  final String description;
  final String categoryId;
  final String? categoryName;
  final DateTime createdAt;
  final DateTime updatedAt;
  final DateTime transactionDate;

  const Transaction({
    required this.id,
    required this.accountId,
    required this.amount,
    required this.type,
    required this.description,
    required this.categoryId,
    required this.categoryName,
    required this.createdAt,
    required this.updatedAt,
    required this.transactionDate,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) {
    return Transaction(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      amount: (json['amount'] as num).toDouble(),
      type: json['type'] as String == 'INCOME' ? TransactionType.income : TransactionType.expense,
      description: json['description'] as String,
      categoryId: json['category'] != null ? (json['category'] is String ? json['category'] as String : json['category']['id'] as String) : '',
      categoryName: json['category'] != null && json['category'] is Map ? json['category']['name'] as String? : null,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      transactionDate: DateTime.parse(json['transactionDate'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'accountId': accountId,
      'amount': amount,
      'type': type,
      'description': description,
      'categoryId': categoryId,
      if (categoryName != null) 'categoryName': categoryName,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'transactionDate': transactionDate.toIso8601String(),
    };
  }

  bool get isIncome => type == TransactionType.income;

  bool get isExpense => type == TransactionType.expense;
}
