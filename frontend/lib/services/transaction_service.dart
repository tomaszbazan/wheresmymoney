import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/transaction.dart';

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
  });

  Future<void> deleteTransaction(String transactionId);
}

class TransactionService implements TransactionServiceInterface {
  @override
  Future<List<Transaction>> getTransactions() async {
    final response = await http.get(
      Uri.parse('${ApiConfig.backendUrl}/transactions'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final Map<String, dynamic> responseData = jsonDecode(response.body);

      if (responseData.containsKey('transactions')) {
        final List<dynamic> transactionsJson = responseData['transactions'];
        return transactionsJson
            .map((json) => Transaction.fromJson(json))
            .toList();
      } else {
        return [];
      }
    } else {
      throw Exception('Failed to load transactions');
    }
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    final response = await http.get(
      Uri.parse('${ApiConfig.backendUrl}/accounts/$accountId/transactions'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final Map<String, dynamic> responseData = jsonDecode(response.body);

      if (responseData.containsKey('transactions')) {
        final List<dynamic> transactionsJson = responseData['transactions'];
        return transactionsJson
            .map((json) => Transaction.fromJson(json))
            .toList();
      } else {
        return [];
      }
    } else {
      throw Exception('Failed to load transactions for account');
    }
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
      'amount': amount,
      'description': description,
      'date': date.toUtc().toIso8601String(),
      'type': type.toUpperCase(),
      'category': category,
      'currency': currency.toUpperCase(),
    };

    final response = await http.post(
      Uri.parse('${ApiConfig.backendUrl}/transactions'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(transactionData),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return Transaction.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to create transaction: ${response.statusCode}');
    }
  }

  @override
  Future<Transaction> updateTransaction({
    required String id,
    required double amount,
    required String description,
    required String category,
  }) async {
    final Map<String, dynamic> transactionData = {
      'amount': amount,
      'description': description,
      'category': category,
    };

    final response = await http.put(
      Uri.parse('${ApiConfig.backendUrl}/transactions/$id'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(transactionData),
    );

    if (response.statusCode == 200) {
      return Transaction.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to update transaction: ${response.statusCode}');
    }
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    final response = await http.delete(
      Uri.parse('${ApiConfig.backendUrl}/transactions/$transactionId'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Failed to delete transaction: ${response.statusCode}');
    }
  }
}
