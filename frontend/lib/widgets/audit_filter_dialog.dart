import 'package:flutter/material.dart';

import '../models/audit_entity_type.dart';
import '../models/audit_operation.dart';

class AuditFilterDialog extends StatefulWidget {
  final List<AuditEntityType>? initialEntityTypes;
  final List<AuditOperation>? initialOperations;
  final DateTime? initialFromDate;
  final DateTime? initialToDate;

  const AuditFilterDialog({this.initialEntityTypes, this.initialOperations, this.initialFromDate, this.initialToDate, super.key});

  @override
  State<AuditFilterDialog> createState() => _AuditFilterDialogState();
}

class _AuditFilterDialogState extends State<AuditFilterDialog> {
  late Set<AuditEntityType> _selectedEntityTypes;
  late Set<AuditOperation> _selectedOperations;
  DateTime? _fromDate;
  DateTime? _toDate;

  @override
  void initState() {
    super.initState();
    _selectedEntityTypes = widget.initialEntityTypes?.toSet() ?? {};
    _selectedOperations = widget.initialOperations?.toSet() ?? {};
    _fromDate = widget.initialFromDate;
    _toDate = widget.initialToDate;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Filtruj logi audytu'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Typ encji', style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            ...AuditEntityType.values.map(
              (entityType) => CheckboxListTile(
                title: Row(children: [Icon(entityType.icon, size: 20), const SizedBox(width: 8), Text(entityType.displayName)]),
                value: _selectedEntityTypes.contains(entityType),
                onChanged: (selected) {
                  setState(() {
                    if (selected == true) {
                      _selectedEntityTypes.add(entityType);
                    } else {
                      _selectedEntityTypes.remove(entityType);
                    }
                  });
                },
                dense: true,
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const SizedBox(height: 16),
            Text('Operacja', style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            ...AuditOperation.values.map(
              (operation) => CheckboxListTile(
                title: Row(children: [Icon(operation.icon, size: 20, color: operation.color), const SizedBox(width: 8), Text(operation.displayName)]),
                value: _selectedOperations.contains(operation),
                onChanged: (selected) {
                  setState(() {
                    if (selected == true) {
                      _selectedOperations.add(operation);
                    } else {
                      _selectedOperations.remove(operation);
                    }
                  });
                },
                dense: true,
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const SizedBox(height: 16),
            Text('Zakres dat', style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.calendar_today, size: 16),
                    label: Text(_fromDate != null ? _formatDate(_fromDate!) : 'Od'),
                    onPressed: () => _selectFromDate(context),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.calendar_today, size: 16),
                    label: Text(_toDate != null ? _formatDate(_toDate!) : 'Do'),
                    onPressed: () => _selectToDate(context),
                  ),
                ),
              ],
            ),
            if (_fromDate != null || _toDate != null)
              TextButton.icon(
                icon: const Icon(Icons.clear, size: 16),
                label: const Text('Wyczyść daty'),
                onPressed: () {
                  setState(() {
                    _fromDate = null;
                    _toDate = null;
                  });
                },
              ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () {
            setState(() {
              _selectedEntityTypes.clear();
              _selectedOperations.clear();
              _fromDate = null;
              _toDate = null;
            });
          },
          child: const Text('Wyczyść wszystko'),
        ),
        TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Anuluj')),
        FilledButton(
          onPressed: () {
            if (_fromDate != null && _toDate != null && _fromDate!.isAfter(_toDate!)) {
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Data początkowa nie może być późniejsza niż końcowa')));
              return;
            }

            Navigator.of(context).pop({
              'entityTypes': _selectedEntityTypes.isEmpty ? null : _selectedEntityTypes.toList(),
              'operations': _selectedOperations.isEmpty ? null : _selectedOperations.toList(),
              'fromDate': _fromDate,
              'toDate': _toDate,
            });
          },
          child: const Text('Zastosuj'),
        ),
      ],
    );
  }

  Future<void> _selectFromDate(BuildContext context) async {
    final picked = await showDatePicker(context: context, initialDate: _fromDate ?? DateTime.now(), firstDate: DateTime(2020), lastDate: DateTime.now());

    if (picked != null) {
      setState(() {
        _fromDate = picked;
      });
    }
  }

  Future<void> _selectToDate(BuildContext context) async {
    final picked = await showDatePicker(context: context, initialDate: _toDate ?? DateTime.now(), firstDate: _fromDate ?? DateTime(2020), lastDate: DateTime.now());

    if (picked != null) {
      setState(() {
        _toDate = picked;
      });
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day.toString().padLeft(2, '0')}.${date.month.toString().padLeft(2, '0')}.${date.year}';
  }
}
