import '../models/category.dart';

class CategoryHierarchy {
  static List<CategoryWithLevel> buildHierarchy(List<Category> categories) {
    final result = <CategoryWithLevel>[];

    final topLevelCategories =
        categories.where((category) => category.isTopLevel).toList()..sort(
          (a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()),
        );

    for (final parentCategory in topLevelCategories) {
      _addCategoryWithChildren(parentCategory, categories, result, 0);
    }

    return result;
  }

  static void _addCategoryWithChildren(
    Category category,
    List<Category> allCategories,
    List<CategoryWithLevel> result,
    int level,
  ) {
    result.add(CategoryWithLevel(category, level));

    final children =
        allCategories.where((child) => child.parentId == category.id).toList()
          ..sort(
            (a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()),
          );

    for (final child in children) {
      _addCategoryWithChildren(child, allCategories, result, level + 1);
    }
  }
}

class CategoryWithLevel {
  final Category category;
  final int level;

  const CategoryWithLevel(this.category, this.level);

  bool get isTopLevel => level == 0;

  bool get isChild => level == 1;
}
