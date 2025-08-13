import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/http_exception.dart';
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
    required String currency,
  });

  Future<void> deleteTransaction(String transactionId);
}

class TransactionService implements TransactionServiceInterface {
  final http.Client _httpClient;

  TransactionService({http.Client? httpClient})
      : _httpClient = httpClient ?? http.Client();
  @override
  Future<List<Transaction>> getTransactions() async {
    try {
      final response = await _httpClient.get(
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
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  @override
  Future<List<Transaction>> getTransactionsByAccountId(String accountId) async {
    try {
      final response = await _httpClient.get(
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
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
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
      'amount': {
        'value': amount,
        'currency': currency.toUpperCase(),
      },
      'description': description,
      'date': date.toUtc().toIso8601String(),
      'type': type.toUpperCase(),
      'category': category,
    };

    try {
      final response = await _httpClient.post(
        Uri.parse('${ApiConfig.backendUrl}/transactions'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(transactionData),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return Transaction.fromJson(jsonDecode(response.body));
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
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

    try {
      final response = await _httpClient.put(
        Uri.parse('${ApiConfig.backendUrl}/transactions/$id'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(transactionData),
      );

      if (response.statusCode == 200) {
        return Transaction.fromJson(jsonDecode(response.body));
      } else {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }

  @override
  Future<void> deleteTransaction(String transactionId) async {
    try {
      final response = await _httpClient.delete(
        Uri.parse('${ApiConfig.backendUrl}/transactions/$transactionId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw HttpException(response.statusCode, response.body);
      }
    } on SocketException {
      throw const HttpException(0, 'Connection refused');
    }
  }
}
