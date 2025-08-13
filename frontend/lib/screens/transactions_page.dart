import 'package:flutter/material.dart';

import '../models/account.dart';
import '../models/transaction.dart';
import '../services/account_service.dart';
import '../services/transaction_service.dart';
import '../widgets/transaction_form.dart';
import '../widgets/transaction_list.dart';

class TransactionsPage extends StatefulWidget {
  const TransactionsPage({super.key});

  @override
  State<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends State<TransactionsPage>
    with TickerProviderStateMixin {
  final TransactionService _transactionService = TransactionService();
  final AccountService _accountService = AccountService();

  late TabController _tabController;
  List<Transaction> _allTransactions = [];
  List<Account> _accounts = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);

    try {
      final transactions = await _transactionService.getTransactions();
      final accounts = await _accountService.getAccounts();

      setState(() {
        _allTransactions = transactions;
        _accounts = accounts;
      });
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Błąd ładowania danych: $e')));
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  List<Transaction> get _incomeTransactions =>
      _allTransactions.where((t) => t.isIncome).toList();

  List<Transaction> get _expenseTransactions =>
      _allTransactions.where((t) => t.isExpense).toList();

  void _showAddTransactionDialog(String type) {
    showDialog(
      context: context,
      builder:
          (context) => Dialog(
            child: SizedBox(
              width: 500,
              child: TransactionForm(
                accounts: _accounts,
                type: type,
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
    showDialog(
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
            content: Text(
              'Czy na pewno chcesz usunąć transakcję "${transaction.description}"?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('Anuluj'),
              ),
              TextButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: const Text('Usuń'),
              ),
            ],
          ),
    );

    if (confirmed == true) {
      try {
        await _transactionService.deleteTransaction(transaction.id);
        _loadData();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Transakcja została usunięta')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Błąd usuwania transakcji: $e')),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Transakcje'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.arrow_upward), text: 'Przychody'),
            Tab(icon: Icon(Icons.arrow_downward), text: 'Wydatki'),
          ],
        ),
      ),
      body:
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : TabBarView(
                controller: _tabController,
                children: [
                  TransactionList(
                    transactions: _incomeTransactions,
                    accounts: _accounts,
                    onEdit: _showEditTransactionDialog,
                    onDelete: _deleteTransaction,
                  ),
                  TransactionList(
                    transactions: _expenseTransactions,
                    accounts: _accounts,
                    onEdit: _showEditTransactionDialog,
                    onDelete: _deleteTransaction,
                  ),
                ],
              ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          final currentTab = _tabController.index;
          final type = currentTab == 0 ? 'INCOME' : 'EXPENSE';
          _showAddTransactionDialog(type);
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
