import 'package:flutter/material.dart';

class AccountTypeHelper {
  static Icon getIconForType(String? type) {
    switch (type) {
      case 'Rachunek bieżący':
        return const Icon(Icons.account_balance, size: 20, color: Colors.white);
      case 'Oszczędnościowe':
        return const Icon(Icons.savings, size: 20, color: Colors.white);
      case 'Gotówka':
        return const Icon(Icons.payments, size: 20, color: Colors.white);
      case 'Kredytowa':
        return const Icon(Icons.credit_card, size: 20, color: Colors.white);
      default:
        return const Icon(Icons.account_balance_wallet, size: 20, color: Colors.white);
    }
  }

  static Color getColorForType(String? type) {
    switch (type) {
      case 'Rachunek bieżący':
        return Colors.blue;
      case 'Oszczędnościowe':
        return Colors.green;
      case 'Gotówka':
        return Colors.amber.shade700;
      case 'Kredytowa':
        return Colors.purple;
      default:
        return Colors.grey;
    }
  }
}
