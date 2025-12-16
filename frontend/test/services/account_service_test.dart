import 'package:flutter_test/flutter_test.dart';

import '../mocks/in_memory_account_service.dart';

void main() {
  group('AccountService', () {
    late InMemoryAccountService accountService;

    setUp(() {
      accountService = InMemoryAccountService();
    });

    test('should return empty list when no accounts exist', () async {
      final accounts = await accountService.getAccounts();

      expect(accounts, isEmpty);
    });

    test('should create account with default values', () async {
      final account = await accountService.createAccount('Test Account');

      expect(account.name, 'Test Account');
      expect(account.balance, 0.0);
      expect(account.type, 'Rachunek bieżący');
      expect(account.currency, 'PLN');
      expect(account.id, isNotEmpty);
    });

    test('should create account with custom type and currency', () async {
      final account = await accountService.createAccount('Savings Account', type: 'Oszczędnościowy', currency: 'EUR');

      expect(account.name, 'Savings Account');
      expect(account.type, 'Oszczędnościowy');
      expect(account.currency, 'EUR');
    });

    test('should return all created accounts', () async {
      await accountService.createAccount('Account 1');
      await accountService.createAccount('Account 2');
      await accountService.createAccount('Account 3');

      final accounts = await accountService.getAccounts();

      expect(accounts.length, 3);
    });

    test('should delete account by id', () async {
      final account1 = await accountService.createAccount('Account 1');
      final account2 = await accountService.createAccount('Account 2');

      await accountService.deleteAccount(account1.id);

      final accounts = await accountService.getAccounts();
      expect(accounts.length, 1);
      expect(accounts.first.id, account2.id);
    });

    test('should persist added accounts', () async {
      accountService.createAccount('Manual Account', type: 'Bieżący', currency: 'USD');

      final accounts = await accountService.getAccounts();
      expect(accounts.length, 1);
      expect(accounts.first.id, isNotEmpty);
      expect(accounts.first.balance, 0.0);
    });
  });
}
