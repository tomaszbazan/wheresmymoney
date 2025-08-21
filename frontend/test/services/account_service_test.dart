import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/services/account_service.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

void main() {
  group('AccountService', () {
    late AccountService accountService;

    setUpAll(() async {
      await Supabase.initialize(
        url: 'https://test-project.supabase.co',
        anonKey: 'test-anon-key',
      );
    });

    setUp(() {
      accountService = AccountService();
    });

    test('should initialize account service', () {
      expect(accountService, isNotNull);
    });
  });
}
