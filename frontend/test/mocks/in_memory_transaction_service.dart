import 'package:frontend/models/transaction.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryTransactionService implements TransactionServiceInterface {
  final Map<String, Transaction> _transactions = {};
  Exception? _apiError;

  @override
  Future<List<Transaction>> getTransactions() async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _transactions.values.toList();
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _transactions.values.where((transaction) => transaction.accountId == accountId).toList();
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime date,
    required TransactionType type,
    required String categoryId,
    required String currency,
  }) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final id = const Uuid().v4();
    final transaction = Transaction(
      id: id,
      accountId: accountId,
      amount: amount,
      description: description,
      createdAt: date,
      updatedAt: date,
      type: type,
      categoryId: categoryId,
      categoryName: null,
    );
    _transactions[id] = transaction;
    return transaction;
  }

  @override
  Future<Transaction> updateTransaction({required String id, required double amount, required String description, required String categoryId, required String currency}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final existingTransaction = _transactions[id];
    if (existingTransaction == null) {
      throw Exception('Transaction not found');
    }

    final updatedTransaction = Transaction(
      id: id,
      accountId: existingTransaction.accountId,
      amount: amount,
      description: description,
      createdAt: existingTransaction.createdAt,
      updatedAt: DateTime.now(),
      type: existingTransaction.type,
      categoryId: categoryId,
      categoryName: null,
    );
    _transactions[id] = updatedTransaction;
    return updatedTransaction;
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    _transactions.remove(transactionId);
  }

  void clear() {
    _transactions.clear();
    _apiError = null;
  }

  void setApiError(Exception error) {
    _apiError = error;
  }
}
