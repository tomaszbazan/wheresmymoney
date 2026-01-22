import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class NavigateToTabIntent extends Intent {
  final int tabIndex;

  const NavigateToTabIntent(this.tabIndex);
}

class KeyboardShortcuts {
  static const int totalTabs = 6;

  static Map<ShortcutActivator, Intent> getShortcuts() {
    final keys = [LogicalKeyboardKey.digit1, LogicalKeyboardKey.digit2, LogicalKeyboardKey.digit3, LogicalKeyboardKey.digit4, LogicalKeyboardKey.digit5, LogicalKeyboardKey.digit6];

    return {for (var i = 0; i < totalTabs; i++) SingleActivator(keys[i], alt: true): NavigateToTabIntent(i)};
  }
}
