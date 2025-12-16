import 'package:flutter/material.dart';

import '../models/category.dart';
import '../models/http_exception.dart';
import '../services/category_service.dart';
import 'searchable_category_dropdown.dart';

class CategoryForm extends StatefulWidget {
  final Category? category;
  final void Function(Category) onSaved;
  final CategoryService? categoryService;
  final String? defaultType;

  const CategoryForm({super.key, this.category, required this.onSaved, this.categoryService, this.defaultType})
    : assert(category != null || defaultType != null, 'defaultType is required when creating new category');

  @override
  State<CategoryForm> createState() => _CategoryFormState();
}

class _CategoryFormState extends State<CategoryForm> {
  final _formKey = GlobalKey<FormState>();
  late final CategoryService _categoryService;

  late TextEditingController _nameController;
  late TextEditingController _descriptionController;

  String _selectedType = 'EXPENSE';
  String _selectedColor = '#FF5722';
  String? _selectedParentId;
  bool _isLoading = false;
  bool _isLoadingParentCategories = false;
  List<Category> _availableParentCategories = [];

  static const List<String> _availableColors = ['#FF5722', '#2196F3', '#4CAF50', '#FF9800', '#9C27B0', '#F44336', '#00BCD4', '#8BC34A', '#FFC107', '#E91E63'];

  @override
  void initState() {
    super.initState();

    _categoryService = widget.categoryService ?? RestCategoryService();

    _nameController = TextEditingController(text: widget.category?.name ?? '');
    _descriptionController = TextEditingController(text: widget.category?.description ?? '');

    if (widget.category != null) {
      _selectedType = widget.category!.type;
      _selectedColor = widget.category!.color;
      _selectedParentId = widget.category!.parentId;
    } else if (widget.defaultType != null) {
      _selectedType = widget.defaultType!;
    }

    _loadAvailableParentCategories();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _loadAvailableParentCategories() async {
    setState(() {
      _isLoadingParentCategories = true;
    });

    try {
      final categories = await _categoryService.getCategoriesByType(_selectedType);

      final filteredCategories =
          widget.category == null ? categories : categories.where((category) => category.id != widget.category!.id && category.parentId != widget.category!.id).toList();

      setState(() {
        _availableParentCategories = filteredCategories;
        _isLoadingParentCategories = false;
      });
    } catch (e) {
      setState(() {
        _availableParentCategories = [];
        _isLoadingParentCategories = false;
      });
    }
  }

  Future<void> _saveCategory() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final Category category;

      if (widget.category != null) {
        category = await _categoryService.updateCategory(
          id: widget.category!.id,
          name: _nameController.text,
          description: _descriptionController.text,
          color: _selectedColor,
          parentId: _selectedParentId,
        );
      } else {
        category = await _categoryService.createCategory(
          name: _nameController.text,
          description: _descriptionController.text,
          type: _selectedType,
          color: _selectedColor,
          parentId: _selectedParentId,
        );
      }

      widget.onSaved(category);
    } on HttpException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.userFriendlyMessage), backgroundColor: Colors.red));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Nieoczekiwany błąd: $e'), backgroundColor: Colors.red));
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.category != null;

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(isEditing ? 'Edytuj kategorię' : 'Dodaj kategorię', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 24),

            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(labelText: 'Nazwa', border: OutlineInputBorder()),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Wprowadź nazwę kategorii';
                }
                if (value.length < 2) {
                  return 'Nazwa musi mieć co najmniej 2 znaki';
                }
                if (value.length > 50) {
                  return 'Nazwa nie może być dłuższa niż 50 znaków';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Checkbox(
                      value: _selectedParentId == null,
                      onChanged: (bool? value) {
                        setState(() {
                          if (value == true) {
                            _selectedParentId = null;
                          }
                        });
                      },
                    ),
                    const Text('Kategoria główna (brak rodzica)'),
                  ],
                ),
                if (_selectedParentId != null || !_isLoadingParentCategories && _availableParentCategories.isNotEmpty)
                  SearchableCategoryDropdown(
                    transactionType: _selectedType,
                    selectedCategoryId: _selectedParentId,
                    onChanged: (categoryId) {
                      setState(() {
                        _selectedParentId = categoryId;
                      });
                    },
                    enabled: _selectedParentId != null,
                    categoryService: _categoryService,
                  ),
              ],
            ),
            const SizedBox(height: 16),

            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Kolor', style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children:
                      _availableColors.map((color) {
                        final colorValue = Color(int.parse(color.substring(1), radix: 16) + 0xFF000000);
                        final isSelected = _selectedColor == color;

                        return GestureDetector(
                          onTap: () {
                            setState(() {
                              _selectedColor = color;
                            });
                          },
                          child: Container(
                            width: 40,
                            height: 40,
                            decoration: BoxDecoration(color: colorValue, shape: BoxShape.circle, border: isSelected ? Border.all(color: Colors.black, width: 3) : null),
                            child: isSelected ? const Icon(Icons.check, color: Colors.white, size: 20) : null,
                          ),
                        );
                      }).toList(),
                ),
              ],
            ),
            const SizedBox(height: 24),

            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Anuluj')),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: _isLoading ? null : _saveCategory,
                  child: _isLoading ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : Text(isEditing ? 'Zapisz' : 'Dodaj'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
