import 'package:frontend/models/category.dart';
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
  Future<List<Category>> getCategoriesByType(String type) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _categories.values.where((category) => category.type == type).toList();
  }

  @override
  Future<Category> createCategory({required String name, required String description, required String type, required String color, String? parentId}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final id = const Uuid().v4();
    final category = Category(
      id: id,
      name: name,
      description: description,
      type: type.toUpperCase(),
      color: color,
      parentId: parentId,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
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

  Future<Category> addCategory(String name, {String? description, String? type, String? color, String? parentId}) async {
    final id = const Uuid().v4();
    final category = Category(
      id: id,
      name: name,
      description: description ?? '',
      type: type ?? 'EXPENSE',
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
