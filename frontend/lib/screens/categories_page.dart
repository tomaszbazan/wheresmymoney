import 'package:flutter/material.dart';

import '../models/category.dart';
import '../models/category_type.dart';
import '../services/category_service.dart';
import '../utils/category_hierarchy.dart';
import '../utils/error_handler.dart';
import '../widgets/category_form.dart';
import '../widgets/category_list_item.dart';

class CategoriesPage extends StatefulWidget {
  final CategoryType transactionType;
  final CategoryService? categoryService;

  const CategoriesPage({super.key, required this.transactionType, this.categoryService});

  @override
  State<CategoriesPage> createState() => _CategoriesPageState();
}

class _CategoriesPageState extends State<CategoriesPage> {
  late final CategoryService _categoryService;
  List<CategoryWithLevel> _hierarchicalCategories = [];
  bool _isLoading = false;

  bool get _isExpense => widget.transactionType == CategoryType.expense;

  @override
  void initState() {
    super.initState();
    _categoryService = widget.categoryService ?? RestCategoryService();
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    setState(() => _isLoading = true);

    try {
      final categories = await _categoryService.getCategoriesByType(widget.transactionType);
      setState(() {
        _hierarchicalCategories = CategoryHierarchy.buildHierarchy(categories);
      });
    } catch (e) {
      // ignore: use_build_context_synchronously
      ErrorHandler.showError(context, e);
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _showCategoryForm({Category? category}) async {
    await showDialog<void>(
      context: context,
      builder:
          (context) => Dialog(
            child: Container(
              width: 400,
              constraints: const BoxConstraints(maxHeight: 600),
              child: CategoryForm(
                category: category,
                defaultType: widget.transactionType,
                categoryService: _categoryService,
                onSaved: (newCategory) {
                  Navigator.of(context).pop();
                  _loadCategories();
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text(
                        category == null
                            ? 'Kategoria ${_isExpense ? 'wydatku' : 'przychodu'} została dodana'
                            : 'Kategoria ${_isExpense ? 'wydatku' : 'przychodu'} została zaktualizowana',
                      ),
                      backgroundColor: Colors.green,
                    ),
                  );
                },
              ),
            ),
          ),
    );
  }

  Future<void> _deleteCategory(Category category) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Usuń kategorię'),
            content: Text('Czy na pewno chcesz usunąć kategorię ${_isExpense ? 'wydatku' : 'przychodu'} "${category.name}"?'),
            actions: [
              TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Anuluj')),
              TextButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Usuń', style: TextStyle(color: Colors.red))),
            ],
          ),
    );

    if (confirmed == true) {
      try {
        await _categoryService.deleteCategory(category.id);
        _loadCategories();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Kategoria ${_isExpense ? 'wydatku' : 'przychodu'} została usunięta'), backgroundColor: Colors.green));
        }
      } catch (e) {
        // ignore: use_build_context_synchronously
        ErrorHandler.showError(context, e);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Icon(_isExpense ? Icons.arrow_downward : Icons.arrow_upward, color: _isExpense ? Colors.red : Colors.green),
                const SizedBox(width: 8),
                Text('Kategorie ${_isExpense ? 'wydatków' : 'przychodów'}', style: Theme.of(context).textTheme.headlineMedium),
              ],
            ),
          ),
          Expanded(
            child:
                _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : _hierarchicalCategories.isEmpty
                    ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(_isExpense ? Icons.category_outlined : Icons.category, size: 64, color: Colors.grey[400]),
                          const SizedBox(height: 16),
                          Text('Brak kategorii ${_isExpense ? 'wydatków' : 'przychodów'}', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Colors.grey[600])),
                          const SizedBox(height: 8),
                          Text('Dodaj pierwszą kategorię ${_isExpense ? 'wydatku' : 'przychodu'}, aby zacząć', style: TextStyle(color: Colors.grey[600])),
                        ],
                      ),
                    )
                    : RefreshIndicator(
                      onRefresh: _loadCategories,
                      child: ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        itemCount: _hierarchicalCategories.length,
                        itemBuilder: (context, index) {
                          final categoryWithLevel = _hierarchicalCategories[index];

                          return CategoryListItem(
                            categoryWithLevel: categoryWithLevel,
                            onEdit: () => _showCategoryForm(category: categoryWithLevel.category),
                            onDelete: () => _deleteCategory(categoryWithLevel.category),
                          );
                        },
                      ),
                    ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(onPressed: () => _showCategoryForm(), child: const Icon(Icons.add)),
    );
  }
}
