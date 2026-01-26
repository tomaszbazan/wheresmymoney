import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/audit_entity_type.dart';

void main() {
  group('AuditEntityType', () {
    group('displayName', () {
      test('returns correct Polish name for ACCOUNT', () {
        expect(AuditEntityType.ACCOUNT.displayName, 'Konto');
      });

      test('returns correct Polish name for TRANSACTION', () {
        expect(AuditEntityType.TRANSACTION.displayName, 'Transakcja');
      });

      test('returns correct Polish name for CATEGORY', () {
        expect(AuditEntityType.CATEGORY.displayName, 'Kategoria');
      });
    });

    group('icon', () {
      test('returns account_balance_wallet icon for ACCOUNT', () {
        expect(AuditEntityType.ACCOUNT.icon, Icons.account_balance_wallet);
      });

      test('returns receipt_long icon for TRANSACTION', () {
        expect(AuditEntityType.TRANSACTION.icon, Icons.receipt_long);
      });

      test('returns category icon for CATEGORY', () {
        expect(AuditEntityType.CATEGORY.icon, Icons.category);
      });
    });

    group('fromString', () {
      test('parses ACCOUNT correctly', () {
        expect(AuditEntityType.fromString('ACCOUNT'), AuditEntityType.ACCOUNT);
      });

      test('parses TRANSACTION correctly', () {
        expect(AuditEntityType.fromString('TRANSACTION'), AuditEntityType.TRANSACTION);
      });

      test('parses CATEGORY correctly', () {
        expect(AuditEntityType.fromString('CATEGORY'), AuditEntityType.CATEGORY);
      });

      test('parses lowercase values correctly', () {
        expect(AuditEntityType.fromString('account'), AuditEntityType.ACCOUNT);
        expect(AuditEntityType.fromString('transaction'), AuditEntityType.TRANSACTION);
        expect(AuditEntityType.fromString('category'), AuditEntityType.CATEGORY);
      });

      test('throws ArgumentError for unknown value', () {
        expect(() => AuditEntityType.fromString('UNKNOWN'), throwsArgumentError);
      });
    });
  });
}
