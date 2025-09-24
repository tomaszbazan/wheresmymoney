import 'package:flutter/material.dart';

import '../models/category.dart';
import '../services/category_service.dart';

class CategorySelector extends StatefulWidget {
  final String? selectedCategoryId;
  final String transactionType;
  final Function(String?) onChanged;
  final CategoryServiceInterface? categoryService;
  final String? Function(String?)? validator;

  const CategorySelector({
    super.key,
    this.selectedCategoryId,
    required this.transactionType,
    required this.onChanged,
    this.categoryService,
    this.validator,
  });

  @override
  State<CategorySelector> createState() => _CategorySelectorState();
}

class _CategorySelectorState extends State<CategorySelector> {
  late final CategoryServiceInterface _categoryService;
  List<Category> _categories = [];
  bool _isLoading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _categoryService = widget.categoryService ?? CategoryService();
    _loadCategories();
  }

  @override
  void didUpdateWidget(CategorySelector oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.transactionType != widget.transactionType) {
      _loadCategories();
    }
  }

  Future<void> _loadCategories() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final categories = await _categoryService.getCategoriesByType(
        widget.transactionType.toUpperCase(),
      );
      setState(() {
        _categories = categories;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Nie udało się załadować kategorii';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const SizedBox(
        height: 56,
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 56,
            width: double.infinity,
            decoration: BoxDecoration(
              border: Border.all(color: Colors.red),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Center(
              child: Text(_error!, style: const TextStyle(color: Colors.red)),
            ),
          ),
          const SizedBox(height: 8),
          TextButton(
            onPressed: _loadCategories,
            child: const Text('Spróbuj ponownie'),
          ),
        ],
      );
    }

    return DropdownButtonFormField<String>(
      initialValue: widget.selectedCategoryId,
      decoration: const InputDecoration(
        labelText: 'Kategoria',
        border: OutlineInputBorder(),
        contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 16),
      ),
      hint: const Text('Wybierz kategorię'),
      items:
          _categories.map((category) {
            return DropdownMenuItem<String>(
              value: category.id,
              child: Row(
                children: [
                  Container(
                    width: 16,
                    height: 16,
                    decoration: BoxDecoration(
                      color: _parseColor(category.color),
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(category.name, overflow: TextOverflow.ellipsis),
                  ),
                ],
              ),
            );
          }).toList(),
      onChanged: widget.onChanged,
      validator: widget.validator,
      isExpanded: true,
    );
  }

  Color _parseColor(String colorString) {
    try {
      // Handle hex colors like "#FF5722" or "FF5722"
      String hexColor = colorString.replaceAll('#', '');
      if (hexColor.length == 6) {
        hexColor = 'FF$hexColor'; // Add alpha if not present
      }
      return Color(int.parse(hexColor, radix: 16));
    } catch (e) {
      // Default color if parsing fails
      return Colors.grey;
    }
  }
}
