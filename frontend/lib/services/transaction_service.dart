import '../models/transaction.dart';
import 'http_client.dart';
import 'auth_service.dart';

abstract class TransactionServiceInterface {
  Future<List<Transaction>> getTransactions();

  Future<List<Transaction>> getTransactionsByAccountId(String accountId);

  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime date,
    required String type,
    required String category,
    required String currency,
  });

  Future<Transaction> updateTransaction({
    required String id,
    required double amount,
    required String description,
    required String category,
    required String currency,
  });

  Future<void> deleteTransaction(String transactionId);
}

class TransactionService implements TransactionServiceInterface {
  final ApiClient _apiClient;

  TransactionService({AuthService? authService})
      : _apiClient = ApiClient(authService ?? AuthService());
  @override
  Future<List<Transaction>> getTransactions() async {
    return await _apiClient.getList<Transaction>(
      '/transactions',
      'transactions',
      Transaction.fromJson,
    );
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    return await _apiClient.getList<Transaction>(
      '/accounts/$accountId/transactions',
      'transactions',
      Transaction.fromJson,
    );
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime date,
    required String type,
    required String category,
    required String currency,
  }) async {
    final Map<String, dynamic> transactionData = {
      'accountId': accountId,
      'amount': {
        'value': amount,
        'currency': currency.toUpperCase(),
      },
      'description': description,
      'date': date.toUtc().toIso8601String(),
      'type': type.toUpperCase(),
      'category': category,
    };

    return await _apiClient.post<Transaction>(
      '/transactions',
      transactionData,
      Transaction.fromJson,
    );
  }

  @override
  Future<Transaction> updateTransaction({
    required String id,
    required double amount,
    required String description,
    required String category,
    required String currency,
  }) async {
    final Map<String, dynamic> transactionData = {
      'amount': {
        'value': amount,
        'currency': currency.toUpperCase(),
      },
      'description': description,
      'category': category,
    };

    return await _apiClient.put<Transaction>(
      '/transactions/$id',
      transactionData,
      Transaction.fromJson,
    );
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    await _apiClient.delete('/transactions/$transactionId');
  }
}
