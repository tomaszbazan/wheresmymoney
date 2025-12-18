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

    if (request is! http.MultipartRequest) {
      request.headers['Content-Type'] = 'application/json';
    }

    return _inner.send(request);
  }
}

class ApiClient {
  late final AuthenticatedHttpClient _httpClient;
  final AuthService _authService;

  ApiClient(AuthService authService, {http.Client? httpClient}) : _authService = authService {
    _httpClient = AuthenticatedHttpClient(httpClient ?? http.Client(), _authService);
  }

  Future<T> get<T>(String endpoint, T Function(Map<String, dynamic>) fromJson) async {
    try {
      final response = await _httpClient.get(Uri.parse('${ApiConfig.backendUrl}$endpoint'));

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body) as Map<String, dynamic>;
        return fromJson(responseData);
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<List<T>> getList<T>(String endpoint, String listKey, T Function(Map<String, dynamic>) fromJson) async {
    try {
      final response = await _httpClient.get(Uri.parse('${ApiConfig.backendUrl}$endpoint'));

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body) as Map<String, dynamic>;

        if (responseData.containsKey(listKey)) {
          final List<dynamic> itemsJson = responseData[listKey] as List<dynamic>;
          return itemsJson.map((json) => fromJson(json as Map<String, dynamic>)).toList();
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

  Future<T> post<T>(String endpoint, Map<String, dynamic> body, T Function(Map<String, dynamic>) fromJson) async {
    try {
      final response = await _httpClient.post(Uri.parse('${ApiConfig.backendUrl}$endpoint'), body: jsonEncode(body));

      if (response.statusCode == 200 || response.statusCode == 201) {
        return fromJson(jsonDecode(response.body) as Map<String, dynamic>);
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<void> delete(String endpoint) async {
    try {
      final response = await _httpClient.delete(Uri.parse('${ApiConfig.backendUrl}$endpoint'));

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<T> put<T>(String endpoint, Map<String, dynamic> body, T Function(Map<String, dynamic>) fromJson) async {
    try {
      final response = await _httpClient.put(Uri.parse('${ApiConfig.backendUrl}$endpoint'), body: jsonEncode(body));

      if (response.statusCode == 200) {
        return fromJson(jsonDecode(response.body) as Map<String, dynamic>);
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  Future<T> postMultipart<T>(String endpoint, Map<String, String> fields, Map<String, http.MultipartFile> files, T Function(Map<String, dynamic>) fromJson) async {
    try {
      final request = http.MultipartRequest('POST', Uri.parse('${ApiConfig.backendUrl}$endpoint'));

      final token = await _authService.getAccessToken();
      if (token != null) {
        request.headers['Authorization'] = 'Bearer $token';
      }

      request.fields.addAll(fields);
      request.files.addAll(files.values);

      final streamedResponse = await _httpClient.send(request);
      final response = await http.Response.fromStream(streamedResponse);

      if (response.statusCode == 200 || response.statusCode == 201) {
        return fromJson(jsonDecode(response.body) as Map<String, dynamic>);
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
