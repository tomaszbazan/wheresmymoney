import '../models/category.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class CategoryServiceInterface {
  Future<List<Category>> getCategories();

  Future<List<Category>> getCategoriesByType(String type);

  Future<Category> createCategory({required String name, required String description, required String type, required String color, String? parentId});

  Future<Category> updateCategory({required String id, required String name, required String description, required String color, String? parentId});

  Future<void> deleteCategory(String categoryId);
}

class CategoryService implements CategoryServiceInterface {
  final ApiClient _apiClient;

  CategoryService({AuthService? authService}) : _apiClient = ApiClient(authService ?? AuthService());

  @override
  Future<List<Category>> getCategories() async {
    return await _apiClient.getList<Category>('/categories', 'categories', Category.fromJson);
  }

  @override
  Future<List<Category>> getCategoriesByType(String type) async {
    return await _apiClient.getList<Category>('/categories?type=$type', 'categories', Category.fromJson);
  }

  @override
  Future<Category> createCategory({required String name, required String description, required String type, required String color, String? parentId}) async {
    final Map<String, dynamic> categoryData = {'name': name, 'description': description, 'type': type.toUpperCase(), 'color': color, if (parentId != null) 'parentId': parentId};

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
