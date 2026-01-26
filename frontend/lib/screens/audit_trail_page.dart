import 'package:flutter/material.dart';

import '../models/audit_entity_type.dart';
import '../models/audit_log.dart';
import '../models/audit_operation.dart';
import '../services/audit_service.dart';
import '../utils/error_handler.dart';
import '../widgets/audit_filter_dialog.dart';
import '../widgets/audit_log_list_item.dart';

class AuditTrailPage extends StatefulWidget {
  final AuditService? auditService;

  const AuditTrailPage({super.key, this.auditService});

  @override
  State<AuditTrailPage> createState() => _AuditTrailPageState();
}

class _AuditTrailPageState extends State<AuditTrailPage> {
  late final AuditService _auditService;
  final ScrollController _scrollController = ScrollController();

  List<AuditLog> _auditLogs = [];
  bool _isLoading = true;
  bool _isLoadingMore = false;
  String? _error;

  List<AuditEntityType>? _filterEntityTypes;
  List<AuditOperation>? _filterOperations;
  DateTime? _filterFromDate;
  DateTime? _filterToDate;

  int _currentPage = 0;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _auditService = widget.auditService ?? RestAuditService();
    _scrollController.addListener(_onScroll);
    _fetchAuditLogs();
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
      _loadMore();
    }
  }

  Future<void> _fetchAuditLogs({bool refresh = false}) async {
    if (refresh) {
      setState(() {
        _currentPage = 0;
        _auditLogs = [];
        _hasMore = true;
      });
    }

    setState(() {
      _isLoading = refresh || _currentPage == 0;
      _error = null;
    });

    try {
      final query = AuditLogQuery(entityTypes: _filterEntityTypes, operations: _filterOperations, fromDate: _filterFromDate, toDate: _filterToDate, page: _currentPage, size: 20);

      final response = await _auditService.getAuditLogs(query);

      setState(() {
        if (refresh || _currentPage == 0) {
          _auditLogs = response.auditLogs;
        } else {
          _auditLogs.addAll(response.auditLogs);
        }
        _hasMore = _currentPage < response.totalPages - 1;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = ErrorHandler.getErrorMessage(e);
        _isLoading = false;
        _auditLogs = [];
      });
    }
  }

  Future<void> _loadMore() async {
    if (_isLoadingMore || !_hasMore) return;

    setState(() {
      _isLoadingMore = true;
      _currentPage++;
    });

    try {
      final query = AuditLogQuery(entityTypes: _filterEntityTypes, operations: _filterOperations, fromDate: _filterFromDate, toDate: _filterToDate, page: _currentPage, size: 20);

      final response = await _auditService.getAuditLogs(query);

      setState(() {
        _auditLogs.addAll(response.auditLogs);
        _hasMore = _currentPage < response.totalPages - 1;
        _isLoadingMore = false;
      });
    } catch (e) {
      setState(() {
        _currentPage--;
        _isLoadingMore = false;
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Błąd ładowania: ${ErrorHandler.getErrorMessage(e)}')));
      }
    }
  }

  void _showFilterDialog() {
    showDialog<Map<String, dynamic>>(
      context: context,
      builder:
          (context) =>
              AuditFilterDialog(initialEntityTypes: _filterEntityTypes, initialOperations: _filterOperations, initialFromDate: _filterFromDate, initialToDate: _filterToDate),
    ).then((filters) {
      if (filters != null) {
        setState(() {
          _filterEntityTypes = filters['entityTypes'] as List<AuditEntityType>?;
          _filterOperations = filters['operations'] as List<AuditOperation>?;
          _filterFromDate = filters['fromDate'] as DateTime?;
          _filterToDate = filters['toDate'] as DateTime?;
        });
        _fetchAuditLogs(refresh: true);
      }
    });
  }

  bool get _hasActiveFilters => _filterEntityTypes != null || _filterOperations != null || _filterFromDate != null || _filterToDate != null;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Expanded(child: Text('Historia zmian', style: Theme.of(context).textTheme.headlineSmall)),
                Badge(isLabelVisible: _hasActiveFilters, child: IconButton(icon: const Icon(Icons.filter_list), onPressed: _showFilterDialog, tooltip: 'Filtruj')),
              ],
            ),
          ),
          if (_error != null)
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Text(_error!, style: const TextStyle(color: Colors.red), textAlign: TextAlign.center),
                  const SizedBox(height: 8),
                  ElevatedButton.icon(icon: const Icon(Icons.refresh), label: const Text('Spróbuj ponownie'), onPressed: () => _fetchAuditLogs(refresh: true)),
                ],
              ),
            ),
          if (_isLoading)
            const Expanded(child: Center(child: CircularProgressIndicator()))
          else if (_auditLogs.isEmpty && !_isLoading)
            Expanded(
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.history, size: 64, color: Colors.grey[400]),
                    const SizedBox(height: 16),
                    Text(
                      _hasActiveFilters ? 'Brak wyników dla wybranych filtrów' : 'Brak zmian do wyświetlenia',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(color: Colors.grey[600]),
                    ),
                    if (_hasActiveFilters) ...[
                      const SizedBox(height: 8),
                      TextButton.icon(
                        icon: const Icon(Icons.clear),
                        label: const Text('Wyczyść filtry'),
                        onPressed: () {
                          setState(() {
                            _filterEntityTypes = null;
                            _filterOperations = null;
                            _filterFromDate = null;
                            _filterToDate = null;
                          });
                          _fetchAuditLogs(refresh: true);
                        },
                      ),
                    ],
                  ],
                ),
              ),
            )
          else
            Expanded(
              child: RefreshIndicator(
                onRefresh: () => _fetchAuditLogs(refresh: true),
                child: ListView.builder(
                  controller: _scrollController,
                  itemCount: _auditLogs.length + (_isLoadingMore ? 1 : 0),
                  itemBuilder: (context, index) {
                    if (index == _auditLogs.length) {
                      return const Padding(padding: EdgeInsets.all(16.0), child: Center(child: CircularProgressIndicator()));
                    }

                    return AuditLogListItem(auditLog: _auditLogs[index]);
                  },
                ),
              ),
            ),
        ],
      ),
    );
  }
}
