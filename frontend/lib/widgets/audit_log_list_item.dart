import 'package:flutter/material.dart';

import '../models/audit_log.dart';
import '../utils/date_formatter.dart';

class AuditLogListItem extends StatefulWidget {
  final AuditLog auditLog;

  const AuditLogListItem({required this.auditLog, super.key});

  @override
  State<AuditLogListItem> createState() => _AuditLogListItemState();
}

class _AuditLogListItemState extends State<AuditLogListItem> {
  bool _isExpanded = false;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Column(
        children: [
          ListTile(
            leading: CircleAvatar(
              backgroundColor: widget.auditLog.operation.color.withValues(alpha: 0.2),
              child: Icon(widget.auditLog.operation.icon, color: widget.auditLog.operation.color),
            ),
            title: Row(
              children: [
                Icon(widget.auditLog.entityType.icon, size: 16, color: Theme.of(context).textTheme.bodyMedium?.color),
                const SizedBox(width: 4),
                Text(widget.auditLog.entityType.displayName, style: Theme.of(context).textTheme.bodyMedium),
                const SizedBox(width: 8),
                Text('•', style: Theme.of(context).textTheme.bodyMedium),
                const SizedBox(width: 8),
                Text(widget.auditLog.operation.displayName, style: TextStyle(color: widget.auditLog.operation.color, fontWeight: FontWeight.w500)),
              ],
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (widget.auditLog.changeDescription != null) ...[
                  const SizedBox(height: 4),
                  Text(widget.auditLog.changeDescription!, maxLines: _isExpanded ? null : 2, overflow: _isExpanded ? null : TextOverflow.ellipsis),
                ],
                const SizedBox(height: 4),
                Text(DateFormatter.formatRelativeDate(widget.auditLog.performedAt), style: Theme.of(context).textTheme.bodySmall),
              ],
            ),
            trailing:
                widget.auditLog.changeDescription != null
                    ? IconButton(
                      icon: Icon(_isExpanded ? Icons.expand_less : Icons.expand_more),
                      onPressed: () {
                        setState(() {
                          _isExpanded = !_isExpanded;
                        });
                      },
                    )
                    : null,
            onTap:
                widget.auditLog.changeDescription != null
                    ? () {
                      setState(() {
                        _isExpanded = !_isExpanded;
                      });
                    }
                    : null,
          ),
          if (_isExpanded && widget.auditLog.changeDescription != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Divider(),
                  const SizedBox(height: 8),
                  _buildDetailRow(context, 'Data', DateFormatter.formatAbsoluteDate(widget.auditLog.performedAt)),
                  _buildDetailRow(context, 'ID encji', widget.auditLog.entityId),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildDetailRow(BuildContext context, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 80, child: Text(label, style: Theme.of(context).textTheme.bodySmall?.copyWith(fontWeight: FontWeight.w600))),
          Expanded(child: Text(value, style: Theme.of(context).textTheme.bodySmall)),
        ],
      ),
    );
  }
}
