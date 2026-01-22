import 'package:flutter/material.dart';

import '../utils/keyboard_shortcuts.dart';

class KeyboardNavigationWrapper extends StatelessWidget {
  final Widget child;
  final void Function(int) onTabSelected;
  final FocusNode focusNode;

  const KeyboardNavigationWrapper({super.key, required this.child, required this.onTabSelected, required this.focusNode});

  @override
  Widget build(BuildContext context) {
    return Shortcuts(
      shortcuts: KeyboardShortcuts.getShortcuts(),
      child: Actions(
        actions: {
          NavigateToTabIntent: CallbackAction<NavigateToTabIntent>(
            onInvoke: (intent) {
              onTabSelected(intent.tabIndex);
              return null;
            },
          ),
        },
        child: FocusableActionDetector(focusNode: focusNode, autofocus: true, child: child),
      ),
    );
  }
}
