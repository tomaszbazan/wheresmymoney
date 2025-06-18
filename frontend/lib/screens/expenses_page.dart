import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/expense.dart';
import '../models/account.dart';
import '../services/expense_service.dart';
import '../services/account_service.dart';

class ExpensesPage extends StatefulWidget {
  const ExpensesPage({super.key});

  @override
  State<ExpensesPage> createState() => _ExpensesPageState();
}

class _ExpensesPageState extends State<ExpensesPage> {
  final ExpenseService _expenseService = ExpenseService();
  final AccountService _accountService = AccountService();
  late Future<List<Expense>> _expensesFuture;

  @override
  void initState() {
    super.initState();
    _loadExpenses();
  }

  void _loadExpenses() {
    _expensesFuture = _expenseService.getExpenses();
  }

  String _formatDate(String dateString) {
    try {
      final DateTime parsedDate = DateTime.parse(dateString);
      final DateTime localDate = parsedDate.toLocal();
      final DateFormat formatter = DateFormat('yyyy-MM-dd HH:mm');
      return formatter.format(localDate);
    } catch (e) {
      return dateString;
    }
  }

  void _showAddExpenseDialog() async {
    final accounts = await _accountService.getAccounts();
    if (!mounted) return;
    
    if (accounts.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Najpierw dodaj konto w zakładce Konta')),
      );
      return;
    }

    showDialog(
      context: context,
      builder: (context) => _AddExpenseDialog(
        accounts: accounts,
        onExpenseAdded: () {
          setState(() {
            _loadExpenses();
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<Expense>>(
      future: _expensesFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          return Center(child: Text('Error: ${snapshot.error}'));
        } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
          return const Center(child: Text('No expenses found'));
        }

        final List<Expense> expenses = snapshot.data!;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Ostatnie wydatki',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              ElevatedButton(
                onPressed: _showAddExpenseDialog,
                child: const Text('Dodaj'),
              ),
            ],
          ),
        ),
        Expanded(
          child: RefreshIndicator(
            onRefresh: () async {
              setState(() {
                _loadExpenses();
              });
            },
            child: ListView.builder(
              itemCount: expenses.length,
              itemBuilder: (context, index) {
                final expense = expenses[index];
                return Card(
                  margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: ListTile(
                    leading: const CircleAvatar(
                      child: Icon(Icons.account_balance_wallet, size: 20),
                    ),
                    title: Text(expense.description),
                    subtitle: Text(_formatDate(expense.date)),
                    trailing: Text(
                      '${expense.amount} ${expense.currency}',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ),
      ],
    );
      }
    );
  }
}

class _AddExpenseDialog extends StatefulWidget {
  final List<Account> accounts;
  final VoidCallback onExpenseAdded;

  const _AddExpenseDialog({
    required this.accounts,
    required this.onExpenseAdded,
  });

  @override
  State<_AddExpenseDialog> createState() => _AddExpenseDialogState();
}

class _AddExpenseDialogState extends State<_AddExpenseDialog> {
  final _formKey = GlobalKey<FormState>();
  final _descriptionController = TextEditingController();
  final _amountController = TextEditingController();
  Account? _selectedAccount;
  DateTime _selectedDate = DateTime.now();
  TimeOfDay _selectedTime = TimeOfDay.now();
  bool _isLoading = false;
  String _selectedCurrency = 'PLN';
  
  final List<String> _currencies = ['PLN', 'EUR', 'USD', 'GBP'];

  @override
  void dispose() {
    _descriptionController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );
    if (date != null) {
      setState(() {
        _selectedDate = date;
      });
    }
  }

  Future<void> _selectTime() async {
    final time = await showTimePicker(
      context: context,
      initialTime: _selectedTime,
    );
    if (time != null) {
      setState(() {
        _selectedTime = time;
      });
    }
  }

  Future<void> _submitExpense() async {
    if (!_formKey.currentState!.validate() || _selectedAccount == null) {
      return;
    }

    // Check currency mismatch
    if (_selectedAccount!.currency != null && 
        _selectedAccount!.currency != _selectedCurrency) {
      final shouldContinue = await _showCurrencyMismatchDialog();
      if (!shouldContinue) return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final expenseService = ExpenseService();
      
      // Combine date and time with system timezone
      final combinedDateTime = DateTime(
        _selectedDate.year,
        _selectedDate.month,
        _selectedDate.day,
        _selectedTime.hour,
        _selectedTime.minute,
      );
      
      // Format with timezone offset (e.g., 2025-06-11T23:02:00+02:00)
      final formattedDateTime = '${combinedDateTime.toIso8601String()}${_getTimezoneOffset()}';
      
      await expenseService.createExpense(
        _selectedAccount!.id,
        double.parse(_amountController.text),
        _descriptionController.text,
        formattedDateTime,
        _selectedCurrency,
      );

      if (mounted) {
        Navigator.of(context).pop();
        widget.onExpenseAdded();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Wydatek został dodany')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Błąd: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  String _getTimezoneOffset() {
    final now = DateTime.now();
    final offset = now.timeZoneOffset;
    final hours = offset.inHours.abs().toString().padLeft(2, '0');
    final minutes = (offset.inMinutes.abs() % 60).toString().padLeft(2, '0');
    final sign = offset.isNegative ? '-' : '+';
    return '$sign$hours:$minutes';
  }

  Future<bool> _showCurrencyMismatchDialog() async {
    return await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Niezgodność waluty'),
        content: Text(
          'Wybrana waluta wydatku ($_selectedCurrency) różni się od waluty konta (${_selectedAccount!.currency}). '
          'Czy chcesz kontynuować?'
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Anuluj'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Kontynuuj'),
          ),
        ],
      ),
    ) ?? false;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Dodaj wydatek'),
      content: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            DropdownButtonFormField<Account>(
              value: _selectedAccount,
              decoration: const InputDecoration(
                labelText: 'Konto',
                border: OutlineInputBorder(),
              ),
              items: widget.accounts.map((account) {
                return DropdownMenuItem(
                  value: account,
                  child: Text(
                    account.currency != null 
                        ? '${account.name} (${account.currency})'
                        : account.name
                  ),
                );
              }).toList(),
              onChanged: (account) {
                setState(() {
                  _selectedAccount = account;
                });
              },
              validator: (value) {
                if (value == null) {
                  return 'Wybierz konto';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(
                labelText: 'Opis',
                border: OutlineInputBorder(),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Wprowadź opis';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  flex: 2,
                  child: TextFormField(
                    controller: _amountController,
                    decoration: const InputDecoration(
                      labelText: 'Kwota',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return 'Wprowadź kwotę';
                      }
                      if (double.tryParse(value) == null) {
                        return 'Wprowadź prawidłową kwotę';
                      }
                      return null;
                    },
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: DropdownButtonFormField<String>(
                    value: _selectedCurrency,
                    decoration: const InputDecoration(
                      labelText: 'Waluta',
                      border: OutlineInputBorder(),
                    ),
                    items: _currencies.map((currency) {
                      return DropdownMenuItem(
                        value: currency,
                        child: Text(currency),
                      );
                    }).toList(),
                    onChanged: (currency) {
                      if (currency != null) {
                        setState(() {
                          _selectedCurrency = currency;
                        });
                      }
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.calendar_today),
              title: Text('Data: ${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}'),
              onTap: _selectDate,
            ),
            ListTile(
              leading: const Icon(Icons.access_time),
              title: Text('Godzina: ${_selectedTime.format(context)}'),
              onTap: _selectTime,
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: _isLoading ? null : () => Navigator.of(context).pop(),
          child: const Text('Anuluj'),
        ),
        ElevatedButton(
          onPressed: _isLoading ? null : _submitExpense,
          child: _isLoading
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Text('Dodaj'),
        ),
      ],
    );
  }
}