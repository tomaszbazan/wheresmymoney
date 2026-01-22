import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/utils/keyboard_shortcuts.dart';

void main() {
  group('NavigateToTabIntent', () {
    test('creates with correct tabIndex', () {
      const intent = NavigateToTabIntent(3);
      expect(intent.tabIndex, equals(3));
    });

    test('creates with tabIndex 0', () {
      const intent = NavigateToTabIntent(0);
      expect(intent.tabIndex, equals(0));
    });

    test('creates with tabIndex 5', () {
      const intent = NavigateToTabIntent(5);
      expect(intent.tabIndex, equals(5));
    });
  });

  group('KeyboardShortcuts', () {
    test('getShortcuts returns map with 6 shortcuts', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();
      expect(shortcuts.length, equals(6));
    });

    test('Alt+1 maps to NavigateToTabIntent(0)', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();
      final entries = shortcuts.entries.toList();

      final firstEntry = entries[0];
      final activator = firstEntry.key as SingleActivator;

      expect(activator.trigger, equals(LogicalKeyboardKey.digit1));
      expect(activator.alt, isTrue);
      expect(firstEntry.value, isA<NavigateToTabIntent>());
      expect((firstEntry.value as NavigateToTabIntent).tabIndex, equals(0));
    });

    test('Alt+2 maps to NavigateToTabIntent(1)', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();
      final entries = shortcuts.entries.toList();

      final secondEntry = entries[1];
      final activator = secondEntry.key as SingleActivator;

      expect(activator.trigger, equals(LogicalKeyboardKey.digit2));
      expect(activator.alt, isTrue);
      expect(secondEntry.value, isA<NavigateToTabIntent>());
      expect((secondEntry.value as NavigateToTabIntent).tabIndex, equals(1));
    });

    test('Alt+6 maps to NavigateToTabIntent(5)', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();
      final entries = shortcuts.entries.toList();

      final sixthEntry = entries[5];
      final activator = sixthEntry.key as SingleActivator;

      expect(activator.trigger, equals(LogicalKeyboardKey.digit6));
      expect(activator.alt, isTrue);
      expect(sixthEntry.value, isA<NavigateToTabIntent>());
      expect((sixthEntry.value as NavigateToTabIntent).tabIndex, equals(5));
    });

    test('all shortcuts use Alt modifier', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();

      for (final activator in shortcuts.keys) {
        expect(activator, isA<SingleActivator>());
        final singleActivator = activator as SingleActivator;
        expect(singleActivator.alt, isTrue);
        expect(singleActivator.control, isFalse);
        expect(singleActivator.shift, isFalse);
        expect(singleActivator.meta, isFalse);
      }
    });

    test('shortcuts map digit keys 1 through 6', () {
      final shortcuts = KeyboardShortcuts.getShortcuts();
      final keys = shortcuts.keys.cast<SingleActivator>().map((a) => a.trigger).toList();

      expect(
        keys,
        containsAll([
          LogicalKeyboardKey.digit1,
          LogicalKeyboardKey.digit2,
          LogicalKeyboardKey.digit3,
          LogicalKeyboardKey.digit4,
          LogicalKeyboardKey.digit5,
          LogicalKeyboardKey.digit6,
        ]),
      );
    });

    test('totalTabs constant equals 6', () {
      expect(KeyboardShortcuts.totalTabs, equals(6));
    });
  });
}
