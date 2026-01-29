import 'package:alchemist/alchemist.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/models/transaction/transaction.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/models/transaction/bill_item.dart';
import 'package:frontend/models/transaction/bill_item_category.dart';
import 'package:frontend/widgets/transaction_list.dart';

void main() {
  group('TransactionList Golden Tests', () {
    goldenTest(
      'renders empty state correctly',
      fileName: 'transaction_list_empty',
      tags: ['golden'],
      builder:
          () => GoldenTestGroup(
            scenarioConstraints: const BoxConstraints(maxWidth: 400, maxHeight: 600),
            children: [
              GoldenTestScenario(
                name: 'empty_state',
                child: SizedBox(
                  width: 400,
                  height: 600,
                  child: MaterialApp(home: Scaffold(body: TransactionList(transactions: [], accounts: [], onEdit: (_) {}, onDelete: (_) {}))),
                ),
              ),
            ],
          ),
    );

    goldenTest(
      'renders transaction list with data',
      fileName: 'transaction_list_with_data',
      tags: ['golden'],
      builder:
          () => GoldenTestGroup(
            scenarioConstraints: const BoxConstraints(maxWidth: 400, maxHeight: 600),
            children: [
              GoldenTestScenario(
                name: 'with_transactions',
                child: SizedBox(
                  width: 400,
                  height: 600,
                  child: MaterialApp(
                    home: Scaffold(
                      body: TransactionList(
                        transactions: [
                          Transaction(
                            id: 't1',
                            accountId: '1',
                            amount: 100.0,
                            type: TransactionType.income,
                            createdAt: DateTime(2024, 1, 15),
                            updatedAt: DateTime(2024, 1, 15),
                            transactionDate: DateTime(2024, 1, 15),
                            billItems: [BillItem(category: BillItemCategory(id: 'salary-id', name: 'Salary'), amount: 100.0, description: 'Salary Payment')],
                          ),
                          Transaction(
                            id: 't2',
                            accountId: '2',
                            amount: -50.0,
                            type: TransactionType.expense,
                            createdAt: DateTime(2024, 1, 16),
                            updatedAt: DateTime(2024, 1, 16),
                            transactionDate: DateTime(2024, 1, 16),
                            billItems: [BillItem(category: BillItemCategory(id: 'food-id', name: 'Food'), amount: -50.0, description: 'Grocery Shopping')],
                          ),
                          Transaction(
                            id: 't3',
                            accountId: '1',
                            amount: 75.0,
                            type: TransactionType.income,
                            createdAt: DateTime(2024, 1, 17),
                            updatedAt: DateTime(2024, 1, 17),
                            transactionDate: DateTime(2024, 1, 17),
                            billItems: [BillItem(category: BillItemCategory(id: 'freelance-id', name: 'Freelance'), amount: 75.0, description: 'Freelance Project')],
                          ),
                        ],
                        accounts: [Account(id: '1', name: 'PLN Account', balance: 1000.0, currency: 'PLN'), Account(id: '2', name: 'USD Account', balance: 500.0, currency: 'USD')],
                        onEdit: (_) {},
                        onDelete: (_) {},
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
    );
  });
}
