import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/http_exception.dart';
import 'auth_service.dart';

class AuthenticatedHttpClient extends http.BaseClient {
  final http.Client _inner;
  final AuthService _authService;

  AuthenticatedHttpClient(this._inner, this._authService);

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) async {
    final token = await _authService.getAccessToken();

    if (token != null) {
      request.headers['Authorization'] = 'Bearer $token';
    }

    request.headers['Content-Type'] = 'application/json';

    return _inner.send(request);
  }
}

class ApiClient {
  late final AuthenticatedHttpClient _httpClient;
  final AuthService _authService;

  ApiClient(AuthService authService) : _authService = authService {
    _httpClient = AuthenticatedHttpClient(http.Client(), _authService);
  }

  Future<T> get<T>(String endpoint,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    try {
      final response = await _httpClient.get(
        Uri.parse('${ApiConfig.backendUrl}$endpoint'),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        return fromJson(responseData);
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<List<T>> getList<T>(String endpoint,
    String listKey,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    try {
      final response = await _httpClient.get(
        Uri.parse('${ApiConfig.backendUrl}$endpoint'),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);

        if (responseData.containsKey(listKey)) {
          final List<dynamic> itemsJson = responseData[listKey];
          return itemsJson.map((json) => fromJson(json)).toList();
        } else {
          return [];
        }
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<T> post<T>(String endpoint,
    Map<String, dynamic> body,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    try {
      final response = await _httpClient.post(
        Uri.parse('${ApiConfig.backendUrl}$endpoint'),
        body: jsonEncode(body),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return fromJson(jsonDecode(response.body));
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<void> delete(String endpoint) async {
    try {
      final response = await _httpClient.delete(
        Uri.parse('${ApiConfig.backendUrl}$endpoint'),
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<T> put<T>(String endpoint,
    Map<String, dynamic> body,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    try {
      final response = await _httpClient.put(
        Uri.parse('${ApiConfig.backendUrl}$endpoint'),
        body: jsonEncode(body),
      );

      if (response.statusCode == 200) {
        return fromJson(jsonDecode(response.body));
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  void close() {
    _httpClient.close();
  }
}
