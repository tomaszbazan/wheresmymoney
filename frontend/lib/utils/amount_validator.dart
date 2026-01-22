class AmountValidator {
  static String normalize(String amount) {
    String normalized = amount.trim().replaceAll(',', '.');

    if (!normalized.contains('.')) {
      normalized = '$normalized.00';
    } else {
      final parts = normalized.split('.');
      if (parts.length == 2 && parts[1].length == 1) {
        normalized = '${parts[0]}.${parts[1]}0';
      }
    }

    return normalized;
  }

  static String? validate(String? value) {
    if (value == null || value.isEmpty) {
      return 'Wprowadź kwotę';
    }

    final regex = RegExp(r'^\d+([.,]\d{1,2})?$');
    if (!regex.hasMatch(value.trim())) {
      return 'Wprowadź poprawną kwotę (np. 100, 100.00, 100,50)';
    }

    final normalizedValue = normalize(value);
    final doubleValue = double.tryParse(normalizedValue);

    if (doubleValue == null) {
      return 'Wprowadź poprawną kwotę';
    }

    if (doubleValue <= 0) {
      return 'Kwota musi być większa od zera';
    }

    return null;
  }
}
