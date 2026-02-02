import 'package:frontend/models/bulk_create_response.dart';
import 'package:frontend/models/money.dart';
import 'package:frontend/models/transaction/bill_item.dart';
import 'package:frontend/models/transaction/bill_item_category.dart';
import 'package:frontend/models/transaction/bill_item_request.dart';
import 'package:frontend/models/transaction/create_transaction_request.dart';
import 'package:frontend/models/transaction/transaction.dart';
import 'package:frontend/models/transaction_page.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryTransactionService implements TransactionService {
  final Map<String, Transaction> _transactions = {};
  Exception? _apiError;

  @override
  Future<TransactionPage> getTransactions({required int page, required int size, required List<TransactionType> types}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    var allTransactions = _transactions.values.toList();

    if (types.isNotEmpty) {
      allTransactions = allTransactions.where((t) => types.contains(t.type)).toList();
    }

    final totalElements = allTransactions.length;
    final totalPages = (totalElements / size).ceil();
    final start = page * size;
    final end = (start + size < totalElements) ? start + size : totalElements;
    final pageTransactions = (start >= totalElements) ? <Transaction>[] : allTransactions.sublist(start, end);

    return TransactionPage(transactions: pageTransactions, page: page, size: size, totalElements: totalElements, totalPages: totalPages);
  }

  @override
  Future<Transaction> createTransaction({
    required String accountId,
    required DateTime transactionDate,
    required TransactionType type,
    required List<BillItemRequest> billItems,
  }) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final id = const Uuid().v4();
    double totalAmount = 0;

    final items =
        billItems.map((item) {
          totalAmount += item.amount;
          return BillItem(
            category: BillItemCategory(id: item.categoryId ?? '', name: 'Category'),
            amount: Money(value: item.amount, currency: 'PLN'),
            description: item.description,
          );
        }).toList();

    final transaction = Transaction(
      id: id,
      accountId: accountId,
      amount: Money(value: totalAmount, currency: 'PLN'),
      createdAt: transactionDate,
      updatedAt: transactionDate,
      transactionDate: transactionDate,
      type: type,
      billItems: items,
    );
    _transactions[id] = transaction;
    return transaction;
  }

  @override
  Future<Transaction> updateTransaction({required String id, required List<BillItemRequest> billItems, String? accountId, DateTime? transactionDate}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final existingTransaction = _transactions[id];
    if (existingTransaction == null) {
      throw Exception('Transaction not found');
    }

    double totalAmount = 0;
    final items =
        billItems.map((item) {
          totalAmount += item.amount;
          return BillItem(
            category: BillItemCategory(id: item.categoryId ?? '', name: 'Category'),
            amount: Money(value: item.amount, currency: 'PLN'),
            description: item.description,
          );
        }).toList();

    final updatedTransaction = Transaction(
      id: id,
      accountId: accountId ?? existingTransaction.accountId,
      amount: Money(value: totalAmount, currency: 'PLN'),
      createdAt: existingTransaction.createdAt,
      updatedAt: DateTime.now(),
      transactionDate: transactionDate ?? existingTransaction.transactionDate,
      type: existingTransaction.type,
      billItems: items,
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
  Future<BulkCreateResponse> bulkCreateTransactions({required String accountId, required List<CreateTransactionRequest> transactions}) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final savedIds = <String>[];
    var duplicateCount = 0;

    for (final txData in transactions) {
      final id = const Uuid().v4();
      double totalAmount = 0;
      final items =
          txData.billItems.map((item) {
            totalAmount += item.amount;
            return BillItem(
              category: BillItemCategory(id: item.categoryId ?? '', name: 'Category'),
              amount: Money(value: item.amount, currency: 'PLN'), // Mock currency
              description: item.description,
            );
          }).toList();

      final transaction = Transaction(
        id: id,
        accountId: accountId,
        amount: Money(value: totalAmount, currency: 'PLN'),
        createdAt: txData.transactionDate,
        updatedAt: txData.transactionDate,
        transactionDate: txData.transactionDate,
        type: txData.type,
        billItems: items,
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
