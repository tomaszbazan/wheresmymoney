class PasswordValidator {
  static String? validate(String? password) {
    if (password == null || password.isEmpty) {
      return 'Please enter your password';
    }

    if (password.length < 8) {
      return 'Password must be at least 8 characters';
    }

    final hasLowercase = password.contains(RegExp(r'[a-z]'));
    if (!hasLowercase) {
      return 'Password must contain at least one lowercase letter';
    }

    final hasUppercase = password.contains(RegExp(r'[A-Z]'));
    if (!hasUppercase) {
      return 'Password must contain at least one uppercase letter';
    }

    final hasDigit = password.contains(RegExp(r'[0-9]'));
    if (!hasDigit) {
      return 'Password must contain at least one digit';
    }

    final hasSpecialChar = password.contains(
      RegExp('[!@#\$%^&*()_+\\-=\\[\\]{};\':"\\\\|<>?,./`~]'),
    );
    if (!hasSpecialChar) {
      return 'Password must contain at least one special character';
    }

    return null;
  }
}
