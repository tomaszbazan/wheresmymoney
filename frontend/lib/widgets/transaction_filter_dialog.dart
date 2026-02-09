import 'package:flutter/material.dart';
import '../models/transaction_filter.dart';
import '../models/account.dart';
import '../models/category.dart';
import 'package:intl/intl.dart';

class TransactionFilterDialog extends StatefulWidget {
  final TransactionFilter initialFilter;
  final List<Account> accounts;
  final List<Category> categories;

  const TransactionFilterDialog({super.key, required this.initialFilter, required this.accounts, required this.categories});

  @override
  State<TransactionFilterDialog> createState() => _TransactionFilterDialogState();
}

class _TransactionFilterDialogState extends State<TransactionFilterDialog> {
  late DateTime? _dateFrom;
  late DateTime? _dateTo;
  late TextEditingController _minAmountController;
  late TextEditingController _maxAmountController;
  late TextEditingController _descriptionController;
  late List<String> _selectedAccountIds;
  late List<String> _selectedCategoryIds;

  @override
  void initState() {
    super.initState();
    _dateFrom = widget.initialFilter.dateFrom;
    _dateTo = widget.initialFilter.dateTo;
    _minAmountController = TextEditingController(text: widget.initialFilter.minAmount?.toString() ?? '');
    _maxAmountController = TextEditingController(text: widget.initialFilter.maxAmount?.toString() ?? '');
    _descriptionController = TextEditingController(text: widget.initialFilter.description ?? '');
    _selectedAccountIds = List.from(widget.initialFilter.accountIds);
    _selectedCategoryIds = List.from(widget.initialFilter.categoryIds);
  }

  @override
  void dispose() {
    _minAmountController.dispose();
    _maxAmountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Filter Transactions'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('Description'),
            TextField(controller: _descriptionController, decoration: const InputDecoration(labelText: 'Contains text...', hintText: 'Search by description')),
            const Divider(),
            _buildSectionTitle('Date Range'),
            Row(
              children: [
                Expanded(
                  child: TextButton(
                    onPressed: () async {
                      final picked = await showDatePicker(context: context, initialDate: _dateFrom ?? DateTime.now(), firstDate: DateTime(2000), lastDate: DateTime(2100));
                      if (picked != null) setState(() => _dateFrom = picked);
                    },
                    child: Text(_dateFrom == null ? 'From' : DateFormat('yyyy-MM-dd').format(_dateFrom!)),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextButton(
                    onPressed: () async {
                      final picked = await showDatePicker(context: context, initialDate: _dateTo ?? DateTime.now(), firstDate: DateTime(2000), lastDate: DateTime(2100));
                      if (picked != null) setState(() => _dateTo = picked);
                    },
                    child: Text(_dateTo == null ? 'To' : DateFormat('yyyy-MM-dd').format(_dateTo!)),
                  ),
                ),
              ],
            ),
            const Divider(),
            _buildSectionTitle('Amount'),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _minAmountController,
                    decoration: const InputDecoration(labelText: 'Min'),
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: TextField(
                    controller: _maxAmountController,
                    decoration: const InputDecoration(labelText: 'Max'),
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  ),
                ),
              ],
            ),
            const Divider(),
            _buildSectionTitle('Accounts'),
            if (widget.accounts.isEmpty) const Text('No accounts available', style: TextStyle(fontStyle: FontStyle.italic)),
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children:
                  widget.accounts.map((account) {
                    return FilterChip(
                      label: Text('${account.name} (${account.currency})'),
                      selected: _selectedAccountIds.contains(account.id),
                      onSelected: (selected) {
                        setState(() {
                          if (selected) {
                            _selectedAccountIds.add(account.id);
                          } else {
                            _selectedAccountIds.remove(account.id);
                          }
                        });
                      },
                    );
                  }).toList(),
            ),
            const Divider(),
            _buildSectionTitle('Categories'),
            if (widget.categories.isEmpty) const Text('No categories available', style: TextStyle(fontStyle: FontStyle.italic)),
            Container(
              constraints: const BoxConstraints(maxHeight: 200),
              child: SingleChildScrollView(
                child: Wrap(
                  spacing: 8,
                  runSpacing: 4,
                  children:
                      widget.categories.map((category) {
                        return FilterChip(
                          label: Text(category.name),
                          selected: _selectedCategoryIds.contains(category.id),
                          onSelected: (selected) {
                            setState(() {
                              if (selected) {
                                _selectedCategoryIds.add(category.id);
                              } else {
                                _selectedCategoryIds.remove(category.id);
                              }
                            });
                          },
                        );
                      }).toList(),
                ),
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () {
            // Reset
            setState(() {
              _dateFrom = null;
              _dateTo = null;
              _minAmountController.clear();
              _maxAmountController.clear();
              _descriptionController.clear();
              _selectedAccountIds.clear();
              _selectedCategoryIds.clear();
            });
          },
          child: const Text('Reset'),
        ),
        TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Cancel')),
        FilledButton(
          onPressed: () {
            final double? minAmount = double.tryParse(_minAmountController.text);
            final double? maxAmount = double.tryParse(_maxAmountController.text);
            final String description = _descriptionController.text;

            final filter = TransactionFilter(
              dateFrom: _dateFrom,
              dateTo: _dateTo,
              minAmount: minAmount,
              maxAmount: maxAmount,
              types: widget.initialFilter.types,
              accountIds: _selectedAccountIds,
              categoryIds: _selectedCategoryIds,
              description: description.isEmpty ? null : description,
              sort: widget.initialFilter.sort,
            );
            Navigator.of(context).pop(filter);
          },
          child: const Text('Apply'),
        ),
      ],
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(padding: const EdgeInsets.symmetric(vertical: 8.0), child: Text(title, style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)));
  }
}
