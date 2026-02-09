import 'package:flutter/material.dart';
import '../models/transaction_filter.dart';

import '../models/account.dart';
import '../models/transaction/transaction.dart';
import '../services/transaction_service.dart';
import '../utils/error_handler.dart';
import 'transaction_list_item.dart';

class TransactionList extends StatefulWidget {
  final List<Account> accounts;
  final TransactionFilter filter;
  final TransactionService transactionService;
  final void Function(Transaction) onEdit;
  final void Function(Transaction) onDelete;
  final void Function(VoidCallback)? onRefreshRequested;

  const TransactionList({
    super.key,
    required this.accounts,
    required this.filter,
    required this.transactionService,
    required this.onEdit,
    required this.onDelete,
    this.onRefreshRequested,
  });

  @override
  State<TransactionList> createState() => _TransactionListState();
}

class _TransactionListState extends State<TransactionList> {
  final ScrollController _scrollController = ScrollController();
  final List<Transaction> _transactions = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  bool _hasMore = true;
  int _currentPage = 0;
  final int _pageSize = 20;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    widget.onRefreshRequested?.call(_loadFirstPage);
    _loadFirstPage();
  }

  @override
  void didUpdateWidget(TransactionList oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.filter != oldWidget.filter) {
      _loadFirstPage();
    }
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_isLoadingMore || !_hasMore) return;

    final threshold = _scrollController.position.maxScrollExtent - 200;
    if (_scrollController.position.pixels >= threshold) {
      _loadNextPage();
    }
  }

  Future<void> _loadFirstPage() async {
    setState(() {
      _isLoading = true;
      _currentPage = 0;
      _transactions.clear();
    });

    await _loadPage(_currentPage);

    setState(() => _isLoading = false);
  }

  Future<void> _loadNextPage() async {
    if (_isLoadingMore || !_hasMore) return;

    setState(() => _isLoadingMore = true);

    _currentPage++;
    await _loadPage(_currentPage);

    setState(() => _isLoadingMore = false);
  }

  Future<void> _loadPage(int page) async {
    try {
      final transactionPage = await widget.transactionService.getTransactions(
        page: page,
        size: _pageSize,
        types: widget.filter.types,
        dateFrom: widget.filter.dateFrom,
        dateTo: widget.filter.dateTo,
        minAmount: widget.filter.minAmount,
        maxAmount: widget.filter.maxAmount,
        accountIds: widget.filter.accountIds,
        categoryIds: widget.filter.categoryIds,
        sort: widget.filter.sort,
      );

      setState(() {
        _transactions.addAll(transactionPage.transactions);
        _hasMore = transactionPage.hasMore;
      });
    } catch (e) {
      if (mounted) {
        ErrorHandler.showError(context, e);
      }
    }
  }

  String _getAccountName(String accountId) {
    final account = widget.accounts.firstWhere((a) => a.id == accountId, orElse: () => Account(id: accountId, name: 'Nieznane konto', balance: 0.0));
    return account.name;
  }

  String _getAccountCurrency(String accountId) {
    final account = widget.accounts.firstWhere((a) => a.id == accountId, orElse: () => Account(id: accountId, name: 'Nieznane konto', balance: 0.0));
    return account.currency ?? 'PLN';
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_transactions.isEmpty) {
      return _buildEmptyState(context);
    }

    return ListView.builder(
      controller: _scrollController,
      itemCount: _transactions.length + (_hasMore ? 1 : 0),
      itemBuilder: (context, index) {
        if (index == _transactions.length) {
          return _buildLoadingIndicator();
        }

        return _buildTransactionCard(_transactions[index]);
      },
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.receipt_long_outlined, size: 64, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text('Brak transakcji', style: Theme.of(context).textTheme.titleMedium?.copyWith(color: Colors.grey[600])),
          const SizedBox(height: 8),
          Text('Dodaj pierwszą transakcję klikając przycisk +', style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.grey[600]), textAlign: TextAlign.center),
        ],
      ),
    );
  }

  Widget _buildLoadingIndicator() {
    return const Padding(padding: EdgeInsets.all(16.0), child: Center(child: CircularProgressIndicator()));
  }

  Widget _buildTransactionCard(Transaction transaction) {
    return TransactionListItem(
      transaction: transaction,
      accountName: _getAccountName(transaction.accountId),
      accountCurrency: _getAccountCurrency(transaction.accountId),
      onEdit: widget.onEdit,
      onDelete: widget.onDelete,
    );
  }
}
