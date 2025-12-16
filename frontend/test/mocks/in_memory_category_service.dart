import 'package:frontend/models/category.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/services/category_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryCategoryService implements CategoryService {
  final Map<String, Category> _categories = {};
  Exception? _apiError;

  @override
  Future<List<Category>> getCategories() async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _categories.values.toList();
  }

  @override
  Future<List<Category>> getCategoriesByType(CategoryType type) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _categories.values.where((category) => category.type == type).toList();
  }

  @override
  Future<Category> createCategory({required String name, required String description, required CategoryType type, required String color, String? parentId}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final id = const Uuid().v4();
    final category = Category(id: id, name: name, description: description, type: type, color: color, parentId: parentId, createdAt: DateTime.now(), updatedAt: DateTime.now());
    _categories[id] = category;
    return category;
  }

  @override
  Future<Category> updateCategory({required String id, required String name, required String description, required String color, String? parentId}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final existingCategory = _categories[id];
    if (existingCategory == null) {
      throw Exception('Category not found');
    }

    final updatedCategory = Category(
      id: id,
      name: name,
      description: description,
      type: existingCategory.type,
      color: color,
      parentId: parentId,
      createdAt: existingCategory.createdAt,
      updatedAt: DateTime.now(),
    );
    _categories[id] = updatedCategory;
    return updatedCategory;
  }

  @override
  Future<void> deleteCategory(String categoryId) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    _categories.remove(categoryId);
  }

  Future<Category> addCategory(String name, {String? description, CategoryType? type, String? color, String? parentId}) async {
    final id = const Uuid().v4();
    final category = Category(
      id: id,
      name: name,
      description: description ?? '',
      type: type ?? CategoryType.expense,
      color: color ?? '#FF5722',
      parentId: parentId,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
    _categories[id] = category;
    return category;
  }

  void setApiError(Exception error) {
    _apiError = error;
  }

  void clear() {
    _categories.clear();
    _apiError = null;
  }
}
