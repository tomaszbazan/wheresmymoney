import '../models/category.dart';
import '../models/category_type.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class CategoryService {
  Future<List<Category>> getCategoriesByType(CategoryType type);

  Future<Category> createCategory({required String name, required String description, required CategoryType type, required String color, String? parentId});

  Future<Category> updateCategory({required String id, required String name, required String description, required String color, String? parentId});

  Future<void> deleteCategory(String categoryId);
}

class RestCategoryService implements CategoryService {
  final ApiClient _apiClient;

  RestCategoryService({AuthService? authService}) : _apiClient = ApiClient(authService ?? AuthService());

  @override
  Future<List<Category>> getCategoriesByType(CategoryType type) async {
    return await _apiClient.getList<Category>('/categories?type=${type.name.toUpperCase()}', 'categories', Category.fromJson);
  }

  @override
  Future<Category> createCategory({required String name, required String description, required CategoryType type, required String color, String? parentId}) async {
    final Map<String, dynamic> categoryData = {
      'name': name,
      'description': description,
      'type': type.name.toUpperCase(),
      'color': color,
      if (parentId != null) 'parentId': parentId,
    };

    return await _apiClient.post<Category>('/categories', categoryData, Category.fromJson);
  }

  @override
  Future<Category> updateCategory({required String id, required String name, required String description, required String color, String? parentId}) async {
    final Map<String, dynamic> categoryData = {'name': name, 'description': description, 'color': color, if (parentId != null) 'parentId': parentId};

    return await _apiClient.put<Category>('/categories/$id', categoryData, Category.fromJson);
  }

  @override
  Future<void> deleteCategory(String categoryId) async {
    await _apiClient.delete('/categories/$categoryId');
  }
}
