import 'package:frontend/models/transaction/bill_item_category.dart';

class BillItem {
  final String? id;
  final BillItemCategory category;
  final double amount;
  final String description;

  const BillItem({this.id, required this.category, required this.amount, required this.description});

  factory BillItem.fromJson(Map<String, dynamic> json) {
    return BillItem(
      id: json['id'] as String?,
      category: BillItemCategory.fromJson(json['category'] as Map<String, dynamic>),
      amount: (json['amount'] as num).toDouble(),
      description: json['description'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {if (id != null) 'id': id, 'categoryId': category.id, 'amount': amount, 'description': description};
  }
}
