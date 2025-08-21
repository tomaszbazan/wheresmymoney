import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

void main() {
  group('TransactionService', () {
    late TransactionService transactionService;

    setUpAll(() async {
      await Supabase.initialize(
        url: 'https://test-project.supabase.co',
        anonKey: 'test-anon-key',
      );
    });

    setUp(() {
      transactionService = TransactionService();
    });

    test('should initialize transaction service', () {
      expect(transactionService, isNotNull);
    });
  });
}