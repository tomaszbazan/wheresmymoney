import 'package:http/http.dart' as http;

import '../models/account.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class AccountService {
  Future<List<Account>> getAccounts();
  Future<Account> createAccount(String name, {String? type, String? currency});
  Future<void> deleteAccount(String accountId);
}

class RestAccountService implements AccountService {
  final ApiClient _apiClient;

  RestAccountService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);

  @override
  Future<List<Account>> getAccounts() async {
    return await _apiClient.getList<Account>('/accounts', 'accounts', Account.fromJson);
  }

  @override
  Future<Account> createAccount(String name, {String? type, String? currency}) async {
    final Map<String, dynamic> accountData = {'name': name};

    if (type != null) {
      accountData['type'] = type;
    }

    if (currency != null) {
      accountData['currency'] = currency;
    }

    return await _apiClient.post<Account>('/accounts', accountData, Account.fromJson);
  }

  @override
  Future<void> deleteAccount(String accountId) async {
    await _apiClient.delete('/accounts/$accountId');
  }
}
