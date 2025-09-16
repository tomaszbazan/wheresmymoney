class HttpException implements Exception {
  final int statusCode;
  final String message;

  const HttpException(this.statusCode, this.message);

  bool get isClientError => statusCode >= 400 && statusCode < 500;

  bool get isServerError => statusCode >= 500 && statusCode < 600;

  bool get isNetworkError => statusCode == 0;

  String get userFriendlyMessage {
    if (message.isNotEmpty && message != 'Connection refused') {
      return message;
    }

    if (isNetworkError) {
      return 'Błąd połączenia z serwerem';
    }

    if (isServerError) {
      return 'Błąd serwera';
    }

    if (isClientError) {
      return switch (statusCode) {
        400 => 'Nieprawidłowe dane w żądaniu',
        401 => 'Brak autoryzacji',
        404 => 'Zasób nie został znaleziony',
        409 => 'Konflikt danych',
        422 => 'Błąd walidacji danych',
        _ => 'Błąd klienta: $statusCode',
      };
    }

    return 'Nieznany błąd: $statusCode';
  }

  @override
  String toString() => 'HttpException: $statusCode - $message';
}
