import 'package:flutter/material.dart';

import '../utils/account_type_helper.dart';

class AccountListItem extends StatelessWidget {
  final Map<String, dynamic> account;
  final VoidCallback onDeleteRequest;
  final VoidCallback? onDismissed;
  final VoidCallback? onTransferRequest;

  const AccountListItem({super.key, required this.account, required this.onDeleteRequest, this.onDismissed, this.onTransferRequest});

  @override
  Widget build(BuildContext context) {
    final isNegative = (account['balance'] as double) < 0;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Dismissible(
        key: Key(account['name'] as String),
        background: _buildDismissBackground(),
        direction: DismissDirection.endToStart,
        confirmDismiss: (_) async => await _confirmDismiss(context),
        onDismissed: (_) => onDismissed?.call(),
        child: _buildListTile(isNegative),
      ),
    );
  }

  Widget _buildDismissBackground() {
    return Container(color: Colors.red, alignment: Alignment.centerRight, padding: const EdgeInsets.only(right: 20.0), child: const Icon(Icons.delete, color: Colors.white));
  }

  Future<bool> _confirmDismiss(BuildContext context) async {
    onDeleteRequest();
    return false;
  }

  Widget _buildListTile(bool isNegative) {
    return ListTile(
      leading: CircleAvatar(backgroundColor: AccountTypeHelper.getColorForType(account['type'] as String?), child: AccountTypeHelper.getIconForType(account['type'] as String?)),
      title: Text('${account['name']} (${account['currency']})'),
      subtitle: Text('${account['type']}'),
      trailing: _buildTrailing(isNegative),
    );
  }

  Widget _buildTrailing(bool isNegative) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          '${account['balance'].toStringAsFixed(2)} ${account['currency'] ?? 'zł'}',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: isNegative ? Colors.red : Colors.black),
        ),
        if (onTransferRequest != null) IconButton(icon: const Icon(Icons.swap_horiz, color: Colors.blue), onPressed: onTransferRequest, tooltip: 'Wykonaj przelew'),
        IconButton(icon: const Icon(Icons.delete_outline, color: Colors.red), onPressed: onDeleteRequest),
      ],
    );
  }
}
