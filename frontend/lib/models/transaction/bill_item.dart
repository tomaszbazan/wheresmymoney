import 'package:frontend/models/transaction/bill_item_category.dart';
import 'package:frontend/models/money.dart';

class BillItem {
  final String? id;
  final BillItemCategory category;
  final Money amount;
  final String description;

  const BillItem({this.id, required this.category, required this.amount, required this.description});

  factory BillItem.fromJson(Map<String, dynamic> json) {
    return BillItem(
      id: json['id'] as String?,
      category: BillItemCategory.fromJson(json['category'] as Map<String, dynamic>),
      amount: Money.fromJson(json['amount'] as Map<String, dynamic>),
      description: json['description'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {if (id != null) 'id': id, 'categoryId': category.id, 'amount': amount.toJson(), 'description': description};
  }
}
