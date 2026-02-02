import 'package:alchemist/alchemist.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/widgets/transaction_staging_list.dart';

import '../mocks/in_memory_category_service.dart';

void main() {
  group('TransactionStagingScreen Golden Tests', () {
    late InMemoryCategoryService categoryService;

    setUp(() async {
      categoryService = InMemoryCategoryService();
      categoryService.addCategory('Wynagrodzenie', type: CategoryType.income, color: '#4CAF50');
      categoryService.addCategory('Zakupy', type: CategoryType.expense, color: '#FF5722');
      categoryService.addCategory('Mieszkanie', type: CategoryType.expense, color: '#2196F3');
      categoryService.addCategory('Transport', type: CategoryType.expense, color: '#FFC107');
    });

    goldenTest(
      'renders screen with loaded transactions',
      fileName: 'transaction_staging_screen_with_transactions',
      builder: () {
        final stagingService = TransactionStagingService();
        stagingService.loadFromCsv(
          CsvParseResult(
            totalRows: 4,
            successCount: 4,
            errorCount: 0,
            proposals: [
              TransactionProposal(
                transactionDate: DateTime(2024, 1, 15),
                description: 'Wynagrodzenie - styczeń',
                amount: 5000.00,
                currency: 'PLN',
                type: TransactionType.income,
                categoryId: null,
              ),
              TransactionProposal(
                transactionDate: DateTime(2024, 1, 16),
                description: 'Zakupy spożywcze Biedronka',
                amount: -120.50,
                currency: 'PLN',
                type: TransactionType.expense,
                categoryId: null,
              ),
              TransactionProposal(
                transactionDate: DateTime(2024, 1, 17),
                description: 'Przelew za czynsz',
                amount: -1500.00,
                currency: 'PLN',
                type: TransactionType.expense,
                categoryId: null,
              ),
              TransactionProposal(
                transactionDate: DateTime(2024, 1, 18),
                description: 'Stacja benzynowa ORLEN',
                amount: -250.75,
                currency: 'PLN',
                type: TransactionType.expense,
                categoryId: null,
              ),
            ],
            errors: [],
          ),
        );

        return GoldenTestGroup(
          children: [
            GoldenTestScenario(
              name: 'with four transactions',
              child: SizedBox(
                width: 800,
                height: 600,
                child: Material(child: TransactionStagingList(stagingService: stagingService, accountId: 'test-account-id', categoryService: categoryService)),
              ),
            ),
          ],
        );
      },
    );
  });
}
