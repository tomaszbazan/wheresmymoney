import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/utils/password_validator.dart';

void main() {
  group('PasswordValidator', () {
    test('should return null for valid password with all character types', () {
      const password = 'Abc123!@#';
      final result = PasswordValidator.validate(password);
      expect(result, isNull);
    });

    test('should return error for password without lowercase letters', () {
      const password = 'ABC123!@#';
      final result = PasswordValidator.validate(password);
      expect(result, contains('lowercase'));
    });

    test('should return error for password without uppercase letters', () {
      const password = 'abc123!@#';
      final result = PasswordValidator.validate(password);
      expect(result, contains('uppercase'));
    });

    test('should return error for password without digits', () {
      const password = 'Abc!@#def';
      final result = PasswordValidator.validate(password);
      expect(result, contains('digit'));
    });

    test('should return error for password without special characters', () {
      const password = 'Abc123def';
      final result = PasswordValidator.validate(password);
      expect(result, contains('special'));
    });

    test('should return error for password shorter than 8 characters', () {
      const password = 'Abc12!';
      final result = PasswordValidator.validate(password);
      expect(result, contains('8 characters'));
    });

    test('should return null for empty password', () {
      const password = '';
      final result = PasswordValidator.validate(password);
      expect(result, equals('Please enter your password'));
    });

    test('should return null for null password', () {
      final result = PasswordValidator.validate(null);
      expect(result, equals('Please enter your password'));
    });

    test('should handle edge case with minimum valid password', () {
      const password = 'Abc123!a';
      final result = PasswordValidator.validate(password);
      expect(result, isNull);
    });

    test(
      'should handle password with all special characters from the requirement',
      () {
        const password = 'Abc123!@#\$%^&*()_+-=[]{};\':"|<>?,./`~';
        final result = PasswordValidator.validate(password);
        expect(result, isNull);
      },
    );
  });
}
