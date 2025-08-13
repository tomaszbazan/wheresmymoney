import 'package:flutter/material.dart';

import '../models/http_exception.dart';
import '../services/account_service.dart';

class AccountsPage extends StatefulWidget {
  final AccountServiceInterface? accountService;
  
  const AccountsPage({super.key, this.accountService});

  @override
  State<AccountsPage> createState() => _AccountsPageState();
}

class _AccountsPageState extends State<AccountsPage> {
  late final AccountServiceInterface _accountService;
  
  List<Map<String, dynamic>> accounts = [];
  
  bool _isLoading = true;
  String? _error;
  
  void _showAddAccountDialog(BuildContext context) {
    final TextEditingController nameController = TextEditingController();
    String selectedCurrency = 'PLN';
    String selectedType = 'Rachunek bieżący';

    const List<String> availableCurrencies = ['PLN', 'EUR', 'USD', 'GBP'];
    const List<String> availableAccountTypes = [
      'Rachunek bieżący',
      'Oszczędnościowe',
      'Gotówka',
      'Kredytowa',
    ];
    
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('Dodaj konto'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    TextField(
                      controller: nameController,
                      decoration: const InputDecoration(
                        labelText: 'Nazwa konta',
                        hintText: 'Wpisz nazwę konta',
                      ),
                      autofocus: true,
                    ),
                    const SizedBox(height: 16),
                    const Text('Typ konta:'),
                    DropdownButton<String>(
                      isExpanded: true,
                      value: selectedType,
                      items: availableAccountTypes.map((String type) {
                        return DropdownMenuItem<String>(
                          value: type,
                          child: Text(type),
                        );
                      }).toList(),
                      onChanged: (String? newValue) {
                        if (newValue != null) {
                          setState(() {
                            selectedType = newValue;
                          });
                        }
                      },
                    ),
                    const SizedBox(height: 16),
                    const Text('Waluta:'),
                    DropdownButton<String>(
                      isExpanded: true,
                      value: selectedCurrency,
                      items: availableCurrencies.map((String currency) {
                        return DropdownMenuItem<String>(
                          value: currency,
                          child: Text(currency),
                        );
                      }).toList(),
                      onChanged: (String? newValue) {
                        if (newValue != null) {
                          setState(() {
                            selectedCurrency = newValue;
                          });
                        }
                      },
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Anuluj'),
                ),
                ElevatedButton(
                  onPressed: () {
                    if (nameController.text.isNotEmpty) {
                      _addAccount(
                        context, 
                        nameController.text,
                        type: selectedType,
                        currency: selectedCurrency,
                      );
                      Navigator.of(context).pop();
                    }
                  },
                  child: const Text('Dodaj'),
                ),
              ],
            );
          }
        );
      },
    );
  }
  
  Future<void> _addAccount(
    BuildContext context, 
    String accountName, 
    {String? type, String? currency}
  ) async {
    final scaffoldMessenger = ScaffoldMessenger.of(context);
    
    try {
      final createdAccount = await _accountService.createAccount(
        accountName,
        type: type,
        currency: currency,
      );
      
      setState(() {
        accounts.add({
          'id': createdAccount.id,
          'name': createdAccount.name,
          'balance': createdAccount.balance,
          'number': createdAccount.number,
          'type': createdAccount.type,
          'currency': createdAccount.currency ?? 'PLN'
        });
      });
      
      scaffoldMessenger.showSnackBar(
        SnackBar(content: Text('Konto "$accountName" zostało dodane')),
      );
    } on HttpException catch (e) {
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text(e.userFriendlyMessage),
          backgroundColor: Colors.red,
        ),
      );
    } catch (e) {
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text('Nieoczekiwany błąd: $e'),
          backgroundColor: Colors.red,
        ),
      );
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
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('Anuluj'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('Usuń'),
            ),
          ],
        );
      },
    ) ?? false;
  }
  
  Future<void> _deleteAccountById(
    Map<String, dynamic> account,
    ScaffoldMessengerState scaffoldMessenger
  ) async {
    final accountId = account['id'];
    final accountName = account['name'];
    
    try {
      await _accountService.deleteAccount(accountId);
      
      setState(() {
        accounts.removeWhere((account) => account['id'] == accountId);
      });
      
      scaffoldMessenger.showSnackBar(
        SnackBar(content: Text('Konto "$accountName" zostało usunięte')),
      );
    } on HttpException catch (e) {
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text(e.userFriendlyMessage),
          backgroundColor: Colors.red,
        ),
      );
    } catch (e) {
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text('Nieoczekiwany błąd: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  void initState() {
    super.initState();
    _accountService = widget.accountService ?? AccountService();
    _fetchAccounts();
  }
  
  Map<String, double> _calculateCurrencySums() {
    final Map<String, double> sums = {};
    
    for (var account in accounts) {
      final currency = account['currency'] ?? 'PLN';
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
        accounts = fetchedAccounts.map((account) => {
          'id': account.id,
          'name': account.name,
          'balance': account.balance,
          'number': account.number,
          'type': account.type,
          'currency': account.currency ?? 'PLN',
        }).toList();
        _isLoading = false;
      });
    } on HttpException catch (e) {
      setState(() {
        _error = e.userFriendlyMessage;
        _isLoading = false;
        accounts = [];
      });
    } catch (e) {
      setState(() {
        _error = 'Nieoczekiwany błąd: $e';
        _isLoading = false;
        accounts = [];
      });
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              // Przycisk odświeżania
              IconButton(
                icon: const Icon(Icons.refresh),
                onPressed: _isLoading ? null : _fetchAccounts,
                tooltip: 'Odśwież listę kont',
              ),
              const SizedBox(width: 8),
              ElevatedButton(
                onPressed: () => _showAddAccountDialog(context),
                child: const Text('Dodaj konto'),
              ),
            ],
          ),
        ),
        if (accounts.isNotEmpty && !_isLoading)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Saldo łączne według walut:',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 8),
                    ..._calculateCurrencySums().entries.map((entry) {
                      final isNegative = entry.value < 0;
                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 4.0),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(entry.key),
                            Text(
                              entry.value.toStringAsFixed(2),
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                color: isNegative ? Colors.red : Colors.black,
                              ),
                            ),
                          ],
                        ),
                      );
                    }),
                  ],
                ),
              ),
            ),
          ),
        const SizedBox(height: 16),
        
        if (_error != null)
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text(
              _error!,
              style: const TextStyle(color: Colors.red),
            ),
          ),
        
        if (_isLoading)
          const Padding(
            padding: EdgeInsets.all(32.0),
            child: Center(child: CircularProgressIndicator()),
          )
        else
          Expanded(
            child: accounts.isEmpty
                ? const Center(child: Text('Brak kont do wyświetlenia'))
                : ListView.builder(
                    itemCount: accounts.length,
                    itemBuilder: (context, index) {
                      final account = accounts[index];
                      final isNegative = (account['balance'] as double) < 0;
              
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Dismissible(
                  key: Key(account['name']),
                  background: Container(
                    color: Colors.red,
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 20.0),
                    child: const Icon(
                      Icons.delete,
                      color: Colors.white,
                    ),
                  ),
                  direction: DismissDirection.endToStart,
                  confirmDismiss: (direction) async {
                    return await _showDeleteConfirmationDialog(context, account['name']);
                  },
                  onDismissed: (direction) {
                    final scaffoldMessenger = ScaffoldMessenger.of(context);
                    final accountToDelete = Map<String, dynamic>.from(account);
                    _deleteAccountById(accountToDelete, scaffoldMessenger);
                  },
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: _getAccountColor(account['type']),
                      child: _getAccountIcon(account['type']),
                    ),
                    title: Text(account['name']),
                    subtitle: account['number'] != null 
                        ? Text('${account['type']} ${account['number']}')
                        : Text(account['type']),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          '${account['balance'].toStringAsFixed(2)} ${account['currency'] ?? 'zł'}',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: isNegative ? Colors.red : Colors.black,
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.delete_outline, color: Colors.red),
                          onPressed: () {
                            // Tworzymy lokalne kopie danych przed async gap
                            final accountToDelete = Map<String, dynamic>.from(account);
                            final scaffoldMessenger = ScaffoldMessenger.of(context);
                            
                            _showDeleteConfirmationDialog(context, account['name'])
                                .then((confirmed) {
                              if (confirmed && mounted) {
                                _deleteAccountById(accountToDelete, scaffoldMessenger);
                              }
                            });
                          },
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _getAccountIcon(String? type) {
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

  Color _getAccountColor(String? type) {
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