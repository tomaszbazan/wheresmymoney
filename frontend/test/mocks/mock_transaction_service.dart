import 'package:frontend/models/transaction.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:mockito/mockito.dart';

class MockTransactionServiceInterface extends Mock
    implements TransactionServiceInterface {
  @override
  Future<List<Transaction>> getTransactions() => super.noSuchMethod(
    Invocation.method(#getTransactions, []),
    returnValue: Future<List<Transaction>>.value([]),
  );

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) =>
      super.noSuchMethod(
        Invocation.method(#getTransactionsByAccountId, [accountId]),
        returnValue: Future<List<Transaction>>.value([]),
      );

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime date,
    required String type,
    required String category,
    required String currency,
  }) => super.noSuchMethod(
    Invocation.method(#createTransaction, [], {
      #accountId: accountId,
      #amount: amount,
      #description: description,
      #date: date,
      #type: type,
      #category: category,
      #currency: currency,
    }),
    returnValue: Future<Transaction>.value(
      Transaction(
        id: 'test-id',
        accountId: accountId,
        amount: amount,
        description: description,
        createdAt: date,
        updatedAt: date,
        type: type,
        category: category,
      ),
    ),
  );

  @override
  Future<Transaction> updateTransaction({
    required String id,
    required double amount,
    required String description,
    required String category,
    required String currency,
  }) => super.noSuchMethod(
    Invocation.method(#updateTransaction, [], {
      #id: id,
      #amount: amount,
      #description: description,
      #category: category,
      #currency: currency,
    }),
    returnValue: Future<Transaction>.value(
      Transaction(
        id: id,
        accountId: 'test-account',
        amount: amount,
        description: description,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
        type: 'EXPENSE',
        category: category,
      ),
    ),
  );

  @override
  Future<void> deleteTransaction(String transactionId) => super.noSuchMethod(
    Invocation.method(#deleteTransaction, [transactionId]),
    returnValue: Future<void>.value(),
  );
}
