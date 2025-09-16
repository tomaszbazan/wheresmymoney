import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category.dart';
import 'package:frontend/utils/category_hierarchy.dart';

void main() {
  group('CategoryHierarchy', () {
    test('should sort top-level categories alphabetically', () {
      final categories = [
        Category(
          id: '3',
          name: 'Transport',
          description: '',
          type: 'EXPENSE',
          color: '#2196F3',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
        Category(
          id: '1',
          name: 'Food',
          description: '',
          type: 'EXPENSE',
          color: '#FF5722',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
        Category(
          id: '2',
          name: 'Entertainment',
          description: '',
          type: 'EXPENSE',
          color: '#9C27B0',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
      ];

      final hierarchy = CategoryHierarchy.buildHierarchy(categories);

      expect(hierarchy.length, equals(3));
      expect(hierarchy[0].category.name, equals('Entertainment'));
      expect(hierarchy[1].category.name, equals('Food'));
      expect(hierarchy[2].category.name, equals('Transport'));

      // All should be top level
      expect(hierarchy[0].level, equals(0));
      expect(hierarchy[1].level, equals(0));
      expect(hierarchy[2].level, equals(0));
    });

    test(
      'should group children under parents and sort them alphabetically',
      () {
        final categories = [
          Category(
            id: 'parent1',
            name: 'Food',
            description: '',
            type: 'EXPENSE',
            color: '#FF5722',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
          Category(
            id: 'child3',
            name: 'Restaurants',
            description: '',
            type: 'EXPENSE',
            color: '#FF9800',
            parentId: 'parent1',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
          Category(
            id: 'child1',
            name: 'Groceries',
            description: '',
            type: 'EXPENSE',
            color: '#4CAF50',
            parentId: 'parent1',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
          Category(
            id: 'child2',
            name: 'Takeout',
            description: '',
            type: 'EXPENSE',
            color: '#FF6B6B',
            parentId: 'parent1',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
          Category(
            id: 'parent2',
            name: 'Transport',
            description: '',
            type: 'EXPENSE',
            color: '#2196F3',
            createdAt: DateTime.now(),
            updatedAt: DateTime.now(),
          ),
        ];

        final hierarchy = CategoryHierarchy.buildHierarchy(categories);

        expect(
          hierarchy.length,
          equals(5),
        ); // Food parent + 3 children + Transport parent

        // Food should come before Transport (alphabetically)
        expect(hierarchy[0].category.name, equals('Food'));
        expect(hierarchy[0].level, equals(0)); // Top level

        // Food's children should be sorted alphabetically
        expect(hierarchy[1].category.name, equals('Groceries'));
        expect(hierarchy[1].level, equals(1)); // Child level
        expect(hierarchy[2].category.name, equals('Restaurants'));
        expect(hierarchy[2].level, equals(1)); // Child level
        expect(hierarchy[3].category.name, equals('Takeout'));
        expect(hierarchy[3].level, equals(1)); // Child level

        // Transport should come after Food and its children
        expect(hierarchy[4].category.name, equals('Transport'));
        expect(hierarchy[4].level, equals(0)); // Top level
      },
    );

    test('should handle empty category list', () {
      final hierarchy = CategoryHierarchy.buildHierarchy([]);
      expect(hierarchy, isEmpty);
    });

    test('should handle categories with no children', () {
      final categories = [
        Category(
          id: '1',
          name: 'Single Category',
          description: '',
          type: 'EXPENSE',
          color: '#FF5722',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
      ];

      final hierarchy = CategoryHierarchy.buildHierarchy(categories);

      expect(hierarchy.length, equals(1));
      expect(hierarchy[0].category.name, equals('Single Category'));
      expect(hierarchy[0].level, equals(0));
    });

    test('should handle multiple levels of nesting (grandchildren)', () {
      final categories = [
        Category(
          id: 'parent1',
          name: 'Food',
          description: '',
          type: 'EXPENSE',
          color: '#FF5722',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
        Category(
          id: 'child1',
          name: 'Restaurants',
          description: '',
          type: 'EXPENSE',
          color: '#FF9800',
          parentId: 'parent1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
        Category(
          id: 'grandchild1',
          name: 'Fast Food',
          description: '',
          type: 'EXPENSE',
          color: '#FF6B6B',
          parentId: 'child1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
        Category(
          id: 'grandchild2',
          name: 'Fine Dining',
          description: '',
          type: 'EXPENSE',
          color: '#9C27B0',
          parentId: 'child1',
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
        ),
      ];

      final hierarchy = CategoryHierarchy.buildHierarchy(categories);

      expect(hierarchy.length, equals(4)); // All categories should be included

      // Check parent category
      expect(hierarchy[0].category.name, equals('Food'));
      expect(hierarchy[0].level, equals(0));

      // Check child category
      expect(hierarchy[1].category.name, equals('Restaurants'));
      expect(hierarchy[1].level, equals(1));

      // Check grandchildren (should be sorted alphabetically)
      expect(hierarchy[2].category.name, equals('Fast Food'));
      expect(hierarchy[2].level, equals(2));
      expect(hierarchy[3].category.name, equals('Fine Dining'));
      expect(hierarchy[3].level, equals(2));
    });

    test('CategoryWithLevel should correctly identify levels', () {
      final category = Category(
        id: '1',
        name: 'Test',
        description: '',
        type: 'EXPENSE',
        color: '#FF5722',
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );

      final topLevel = CategoryWithLevel(category, 0);
      final childLevel = CategoryWithLevel(category, 1);
      final grandChildLevel = CategoryWithLevel(category, 2);

      expect(topLevel.isTopLevel, isTrue);
      expect(topLevel.isChild, isFalse);

      expect(childLevel.isTopLevel, isFalse);
      expect(childLevel.isChild, isTrue);

      expect(grandChildLevel.isTopLevel, isFalse);
      expect(grandChildLevel.isChild, isFalse);
    });
  });
}
