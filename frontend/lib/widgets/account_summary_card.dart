import 'package:flutter/material.dart';

class AccountSummaryCard extends StatelessWidget {
  final Map<String, double> currencySums;

  const AccountSummaryCard({super.key, required this.currencySums});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Saldo łączne według walut:', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              ...currencySums.entries.map(_buildCurrencyRow),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildCurrencyRow(MapEntry<String, double> entry) {
    final isNegative = entry.value < 0;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [Text(entry.key), Text(entry.value.toStringAsFixed(2), style: TextStyle(fontWeight: FontWeight.bold, color: isNegative ? Colors.red : Colors.black))],
      ),
    );
  }
}
