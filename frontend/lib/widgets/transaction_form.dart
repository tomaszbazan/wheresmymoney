import 'package:flutter/material.dart';

import '../models/account.dart';
import '../models/http_exception.dart';
import '../models/transaction.dart';
import '../services/transaction_service.dart';

class TransactionForm extends StatefulWidget {
  final List<Account> accounts;
  final Transaction? transaction;
  final String? type;
  final Function(Transaction) onSaved;

  const TransactionForm({
    super.key,
    required this.accounts,
    this.transaction,
    this.type,
    required this.onSaved,
  });

  static String normalizeAmount(String amount) {
    String normalized = amount.trim().replaceAll(',', '.');
    
    if (!normalized.contains('.')) {
      normalized = '$normalized.00';
    } else {
      final parts = normalized.split('.');
      if (parts.length == 2 && parts[1].length == 1) {
        normalized = '${parts[0]}.${parts[1]}0';
      }
    }
    
    return normalized;
  }

  @override
  State<TransactionForm> createState() => _TransactionFormState();
}

class _TransactionFormState extends State<TransactionForm> {
  final _formKey = GlobalKey<FormState>();
  final _transactionService = TransactionService();

  late TextEditingController _amountController;
  late TextEditingController _descriptionController;
  late TextEditingController _categoryController;

  String? _selectedAccountId;
  String? _selectedType;
  String _selectedCurrency = 'PLN';
  DateTime _selectedDate = DateTime.now();
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();

    _amountController = TextEditingController(
      text: widget.transaction?.amount.abs().toStringAsFixed(2) ?? '',
    );
    _descriptionController = TextEditingController(
      text: widget.transaction?.description ?? '',
    );
    _categoryController = TextEditingController(
      text: widget.transaction?.category ?? '',
    );

    if (widget.transaction != null) {
      _selectedAccountId = widget.transaction!.accountId;
      _selectedType = widget.transaction!.type;
      _selectedDate = widget.transaction!.createdAt;
      _updateCurrencyFromAccount();
    } else {
      _selectedType = widget.type;
      _selectedAccountId =
          widget.accounts.isNotEmpty ? widget.accounts.first.id : null;
      _updateCurrencyFromAccount();
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    _categoryController.dispose();
    super.dispose();
  }

  void _updateCurrencyFromAccount() {
    if (_selectedAccountId != null) {
      final selectedAccount = widget.accounts.firstWhere(
        (account) => account.id == _selectedAccountId,
        orElse: () => widget.accounts.first,
      );
      _selectedCurrency = selectedAccount.currency ?? 'PLN';
    }
  }

  Future<void> _saveTransaction() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedAccountId == null) return;

    setState(() => _isLoading = true);

    try {
      final normalizedAmount = TransactionForm.normalizeAmount(_amountController.text);
      final Transaction transaction;

      if (widget.transaction != null) {
        transaction = await _transactionService.updateTransaction(
          id: widget.transaction!.id,
          amount: double.parse(normalizedAmount),
          description: _descriptionController.text,
          category: _categoryController.text,
          currency: _selectedCurrency,
        );
      } else {
        transaction = await _transactionService.createTransaction(
          accountId: _selectedAccountId!,
          amount: double.parse(normalizedAmount),
          description: _descriptionController.text,
          date: _selectedDate,
          type: _selectedType!,
          category: _categoryController.text,
          currency: _selectedCurrency,
        );
      }

      widget.onSaved(transaction);
    } on HttpException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(e.userFriendlyMessage),
            backgroundColor: Colors.red,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Nieoczekiwany błąd: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (picked != null && picked != _selectedDate) {
      setState(() => _selectedDate = picked);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.transaction != null;

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              isEditing ? 'Edytuj transakcję' : 'Dodaj transakcję',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 24),

            if (!isEditing) ...[
              DropdownButtonFormField<String>(
                value: _selectedAccountId,
                decoration: const InputDecoration(
                  labelText: 'Konto',
                  border: OutlineInputBorder(),
                ),
                items:
                    widget.accounts.map((account) {
                      return DropdownMenuItem(
                        value: account.id,
                        child: Text(account.name),
                      );
                    }).toList(),
                onChanged: (value) {
                  setState(() {
                    _selectedAccountId = value;
                    _updateCurrencyFromAccount();
                  });
                },
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Wybierz konto';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              DropdownButtonFormField<String>(
                value: _selectedType,
                decoration: const InputDecoration(
                  labelText: 'Typ',
                  border: OutlineInputBorder(),
                ),
                items: const [
                  DropdownMenuItem(value: 'INCOME', child: Text('Przychód')),
                  DropdownMenuItem(value: 'EXPENSE', child: Text('Wydatek')),
                ],
                onChanged: (value) {
                  setState(() => _selectedType = value);
                },
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Wybierz typ transakcji';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
            ],

            TextFormField(
              controller: _amountController,
              decoration: InputDecoration(
                labelText: 'Kwota',
                border: const OutlineInputBorder(),
                prefixText: '$_selectedCurrency ',
                hintText: '0.00',
              ),
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Wprowadź kwotę';
                }

                final normalizedValue = TransactionForm.normalizeAmount(value);
                final doubleValue = double.tryParse(normalizedValue);
                if (doubleValue == null) {
                  return 'Wprowadź poprawną kwotę';
                }

                if (doubleValue <= 0) {
                  return 'Kwota musi być większa od zera';
                }

                final regex = RegExp(r'^\d+([.,]\d{1,2})?$');
                if (!regex.hasMatch(value.trim())) {
                  return 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)';
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
            ),
            const SizedBox(height: 16),

            TextFormField(
              controller: _categoryController,
              decoration: const InputDecoration(
                labelText: 'Kategoria',
                border: OutlineInputBorder(),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Wprowadź kategorię';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            if (!isEditing) ...[
              ListTile(
                leading: const Icon(Icons.calendar_today),
                title: const Text('Data'),
                subtitle: Text(
                  '${_selectedDate.day}.${_selectedDate.month.toString().padLeft(2, '0')}.${_selectedDate.year}',
                ),
                onTap: _selectDate,
                contentPadding: EdgeInsets.zero,
              ),
              const SizedBox(height: 16),
            ],

            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Anuluj'),
                ),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: _isLoading ? null : _saveTransaction,
                  child:
                      _isLoading
                          ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                          : Text(isEditing ? 'Zapisz' : 'Dodaj'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
