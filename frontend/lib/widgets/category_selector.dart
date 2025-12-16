import 'package:flutter/material.dart';
import 'package:frontend/models/category_type.dart';

import '../services/category_service.dart';
import 'searchable_category_dropdown.dart';

class CategorySelector extends StatelessWidget {
  final String? selectedCategoryId;
  final CategoryType transactionType;
  final void Function(String?) onChanged;
  final CategoryService? categoryService;
  final String? Function(String?)? validator;

  const CategorySelector({super.key, this.selectedCategoryId, required this.transactionType, required this.onChanged, this.categoryService, this.validator});

  @override
  Widget build(BuildContext context) {
    return SearchableCategoryDropdown(
      transactionType: transactionType,
      selectedCategoryId: selectedCategoryId,
      onChanged: onChanged,
      validator: validator,
      categoryService: categoryService,
    );
  }
}
