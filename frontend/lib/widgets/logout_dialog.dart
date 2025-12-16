import 'package:flutter/material.dart';

import '../services/auth_service.dart';

class LogoutDialog extends StatelessWidget {
  const LogoutDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Wylogowanie'),
      content: const Text('Czy na pewno chcesz się wylogować?'),
      actions: [
        TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Anuluj')),
        TextButton(
          onPressed: () async {
            final authService = AuthService();
            await authService.signOut();
            if (context.mounted) {
              Navigator.of(context).pushNamedAndRemoveUntil('/', (route) => false);
            }
          },
          child: const Text('Wyloguj', style: TextStyle(color: Colors.red)),
        ),
      ],
    );
  }
}
