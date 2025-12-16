import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category.dart';
import 'package:frontend/widgets/category_option_item.dart';

void main() {
  group('CategoryOptionItem', () {
    Color parseColor(String colorString) {
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

    testWidgets('should render category name', (WidgetTester tester) async {
      final category = Category(id: '1', name: 'Food', description: 'Food expenses', type: 'EXPENSE', color: '#FF5722', createdAt: DateTime.now(), updatedAt: DateTime.now());

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryOptionItem(category: category, level: 0, parseColor: parseColor))));

      expect(find.text('Food'), findsOneWidget);
    });

    testWidgets('should display color indicator with correct color', (WidgetTester tester) async {
      final category = Category(id: '1', name: 'Food', description: 'Food expenses', type: 'EXPENSE', color: '#FF5722', createdAt: DateTime.now(), updatedAt: DateTime.now());

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryOptionItem(category: category, level: 0, parseColor: parseColor))));

      final containers = tester.widgetList<Container>(find.byType(Container));
      final colorIndicators = containers.where((container) {
        final decoration = container.decoration;
        return decoration is BoxDecoration && decoration.shape == BoxShape.circle;
      });

      expect(colorIndicators.isNotEmpty, isTrue);

      final colorIndicator = colorIndicators.first;
      final decoration = colorIndicator.decoration as BoxDecoration;
      expect(decoration.color, equals(parseColor('#FF5722')));
    });

    testWidgets('should apply correct indentation for level 0', (WidgetTester tester) async {
      final category = Category(id: '1', name: 'Food', description: 'Food expenses', type: 'EXPENSE', color: '#FF5722', createdAt: DateTime.now(), updatedAt: DateTime.now());

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryOptionItem(category: category, level: 0, parseColor: parseColor))));

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, equals(const EdgeInsets.only(left: 0.0, top: 8.0, bottom: 8.0, right: 8.0)));
    });

    testWidgets('should apply correct indentation for level 1', (WidgetTester tester) async {
      final category = Category(
        id: '2',
        name: 'Groceries',
        description: 'Grocery expenses',
        type: 'EXPENSE',
        color: '#4CAF50',
        parentId: '1',
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryOptionItem(category: category, level: 1, parseColor: parseColor))));

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, equals(const EdgeInsets.only(left: 24.0, top: 8.0, bottom: 8.0, right: 8.0)));
    });

    testWidgets('should apply correct indentation for level 2', (WidgetTester tester) async {
      final category = Category(
        id: '3',
        name: 'Vegetables',
        description: 'Vegetable expenses',
        type: 'EXPENSE',
        color: '#8BC34A',
        parentId: '2',
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: CategoryOptionItem(category: category, level: 2, parseColor: parseColor))));

      final padding = tester.widget<Padding>(find.byType(Padding).first);
      expect(padding.padding, equals(const EdgeInsets.only(left: 48.0, top: 8.0, bottom: 8.0, right: 8.0)));
    });
  });
}
