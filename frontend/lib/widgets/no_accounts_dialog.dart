import 'package:flutter/material.dart';

class NoAccountsDialog extends StatelessWidget {
  const NoAccountsDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Brak kont'),
      content: const Text(
        'Nie można dodać transakcji, ponieważ nie ma jeszcze żadnych kont. '
        'Proszę najpierw utworzyć konto w sekcji Konta.',
      ),
      actions: [ElevatedButton(onPressed: () => Navigator.of(context).pop(), child: const Text('OK'))],
    );
  }
}
