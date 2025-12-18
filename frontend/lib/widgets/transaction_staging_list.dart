import 'package:flutter/material.dart';
import 'package:frontend/models/category_type.dart';
import 'package:frontend/models/transaction_proposal.dart';
import 'package:frontend/models/transaction_type.dart';
import 'package:frontend/services/category_service.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/widgets/searchable_category_dropdown.dart';
import 'package:intl/intl.dart';

class TransactionStagingList extends StatefulWidget {
  final TransactionStagingService stagingService;
  final String accountId;
  final CategoryService? categoryService;

  const TransactionStagingList({super.key, required this.stagingService, required this.accountId, this.categoryService});

  @override
  State<TransactionStagingList> createState() => _TransactionStagingListState();
}

class _TransactionStagingListState extends State<TransactionStagingList> {
  final DateFormat _dateFormat = DateFormat('yyyy-MM-dd');
  final NumberFormat _numberFormat = NumberFormat('#,##0.00', 'pl_PL');

  @override
  void initState() {
    super.initState();
    widget.stagingService.addListener(_onStagingChanged);
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

  @override
  Widget build(BuildContext context) {
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
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [Text('Transakcji: ${proposals.length}', style: Theme.of(context).textTheme.titleMedium)]),
      ),
    );
  }

  Widget _buildTransactionCard(TransactionProposal proposal, int index) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(_dateFormat.format(proposal.transactionDate), style: Theme.of(context).textTheme.bodySmall),
                  const SizedBox(height: 4),
                  Text(proposal.description, style: Theme.of(context).textTheme.bodyLarge),
                  const SizedBox(height: 4),
                  Text('${_numberFormat.format(proposal.amount)} ${proposal.currency}', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
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
                      categoryService: widget.categoryService,
                    ),
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
