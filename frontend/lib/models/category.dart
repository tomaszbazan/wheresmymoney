import 'package:frontend/models/category_type.dart';

class Category {
  final String id;
  final String name;
  final String description;
  final CategoryType type;
  final String color;
  final String? parentId;
  final DateTime createdAt;
  final DateTime updatedAt;

  const Category({
    required this.id,
    required this.name,
    required this.description,
    required this.type,
    required this.color,
    this.parentId,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Category.fromJson(Map<String, dynamic> json) {
    return Category(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String? ?? '',
      type: json['type'] as String == 'INCOME' ? CategoryType.income : CategoryType.expense,
      color: json['color'] as String,
      parentId: json['parentId'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'type': type,
      'color': color,
      'parentId': parentId,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
    };
  }

  bool get isIncome => type == CategoryType.income;

  bool get isExpense => type == CategoryType.expense;

  bool get hasParent => parentId != null;

  bool get isTopLevel => parentId == null;
}
