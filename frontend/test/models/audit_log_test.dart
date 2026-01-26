import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/audit_entity_type.dart';
import 'package:frontend/models/audit_log.dart';
import 'package:frontend/models/audit_operation.dart';

void main() {
  group('AuditLog', () {
    group('fromJson', () {
      test('deserializes complete JSON correctly', () {
        final json = {
          'id': '123e4567-e89b-12d3-a456-426614174000',
          'operation': 'CREATE',
          'entityType': 'ACCOUNT',
          'entityId': '223e4567-e89b-12d3-a456-426614174000',
          'performedBy': '323e4567-e89b-12d3-a456-426614174000',
          'groupId': '423e4567-e89b-12d3-a456-426614174000',
          'performedAt': '2026-01-23T12:30:00.000Z',
          'changeDescription': 'Created new account',
        };

        final auditLog = AuditLog.fromJson(json);

        expect(auditLog.id, '123e4567-e89b-12d3-a456-426614174000');
        expect(auditLog.operation, AuditOperation.CREATE);
        expect(auditLog.entityType, AuditEntityType.ACCOUNT);
        expect(auditLog.entityId, '223e4567-e89b-12d3-a456-426614174000');
        expect(auditLog.performedBy, '323e4567-e89b-12d3-a456-426614174000');
        expect(auditLog.groupId, '423e4567-e89b-12d3-a456-426614174000');
        expect(auditLog.performedAt, DateTime.parse('2026-01-23T12:30:00.000Z'));
        expect(auditLog.changeDescription, 'Created new account');
      });

      test('deserializes JSON with null changeDescription', () {
        final json = {
          'id': '123e4567-e89b-12d3-a456-426614174000',
          'operation': 'UPDATE',
          'entityType': 'TRANSACTION',
          'entityId': '223e4567-e89b-12d3-a456-426614174000',
          'performedBy': '323e4567-e89b-12d3-a456-426614174000',
          'groupId': '423e4567-e89b-12d3-a456-426614174000',
          'performedAt': '2026-01-23T12:30:00.000Z',
          'changeDescription': null,
        };

        final auditLog = AuditLog.fromJson(json);

        expect(auditLog.changeDescription, isNull);
      });

      test('deserializes all operation types correctly', () {
        final createLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'CREATE',
          'entityType': 'ACCOUNT',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(createLog.operation, AuditOperation.CREATE);

        final updateLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'UPDATE',
          'entityType': 'ACCOUNT',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(updateLog.operation, AuditOperation.UPDATE);

        final deleteLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'DELETE',
          'entityType': 'ACCOUNT',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(deleteLog.operation, AuditOperation.DELETE);
      });

      test('deserializes all entity types correctly', () {
        final accountLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'CREATE',
          'entityType': 'ACCOUNT',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(accountLog.entityType, AuditEntityType.ACCOUNT);

        final transactionLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'CREATE',
          'entityType': 'TRANSACTION',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(transactionLog.entityType, AuditEntityType.TRANSACTION);

        final categoryLog = AuditLog.fromJson({
          'id': '1',
          'operation': 'CREATE',
          'entityType': 'CATEGORY',
          'entityId': '2',
          'performedBy': '3',
          'groupId': '4',
          'performedAt': '2026-01-23T12:30:00.000Z',
        });
        expect(categoryLog.entityType, AuditEntityType.CATEGORY);
      });
    });

    group('toJson', () {
      test('serializes complete AuditLog correctly', () {
        final auditLog = AuditLog(
          id: '123e4567-e89b-12d3-a456-426614174000',
          operation: AuditOperation.CREATE,
          entityType: AuditEntityType.ACCOUNT,
          entityId: '223e4567-e89b-12d3-a456-426614174000',
          performedBy: '323e4567-e89b-12d3-a456-426614174000',
          groupId: '423e4567-e89b-12d3-a456-426614174000',
          performedAt: DateTime.parse('2026-01-23T12:30:00.000Z'),
          changeDescription: 'Created new account',
        );

        final json = auditLog.toJson();

        expect(json['id'], '123e4567-e89b-12d3-a456-426614174000');
        expect(json['operation'], 'CREATE');
        expect(json['entityType'], 'ACCOUNT');
        expect(json['entityId'], '223e4567-e89b-12d3-a456-426614174000');
        expect(json['performedBy'], '323e4567-e89b-12d3-a456-426614174000');
        expect(json['groupId'], '423e4567-e89b-12d3-a456-426614174000');
        expect(json['performedAt'], '2026-01-23T12:30:00.000Z');
        expect(json['changeDescription'], 'Created new account');
      });

      test('serializes AuditLog with null changeDescription', () {
        final auditLog = AuditLog(
          id: '123e4567-e89b-12d3-a456-426614174000',
          operation: AuditOperation.UPDATE,
          entityType: AuditEntityType.TRANSACTION,
          entityId: '223e4567-e89b-12d3-a456-426614174000',
          performedBy: '323e4567-e89b-12d3-a456-426614174000',
          groupId: '423e4567-e89b-12d3-a456-426614174000',
          performedAt: DateTime.parse('2026-01-23T12:30:00.000Z'),
        );

        final json = auditLog.toJson();

        expect(json['changeDescription'], isNull);
      });

      test('round-trip serialization preserves data', () {
        final original = AuditLog(
          id: '123e4567-e89b-12d3-a456-426614174000',
          operation: AuditOperation.DELETE,
          entityType: AuditEntityType.CATEGORY,
          entityId: '223e4567-e89b-12d3-a456-426614174000',
          performedBy: '323e4567-e89b-12d3-a456-426614174000',
          groupId: '423e4567-e89b-12d3-a456-426614174000',
          performedAt: DateTime.parse('2026-01-23T12:30:00.000Z'),
          changeDescription: 'Deleted category',
        );

        final json = original.toJson();
        final deserialized = AuditLog.fromJson(json);

        expect(deserialized.id, original.id);
        expect(deserialized.operation, original.operation);
        expect(deserialized.entityType, original.entityType);
        expect(deserialized.entityId, original.entityId);
        expect(deserialized.performedBy, original.performedBy);
        expect(deserialized.groupId, original.groupId);
        expect(deserialized.performedAt, original.performedAt);
        expect(deserialized.changeDescription, original.changeDescription);
      });
    });
  });
}
