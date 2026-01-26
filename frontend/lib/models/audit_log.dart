import 'audit_entity_type.dart';
import 'audit_operation.dart';

class AuditLog {
  final String id;
  final AuditOperation operation;
  final AuditEntityType entityType;
  final String entityId;
  final String performedBy;
  final String groupId;
  final DateTime performedAt;
  final String? changeDescription;

  AuditLog({
    required this.id,
    required this.operation,
    required this.entityType,
    required this.entityId,
    required this.performedBy,
    required this.groupId,
    required this.performedAt,
    this.changeDescription,
  });

  factory AuditLog.fromJson(Map<String, dynamic> json) {
    return AuditLog(
      id: json['id'] as String,
      operation: AuditOperation.fromString(json['operation'] as String),
      entityType: AuditEntityType.fromString(json['entityType'] as String),
      entityId: json['entityId'] as String,
      performedBy: json['performedBy'] as String,
      groupId: json['groupId'] as String,
      performedAt: DateTime.parse(json['performedAt'] as String),
      changeDescription: json['changeDescription'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'operation': operation.name,
      'entityType': entityType.name,
      'entityId': entityId,
      'performedBy': performedBy,
      'groupId': groupId,
      'performedAt': performedAt.toIso8601String(),
      'changeDescription': changeDescription,
    };
  }
}
