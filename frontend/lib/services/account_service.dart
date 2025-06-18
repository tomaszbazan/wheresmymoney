import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/account.dart';

abstract class AccountServiceInterface {
  Future<List<Account>> getAccounts();
  Future<Account> createAccount(String name, {String? type, String? currency});
  Future<void> deleteAccount(String accountId);
}

class AccountService implements AccountServiceInterface {
  static const String baseUrl = 'http://localhost:8080/api';

  @override
  Future<List<Account>> getAccounts() async {
    final response = await http.get(
      Uri.parse('$baseUrl/accounts'),
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
      throw Exception('Failed to load accounts');
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
    
    final response = await http.post(
      Uri.parse('$baseUrl/accounts'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(accountData),
    );

    if (response.statusCode == 201) {
      return Account.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to create account: ${response.statusCode}');
    }
  }

  @override
  Future<void> deleteAccount(String accountId) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/accounts/$accountId'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Failed to delete account: ${response.statusCode}');
    }
  }
}