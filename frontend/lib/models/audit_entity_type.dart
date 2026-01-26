import 'package:flutter/material.dart';

// ignore_for_file: constant_identifier_names

enum AuditEntityType {
  ACCOUNT,
  TRANSACTION,
  CATEGORY;

  String get displayName {
    return switch (this) {
      AuditEntityType.ACCOUNT => 'Konto',
      AuditEntityType.TRANSACTION => 'Transakcja',
      AuditEntityType.CATEGORY => 'Kategoria',
    };
  }

  IconData get icon {
    return switch (this) {
      AuditEntityType.ACCOUNT => Icons.account_balance_wallet,
      AuditEntityType.TRANSACTION => Icons.receipt_long,
      AuditEntityType.CATEGORY => Icons.category,
    };
  }

  static AuditEntityType fromString(String value) {
    return switch (value.toUpperCase()) {
      'ACCOUNT' => AuditEntityType.ACCOUNT,
      'TRANSACTION' => AuditEntityType.TRANSACTION,
      'CATEGORY' => AuditEntityType.CATEGORY,
      _ => throw ArgumentError('Unknown audit entity type: $value'),
    };
  }
}
