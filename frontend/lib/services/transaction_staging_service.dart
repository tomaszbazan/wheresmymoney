import 'package:flutter/foundation.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/services/transaction_service.dart';

import '../models/bulk_create_response.dart';

class TransactionStagingService extends ChangeNotifier {
  List<TransactionProposal> _proposals = [];

  List<TransactionProposal> get proposals => List.unmodifiable(_proposals);

  void loadFromCsv(CsvParseResult result) {
    _proposals = List.from(result.proposals);
    notifyListeners();
  }

  void updateCategory(int index, String categoryId) {
    _proposals[index] = _proposals[index].copyWith(categoryId: categoryId);
    notifyListeners();
  }

  void removeTransaction(int index) {
    _proposals.removeAt(index);
    notifyListeners();
  }

  void clear() {
    _proposals.clear();
    notifyListeners();
  }

  bool hasIncompleteCategorization() {
    return _proposals.any((proposal) => proposal.categoryId == null);
  }

  Future<BulkCreateResponse> saveAll(String accountId, TransactionService transactionService) async {
    final transactions =
        _proposals.map((proposal) {
          return {
            'amount': {'value': proposal.amount, 'currency': proposal.currency.toUpperCase()},
            'description': proposal.description,
            'transactionDate': proposal.transactionDate.toIso8601String().split('T').first,
            'type': proposal.type.name.toUpperCase(),
            'categoryId': proposal.categoryId ?? '',
          };
        }).toList();

    final result = await transactionService.bulkCreateTransactions(accountId: accountId, transactions: transactions);

    clear();
    return result;
  }
}
