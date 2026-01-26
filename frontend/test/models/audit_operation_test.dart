import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/audit_operation.dart';

void main() {
  group('AuditOperation', () {
    group('displayName', () {
      test('returns correct Polish name for CREATE', () {
        expect(AuditOperation.CREATE.displayName, 'Utworzenie');
      });

      test('returns correct Polish name for UPDATE', () {
        expect(AuditOperation.UPDATE.displayName, 'Aktualizacja');
      });

      test('returns correct Polish name for DELETE', () {
        expect(AuditOperation.DELETE.displayName, 'Usunięcie');
      });
    });

    group('icon', () {
      test('returns add_circle_outline icon for CREATE', () {
        expect(AuditOperation.CREATE.icon, Icons.add_circle_outline);
      });

      test('returns edit_outlined icon for UPDATE', () {
        expect(AuditOperation.UPDATE.icon, Icons.edit_outlined);
      });

      test('returns delete_outline icon for DELETE', () {
        expect(AuditOperation.DELETE.icon, Icons.delete_outline);
      });
    });

    group('color', () {
      test('returns green color for CREATE', () {
        expect(AuditOperation.CREATE.color, Colors.green);
      });

      test('returns orange color for UPDATE', () {
        expect(AuditOperation.UPDATE.color, Colors.orange);
      });

      test('returns red color for DELETE', () {
        expect(AuditOperation.DELETE.color, Colors.red);
      });
    });

    group('fromString', () {
      test('parses CREATE correctly', () {
        expect(AuditOperation.fromString('CREATE'), AuditOperation.CREATE);
      });

      test('parses UPDATE correctly', () {
        expect(AuditOperation.fromString('UPDATE'), AuditOperation.UPDATE);
      });

      test('parses DELETE correctly', () {
        expect(AuditOperation.fromString('DELETE'), AuditOperation.DELETE);
      });

      test('parses lowercase values correctly', () {
        expect(AuditOperation.fromString('create'), AuditOperation.CREATE);
        expect(AuditOperation.fromString('update'), AuditOperation.UPDATE);
        expect(AuditOperation.fromString('delete'), AuditOperation.DELETE);
      });

      test('throws ArgumentError for unknown value', () {
        expect(() => AuditOperation.fromString('UNKNOWN'), throwsArgumentError);
      });
    });
  });
}
