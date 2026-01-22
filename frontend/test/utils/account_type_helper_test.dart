import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/utils/account_type_helper.dart';

void main() {
  group('AccountTypeHelper', () {
    group('getIconForType', () {
      test('returns account_balance icon for Rachunek bieżący', () {
        final icon = AccountTypeHelper.getIconForType('Rachunek bieżący');

        expect(icon.icon, Icons.account_balance);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });

      test('returns savings icon for Oszczędnościowe', () {
        final icon = AccountTypeHelper.getIconForType('Oszczędnościowe');

        expect(icon.icon, Icons.savings);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });

      test('returns payments icon for Gotówka', () {
        final icon = AccountTypeHelper.getIconForType('Gotówka');

        expect(icon.icon, Icons.payments);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });

      test('returns credit_card icon for Kredytowa', () {
        final icon = AccountTypeHelper.getIconForType('Kredytowa');

        expect(icon.icon, Icons.credit_card);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });

      test('returns default icon for unknown type', () {
        final icon = AccountTypeHelper.getIconForType('Unknown');

        expect(icon.icon, Icons.account_balance_wallet);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });

      test('returns default icon for null type', () {
        final icon = AccountTypeHelper.getIconForType(null);

        expect(icon.icon, Icons.account_balance_wallet);
        expect(icon.size, 20);
        expect(icon.color, Colors.white);
      });
    });

    group('getColorForType', () {
      test('returns blue for Rachunek bieżący', () {
        final color = AccountTypeHelper.getColorForType('Rachunek bieżący');

        expect(color, Colors.blue);
      });

      test('returns green for Oszczędnościowe', () {
        final color = AccountTypeHelper.getColorForType('Oszczędnościowe');

        expect(color, Colors.green);
      });

      test('returns amber.shade700 for Gotówka', () {
        final color = AccountTypeHelper.getColorForType('Gotówka');

        expect(color, Colors.amber.shade700);
      });

      test('returns purple for Kredytowa', () {
        final color = AccountTypeHelper.getColorForType('Kredytowa');

        expect(color, Colors.purple);
      });

      test('returns grey for unknown type', () {
        final color = AccountTypeHelper.getColorForType('Unknown');

        expect(color, Colors.grey);
      });

      test('returns grey for null type', () {
        final color = AccountTypeHelper.getColorForType(null);

        expect(color, Colors.grey);
      });
    });
  });
}
