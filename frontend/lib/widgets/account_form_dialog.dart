import 'package:flutter/material.dart';

class AccountFormDialog extends StatefulWidget {
  final void Function(String name, String type, String currency) onSave;

  const AccountFormDialog({super.key, required this.onSave});

  @override
  State<AccountFormDialog> createState() => _AccountFormDialogState();
}

class _AccountFormDialogState extends State<AccountFormDialog> {
  final TextEditingController _nameController = TextEditingController();
  String _selectedCurrency = 'PLN';
  String _selectedType = 'Rachunek bieżący';

  static const List<String> _availableCurrencies = ['PLN', 'EUR', 'USD', 'GBP'];
  static const List<String> _availableAccountTypes = ['Rachunek bieżący', 'Oszczędnościowe', 'Gotówka', 'Kredytowa'];

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  void _handleSave() {
    if (_nameController.text.isNotEmpty) {
      widget.onSave(_nameController.text, _selectedType, _selectedCurrency);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Dodaj konto'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [_buildNameField(), const SizedBox(height: 16), _buildTypeSelector(), const SizedBox(height: 16), _buildCurrencySelector()],
        ),
      ),
      actions: _buildActions(),
    );
  }

  Widget _buildNameField() {
    return TextField(controller: _nameController, decoration: const InputDecoration(labelText: 'Nazwa konta', hintText: 'Wpisz nazwę konta'), autofocus: true);
  }

  Widget _buildTypeSelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Typ konta:'),
        DropdownButton<String>(
          isExpanded: true,
          value: _selectedType,
          items: _availableAccountTypes.map((type) => DropdownMenuItem(value: type, child: Text(type))).toList(),
          onChanged: (newValue) {
            if (newValue != null) {
              setState(() => _selectedType = newValue);
            }
          },
        ),
      ],
    );
  }

  Widget _buildCurrencySelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Waluta:'),
        DropdownButton<String>(
          isExpanded: true,
          value: _selectedCurrency,
          items: _availableCurrencies.map((currency) => DropdownMenuItem(value: currency, child: Text(currency))).toList(),
          onChanged: (newValue) {
            if (newValue != null) {
              setState(() => _selectedCurrency = newValue);
            }
          },
        ),
      ],
    );
  }

  List<Widget> _buildActions() {
    return [TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Anuluj')), ElevatedButton(onPressed: _handleSave, child: const Text('Dodaj'))];
  }
}
