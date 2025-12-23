import 'package:frontend/models/bulk_create_response.dart';
import 'package:frontend/models/transaction.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryTransactionService implements TransactionService {
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

  @override
  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<Map<String, dynamic>> transactions}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final savedIds = <String>[];
    var duplicateCount = 0;

    for (final txData in transactions) {
      final id = const Uuid().v4();
      final transaction = Transaction(
        id: id,
        accountId: accountId,
        amount: (txData['amount']['value'] as num).toDouble(),
        description: txData['description'] as String,
        createdAt: DateTime.parse(txData['date'] as String),
        updatedAt: DateTime.parse(txData['date'] as String),
        type: TransactionType.values.firstWhere((t) => t.name.toUpperCase() == txData['type']),
        categoryId: txData['categoryId'] as String,
        categoryName: null,
      );
      _transactions[id] = transaction;
      savedIds.add(id);
    }

    return BulkCreateResponse(savedCount: savedIds.length, duplicateCount: duplicateCount, savedTransactionIds: savedIds);
  }

  void clear() {
    _transactions.clear();
    _apiError = null;
  }

  void setApiError(Exception error) {
    _apiError = error;
  }
}
