import 'package:frontend/models/transaction/bill_item_request.dart';
import 'package:frontend/models/transaction_type.dart';

class CreateTransactionRequest {
  final String accountId;
  final DateTime transactionDate;
  final TransactionType type;
  final List<BillItemRequest> billItems;

  const CreateTransactionRequest({required this.accountId, required this.transactionDate, required this.type, required this.billItems});

  Map<String, dynamic> toJson() {
    return {
      'accountId': accountId,
      'transactionDate': transactionDate.toUtc().toIso8601String(),
      'type': type.name.toUpperCase(),
      'bill': {'billItems': billItems.map((item) => item.toJson()).toList()},
    };
  }
}
