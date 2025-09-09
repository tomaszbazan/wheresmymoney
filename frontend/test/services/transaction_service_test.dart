import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/services/transaction_service.dart';

import '../mocks/mock_transaction_service.dart';

void main() {
  group('TransactionService', () {
    late TransactionServiceInterface transactionService;

    setUp(() {
      transactionService = MockTransactionServiceInterface();
    });

    test('should initialize transaction service', () {
      expect(transactionService, isNotNull);
    });
  });
}
