import 'package:flutter/material.dart';
import 'package:frontend/models/transaction/bill_item.dart';
import 'package:frontend/models/transaction/transaction.dart';
import 'package:frontend/utils/date_formatter.dart';

class TransactionListItem extends StatefulWidget {
  final Transaction transaction;
  final String accountName;
  final String accountCurrency;
  final void Function(Transaction) onEdit;
  final void Function(Transaction) onDelete;

  const TransactionListItem({super.key, required this.transaction, required this.accountName, required this.accountCurrency, required this.onEdit, required this.onDelete});

  @override
  State<TransactionListItem> createState() => _TransactionListItemState();
}

class _TransactionListItemState extends State<TransactionListItem> {
  bool _isExpanded = false;

  void _toggleExpanded() {
    setState(() {
      _isExpanded = !_isExpanded;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (widget.transaction.billItems.length > 1) {
      return _buildExpandableCard(context);
    }
    return _buildSingleItemCard(context);
  }

  Widget _buildSingleItemCard(BuildContext context) {
    final transaction = widget.transaction;
    final isIncome = transaction.isIncome;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: ListTile(
        leading: _buildLeadingIcon(isIncome),
        title: Text(transaction.description, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: _buildSubtitle(context, transaction),
        trailing: _buildTrailing(context, transaction, isIncome),
        isThreeLine: true,
      ),
    );
  }

  Widget _buildExpandableCard(BuildContext context) {
    final transaction = widget.transaction;
    final isIncome = transaction.isIncome;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Column(
        children: [
          ListTile(
            onTap: _toggleExpanded,
            leading: _buildLeadingIcon(isIncome),
            title: Text(transaction.description, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: _buildSubtitle(context, transaction),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [_buildAmountText(transaction, isIncome), Icon(_isExpanded ? Icons.expand_less : Icons.expand_more), _buildPopupMenu(transaction)],
            ),
            isThreeLine: true,
          ),
          const Divider(),
          if (_isExpanded)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
              child: Column(
                children:
                    transaction.billItems.asMap().entries.map((entry) {
                      final index = entry.key;
                      final item = entry.value;
                      return Column(children: [_buildBillItemRow(context, item), if (index < transaction.billItems.length - 1) const Divider()]);
                    }).toList(),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildBillItemRow(BuildContext context, BillItem item) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Text.rich(
              TextSpan(
                children: [
                  const TextSpan(text: 'Kategoria: ', style: TextStyle(fontWeight: FontWeight.bold)),
                  TextSpan(text: '${item.category.name}, '),
                  const TextSpan(text: 'Opis: ', style: TextStyle(fontWeight: FontWeight.bold)),
                  TextSpan(text: item.description),
                ],
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ),
          const SizedBox(width: 8),
          Text(item.amount.toString(), style: Theme.of(context).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  Widget _buildLeadingIcon(bool isIncome) {
    return CircleAvatar(
      backgroundColor: isIncome ? Colors.green[100] : Colors.red[100],
      child: Icon(isIncome ? Icons.arrow_upward : Icons.arrow_downward, color: isIncome ? Colors.green[700] : Colors.red[700]),
    );
  }

  Widget _buildSubtitle(BuildContext context, Transaction transaction) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Konto: ${widget.accountName} (${widget.accountCurrency})'),
        Text('Kategoria: ${transaction.categoryName ?? transaction.categoryId}'),
        Text('Data: ${DateFormatter.format(transaction.transactionDate)}'),
      ],
    );
  }

  Widget _buildTrailing(BuildContext context, Transaction transaction, bool isIncome) {
    return Row(mainAxisSize: MainAxisSize.min, children: [_buildAmountText(transaction, isIncome), _buildPopupMenu(transaction)]);
  }

  Widget _buildAmountText(Transaction transaction, bool isIncome) {
    return Text(transaction.amount.toString(), style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: isIncome ? Colors.green[700] : Colors.red[700]));
  }

  Widget _buildPopupMenu(Transaction transaction) {
    return PopupMenuButton<String>(
      onSelected: (value) {
        switch (value) {
          case 'edit':
            widget.onEdit(transaction);
            break;
          case 'delete':
            widget.onDelete(transaction);
            break;
        }
      },
      itemBuilder:
          (context) => [
            const PopupMenuItem(value: 'edit', child: ListTile(leading: Icon(Icons.edit), title: Text('Edytuj'), contentPadding: EdgeInsets.zero)),
            const PopupMenuItem(value: 'delete', child: ListTile(leading: Icon(Icons.delete), title: Text('Usuń'), contentPadding: EdgeInsets.zero)),
          ],
    );
  }
}
