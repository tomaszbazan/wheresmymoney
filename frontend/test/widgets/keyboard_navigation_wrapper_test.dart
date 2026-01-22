import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/utils/keyboard_shortcuts.dart';
import 'package:frontend/widgets/keyboard_navigation_wrapper.dart';

void main() {
  group('KeyboardNavigationWrapper', () {
    testWidgets('renders child widget', (tester) async {
      final focusNode = FocusNode();
      var callbackInvoked = false;

      await tester.pumpWidget(
        MaterialApp(
          home: KeyboardNavigationWrapper(
            focusNode: focusNode,
            onTabSelected: (_) {
              callbackInvoked = true;
            },
            child: const Text('Test Child'),
          ),
        ),
      );

      expect(find.text('Test Child'), findsOneWidget);
      expect(callbackInvoked, isFalse);

      focusNode.dispose();
    });

    testWidgets('uses FocusNode correctly', (tester) async {
      final focusNode = FocusNode();

      await tester.pumpWidget(MaterialApp(home: Scaffold(body: KeyboardNavigationWrapper(focusNode: focusNode, onTabSelected: (_) {}, child: const Text('Test')))));

      focusNode.requestFocus();
      await tester.pump();

      expect(focusNode.hasFocus, isTrue);

      focusNode.dispose();
    });

    testWidgets('invokes onTabSelected when action is triggered', (tester) async {
      final focusNode = FocusNode();
      int? receivedTabIndex;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: KeyboardNavigationWrapper(
              focusNode: focusNode,
              onTabSelected: (index) {
                receivedTabIndex = index;
              },
              child: const Text('Test'),
            ),
          ),
        ),
      );

      focusNode.requestFocus();
      await tester.pump();

      final context = tester.element(find.text('Test'));
      Actions.invoke(context, const NavigateToTabIntent(2));

      expect(receivedTabIndex, equals(2));

      focusNode.dispose();
    });

    testWidgets('callback receives correct tabIndex from intent', (tester) async {
      final focusNode = FocusNode();
      final receivedIndices = <int>[];

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: KeyboardNavigationWrapper(
              focusNode: focusNode,
              onTabSelected: (index) {
                receivedIndices.add(index);
              },
              child: const Text('Test'),
            ),
          ),
        ),
      );

      focusNode.requestFocus();
      await tester.pump();

      final context = tester.element(find.text('Test'));

      Actions.invoke(context, const NavigateToTabIntent(0));
      Actions.invoke(context, const NavigateToTabIntent(3));
      Actions.invoke(context, const NavigateToTabIntent(5));

      expect(receivedIndices, equals([0, 3, 5]));

      focusNode.dispose();
    });

    testWidgets('handles keyboard shortcut Alt+1', (tester) async {
      final focusNode = FocusNode();
      int? receivedTabIndex;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: KeyboardNavigationWrapper(
              focusNode: focusNode,
              onTabSelected: (index) {
                receivedTabIndex = index;
              },
              child: const Text('Test'),
            ),
          ),
        ),
      );

      focusNode.requestFocus();
      await tester.pump();

      await tester.sendKeyDownEvent(LogicalKeyboardKey.alt);
      await tester.sendKeyDownEvent(LogicalKeyboardKey.digit1);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.digit1);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.alt);

      await tester.pump();

      expect(receivedTabIndex, equals(0));

      focusNode.dispose();
    });

    testWidgets('handles keyboard shortcut Alt+6', (tester) async {
      final focusNode = FocusNode();
      int? receivedTabIndex;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: KeyboardNavigationWrapper(
              focusNode: focusNode,
              onTabSelected: (index) {
                receivedTabIndex = index;
              },
              child: const Text('Test'),
            ),
          ),
        ),
      );

      focusNode.requestFocus();
      await tester.pump();

      await tester.sendKeyDownEvent(LogicalKeyboardKey.alt);
      await tester.sendKeyDownEvent(LogicalKeyboardKey.digit6);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.digit6);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.alt);

      await tester.pump();

      expect(receivedTabIndex, equals(5));

      focusNode.dispose();
    });
  });
}
