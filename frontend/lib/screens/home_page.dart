import 'package:flutter/material.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.account_balance_wallet,
            size: 100,
            color: Colors.blue,
          ),
          const SizedBox(height: 24),
          const Text(
            'Witaj w aplikacji Where\'s My Money!',
            style: TextStyle(fontSize: 24),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          const Text(
            'Śledź swoje wydatki i zarządzaj finansami',
            style: TextStyle(fontSize: 16),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),
          ElevatedButton(
            onPressed: () {},
            child: const Text('Dodaj nowy wydatek'),
          ),
        ],
      ),
    );
  }
}