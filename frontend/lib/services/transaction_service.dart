import 'package:frontend/models/transaction_type.dart';
import 'package:http/http.dart' as http;

import '../models/bulk_create_response.dart';
import '../models/transaction/bill_item_request.dart';
import '../models/transaction/create_transaction_request.dart';
import '../models/transaction/transaction.dart';
import '../models/transaction_page.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class TransactionService {
  Future<TransactionPage> getTransactions({required int page, required int size, required List<TransactionType> types});

  Future<Transaction> createTransaction({required String accountId, required DateTime transactionDate, required TransactionType type, required List<BillItemRequest> billItems});

  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<CreateTransactionRequest> transactions});

  Future<Transaction> updateTransaction({required String id, required List<BillItemRequest> billItems, String? accountId, DateTime? transactionDate});

  Future<void> deleteTransaction(String transactionId);
}

class RestTransactionService implements TransactionService {
  final ApiClient _apiClient;

  RestTransactionService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);
  @override
  Future<TransactionPage> getTransactions({required int page, required int size, required List<TransactionType> types}) async {
    String query = '/transactions?page=$page&size=$size';
    if (types.isNotEmpty) {
      for (final type in types) {
        query += '&types=${type.name.toUpperCase()}';
      }
    }
    return await _apiClient.get<TransactionPage>(query, TransactionPage.fromJson);
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required DateTime transactionDate,
    required TransactionType type,
    required List<BillItemRequest> billItems,
  }) async {
    final Map<String, dynamic> transactionData = {
      'accountId': accountId,
      'transactionDate': transactionDate.toUtc().toIso8601String(),
      'type': type.name.toUpperCase(),
      'bill': {'billItems': billItems.map((item) => item.toJson()).toList()},
    };

    return await _apiClient.post<Transaction>('/transactions', transactionData, Transaction.fromJson);
  }

  @override
  Future<Transaction> updateTransaction({required String id, required List<BillItemRequest> billItems, String? accountId, DateTime? transactionDate}) async {
    final Map<String, dynamic> transactionData = {
      'bill': {'billItems': billItems.map((item) => item.toJson()).toList()},
      if (accountId != null) 'accountId': accountId,
      if (transactionDate != null) 'transactionDate': transactionDate.toUtc().toIso8601String(),
    };

    return await _apiClient.put<Transaction>('/transactions/$id', transactionData, Transaction.fromJson);
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    await _apiClient.delete('/transactions/$transactionId');
  }

  @override
  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<CreateTransactionRequest> transactions}) async {
    final Map<String, dynamic> bulkData = {'accountId': accountId, 'transactions': transactions.map((t) => t.toJson()).toList()};

    return await _apiClient.post<BulkCreateResponse>('/transactions/bulk', bulkData, BulkCreateResponse.fromJson);
  }
}
