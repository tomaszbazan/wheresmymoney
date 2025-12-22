import 'package:flutter/material.dart';
import 'package:frontend/services/category_service.dart';
import 'package:frontend/services/transaction_service.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/widgets/transaction_staging_list.dart';

class TransactionStagingScreen extends StatefulWidget {
  final TransactionStagingService stagingService;
  final String accountId;
  final TransactionService? transactionService;
  final CategoryService? categoryService;

  const TransactionStagingScreen({super.key, required this.stagingService, required this.accountId, this.transactionService, this.categoryService});

  @override
  State<TransactionStagingScreen> createState() => _TransactionStagingScreenState();
}

class _TransactionStagingScreenState extends State<TransactionStagingScreen> {
  bool _isSaving = false;
  late final TransactionService _transactionService;

  @override
  void initState() {
    super.initState();
    _transactionService = widget.transactionService ?? TransactionService();
  }

  Future<void> _saveAll() async {
    if (widget.stagingService.hasIncompleteCategorization()) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Wszystkie transakcje muszą mieć przypisaną kategorię'), backgroundColor: Colors.red));
      return;
    }

    final aiSuggestedCount = widget.stagingService.proposals.where((p) => p.isSuggestedByAi).length;
    final totalCount = widget.stagingService.proposals.length;

    final confirmed = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Potwierdź zapis'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Zapisać $totalCount transakcji?'),
                if (aiSuggestedCount > 0) ...[
                  const SizedBox(height: 8),
                  Row(children: [const Icon(Icons.auto_awesome, size: 16), const SizedBox(width: 4), Text('$aiSuggestedCount kategorii zasugerowanych przez AI')]),
                ],
              ],
            ),
            actions: [
              TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Anuluj')),
              ElevatedButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Zapisz')),
            ],
          ),
    );

    if (confirmed != true) return;

    setState(() {
      _isSaving = true;
    });

    try {
      await widget.stagingService.saveAll(widget.accountId, _transactionService);

      if (!mounted) return;

      Navigator.of(context).pop(true);
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Wszystkie transakcje zostały zapisane')));
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isSaving = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Błąd podczas zapisywania transakcji: ${e.toString()}')));
    }
  }

  Future<void> _clearAll() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Wyczyść wszystko'),
            content: const Text('Czy na pewno chcesz usunąć wszystkie transakcje z listy?'),
            actions: [
              TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Anuluj')),
              ElevatedButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Usuń')),
            ],
          ),
    );

    if (confirmed == true) {
      widget.stagingService.clear();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Podgląd transakcji'),
        actions: [IconButton(icon: const Icon(Icons.delete_sweep), onPressed: _isSaving ? null : _clearAll, tooltip: 'Wyczyść wszystko')],
      ),
      body: TransactionStagingList(stagingService: widget.stagingService, accountId: widget.accountId, categoryService: widget.categoryService),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Expanded(child: OutlinedButton(onPressed: _isSaving ? null : () => Navigator.of(context).pop(false), child: const Text('Anuluj'))),
            const SizedBox(width: 16),
            Expanded(
              child: ElevatedButton(
                onPressed: _isSaving ? null : _saveAll,
                child: _isSaving ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Zapisz wszystkie'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
