import 'package:flutter/material.dart';

import '../models/account.dart';
import '../models/transaction.dart';

class TransactionList extends StatelessWidget {
  final List<Transaction> transactions;
  final List<Account> accounts;
  final void Function(Transaction) onEdit;
  final void Function(Transaction) onDelete;

  const TransactionList({super.key, required this.transactions, required this.accounts, required this.onEdit, required this.onDelete});

  String _getAccountName(String accountId) {
    final account = accounts.firstWhere((a) => a.id == accountId, orElse: () => Account(id: accountId, name: 'Nieznane konto', balance: 0.0));
    return account.name;
  }

  String _getAccountCurrency(String accountId) {
    final account = accounts.firstWhere((a) => a.id == accountId, orElse: () => Account(id: accountId, name: 'Nieznane konto', balance: 0.0));
    return account.currency ?? 'PLN';
  }

  @override
  Widget build(BuildContext context) {
    if (transactions.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.receipt_long_outlined, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text('Brak transakcji', style: Theme.of(context).textTheme.titleMedium?.copyWith(color: Colors.grey[600])),
            const SizedBox(height: 8),
            Text('Dodaj pierwszą transakcję klikając przycisk +', style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.grey[600]), textAlign: TextAlign.center),
          ],
        ),
      );
    }

    return ListView.builder(
      itemCount: transactions.length,
      itemBuilder: (context, index) {
        final transaction = transactions[index];
        final isIncome = transaction.isIncome;

        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: isIncome ? Colors.green[100] : Colors.red[100],
              child: Icon(isIncome ? Icons.arrow_upward : Icons.arrow_downward, color: isIncome ? Colors.green[700] : Colors.red[700]),
            ),
            title: Text(transaction.description, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Konto: ${_getAccountName(transaction.accountId)}'),
                Text('Kategoria: ${transaction.categoryName ?? transaction.categoryId}'),
                Text(
                  '${transaction.createdAt.day}.${transaction.createdAt.month.toString().padLeft(2, '0')}.${transaction.createdAt.year}',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  '${transaction.amount.abs().toStringAsFixed(2)} ${_getAccountCurrency(transaction.accountId)}',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: isIncome ? Colors.green[700] : Colors.red[700]),
                ),
                PopupMenuButton<String>(
                  onSelected: (value) {
                    switch (value) {
                      case 'edit':
                        onEdit(transaction);
                        break;
                      case 'delete':
                        onDelete(transaction);
                        break;
                    }
                  },
                  itemBuilder:
                      (context) => [
                        const PopupMenuItem(value: 'edit', child: ListTile(leading: Icon(Icons.edit), title: Text('Edytuj'), contentPadding: EdgeInsets.zero)),
                        const PopupMenuItem(value: 'delete', child: ListTile(leading: Icon(Icons.delete), title: Text('Usuń'), contentPadding: EdgeInsets.zero)),
                      ],
                ),
              ],
            ),
            isThreeLine: true,
          ),
        );
      },
    );
  }
}
