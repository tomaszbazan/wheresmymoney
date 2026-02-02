import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/models/transaction/bill_item.dart';
import 'package:frontend/models/money.dart';

class Transaction {
  final String id;
  final String accountId;
  final Money amount;
  final TransactionType type;
  final List<BillItem> billItems;
  final DateTime createdAt;
  final DateTime updatedAt;
  final DateTime transactionDate;

  const Transaction({
    required this.id,
    required this.accountId,
    required this.amount,
    required this.type,
    required this.billItems,
    required this.createdAt,
    required this.updatedAt,
    required this.transactionDate,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) {
    var billItems = <BillItem>[];
    if (json['bill'] != null && json['bill']['items'] != null) {
      billItems = (json['bill']['items'] as List).map((item) => BillItem.fromJson(item as Map<String, dynamic>)).toList();
    }

    return Transaction(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      amount: Money.fromJson(json['amount'] as Map<String, dynamic>),
      type: json['type'] as String == 'INCOME' ? TransactionType.income : TransactionType.expense,
      billItems: billItems,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      transactionDate: DateTime.parse(json['transactionDate'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'accountId': accountId,
      'amount': amount.toJson(),
      'type': type,
      'bill': {'items': billItems.map((e) => e.toJson()).toList()},
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'transactionDate': transactionDate.toIso8601String(),
    };
  }

  bool get isIncome => type == TransactionType.income;

  bool get isExpense => type == TransactionType.expense;

  // Legacy getters for backward compatibility / ease of use
  String get description {
    if (billItems.isEmpty) return '';
    if (billItems.length == 1) return billItems.first.description;
    return '${billItems.first.description} (+${billItems.length - 1} więcej)';
  }

  String get categoryId {
    if (billItems.isEmpty) return '';
    return billItems.first.category.id;
  }

  String? get categoryName {
    if (billItems.isEmpty) return null;
    if (billItems.length == 1) return billItems.first.category.name;
    return 'Wiele kategorii';
  }
}
