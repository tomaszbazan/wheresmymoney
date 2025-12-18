import 'package:flutter/foundation.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/services/transaction_service.dart';

class TransactionStagingService extends ChangeNotifier {
  List<TransactionProposal> _proposals = [];

  List<TransactionProposal> get proposals => List.unmodifiable(_proposals);

  void loadFromCsv(CsvParseResult result) {
    _proposals = List.from(result.proposals);
    notifyListeners();
  }

  void updateCategory(int index, String categoryId) {
    final proposal = _proposals[index];
    _proposals[index] = TransactionProposal(
      transactionDate: proposal.transactionDate,
      description: proposal.description,
      amount: proposal.amount,
      currency: proposal.currency,
      type: proposal.type,
      categoryId: categoryId,
    );
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

  Future<void> saveAll(String accountId, TransactionServiceInterface transactionService) async {
    for (final proposal in _proposals) {
      await transactionService.createTransaction(
        accountId: accountId,
        amount: proposal.amount,
        description: proposal.description,
        date: proposal.transactionDate,
        type: proposal.type,
        categoryId: proposal.categoryId ?? '',
        currency: proposal.currency,
      );
    }
    clear();
  }
}
