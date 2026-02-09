import 'package:frontend/models/transaction_type.dart';

class TransactionFilter {
  final DateTime? dateFrom;
  final DateTime? dateTo;
  final double? minAmount;
  final double? maxAmount;
  final List<TransactionType> types;
  final List<String> accountIds;
  final List<String> categoryIds;
  final String? description;
  final String? sort;

  const TransactionFilter({
    this.dateFrom,
    this.dateTo,
    this.minAmount,
    this.maxAmount,
    this.types = const [],
    this.accountIds = const [],
    this.categoryIds = const [],
    this.description,
    this.sort,
  });

  TransactionFilter copyWith({
    DateTime? dateFrom,
    DateTime? dateTo,
    double? minAmount,
    double? maxAmount,
    List<TransactionType>? types,
    List<String>? accountIds,
    List<String>? categoryIds,
    String? description,
    String? sort,
  }) {
    return TransactionFilter(
      dateFrom: dateFrom ?? this.dateFrom,
      dateTo: dateTo ?? this.dateTo,
      minAmount: minAmount ?? this.minAmount,
      maxAmount: maxAmount ?? this.maxAmount,
      types: types ?? this.types,
      accountIds: accountIds ?? this.accountIds,
      categoryIds: categoryIds ?? this.categoryIds,
      description: description ?? this.description,
      sort: sort ?? this.sort,
    );
  }
}
