import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/widgets/ai_category_indicator.dart';

void main() {
  group('AiCategoryIndicator', () {
    testWidgets('shows icon when isSuggestedByAi is true', (tester) async {
      await tester.pumpWidget(const MaterialApp(home: Scaffold(body: AiCategoryIndicator(isSuggestedByAi: true))));

      expect(find.byType(Icon), findsOneWidget);
      expect(find.byIcon(Icons.auto_awesome), findsOneWidget);
    });

    testWidgets('does not show icon when isSuggestedByAi is false', (tester) async {
      await tester.pumpWidget(const MaterialApp(home: Scaffold(body: AiCategoryIndicator(isSuggestedByAi: false))));

      expect(find.byType(Icon), findsNothing);
    });

    testWidgets('has correct tooltip when shown', (tester) async {
      await tester.pumpWidget(const MaterialApp(home: Scaffold(body: AiCategoryIndicator(isSuggestedByAi: true))));

      final tooltip = find.byType(Tooltip);
      expect(tooltip, findsOneWidget);

      await tester.longPress(tooltip);
      await tester.pumpAndSettle();

      expect(find.text('Zasugerowana przez AI'), findsOneWidget);
    });
  });
}
