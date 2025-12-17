import 'package:flutter/material.dart';
import 'package:frontend/models/category_type.dart';

import '../services/category_service.dart';
import '../utils/category_hierarchy.dart';
import 'category_option_item.dart';

class SearchableCategoryDropdown extends StatefulWidget {
  final CategoryType transactionType;
  final String? selectedCategoryId;
  final void Function(String?) onChanged;
  final String? Function(String?)? validator;
  final bool enabled;
  final CategoryService? categoryService;

  const SearchableCategoryDropdown({
    super.key,
    required this.transactionType,
    this.selectedCategoryId,
    required this.onChanged,
    this.validator,
    this.enabled = true,
    this.categoryService,
  });

  @override
  State<SearchableCategoryDropdown> createState() => _SearchableCategoryDropdownState();
}

class _SearchableCategoryDropdownState extends State<SearchableCategoryDropdown> {
  late final CategoryService _categoryService;
  List<CategoryWithLevel> _categoriesWithLevel = [];
  bool _isLoading = false;
  String? _error;
  final TextEditingController _textController = TextEditingController();
  String? _selectedCategoryId;

  @override
  void initState() {
    super.initState();
    _categoryService = widget.categoryService ?? RestCategoryService();
    _selectedCategoryId = widget.selectedCategoryId;
    _loadCategories();
  }

  @override
  void didUpdateWidget(SearchableCategoryDropdown oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.transactionType != widget.transactionType) {
      _loadCategories();
    }
    if (oldWidget.selectedCategoryId != widget.selectedCategoryId) {
      _selectedCategoryId = widget.selectedCategoryId;
      _updateTextController();
    }
  }

  @override
  void dispose() {
    _textController.dispose();
    super.dispose();
  }

  Future<void> _loadCategories() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final categories = await _categoryService.getCategoriesByType(widget.transactionType);
      setState(() {
        _categoriesWithLevel = CategoryHierarchy.buildHierarchy(categories);
        _isLoading = false;
        _updateTextController();
      });
    } catch (e) {
      setState(() {
        _error = 'Nie udało się załadować kategorii';
        _isLoading = false;
      });
    }
  }

  void _updateTextController() {
    if (_selectedCategoryId != null) {
      final selectedCategory = _categoriesWithLevel.firstWhere((item) => item.category.id == _selectedCategoryId, orElse: () => _categoriesWithLevel.first);
      _textController.text = selectedCategory.category.name;
    } else {
      _textController.clear();
    }
  }

  List<String> _filterCategories(String query) {
    if (query.isEmpty) {
      return _categoriesWithLevel.map((item) => item.category.id).toList();
    }

    final lowerQuery = query.toLowerCase();
    return _categoriesWithLevel.where((item) => item.category.name.toLowerCase().contains(lowerQuery)).map((item) => item.category.id).toList();
  }

  CategoryWithLevel? _getCategoryWithLevelById(String categoryId) {
    try {
      return _categoriesWithLevel.firstWhere((item) => item.category.id == categoryId);
    } catch (e) {
      return null;
    }
  }

  Color _parseColor(String colorString) {
    try {
      String hexColor = colorString.replaceAll('#', '');
      if (hexColor.length == 6) {
        hexColor = 'FF$hexColor';
      }
      return Color(int.parse(hexColor, radix: 16));
    } catch (e) {
      return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const SizedBox(height: 56, child: Center(child: CircularProgressIndicator()));
    }

    if (_error != null) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 56,
            width: double.infinity,
            decoration: BoxDecoration(border: Border.all(color: Colors.red), borderRadius: BorderRadius.circular(4)),
            child: Center(child: Text(_error!, style: const TextStyle(color: Colors.red))),
          ),
          const SizedBox(height: 8),
          TextButton(onPressed: _loadCategories, child: const Text('Spróbuj ponownie')),
        ],
      );
    }

    return Autocomplete<String>(
      optionsBuilder: (TextEditingValue textEditingValue) {
        return _filterCategories(textEditingValue.text);
      },
      displayStringForOption: (String categoryId) {
        final categoryWithLevel = _getCategoryWithLevelById(categoryId);
        return categoryWithLevel?.category.name ?? '';
      },
      onSelected: (String categoryId) {
        setState(() {
          _selectedCategoryId = categoryId;
        });
        widget.onChanged(categoryId);
      },
      optionsViewBuilder: (context, onSelected, options) {
        return Align(
          alignment: Alignment.topLeft,
          child: ClipRRect(
            child: Material(
              elevation: 4.0,
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxHeight: 300),
                child: ListView.builder(
                  padding: const EdgeInsets.symmetric(vertical: 8.0),
                  shrinkWrap: true,
                  itemCount: options.length,
                  itemBuilder: (context, index) {
                    final categoryId = options.elementAt(index);
                    final categoryWithLevel = _getCategoryWithLevelById(categoryId);

                    if (categoryWithLevel == null) {
                      return const SizedBox.shrink();
                    }

                    return InkWell(
                      onTap: () => onSelected(categoryId),
                      child: CategoryOptionItem(category: categoryWithLevel.category, level: categoryWithLevel.level, parseColor: _parseColor),
                    );
                  },
                ),
              ),
            ),
          ),
        );
      },
      fieldViewBuilder: (context, textEditingController, focusNode, onFieldSubmitted) {
        textEditingController.text = _textController.text;
        textEditingController.addListener(() {
          _textController.text = textEditingController.text;
        });

        return TextFormField(
          controller: textEditingController,
          focusNode: focusNode,
          decoration: const InputDecoration(labelText: 'Kategoria', border: OutlineInputBorder()),
          enabled: widget.enabled,
          onFieldSubmitted: (value) {
            final filteredCategories = _filterCategories(value);
            if (filteredCategories.isNotEmpty) {
              final firstCategoryId = filteredCategories.first;
              setState(() {
                _selectedCategoryId = firstCategoryId;
              });
              widget.onChanged(firstCategoryId);
              _updateTextController();
            }
          },
          validator: (value) {
            if (widget.validator != null) {
              return widget.validator!(_selectedCategoryId);
            }
            return null;
          },
        );
      },
    );
  }
}
