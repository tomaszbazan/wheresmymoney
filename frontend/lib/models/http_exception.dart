class HttpException implements Exception {
  final int statusCode;
  final String message;

  const HttpException(this.statusCode, this.message);

  bool get isClientError => statusCode >= 400 && statusCode < 500;

  bool get isServerError => statusCode >= 500 && statusCode < 600;

  bool get isNetworkError => statusCode == 0;

  String get userFriendlyMessage {
    if (isNetworkError) {
      return 'Błąd połączenia z serwerem';
    }

    if (isServerError) {
      return 'Błąd serwera';
    }

    if (isClientError) {
      return message.isNotEmpty ? message : 'Błąd klienta: $statusCode';
    }

    return 'Nieznany błąd: $statusCode';
  }

  @override
  String toString() => 'HttpException: $statusCode - $message';
}
