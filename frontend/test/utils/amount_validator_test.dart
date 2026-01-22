import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/utils/amount_validator.dart';

void main() {
  group('AmountValidator', () {
    group('normalize', () {
      test('adds .00 when no decimal separator', () {
        expect(AmountValidator.normalize('100'), '100.00');
      });

      test('adds trailing zero when only one decimal place', () {
        expect(AmountValidator.normalize('100.5'), '100.50');
      });

      test('preserves two decimal places', () {
        expect(AmountValidator.normalize('100.50'), '100.50');
      });

      test('replaces comma with dot', () {
        expect(AmountValidator.normalize('100,50'), '100.50');
      });

      test('trims whitespace', () {
        expect(AmountValidator.normalize('  100.50  '), '100.50');
      });

      test('handles comma with one decimal place', () {
        expect(AmountValidator.normalize('100,5'), '100.50');
      });
    });

    group('validate', () {
      test('returns null for valid amount with two decimals', () {
        expect(AmountValidator.validate('100.50'), null);
      });

      test('returns null for valid amount with one decimal', () {
        expect(AmountValidator.validate('100.5'), null);
      });

      test('returns null for valid amount without decimals', () {
        expect(AmountValidator.validate('100'), null);
      });

      test('returns null for valid amount with comma', () {
        expect(AmountValidator.validate('100,50'), null);
      });

      test('returns error for empty value', () {
        expect(AmountValidator.validate(''), 'Wprowadź kwotę');
      });

      test('returns error for null value', () {
        expect(AmountValidator.validate(null), 'Wprowadź kwotę');
      });

      test('returns error for zero amount', () {
        expect(AmountValidator.validate('0'), 'Kwota musi być większa od zera');
      });

      test('returns error for negative amount', () {
        expect(AmountValidator.validate('-100'), 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)');
      });

      test('returns error for invalid format', () {
        expect(AmountValidator.validate('abc'), 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)');
      });

      test('returns error for amount with more than two decimal places', () {
        expect(AmountValidator.validate('100.123'), 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)');
      });

      test('returns error for non-numeric input', () {
        expect(AmountValidator.validate('10a0'), 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)');
      });
    });
  });
}
