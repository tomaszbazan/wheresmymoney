import 'package:flutter_test/flutter_test.dart';

void main() {
  group('Login error message handling', () {
    test('should return user-friendly message for invalid credentials', () {
      String getErrorMessage(dynamic error) {
        String errorString = error.toString().toLowerCase();
        
        if (errorString.contains('invalid') && 
            (errorString.contains('credential') || errorString.contains('password') || errorString.contains('email'))) {
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

      expect(getErrorMessage(Exception('Invalid email or password')), 'Nieprawidłowy email lub hasło');
      expect(getErrorMessage(Exception('Invalid credentials')), 'Nieprawidłowy email lub hasło');
      expect(getErrorMessage(Exception('Too many requests')), 'Zbyt wiele prób logowania. Spróbuj ponownie za chwilę');
      expect(getErrorMessage(Exception('Network error')), 'Błąd połączenia z serwerem. Sprawdź połączenie internetowe');
      expect(getErrorMessage(Exception('Some other error')), 'Błąd logowania. Spróbuj ponownie');
    });
  });
}