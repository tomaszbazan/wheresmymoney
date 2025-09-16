import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category.dart';

void main() {
  group('Category Model', () {
    const sampleCategoryJson = {
      'id': 'test-id-123',
      'name': 'Food',
      'description': 'Food expenses',
      'type': 'EXPENSE',
      'color': '#FF5722',
      'parentId': null,
      'createdAt': '2023-01-01T10:00:00Z',
      'updatedAt': '2023-01-01T10:00:00Z',
    };

    const sampleCategoryWithParentJson = {
      'id': 'test-child-123',
      'name': 'Restaurants',
      'description': 'Restaurant expenses',
      'type': 'EXPENSE',
      'color': '#FF9800',
      'parentId': 'test-parent-123',
      'createdAt': '2023-01-01T11:00:00Z',
      'updatedAt': '2023-01-01T11:00:00Z',
    };

    test('should create Category from JSON without parent', () {
      final category = Category.fromJson(sampleCategoryJson);

      expect(category.id, equals('test-id-123'));
      expect(category.name, equals('Food'));
      expect(category.description, equals('Food expenses'));
      expect(category.type, equals('EXPENSE'));
      expect(category.color, equals('#FF5722'));
      expect(category.parentId, isNull);
      expect(category.isTopLevel, isTrue);
      expect(category.hasParent, isFalse);
    });

    test('should create Category from JSON with parent', () {
      final category = Category.fromJson(sampleCategoryWithParentJson);

      expect(category.id, equals('test-child-123'));
      expect(category.name, equals('Restaurants'));
      expect(category.parentId, equals('test-parent-123'));
      expect(category.isTopLevel, isFalse);
      expect(category.hasParent, isTrue);
    });

    test('should convert Category to JSON', () {
      final category = Category.fromJson(sampleCategoryWithParentJson);
      final json = category.toJson();

      expect(json['id'], equals('test-child-123'));
      expect(json['name'], equals('Restaurants'));
      expect(json['parentId'], equals('test-parent-123'));
      expect(json['type'], equals('EXPENSE'));
      expect(json['color'], equals('#FF9800'));
    });

    test('should handle missing description in JSON', () {
      final jsonWithoutDescription = Map<String, dynamic>.from(
        sampleCategoryJson,
      );
      jsonWithoutDescription.remove('description');

      final category = Category.fromJson(jsonWithoutDescription);

      expect(category.description, equals(''));
    });

    test('should correctly identify income and expense types', () {
      final expenseCategory = Category.fromJson(sampleCategoryJson);
      final incomeJson = Map<String, dynamic>.from(sampleCategoryJson);
      incomeJson['type'] = 'INCOME';
      final incomeCategory = Category.fromJson(incomeJson);

      expect(expenseCategory.isExpense, isTrue);
      expect(expenseCategory.isIncome, isFalse);
      expect(incomeCategory.isIncome, isTrue);
      expect(incomeCategory.isExpense, isFalse);
    });
  });
}
