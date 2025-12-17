import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';

void main() {
  group('Account Model', () {
    const sampleAccountJson = {
      'id': '5d98513c-d224-43d9-a521-960af3ce9b46',
      'name': 'Portfel',
      'balance': 0,
      'currency': 'USD',
      'createdAt': '2025-12-16T13:35:28.888182Z',
      'updatedAt': '2025-12-16T13:35:28.888202Z',
    };

    const sampleAccountWithNegativeBalance = {
      'id': '4709ddae-1d06-4c99-9e6c-d1da33162e29',
      'name': 'Portfel',
      'balance': -11.11,
      'currency': 'PLN',
      'createdAt': '2025-12-16T12:29:00.051545Z',
      'updatedAt': '2025-12-16T20:11:35.033859Z',
    };

    test('should create Account from JSON with backend response format', () {
      final account = Account.fromJson(sampleAccountJson);

      expect(account.id, equals('5d98513c-d224-43d9-a521-960af3ce9b46'));
      expect(account.name, equals('Portfel'));
      expect(account.balance, equals(0));
      expect(account.currency, equals('USD'));
      expect(account.createdAt, equals(DateTime.parse('2025-12-16T13:35:28.888182Z')));
      expect(account.updatedAt, equals(DateTime.parse('2025-12-16T13:35:28.888202Z')));
    });

    test('should handle negative balance', () {
      final account = Account.fromJson(sampleAccountWithNegativeBalance);

      expect(account.id, equals('4709ddae-1d06-4c99-9e6c-d1da33162e29'));
      expect(account.balance, equals(-11.11));
      expect(account.currency, equals('PLN'));
    });

    test('should handle integer balance as double', () {
      final jsonWithIntBalance = {
        'id': 'test-id',
        'name': 'Test Account',
        'balance': 100,
        'currency': 'USD',
        'createdAt': '2025-12-16T13:35:28.888182Z',
        'updatedAt': '2025-12-16T13:35:28.888202Z',
      };

      final account = Account.fromJson(jsonWithIntBalance);

      expect(account.balance, equals(100.0));
      expect(account.balance, isA<double>());
    });

    test('should convert Account to JSON', () {
      final account = Account.fromJson(sampleAccountJson);
      final json = account.toJson();

      expect(json['id'], equals('5d98513c-d224-43d9-a521-960af3ce9b46'));
      expect(json['name'], equals('Portfel'));
      expect(json['balance'], equals(0));
      expect(json['currency'], equals('USD'));
    });
  });
}
