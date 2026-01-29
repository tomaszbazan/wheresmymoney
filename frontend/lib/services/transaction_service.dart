import 'package:frontend/models/transaction_type.dart';
import 'package:http/http.dart' as http;

import '../models/bulk_create_response.dart';
import '../models/transaction/transaction.dart';
import '../models/transaction_page.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class TransactionService {
  Future<TransactionPage> getTransactions({required int page, required int size});

  Future<Transaction> createTransaction({
    required String accountId,
    required DateTime transactionDate,
    required TransactionType type,
    required List<Map<String, dynamic>> billItems,
    required String currency,
  });

  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<Map<String, dynamic>> transactions});

  Future<Transaction> updateTransaction({required String id, required List<Map<String, dynamic>> billItems, required String currency});

  Future<void> deleteTransaction(String transactionId);
}

class RestTransactionService implements TransactionService {
  final ApiClient _apiClient;

  RestTransactionService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);
  @override
  Future<TransactionPage> getTransactions({required int page, required int size}) async {
    return await _apiClient.get<TransactionPage>('/transactions?page=$page&size=$size', TransactionPage.fromJson);
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required DateTime transactionDate,
    required TransactionType type,
    required List<Map<String, dynamic>> billItems,
    required String currency,
  }) async {
    final Map<String, dynamic> transactionData = {
      'accountId': accountId,
      'transactionDate': transactionDate.toUtc().toIso8601String(),
      'type': type.name.toUpperCase(),
      'bill': {
        'billItems':
            billItems
                .map(
                  (item) => {
                    'categoryId': item['categoryId'],
                    'amount': {'value': item['amount'], 'currency': currency.toUpperCase()},
                    'description': item['description'],
                  },
                )
                .toList(),
      },
    };

    return await _apiClient.post<Transaction>('/transactions', transactionData, Transaction.fromJson);
  }

  @override
  Future<Transaction> updateTransaction({required String id, required List<Map<String, dynamic>> billItems, required String currency}) async {
    final Map<String, dynamic> transactionData = {
      'bill': {
        'billItems':
            billItems
                .map(
                  (item) => {
                    'categoryId': item['categoryId'],
                    'amount': {'value': item['amount'], 'currency': currency.toUpperCase()},
                    'description': item['description'],
                  },
                )
                .toList(),
      },
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
