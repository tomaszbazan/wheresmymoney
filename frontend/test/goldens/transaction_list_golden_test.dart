import 'package:alchemist/alchemist.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction/bill_item_request.dart';
import 'package:frontend/models/transaction_filter.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/screens/transaction_page.dart';
import 'package:frontend/widgets/transaction_filter_dialog.dart';

import '../mocks/in_memory_account_service.dart';
import '../mocks/in_memory_category_service.dart';
import '../mocks/in_memory_csv_import_service.dart';
import '../mocks/in_memory_transaction_service.dart';

void main() {
  group('TransactionList Golden Tests', () {
    goldenTest(
      'renders transaction list',
      fileName: 'transaction_list',
      tags: ['golden'],
      builder: () {
        final emptyService = InMemoryTransactionService();
        final serviceWithData = InMemoryTransactionService();

        final accountService = InMemoryAccountService();
        var account1 = accountService.addAccount('Konto Główne', balance: 5432.50, currency: 'PLN', type: 'Rachunek bieżący');
        var account2 = accountService.addAccount('Oszczędności', balance: 10000.00, currency: 'EUR', type: 'Oszczędnościowe');

        final categoryService = InMemoryCategoryService();
        var categoryIncome1 = categoryService.addCategory('Wynagrodzenie', type: CategoryType.income);
        var categoryIncome2 = categoryService.addCategory('Odsetki', type: CategoryType.income);
        var categoryExpense = categoryService.addCategory('Zakupy', type: CategoryType.expense);
        categoryService.addCategory('Zakupy', type: CategoryType.expense);
        final csvImportService = InMemoryCsvImportService();

        serviceWithData.createTransaction(
          accountId: account1.id,
          transactionDate: DateTime(2024, 1, 15),
          type: TransactionType.income,
          billItems: [BillItemRequest(amount: 1500.0, categoryId: categoryIncome1.id, description: 'Wypłata')],
        );

        serviceWithData.createTransaction(
          accountId: account1.id,
          transactionDate: DateTime(2024, 1, 16),
          type: TransactionType.expense,
          billItems: [BillItemRequest(amount: 250.0, categoryId: categoryExpense.id, description: 'Zakupy spożywcze')],
        );

        serviceWithData.createTransaction(
          accountId: account2.id,
          transactionDate: DateTime(2024, 1, 17),
          type: TransactionType.income,
          billItems: [BillItemRequest(amount: 50.0, categoryId: categoryIncome2.id, description: 'Odsetki')],
        );

        return GoldenTestGroup(
          scenarioConstraints: const BoxConstraints(maxWidth: 400, maxHeight: 600),
          children: [
            GoldenTestScenario(
              name: 'empty_state',
              child: SizedBox(
                width: 400,
                height: 600,
                child: TransactionsPage(
                  type: TransactionType.income,
                  transactionService: emptyService,
                  accountService: accountService,
                  categoryService: categoryService,
                  csvImportService: csvImportService,
                ),
              ),
            ),
            GoldenTestScenario(
              name: 'income_transactions',
              child: SizedBox(
                width: 400,
                height: 600,
                child: TransactionsPage(
                  type: TransactionType.income,
                  transactionService: serviceWithData,
                  accountService: accountService,
                  categoryService: categoryService,
                  csvImportService: csvImportService,
                ),
              ),
            ),
            GoldenTestScenario(
              name: 'expense_transactions',
              child: SizedBox(
                width: 400,
                height: 600,
                child: TransactionsPage(
                  type: TransactionType.expense,
                  transactionService: serviceWithData,
                  accountService: accountService,
                  categoryService: categoryService,
                  csvImportService: csvImportService,
                ),
              ),
            ),

            GoldenTestScenario(
              name: 'filter_dialog_open',
              child: SizedBox(
                width: 400,
                height: 600,
                child: Stack(
                  children: [
                    TransactionsPage(
                      type: TransactionType.income,
                      transactionService: serviceWithData,
                      accountService: accountService,
                      categoryService: categoryService,
                      csvImportService: csvImportService,
                    ),
                    Container(color: Colors.black54),
                    Center(
                      child: TransactionFilterDialog(
                        initialFilter: const TransactionFilter(types: [TransactionType.income]),
                        accounts: [account1, account2],
                        categories: [categoryIncome1, categoryIncome2, categoryExpense],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    );
  });
}
