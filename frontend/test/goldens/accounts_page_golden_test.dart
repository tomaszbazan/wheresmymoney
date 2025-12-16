import 'package:alchemist/alchemist.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/screens/accounts_page.dart';

import '../mocks/in_memory_account_service.dart';

void main() {
  group('AccountsPage Golden Tests', () {
    goldenTest(
      'renders accounts page',
      fileName: 'accounts_page',
      tags: ['golden'],
      builder: () {
        final emptyService = InMemoryAccountService();
        final serviceWithData = InMemoryAccountService();

        serviceWithData.addAccount('Konto Główne', balance: 5432.50, currency: 'PLN', type: 'Rachunek bieżący');
        serviceWithData.addAccount('Oszczędności', balance: 10000.00, currency: 'PLN', type: 'Oszczędnościowe');
        serviceWithData.addAccount('Gotówka', balance: 250.75, currency: 'PLN', type: 'Gotówka');
        serviceWithData.addAccount('Karta Kredytowa', balance: -1500.00, currency: 'PLN', type: 'Kredytowa');
        serviceWithData.addAccount('Euro Savings', balance: 2500.00, currency: 'EUR', type: 'Oszczędnościowe');
        serviceWithData.addAccount('USD Account', balance: -1000.00, currency: 'USD', type: 'Rachunek bieżący');

        return GoldenTestGroup(
          scenarioConstraints: const BoxConstraints(maxWidth: 400, maxHeight: 600),
          children: [
            GoldenTestScenario(name: 'empty_state', child: SizedBox(width: 400, height: 600, child: MaterialApp(home: AccountsPage(accountService: emptyService)))),
            GoldenTestScenario(name: 'with_accounts', child: SizedBox(width: 400, height: 600, child: MaterialApp(home: AccountsPage(accountService: serviceWithData)))),
          ],
        );
      },
    );
  });
}
