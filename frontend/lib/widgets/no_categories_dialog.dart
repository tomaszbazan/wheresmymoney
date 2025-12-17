import 'package:flutter/material.dart';

import '../models/transaction_type.dart';

class NoCategoriesDialog extends StatelessWidget {
  final TransactionType type;

  const NoCategoriesDialog({super.key, required this.type});

  @override
  Widget build(BuildContext context) {
    final categoryTypeName = type == TransactionType.income ? 'przychodów' : 'wydatków';

    return AlertDialog(
      title: const Text('Brak kategorii'),
      content: Text(
        'Nie można dodać transakcji, ponieważ nie ma jeszcze żadnych kategorii $categoryTypeName. '
        'Proszę najpierw utworzyć kategorie w sekcji Kategorie $categoryTypeName.',
      ),
      actions: [ElevatedButton(onPressed: () => Navigator.of(context).pop(), child: const Text('OK'))],
    );
  }
}
