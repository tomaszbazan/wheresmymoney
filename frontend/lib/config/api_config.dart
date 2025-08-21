class ApiConfig {
  static const String _defaultBackendUrl = 'http://localhost:9080/api';

  static String get backendUrl {
    const envUrl = String.fromEnvironment('BACKEND_BASE_URL');

    if (envUrl.isNotEmpty) {
      return envUrl;
    }

    return _defaultBackendUrl;
  }
}
