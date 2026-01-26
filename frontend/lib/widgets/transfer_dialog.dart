import 'package:flutter/material.dart';

import '../services/transfer_service.dart';
import '../utils/amount_validator.dart';
import '../utils/error_handler.dart';

class TransferDialog extends StatefulWidget {
  final Map<String, dynamic>? sourceAccount;
  final List<Map<String, dynamic>> accounts;
  final TransferService? transferService;
  final VoidCallback onSuccess;

  const TransferDialog({super.key, required this.accounts, this.sourceAccount, this.transferService, required this.onSuccess});

  @override
  State<TransferDialog> createState() => _TransferDialogState();
}

class _TransferDialogState extends State<TransferDialog> {
  final _formKey = GlobalKey<FormState>();
  late final TransferService _transferService;

  String? _sourceAccountId;
  String? _targetAccountId;
  final _sourceAmountController = TextEditingController();
  final _targetAmountController = TextEditingController();
  final _descriptionController = TextEditingController();

  bool _isLoading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _transferService = widget.transferService ?? RestTransferService();

    if (widget.sourceAccount != null) {
      _sourceAccountId = widget.sourceAccount!['id'] as String;
    }
  }

  @override
  void dispose() {
    _sourceAmountController.dispose();
    _targetAmountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Map<String, dynamic>? _getAccountById(String? id) {
    if (id == null) return null;
    try {
      return widget.accounts.firstWhere((a) => a['id'] == id);
    } catch (_) {
      return null;
    }
  }

  bool get _isSameCurrency {
    final source = _getAccountById(_sourceAccountId);
    final target = _getAccountById(_targetAccountId);
    if (source == null || target == null) return true;
    return source['currency'] == target['currency'];
  }

  void _onSourceAmountChanged(String value) {
    if (_isSameCurrency) {
      _targetAmountController.text = value;
    }
    setState(() {});
  }

  void _onTargetAmountChanged(String value) {
    setState(() {});
  }

  String? _calculateRate() {
    final sourceAmount = double.tryParse(_sourceAmountController.text.replaceAll(',', '.'));
    final targetAmount = double.tryParse(_targetAmountController.text.replaceAll(',', '.'));

    if (sourceAmount != null && targetAmount != null && sourceAmount > 0) {
      return (targetAmount / sourceAmount).toStringAsFixed(4);
    }
    return null;
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final sourceAmount = double.parse(_sourceAmountController.text.replaceAll(',', '.'));
      final targetAmount = _isSameCurrency ? null : double.parse(_targetAmountController.text.replaceAll(',', '.'));

      await _transferService.createTransfer(
        sourceAccountId: _sourceAccountId!,
        targetAccountId: _targetAccountId!,
        sourceAmount: sourceAmount,
        targetAmount: targetAmount,
        description: _descriptionController.text.isEmpty ? null : _descriptionController.text,
      );

      if (mounted) {
        widget.onSuccess();
        Navigator.of(context).pop();
      }
    } catch (e) {
      setState(() {
        _error = ErrorHandler.getErrorMessage(e);
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final availableTargets = widget.accounts.where((a) => a['id'] != _sourceAccountId).toList();
    final rate = _calculateRate();

    return AlertDialog(
      title: const Text('Wykonaj transfer'),
      content: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (_error != null) Padding(padding: const EdgeInsets.only(bottom: 16.0), child: Text(_error!, style: const TextStyle(color: Colors.red))),

              _buildDropdown(
                label: 'Z konta',
                value: _sourceAccountId,
                items: widget.accounts,
                onChanged: (val) {
                  setState(() {
                    _sourceAccountId = val;
                    if (_sourceAccountId == _targetAccountId) {
                      _targetAccountId = null;
                    }
                    if (_isSameCurrency) {
                      _targetAmountController.text = _sourceAmountController.text;
                    }
                  });
                },
              ),
              const SizedBox(height: 16),
              _buildDropdown(
                label: 'Na konto',
                value: _targetAccountId,
                items: availableTargets,
                onChanged: (val) {
                  setState(() {
                    _targetAccountId = val;
                    if (_isSameCurrency) {
                      _targetAmountController.text = _sourceAmountController.text;
                    }
                  });
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _sourceAmountController,
                decoration: InputDecoration(
                  labelText: 'Kwota źródłowa',
                  suffixText: (_getAccountById(_sourceAccountId)?['currency'] as String?) ?? '',
                  border: const OutlineInputBorder(),
                ),
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                validator: AmountValidator.validate,
                onChanged: _onSourceAmountChanged,
              ),
              if (!_isSameCurrency && _targetAccountId != null) ...[
                const SizedBox(height: 16),
                TextFormField(
                  controller: _targetAmountController,
                  decoration: InputDecoration(
                    labelText: 'Kwota docelowa',
                    suffixText: (_getAccountById(_targetAccountId)?['currency'] as String?) ?? '',
                    border: const OutlineInputBorder(),
                    helperText: rate != null ? 'Kurs: $rate' : null,
                  ),
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  validator: AmountValidator.validate,
                  onChanged: _onTargetAmountChanged,
                ),
              ],
              const SizedBox(height: 16),
              TextFormField(controller: _descriptionController, decoration: const InputDecoration(labelText: 'Opis (opcjonalnie)', border: OutlineInputBorder()), maxLength: 200),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(onPressed: _isLoading ? null : () => Navigator.of(context).pop(), child: const Text('Anuluj')),
        ElevatedButton(
          onPressed: _isLoading ? null : _submit,
          child: _isLoading ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Przelej'),
        ),
      ],
    );
  }

  Widget _buildDropdown({required String label, required String? value, required List<Map<String, dynamic>> items, required ValueChanged<String?> onChanged}) {
    return DropdownButtonFormField<String>(
      initialValue: value,
      decoration: InputDecoration(labelText: label, border: const OutlineInputBorder()),
      items:
          items.map((a) {
            return DropdownMenuItem<String>(value: a['id'] as String, child: Text('${a['name']} (${a['currency']})'));
          }).toList(),
      onChanged: onChanged,
      validator: (val) => val == null ? 'Pole wymagane' : null,
    );
  }
}
