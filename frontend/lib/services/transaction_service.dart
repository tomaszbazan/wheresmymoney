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
  Future<TransactionPage> getTransactions({
    required int page,
    required int size,
    List<TransactionType> types = const [],
    DateTime? dateFrom,
    DateTime? dateTo,
    double? minAmount,
    double? maxAmount,
    List<String>? accountIds,
    List<String>? categoryIds,
    String? description,
    String? sort,
  });

  Future<Transaction> createTransaction({required String accountId, required DateTime transactionDate, required TransactionType type, required List<BillItemRequest> billItems});

  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<CreateTransactionRequest> transactions});

  Future<Transaction> updateTransaction({required String id, required List<BillItemRequest> billItems, String? accountId, DateTime? transactionDate});

  Future<void> deleteTransaction(String transactionId);
}

class RestTransactionService implements TransactionService {
  final ApiClient _apiClient;

  RestTransactionService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);
  @override
  Future<TransactionPage> getTransactions({
    required int page,
    required int size,
    List<TransactionType> types = const [],
    DateTime? dateFrom,
    DateTime? dateTo,
    double? minAmount,
    double? maxAmount,
    List<String>? accountIds,
    List<String>? categoryIds,
    String? description,
    String? sort,
  }) async {
    final Map<String, dynamic> queryParams = {'page': page.toString(), 'size': size.toString()};

    if (types.isNotEmpty) {
      queryParams['types'] = types.map((e) => e.name.toUpperCase()).toList();
    }
    if (dateFrom != null) {
      queryParams['dateFrom'] = dateFrom.toIso8601String().split('T').first;
    }
    if (dateTo != null) {
      queryParams['dateTo'] = dateTo.toIso8601String().split('T').first;
    }
    if (minAmount != null) {
      queryParams['minAmount'] = minAmount.toString();
    }
    if (maxAmount != null) {
      queryParams['maxAmount'] = maxAmount.toString();
    }
    if (accountIds != null && accountIds.isNotEmpty) {
      queryParams['accountIds'] = accountIds;
    }
    if (categoryIds != null && categoryIds.isNotEmpty) {
      queryParams['categoryIds'] = categoryIds;
    }
    if (description != null && description.isNotEmpty) {
      queryParams['description'] = description;
    }
    if (sort != null) {
      queryParams['sort'] = sort;
    }

    String queryString = Uri(queryParameters: queryParams).query;
    return await _apiClient.get<TransactionPage>('/transactions?$queryString', TransactionPage.fromJson);
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
