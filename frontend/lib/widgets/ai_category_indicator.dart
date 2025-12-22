import 'package:flutter/material.dart';

class AiCategoryIndicator extends StatelessWidget {
  final bool isSuggestedByAi;

  const AiCategoryIndicator({super.key, required this.isSuggestedByAi});

  @override
  Widget build(BuildContext context) {
    if (!isSuggestedByAi) {
      return const SizedBox.shrink();
    }

    return Tooltip(message: 'Zasugerowana przez AI', child: Icon(Icons.auto_awesome, size: 16, color: Theme.of(context).colorScheme.secondary));
  }
}
