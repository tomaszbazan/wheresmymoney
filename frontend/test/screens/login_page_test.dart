import 'package:flutter_test/flutter_test.dart';

void main() {
  group('Login error message handling', () {
    test('should return user-friendly message for invalid credentials', () {
      String getErrorMessage(dynamic error) {
        String errorString = error.toString().toLowerCase();

        if (errorString.contains('email not confirmed')) {
          return 'Email nie został potwierdzony. Sprawdź swoją skrzynkę mailową i kliknij link aktywacyjny';
        }

        if (errorString.contains('invalid') && (errorString.contains('credential') || errorString.contains('password') || errorString.contains('email'))) {
          return 'Nieprawidłowy email lub hasło';
        }

        if (errorString.contains('invalid email or password')) {
          return 'Nieprawidłowy email lub hasło';
        }

        if (errorString.contains('too many requests')) {
          return 'Zbyt wiele prób logowania. Spróbuj ponownie za chwilę';
        }

        if (errorString.contains('network') || errorString.contains('connection')) {
          return 'Błąd połączenia z serwerem. Sprawdź połączenie internetowe';
        }

        return 'Błąd logowania. Spróbuj ponownie';
      }

      expect(getErrorMessage(Exception('Email not confirmed')), 'Email nie został potwierdzony. Sprawdź swoją skrzynkę mailową i kliknij link aktywacyjny');
      expect(getErrorMessage(Exception('Invalid email or password')), 'Nieprawidłowy email lub hasło');
      expect(getErrorMessage(Exception('Invalid credentials')), 'Nieprawidłowy email lub hasło');
      expect(getErrorMessage(Exception('Too many requests')), 'Zbyt wiele prób logowania. Spróbuj ponownie za chwilę');
      expect(getErrorMessage(Exception('Network error')), 'Błąd połączenia z serwerem. Sprawdź połączenie internetowe');
      expect(getErrorMessage(Exception('Some other error')), 'Błąd logowania. Spróbuj ponownie');
    });
  });
}
