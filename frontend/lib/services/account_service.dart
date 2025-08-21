import '../models/account.dart';
import 'http_client.dart';
import 'auth_service.dart';

abstract class AccountServiceInterface {
  Future<List<Account>> getAccounts();
  Future<Account> createAccount(String name, {String? type, String? currency});
  Future<void> deleteAccount(String accountId);
}

class AccountService implements AccountServiceInterface {
  final ApiClient _apiClient;

  AccountService({AuthService? authService}) 
    : _apiClient = ApiClient(authService ?? AuthService());

  @override
  Future<List<Account>> getAccounts() async {
    return await _apiClient.getList<Account>(
      '/accounts',
      'accounts',
      Account.fromJson,
    );
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

    return await _apiClient.post<Account>(
      '/accounts',
      accountData,
      Account.fromJson,
    );
  }

  @override
  Future<void> deleteAccount(String accountId) async {
    await _apiClient.delete('/accounts/$accountId');
  }
}