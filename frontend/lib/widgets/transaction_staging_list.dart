import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:frontend/models/category.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/category_service.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/widgets/ai_category_indicator.dart';
import 'package:frontend/widgets/searchable_category_dropdown.dart';
import 'package:intl/intl.dart';
import 'package:frontend/utils/date_formatter.dart';

class TransactionStagingList extends StatefulWidget {
  final TransactionStagingService stagingService;
  final String accountId;
  final CategoryService? categoryService;

  const TransactionStagingList({super.key, required this.stagingService, required this.accountId, this.categoryService});

  @override
  State<TransactionStagingList> createState() => _TransactionStagingListState();
}

class _TransactionStagingListState extends State<TransactionStagingList> {
  final NumberFormat _numberFormat = NumberFormat('#,##0.00', 'pl_PL');
  late final CategoryService _categoryService;
  List<Category> _expenseCategories = [];
  List<Category> _incomeCategories = [];
  bool _isLoadingCategories = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _categoryService = widget.categoryService ?? RestCategoryService();
    widget.stagingService.addListener(_onStagingChanged);
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    try {
      final proposals = widget.stagingService.proposals;
      final hasExpenseTransactions = proposals.any((p) => p.type == TransactionType.expense);
      final hasIncomeTransactions = proposals.any((p) => p.type == TransactionType.income);

      final futures = <Future<List<Category>>>[];
      if (hasExpenseTransactions) {
        futures.add(_categoryService.getCategoriesByType(CategoryType.expense));
      }
      if (hasIncomeTransactions) {
        futures.add(_categoryService.getCategoriesByType(CategoryType.income));
      }

      final results = await Future.wait(futures);

      setState(() {
        var resultIndex = 0;
        if (hasExpenseTransactions) {
          _expenseCategories = results[resultIndex++];
        }
        if (hasIncomeTransactions) {
          _incomeCategories = results[resultIndex++];
        }
        _isLoadingCategories = false;
      });
    } catch (e, s) {
      developer.log('Failed to load categories', name: 'transaction_staging_list', level: 1000, error: e, stackTrace: s);
      setState(() {
        _errorMessage = 'Nie udało się załadować kategorii';
        _isLoadingCategories = false;
      });
    }
  }

  @override
  void dispose() {
    widget.stagingService.removeListener(_onStagingChanged);
    super.dispose();
  }

  void _onStagingChanged() {
    setState(() {});
  }

  CategoryType _getCategoryType(TransactionType type) {
    return type == TransactionType.income ? CategoryType.income : CategoryType.expense;
  }

  List<Category> _getCategoriesForType(TransactionType type) {
    return type == TransactionType.income ? _incomeCategories : _expenseCategories;
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoadingCategories) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorMessage != null) {
      return Center(child: Text(_errorMessage!));
    }

    final proposals = widget.stagingService.proposals;

    if (proposals.isEmpty) {
      return const Center(child: Text('Brak transakcji do wyświetlenia'));
    }

    return Column(
      children: [
        _buildSummary(proposals),
        const SizedBox(height: 16),
        Expanded(
          child: ListView.builder(
            itemCount: proposals.length,
            itemBuilder: (context, index) {
              return _buildTransactionCard(proposals[index], index);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildSummary(List<TransactionProposal> proposals) {
    final aiSuggestedCount = proposals.where((p) => p.isSuggestedByAi).length;
    final missingCategoryCount = proposals.where((p) => p.categoryId == null).length;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            Text('Transakcji: ${proposals.length}', style: Theme.of(context).textTheme.titleMedium),
            if (aiSuggestedCount > 0)
              Row(children: [const Icon(Icons.auto_awesome, size: 16), const SizedBox(width: 4), Text('AI: $aiSuggestedCount', style: Theme.of(context).textTheme.titleMedium)]),
            if (missingCategoryCount > 0)
              Row(
                children: [
                  Icon(Icons.warning, size: 16, color: Theme.of(context).colorScheme.error),
                  const SizedBox(width: 4),
                  Text('Brak kategorii: $missingCategoryCount', style: Theme.of(context).textTheme.titleMedium?.copyWith(color: Theme.of(context).colorScheme.error)),
                ],
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionCard(TransactionProposal proposal, int index) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      color: proposal.isSuggestedByAi ? Theme.of(context).colorScheme.secondaryContainer.withValues(alpha: 0.3) : null,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(DateFormatter.format(proposal.transactionDate), style: Theme.of(context).textTheme.bodySmall),
                  const SizedBox(height: 4),
                  Text(proposal.description, style: Theme.of(context).textTheme.bodyLarge),
                  const SizedBox(height: 4),
                  Text('${_numberFormat.format(proposal.amount)} ${proposal.currency}', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      SizedBox(
                        width: 300,
                        child: SearchableCategoryDropdown(
                          transactionType: _getCategoryType(proposal.type),
                          selectedCategoryId: proposal.categoryId,
                          onChanged: (categoryId) {
                            if (categoryId != null) {
                              widget.stagingService.updateCategory(index, categoryId);
                            }
                          },
                          categoryService: _categoryService,
                          preloadedCategories: _getCategoriesForType(proposal.type),
                        ),
                      ),
                      const SizedBox(width: 8),
                      AiCategoryIndicator(isSuggestedByAi: proposal.isSuggestedByAi),
                    ],
                  ),
                ],
              ),
            ),
            IconButton(
              icon: const Icon(Icons.delete),
              onPressed: () {
                widget.stagingService.removeTransaction(index);
              },
            ),
          ],
        ),
      ),
    );
  }
}
