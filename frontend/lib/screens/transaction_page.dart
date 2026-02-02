import 'package:flutter/material.dart';

import '../models/account.dart';
import '../models/category_type.dart';
import '../models/transaction/transaction.dart';
import '../models/transaction_type.dart';
import '../services/account_service.dart';
import '../services/category_service.dart';
import '../services/csv_import_service.dart';
import '../services/transaction_service.dart';
import '../utils/error_handler.dart';
import '../widgets/csv_upload_dialog.dart';
import '../widgets/no_accounts_dialog.dart';
import '../widgets/no_categories_dialog.dart';
import '../widgets/transaction_list.dart';
import '../widgets/transaction_form.dart';

class TransactionsPage extends StatefulWidget {
  final TransactionType type;
  final TransactionService? transactionService;
  final AccountService? accountService;
  final CategoryService? categoryService;
  final CsvImportService? csvImportService;

  const TransactionsPage({super.key, required this.type, this.transactionService, this.accountService, this.categoryService, this.csvImportService});

  @override
  State<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends State<TransactionsPage> {
  late final TransactionService _transactionService;
  late final AccountService _accountService;
  late final CategoryService _categoryService;
  late final CsvImportService _csvImportService;

  List<Account> _accounts = [];
  bool _isLoading = false;
  VoidCallback? _refreshCallback;

  @override
  void initState() {
    super.initState();
    _transactionService = widget.transactionService ?? RestTransactionService();
    _accountService = widget.accountService ?? RestAccountService();
    _categoryService = widget.categoryService ?? RestCategoryService();
    _csvImportService = widget.csvImportService ?? RestCsvImportService();

    _loadAccounts();
  }

  Future<void> _loadAccounts() async {
    setState(() => _isLoading = true);

    try {
      final accounts = await _accountService.getAccounts();

      setState(() {
        _accounts = accounts;
      });
    } catch (e) {
      // ignore: use_build_context_synchronously
      ErrorHandler.showError(context, e);
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _refreshTransactionList() {
    _refreshCallback?.call();
  }

  Future<bool> _hasCategoriesForType() async {
    try {
      final categoryType = widget.type == TransactionType.income ? CategoryType.income : CategoryType.expense;
      final categories = await _categoryService.getCategoriesByType(categoryType);
      return categories.isNotEmpty;
    } catch (e) {
      return false;
    }
  }

  Future<void> _showAddTransactionDialog() async {
    final hasCategories = await _hasCategoriesForType();

    if (!hasCategories && mounted) {
      showDialog<void>(context: context, builder: (context) => NoCategoriesDialog(type: widget.type));
      return;
    }

    if (_accounts.isEmpty && mounted) {
      showDialog<void>(context: context, builder: (context) => const NoAccountsDialog());
      return;
    }

    if (!mounted) return;

    showDialog<void>(
      context: context,
      builder:
          (context) => Dialog(
            child: SizedBox(
              width: 500,
              child: TransactionForm(
                accounts: _accounts,
                type: widget.type,
                onSaved: (transaction) {
                  Navigator.of(context).pop();
                  _refreshTransactionList();
                },
              ),
            ),
          ),
    );
  }

  Future<void> _showEditTransactionDialog(Transaction transaction) async {
    final hasCategories = await _hasCategoriesForType();

    if (!hasCategories && mounted) {
      showDialog<void>(context: context, builder: (context) => NoCategoriesDialog(type: widget.type));
      return;
    }

    if (!mounted) return;

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
                  _refreshTransactionList();
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
        _refreshTransactionList();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Transakcja została usunięta')));
        }
      } catch (e) {
        // ignore: use_build_context_synchronously
        ErrorHandler.showError(context, e);
      }
    }
  }

  Future<void> _showCsvUploadDialog() async {
    if (_accounts.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Nie można importować transakcji - brak kont'), backgroundColor: Colors.red));
      return;
    }

    final result = await showDialog<bool>(context: context, builder: (context) => CsvUploadDialog(csvImportService: _csvImportService, accounts: _accounts));

    if (result == true) {
      _refreshTransactionList();
    }
  }

  void _showAddTransactionMenu() {
    showModalBottomSheet<void>(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.edit),
                title: const Text('Dodaj ręcznie'),
                onTap: () {
                  Navigator.pop(context);
                  _showAddTransactionDialog();
                },
              ),
              ListTile(
                leading: const Icon(Icons.file_upload),
                title: const Text('Importuj z CSV'),
                onTap: () {
                  Navigator.pop(context);
                  _showCsvUploadDialog();
                },
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.type == TransactionType.income ? 'Przychody' : 'Wydatki'), automaticallyImplyLeading: false),
      body:
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : TransactionList(
                accounts: _accounts,
                type: widget.type,
                transactionService: _transactionService,
                onEdit: _showEditTransactionDialog,
                onDelete: _deleteTransaction,
                onRefreshRequested: (callback) => _refreshCallback = callback,
              ),
      floatingActionButton: FloatingActionButton(onPressed: _showAddTransactionMenu, tooltip: 'Dodaj transakcję', child: const Icon(Icons.add)),
    );
  }
}
