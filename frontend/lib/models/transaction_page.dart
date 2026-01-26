import 'package:frontend/models/transaction.dart';

class TransactionPage {
  final List<Transaction> transactions;
  final int page;
  final int size;
  final int totalElements;
  final int totalPages;

  const TransactionPage({required this.transactions, required this.page, required this.size, required this.totalElements, required this.totalPages});

  factory TransactionPage.fromJson(Map<String, dynamic> json) {
    return TransactionPage(
      transactions: (json['transactions'] as List<dynamic>).map((t) => Transaction.fromJson(t as Map<String, dynamic>)).toList(),
      page: json['page'] as int,
      size: json['size'] as int,
      totalElements: json['totalElements'] as int,
      totalPages: json['totalPages'] as int,
    );
  }

  bool get hasMore => page + 1 < totalPages;
}
