import 'package:frontend/models/transaction_type.dart';
import 'package:http/http.dart' as http;

import '../models/bulk_create_response.dart';
import '../models/transaction.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class TransactionService {
  Future<List<Transaction>> getTransactions();

  Future<List<Transaction>> getTransactionsByAccountId(String accountId);

  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime transactionDate,
    required TransactionType type,
    required String categoryId,
    required String currency,
  });

  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<Map<String, dynamic>> transactions});

  Future<Transaction> updateTransaction({required String id, required double amount, required String description, required String categoryId, required String currency});

  Future<void> deleteTransaction(String transactionId);
}

class RestTransactionService implements TransactionService {
  final ApiClient _apiClient;

  RestTransactionService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);
  @override
  Future<List<Transaction>> getTransactions() async {
    return await _apiClient.getList<Transaction>('/transactions', 'transactions', Transaction.fromJson);
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    return await _apiClient.getList<Transaction>('/accounts/$accountId/transactions', 'transactions', Transaction.fromJson);
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required double amount,
    required String description,
    required DateTime transactionDate,
    required TransactionType type,
    required String categoryId,
    required String currency,
  }) async {
    final Map<String, dynamic> transactionData = {
      'accountId': accountId,
      'amount': {'value': amount, 'currency': currency.toUpperCase()},
      'description': description,
      'transactionDate': transactionDate.toUtc().toIso8601String(),
      'type': type.name.toUpperCase(),
      'categoryId': categoryId,
    };

    return await _apiClient.post<Transaction>('/transactions', transactionData, Transaction.fromJson);
  }

  @override
  Future<Transaction> updateTransaction({required String id, required double amount, required String description, required String categoryId, required String currency}) async {
    final Map<String, dynamic> transactionData = {
      'amount': {'value': amount, 'currency': currency.toUpperCase()},
      'description': description,
      'categoryId': categoryId,
    };

    return await _apiClient.put<Transaction>('/transactions/$id', transactionData, Transaction.fromJson);
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    await _apiClient.delete('/transactions/$transactionId');
  }

  @override
  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<Map<String, dynamic>> transactions}) async {
    final Map<String, dynamic> bulkData = {'accountId': accountId, 'transactions': transactions};

    return await _apiClient.post<BulkCreateResponse>('/transactions/bulk', bulkData, BulkCreateResponse.fromJson);
  }
}
