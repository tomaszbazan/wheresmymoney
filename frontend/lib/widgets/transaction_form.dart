import 'package:flutter/material.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction_type.dart';

import '../models/account.dart';
import '../models/transaction/transaction.dart';
import '../services/transaction_service.dart';
import '../utils/amount_validator.dart';
import '../utils/error_handler.dart';
import 'category_selector.dart';
import 'date_selector.dart';

class TransactionForm extends StatefulWidget {
  final List<Account> accounts;
  final Transaction? transaction;
  final TransactionType? type;
  final void Function(Transaction) onSaved;
  final TransactionService? transactionService;

  const TransactionForm({super.key, required this.accounts, this.transaction, this.type, required this.onSaved, this.transactionService});

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
  bool _isBillMode = false;
  final List<Map<String, dynamic>> _billItems = []; // {amountController, descriptionController, categoryId}

  @override
  void initState() {
    super.initState();

    _transactionService = widget.transactionService ?? RestTransactionService();

    _amountController = TextEditingController(text: widget.transaction?.amount.abs().toStringAsFixed(2) ?? '');
    _descriptionController = TextEditingController(text: widget.transaction?.description ?? '');

    if (widget.transaction != null) {
      _selectedAccountId = widget.transaction?.accountId;
      _selectedType = widget.transaction?.type;
      _selectedDate = widget.transaction?.createdAt ?? DateTime.now();

      if (widget.transaction != null && widget.transaction!.billItems.isNotEmpty) {
        if (widget.transaction!.billItems.length > 1) {
          _isBillMode = true;
          for (var item in widget.transaction!.billItems) {
            final amountController = TextEditingController(text: item.amount.toStringAsFixed(2));
            amountController.addListener(() => setState(() {}));

            _billItems.add({'amountController': amountController, 'descriptionController': TextEditingController(text: item.description), 'categoryId': item.category.id});
          }
        } else {
          // Single item - map to simple mode fields
          var item = widget.transaction!.billItems.first;
          _amountController.text = item.amount.toStringAsFixed(2);
          _descriptionController.text = item.description;
          _selectedCategoryId = item.category.id;
        }
      }
      _updateCurrencyFromAccount();
    } else {
      _selectedType = widget.type;
      _selectedAccountId = widget.accounts.isNotEmpty ? widget.accounts.first.id : null;
      _updateCurrencyFromAccount();
      // Initialize with one empty item for bill mode
      _addBillItem();
    }
  }

  void _addBillItem() {
    setState(() {
      final amountController = TextEditingController();
      amountController.addListener(() {
        setState(() {}); // Rebuild to update sum
      });

      _billItems.add({'amountController': amountController, 'descriptionController': TextEditingController(), 'categoryId': null});
    });
  }

  void _removeBillItem(int index) {
    if (_billItems.length > 1) {
      setState(() {
        var item = _billItems.removeAt(index);
        (item['amountController'] as TextEditingController).dispose();
        (item['descriptionController'] as TextEditingController).dispose();
      });
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    for (var item in _billItems) {
      (item['amountController'] as TextEditingController).dispose();
      (item['descriptionController'] as TextEditingController).dispose();
    }
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
      final normalizedAmount = AmountValidator.normalize(_amountController.text);
      final Transaction transaction;

      if (widget.transaction != null) {
        List<Map<String, dynamic>> itemsPayload;

        if (_isBillMode) {
          itemsPayload =
              _billItems
                  .map(
                    (item) => {
                      'amount': double.parse(AmountValidator.normalize((item['amountController'] as TextEditingController).text)),
                      'description': (item['descriptionController'] as TextEditingController).text,
                      'categoryId': item['categoryId'],
                    },
                  )
                  .toList();
        } else {
          itemsPayload = [
            {'amount': double.parse(normalizedAmount), 'description': _descriptionController.text, 'categoryId': _selectedCategoryId},
          ];
        }

        transaction = await _transactionService.updateTransaction(
          id: widget.transaction!.id,
          billItems: itemsPayload,
          currency: _selectedCurrency,
          accountId: _selectedAccountId,
          transactionDate: _selectedDate,
        );
      } else {
        List<Map<String, dynamic>> itemsPayload;

        if (_isBillMode) {
          itemsPayload =
              _billItems
                  .map(
                    (item) => {
                      'amount': double.parse(AmountValidator.normalize((item['amountController'] as TextEditingController).text)),
                      'description': (item['descriptionController'] as TextEditingController).text,
                      'categoryId': item['categoryId'],
                    },
                  )
                  .toList();
        } else {
          itemsPayload = [
            {'amount': double.parse(normalizedAmount), 'description': _descriptionController.text, 'categoryId': _selectedCategoryId},
          ];
        }

        transaction = await _transactionService.createTransaction(
          accountId: _selectedAccountId!,
          transactionDate: _selectedDate,
          type: _selectedType!,
          billItems: itemsPayload,
          currency: _selectedCurrency,
        );
      }

      widget.onSaved(transaction);
    } catch (e) {
      // ignore: use_build_context_synchronously
      ErrorHandler.showError(context, e);
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.transaction != null;

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(isEditing ? 'Edytuj transakcję' : 'Dodaj transakcję', style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 24),

              if (!isEditing) ...[
                Row(
                  children: [
                    Expanded(
                      child: ListTile(
                        title: const Text('Prosty'),
                        leading: Icon(!_isBillMode ? Icons.radio_button_checked : Icons.radio_button_off, color: !_isBillMode ? Theme.of(context).primaryColor : null),
                        onTap: () => setState(() => _isBillMode = false),
                        contentPadding: EdgeInsets.zero,
                      ),
                    ),
                    Expanded(
                      child: ListTile(
                        title: const Text('Rachunek'),
                        leading: Icon(_isBillMode ? Icons.radio_button_checked : Icons.radio_button_off, color: _isBillMode ? Theme.of(context).primaryColor : null),
                        onTap: () => setState(() => _isBillMode = true),
                        contentPadding: EdgeInsets.zero,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
              ],

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
              ] else ...[
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

              if (_isBillMode) ...[
                ListView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: _billItems.length,
                  itemBuilder: (context, index) {
                    return Card(
                      margin: const EdgeInsets.only(bottom: 16),
                      child: Padding(
                        padding: const EdgeInsets.all(12),
                        child: Column(
                          children: [
                            Row(
                              children: [
                                Expanded(child: Text('Pozycja ${index + 1}', style: const TextStyle(fontWeight: FontWeight.bold))),
                                if (_billItems.length > 1) IconButton(icon: const Icon(Icons.delete, color: Colors.red), onPressed: () => _removeBillItem(index)),
                              ],
                            ),
                            TextFormField(
                              controller: _billItems[index]['amountController'] as TextEditingController,
                              decoration: InputDecoration(labelText: 'Kwota', border: const OutlineInputBorder(), prefixText: '$_selectedCurrency ', hintText: '0.00'),
                              keyboardType: const TextInputType.numberWithOptions(decimal: true),
                              validator: AmountValidator.validate,
                            ),
                            const SizedBox(height: 12),
                            TextFormField(
                              controller: _billItems[index]['descriptionController'] as TextEditingController,
                              decoration: const InputDecoration(labelText: 'Opis', border: OutlineInputBorder()),
                            ),
                            const SizedBox(height: 12),
                            CategorySelector(
                              selectedCategoryId: _billItems[index]['categoryId'] as String?,
                              transactionType: (_selectedType ?? TransactionType.expense) == TransactionType.income ? CategoryType.income : CategoryType.expense,
                              onChanged: (categoryId) {
                                setState(() {
                                  _billItems[index]['categoryId'] = categoryId;
                                });
                              },
                              validator: (value) {
                                if (value == null || value.isEmpty) return 'Wybierz kategorię';
                                return null;
                              },
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
                TextButton.icon(onPressed: _addBillItem, icon: const Icon(Icons.add), label: const Text('Dodaj pozycję')),
                const SizedBox(height: 16),
                Text(
                  'Suma: ${_billItems.fold<double>(0, (sum, item) {
                    final val = double.tryParse(AmountValidator.normalize((item['amountController'] as TextEditingController).text)) ?? 0;
                    return sum + val;
                  }).toStringAsFixed(2)} $_selectedCurrency',
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                  textAlign: TextAlign.right,
                ),
              ] else ...[
                TextFormField(
                  controller: _amountController,
                  decoration: InputDecoration(labelText: 'Kwota', border: const OutlineInputBorder(), prefixText: '$_selectedCurrency ', hintText: '0.00'),
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  validator: AmountValidator.validate,
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
              ],
              const SizedBox(height: 16),

              DateSelector(selectedDate: _selectedDate, onDateChanged: (date) => setState(() => _selectedDate = date)),
              const SizedBox(height: 16),

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
      ),
    );
  }
}
