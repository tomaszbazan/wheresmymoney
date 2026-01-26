import 'package:frontend/models/transfer.dart';
import 'package:frontend/services/transfer_service.dart';
import 'package:uuid/uuid.dart';

class InMemoryTransferService implements TransferService {
  final Map<String, Transfer> _transfers = {};
  final Map<String, String> _accountCurrencies = {};
  Exception? _apiError;

  void setAccountCurrency(String accountId, String currency) {
    _accountCurrencies[accountId] = currency;
  }

  @override
  Future<Transfer> createTransfer({
    required String sourceAccountId,
    required String targetAccountId,
    required double sourceAmount,
    double? targetAmount,
    String? description,
  }) async {
    if (_apiError != null) {
      throw _apiError!;
    }

    final sourceCurrency = _accountCurrencies[sourceAccountId] ?? 'PLN';
    final targetCurrency = _accountCurrencies[targetAccountId] ?? 'PLN';
    final effectiveTargetAmount = targetAmount ?? sourceAmount;
    final exchangeRate = effectiveTargetAmount / sourceAmount;

    final id = const Uuid().v4();
    final transfer = Transfer(
      id: id,
      sourceAccountId: sourceAccountId,
      targetAccountId: targetAccountId,
      sourceAmount: sourceAmount,
      sourceCurrency: sourceCurrency,
      targetAmount: effectiveTargetAmount,
      targetCurrency: targetCurrency,
      exchangeRate: exchangeRate,
      description: description,
      createdAt: DateTime.now(),
    );

    _transfers[id] = transfer;
    return transfer;
  }

  @override
  Future<List<Transfer>> getTransfers() async {
    if (_apiError != null) {
      throw _apiError!;
    }

    return _transfers.values.toList();
  }

  void clear() {
    _transfers.clear();
    _accountCurrencies.clear();
    _apiError = null;
  }

  void setApiError(Exception error) {
    _apiError = error;
  }
}
