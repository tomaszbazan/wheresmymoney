import 'package:flutter/material.dart';

// ignore_for_file: constant_identifier_names

enum AuditOperation {
  CREATE,
  UPDATE,
  DELETE;

  String get displayName {
    return switch (this) {
      AuditOperation.CREATE => 'Utworzenie',
      AuditOperation.UPDATE => 'Aktualizacja',
      AuditOperation.DELETE => 'Usunięcie',
    };
  }

  IconData get icon {
    return switch (this) {
      AuditOperation.CREATE => Icons.add_circle_outline,
      AuditOperation.UPDATE => Icons.edit_outlined,
      AuditOperation.DELETE => Icons.delete_outline,
    };
  }

  Color get color {
    return switch (this) {
      AuditOperation.CREATE => Colors.green,
      AuditOperation.UPDATE => Colors.orange,
      AuditOperation.DELETE => Colors.red,
    };
  }

  static AuditOperation fromString(String value) {
    return switch (value.toUpperCase()) {
      'CREATE' => AuditOperation.CREATE,
      'UPDATE' => AuditOperation.UPDATE,
      'DELETE' => AuditOperation.DELETE,
      _ => throw ArgumentError('Unknown audit operation: $value'),
    };
  }
}
