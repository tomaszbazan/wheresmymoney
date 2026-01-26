import 'package:flutter/material.dart';

import '../services/account_service.dart';
import '../utils/error_handler.dart';
import '../widgets/account_form_dialog.dart';
import '../widgets/account_list_item.dart';
import '../widgets/account_summary_card.dart';
import '../widgets/transfer_dialog.dart';

class AccountsPage extends StatefulWidget {
  final AccountService? accountService;

  const AccountsPage({super.key, this.accountService});

  @override
  State<AccountsPage> createState() => _AccountsPageState();
}

class _AccountsPageState extends State<AccountsPage> {
  late final AccountService _accountService;

  List<Map<String, dynamic>> accounts = [];

  bool _isLoading = true;
  String? _error;

  void _showAddAccountDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      builder:
          (context) => AccountFormDialog(
            onSave: (name, type, currency) {
              _addAccount(context, name, type: type, currency: currency);
              Navigator.of(context).pop();
            },
          ),
    );
  }

  Future<void> _addAccount(BuildContext context, String accountName, {String? type, String? currency}) async {
    try {
      final createdAccount = await _accountService.createAccount(accountName, type: type, currency: currency);

      setState(() {
        accounts.add({
          'id': createdAccount.id,
          'name': createdAccount.name,
          'balance': createdAccount.balance,
          'type': createdAccount.type,
          'currency': createdAccount.currency ?? 'PLN',
        });
      });

      if (mounted) {
        // ignore: use_build_context_synchronously
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Konto "$accountName" zostało dodane')));
      }
    } catch (e) {
      // ignore: use_build_context_synchronously
      ErrorHandler.showError(context, e);
    }
  }

  Future<bool> _showDeleteConfirmationDialog(BuildContext context, String accountName) async {
    return await showDialog<bool>(
          context: context,
          builder: (BuildContext context) {
            return AlertDialog(
              title: const Text('Usunąć konto?'),
              content: Text('Czy na pewno chcesz usunąć konto "$accountName"?'),
              actions: [
                TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Anuluj')),
                TextButton(onPressed: () => Navigator.of(context).pop(true), style: TextButton.styleFrom(foregroundColor: Colors.red), child: const Text('Usuń')),
              ],
            );
          },
        ) ??
        false;
  }

  Future<void> _deleteAccountById(Map<String, dynamic> account, BuildContext context) async {
    final accountId = account['id'] as String;
    final accountName = account['name'] as String;

    try {
      await _accountService.deleteAccount(accountId);

      setState(() {
        accounts.removeWhere((account) => account['id'] == accountId);
      });

      if (mounted) {
        // ignore: use_build_context_synchronously
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Konto "$accountName" zostało usunięte')));
      }
    } catch (e) {
      // ignore: use_build_context_synchronously
      ErrorHandler.showError(context, e);
    }
  }

  void _showTransferDialog(Map<String, dynamic> sourceAccount) {
    showDialog<void>(
      context: context,
      builder:
          (context) => TransferDialog(
            accounts: accounts,
            sourceAccount: sourceAccount,
            onSuccess: () {
              _fetchAccounts();
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Transfer zakończony sukcesem')));
            },
          ),
    );
  }

  @override
  void initState() {
    super.initState();
    _accountService = widget.accountService ?? RestAccountService();
    _fetchAccounts();
  }

  Map<String, double> _calculateCurrencySums() {
    final Map<String, double> sums = {};

    for (var account in accounts) {
      final currency = account['currency'] as String? ?? 'PLN';
      final balance = account['balance'] as double;

      sums[currency] = (sums[currency] ?? 0.0) + balance;
    }

    return sums;
  }

  Future<void> _fetchAccounts() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final fetchedAccounts = await _accountService.getAccounts();

      setState(() {
        accounts =
            fetchedAccounts
                .map((account) => {'id': account.id, 'name': account.name, 'balance': account.balance, 'type': account.type, 'currency': account.currency ?? 'PLN'})
                .toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = ErrorHandler.getErrorMessage(e);
        _isLoading = false;
        accounts = [];
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          if (accounts.isNotEmpty && !_isLoading) AccountSummaryCard(currencySums: _calculateCurrencySums()),
          const SizedBox(height: 16),

          if (_error != null) Padding(padding: const EdgeInsets.all(16.0), child: Text(_error!, style: const TextStyle(color: Colors.red))),

          if (_isLoading)
            const Padding(padding: EdgeInsets.all(32.0), child: Center(child: CircularProgressIndicator()))
          else
            Expanded(
              child:
                  accounts.isEmpty
                      ? const Center(child: Text('Brak kont do wyświetlenia'))
                      : ListView.builder(
                        itemCount: accounts.length,
                        itemBuilder: (context, index) {
                          final account = accounts[index];

                          return AccountListItem(
                            account: account,
                            onDeleteRequest: () {
                              _showDeleteConfirmationDialog(context, account['name'] as String).then((confirmed) {
                                if (confirmed && mounted) {
                                  final accountToDelete = Map<String, dynamic>.from(account);
                                  // ignore: use_build_context_synchronously
                                  _deleteAccountById(accountToDelete, context);
                                }
                              });
                            },
                            onDismissed: () {
                              final accountToDelete = Map<String, dynamic>.from(account);
                              _deleteAccountById(accountToDelete, context);
                            },
                            onTransferRequest: () => _showTransferDialog(account),
                          );
                        },
                      ),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton(onPressed: () => _showAddAccountDialog(context), tooltip: 'Dodaj konto', child: const Icon(Icons.add)),
    );
  }
}
