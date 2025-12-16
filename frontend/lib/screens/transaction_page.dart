import 'package:flutter/material.dart';

import '../models/account.dart';
import '../models/http_exception.dart';
import '../models/transaction.dart';
import '../models/transaction_type.dart';
import '../services/account_service.dart';
import '../services/transaction_service.dart';
import '../widgets/transaction_form.dart';
import '../widgets/transaction_list.dart';

class TransactionsPage extends StatefulWidget {
  final TransactionType type;

  const TransactionsPage({super.key, required this.type});

  @override
  State<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends State<TransactionsPage> {
  final TransactionService _transactionService = TransactionService();
  final RestAccountService _accountService = RestAccountService();

  List<Transaction> _transactions = [];
  List<Account> _accounts = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);

    try {
      final allTransactions = await _transactionService.getTransactions();
      final accounts = await _accountService.getAccounts();

      setState(() {
        _transactions = allTransactions.where((t) => widget.type == TransactionType.income ? t.isIncome : t.isExpense).toList();
        _accounts = accounts;
      });
    } on HttpException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.userFriendlyMessage), backgroundColor: Colors.red));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Nieoczekiwany błąd: $e'), backgroundColor: Colors.red));
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _showAddTransactionDialog() {
    showDialog<void>(
      context: context,
      builder:
          (context) => Dialog(
            child: SizedBox(
              width: 500,
              child: TransactionForm(
                accounts: _accounts,
                type: widget.type == TransactionType.income ? 'INCOME' : 'EXPENSE',
                onSaved: (transaction) {
                  Navigator.of(context).pop();
                  _loadData();
                },
              ),
            ),
          ),
    );
  }

  void _showEditTransactionDialog(Transaction transaction) {
    showDialog<void>(
      context: context,
      builder:
          (context) => Dialog(
            child: SizedBox(
              width: 500,
              child: TransactionForm(
                accounts: _accounts,
                transaction: transaction,
                onSaved: (updatedTransaction) {
                  Navigator.of(context).pop();
                  _loadData();
                },
              ),
            ),
          ),
    );
  }

  Future<void> _deleteTransaction(Transaction transaction) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Usuń transakcję'),
            content: Text('Czy na pewno chcesz usunąć transakcję "${transaction.description}"?'),
            actions: [
              TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Anuluj')),
              TextButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Usuń')),
            ],
          ),
    );

    if (confirmed == true) {
      try {
        await _transactionService.deleteTransaction(transaction.id);
        _loadData();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Transakcja została usunięta')));
        }
      } on HttpException catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.userFriendlyMessage), backgroundColor: Colors.red));
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Nieoczekiwany błąd: $e'), backgroundColor: Colors.red));
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.type == TransactionType.income ? 'Przychody' : 'Wydatki'), automaticallyImplyLeading: false),
      body:
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : TransactionList(transactions: _transactions, accounts: _accounts, onEdit: _showEditTransactionDialog, onDelete: _deleteTransaction),
      floatingActionButton: FloatingActionButton(onPressed: _showAddTransactionDialog, child: const Icon(Icons.add)),
    );
  }
}
