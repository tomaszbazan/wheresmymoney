import 'package:frontend/models/account.dart';
import 'package:frontend/services/account_service.dart';
import 'package:mockito/mockito.dart';

class MockAccountServiceInterface extends Mock implements AccountServiceInterface {
  @override
  Future<List<Account>> getAccounts() => super.noSuchMethod(
        Invocation.method(#getAccounts, []),
        returnValue: Future<List<Account>>.value([]),
      );

  @override
  Future<Account> createAccount(String name, {String? type, String? currency}) => 
      super.noSuchMethod(
        Invocation.method(#createAccount, [name], {#type: type, #currency: currency}),
        returnValue: Future<Account>.value(Account(
          id: 'test-id',
          name: name,
          balance: 0.0,
          number: 'TEST000001',
          type: type ?? 'Rachunek bieżący',
          currency: currency ?? 'PLN',
        )),
      );

  @override
  Future<void> deleteAccount(String accountId) => super.noSuchMethod(
        Invocation.method(#deleteAccount, [accountId]),
        returnValue: Future<void>.value(),
      );
}