import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/account.dart';
import '../models/http_exception.dart';

abstract class AccountServiceInterface {
  Future<List<Account>> getAccounts();
  Future<Account> createAccount(String name, {String? type, String? currency});
  Future<void> deleteAccount(String accountId);
}

class AccountService implements AccountServiceInterface {
  final http.Client _httpClient;

  AccountService({http.Client? httpClient})
    : _httpClient = httpClient ?? http.Client();

  @override
  Future<List<Account>> getAccounts() async {
    try {
      final response = await _httpClient.get(
        Uri.parse('${ApiConfig.backendUrl}/accounts'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);

        if (responseData.containsKey('accounts')) {
          final List<dynamic> accountsJson = responseData['accounts'];
          return accountsJson.map((json) => Account.fromJson(json)).toList();
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

  @override
  Future<Account> createAccount(String name, {String? type, String? currency}) async {
    final Map<String, dynamic> accountData = {
      'name': name,
    };
    
    if (type != null) {
      accountData['type'] = type;
    }
    
    if (currency != null) {
      accountData['currency'] = currency;
    }

    try {
      final response = await _httpClient.post(
        Uri.parse('${ApiConfig.backendUrl}/accounts'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(accountData),
      );

      if (response.statusCode == 201) {
        return Account.fromJson(jsonDecode(response.body));
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  @override
  Future<void> deleteAccount(String accountId) async {
    try {
      final response = await _httpClient.delete(
        Uri.parse('${ApiConfig.backendUrl}/accounts/$accountId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }
}