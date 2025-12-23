import 'package:flutter/material.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction_type.dart';

import '../models/account.dart';
import '../models/http_exception.dart';
import '../models/transaction.dart';
import '../services/transaction_service.dart';
import 'category_selector.dart';

class TransactionForm extends StatefulWidget {
  final List<Account> accounts;
  final Transaction? transaction;
  final TransactionType? type;
  final void Function(Transaction) onSaved;
  final TransactionService? transactionService;

  const TransactionForm({super.key, required this.accounts, this.transaction, this.type, required this.onSaved, this.transactionService});

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
  late final TransactionService _transactionService;

  late TextEditingController _amountController;
  late TextEditingController _descriptionController;

  String? _selectedAccountId;
  TransactionType? _selectedType;
  String? _selectedCategoryId;
  String _selectedCurrency = 'PLN';
  DateTime _selectedDate = DateTime.now();
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();

    _transactionService = widget.transactionService ?? RestTransactionService();

    _amountController = TextEditingController(text: widget.transaction?.amount.abs().toStringAsFixed(2) ?? '');
    _descriptionController = TextEditingController(text: widget.transaction?.description ?? '');

    if (widget.transaction != null) {
      _selectedAccountId = widget.transaction!.accountId;
      _selectedType = widget.transaction!.type;
      _selectedCategoryId = widget.transaction!.categoryId;
      _selectedDate = widget.transaction!.createdAt;
      _updateCurrencyFromAccount();
    } else {
      _selectedType = widget.type;
      _selectedAccountId = widget.accounts.isNotEmpty ? widget.accounts.first.id : null;
      _updateCurrencyFromAccount();
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _updateCurrencyFromAccount() {
    if (_selectedAccountId != null) {
      final selectedAccount = widget.accounts.firstWhere((account) => account.id == _selectedAccountId, orElse: () => widget.accounts.first);
      _selectedCurrency = selectedAccount.currency ?? 'PLN';
    }
  }

  Future<void> _saveTransaction() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedAccountId == null) return;
    if (_selectedType == null) return;

    setState(() => _isLoading = true);

    try {
      final normalizedAmount = TransactionForm.normalizeAmount(_amountController.text);
      final Transaction transaction;

      if (widget.transaction != null) {
        transaction = await _transactionService.updateTransaction(
          id: widget.transaction!.id,
          amount: double.parse(normalizedAmount),
          description: _descriptionController.text,
          categoryId: _selectedCategoryId ?? '',
          currency: _selectedCurrency,
        );
      } else {
        transaction = await _transactionService.createTransaction(
          accountId: _selectedAccountId!,
          amount: double.parse(normalizedAmount),
          description: _descriptionController.text,
          date: _selectedDate,
          type: _selectedType!,
          categoryId: _selectedCategoryId ?? '',
          currency: _selectedCurrency,
        );
      }

      widget.onSaved(transaction);
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

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(context: context, initialDate: _selectedDate, firstDate: DateTime(2020), lastDate: DateTime.now().add(const Duration(days: 365)));

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
            Text(isEditing ? 'Edytuj transakcję' : 'Dodaj transakcję', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 24),
            if (!isEditing) ...[
              DropdownButtonFormField<String>(
                initialValue: _selectedAccountId,
                decoration: const InputDecoration(labelText: 'Konto', border: OutlineInputBorder()),
                items:
                    widget.accounts.map((account) {
                      return DropdownMenuItem(value: account.id, child: Text('${account.name} (${account.currency})'));
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
            ],

            TextFormField(
              controller: _amountController,
              decoration: InputDecoration(labelText: 'Kwota', border: const OutlineInputBorder(), prefixText: '$_selectedCurrency ', hintText: '0.00'),
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
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

            TextFormField(controller: _descriptionController, decoration: const InputDecoration(labelText: 'Opis', border: OutlineInputBorder())),
            const SizedBox(height: 16),

            CategorySelector(
              selectedCategoryId: _selectedCategoryId,
              transactionType: (_selectedType ?? TransactionType.expense) == TransactionType.income ? CategoryType.income : CategoryType.expense,
              onChanged: (categoryId) {
                setState(() {
                  _selectedCategoryId = categoryId;
                });
              },
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Wybierz kategorię';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),

            if (!isEditing) ...[
              ListTile(
                leading: const Icon(Icons.calendar_today),
                title: const Text('Data'),
                subtitle: Text('${_selectedDate.day}.${_selectedDate.month.toString().padLeft(2, '0')}.${_selectedDate.year}'),
                onTap: _selectDate,
                contentPadding: EdgeInsets.zero,
              ),
              const SizedBox(height: 16),
            ],

            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Anuluj')),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: _isLoading ? null : _saveTransaction,
                  child: _isLoading ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : Text(isEditing ? 'Zapisz' : 'Dodaj'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
