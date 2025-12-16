import 'package:frontend/models/account.dart';
import 'package:frontend/services/account_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryAccountService implements AccountService {
  final Map<String, Account> _accounts = {};
  Exception? _apiError;

  @override
  Future<List<Account>> getAccounts() async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _accounts.values.toList();
  }

  @override
  Future<Account> createAccount(String name, {String? type, String? currency}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final id = Uuid().v4();
    final account = Account(id: id, name: name, balance: 0.0, type: type ?? 'Rachunek bieżący', currency: currency ?? 'PLN', createdAt: DateTime.now(), updatedAt: DateTime.now());
    _accounts[id] = account;
    return account;
  }

  Future<Account> addAccount(String name, {double? balance, String? currency}) async {
    final id = Uuid().v4();
    final account = Account(
      id: id,
      name: name,
      balance: balance ?? 0.0,
      type: 'Rachunek bieżący',
      currency: currency ?? 'PLN',
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );
    _accounts[id] = account;
    return account;
  }

  @override
  Future<void> deleteAccount(String accountId) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    _accounts.remove(accountId);
  }

  void clear() {
    _accounts.clear();
    _apiError = null;
  }

  void setGetAccountsError(Exception error) {
    _apiError = error;
  }
}
