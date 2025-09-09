import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/services/account_service.dart';

import '../mocks/mock_account_service.dart';

void main() {
  group('AccountService', () {
    late AccountServiceInterface accountService;

    setUp(() {
      accountService = MockAccountServiceInterface();
    });

    test('should initialize account service', () {
      expect(accountService, isNotNull);
    });
  });
}
