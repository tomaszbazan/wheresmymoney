import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:frontend/models/account.dart';
import 'package:frontend/models/csv_parse_result.dart';
import 'package:frontend/screens/transaction_staging_screen.dart';
import 'package:frontend/services/csv_import_service.dart';
import 'package:frontend/services/transaction_staging_service.dart';
import 'package:frontend/utils/error_messages.dart';

class CsvUploadDialog extends StatefulWidget {
  final CsvImportService csvImportService;
  final List<Account> accounts;

  const CsvUploadDialog({super.key, required this.csvImportService, required this.accounts});

  @override
  State<CsvUploadDialog> createState() => _CsvUploadDialogState();
}

class _CsvUploadDialogState extends State<CsvUploadDialog> {
  File? _selectedFile;
  String? _selectedAccountId;
  bool _isUploading = false;
  List<String> _errors = [];

  Future<void> _pickFile() async {
    final result = await FilePicker.platform.pickFiles(type: FileType.custom, allowedExtensions: ['csv'], allowMultiple: false);

    if (result != null && result.files.single.path != null) {
      setState(() {
        _selectedFile = File(result.files.single.path!);
        _errors = [];
      });
    }
  }

  Future<void> _uploadFile() async {
    if (_selectedFile == null || _selectedAccountId == null) {
      return;
    }

    final fileSize = await _selectedFile!.length();
    const maxFileSize = 10 * 1024 * 1024;

    if (fileSize > maxFileSize) {
      setState(() {
        _errors = ['Plik jest za duży (maksymalnie 10MB)'];
      });
      return;
    }

    setState(() {
      _isUploading = true;
      _errors = [];
    });

    try {
      final result = await widget.csvImportService.uploadCsv(_selectedFile!, _selectedAccountId!);

      if (!mounted) return;

      _handleUploadResult(result);
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isUploading = false;
        _errors = ['Błąd podczas przesyłania pliku: ${e.toString()}'];
      });
    }
  }

  void _handleUploadResult(CsvParseResult result) {
    if (result.hasErrors) {
      setState(() {
        _isUploading = false;
        _errors = result.errors.take(10).map((e) => ErrorMessages.getMessage(e.type, e.lineNumber)).toList();

        if (result.errorCount > 10) {
          _errors.add('... i ${result.errorCount - 10} więcej błędów');
        }
      });
    } else if (result.hasProposals) {
      final stagingService = TransactionStagingService();
      stagingService.loadFromCsv(result);

      Navigator.of(context).pop();

      Navigator.of(context).push(MaterialPageRoute<void>(builder: (context) => TransactionStagingScreen(stagingService: stagingService, accountId: _selectedAccountId!)));
    } else {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Brak transakcji do zaimportowania')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final canUpload = _selectedFile != null && _selectedAccountId != null && !_isUploading;

    return AlertDialog(
      title: const Text('Importuj transakcje z CSV'),
      content: SizedBox(
        width: 500,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (_selectedFile != null) Text('Wybrany plik: ${_selectedFile!.path.split('/').last}', style: Theme.of(context).textTheme.bodyMedium),
            const SizedBox(height: 16),
            ElevatedButton.icon(onPressed: _isUploading ? null : _pickFile, icon: const Icon(Icons.file_upload), label: const Text('Wybierz plik CSV')),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              initialValue: _selectedAccountId,
              decoration: const InputDecoration(labelText: 'Konto', border: OutlineInputBorder()),
              items: widget.accounts.map((account) => DropdownMenuItem(value: account.id, child: Text(account.name))).toList(),
              onChanged:
                  _isUploading
                      ? null
                      : (value) {
                        setState(() {
                          _selectedAccountId = value;
                        });
                      },
            ),
            if (_isUploading) ...[
              const SizedBox(height: 16),
              const LinearProgressIndicator(),
              const SizedBox(height: 8),
              const Text('Przesyłanie i przetwarzanie pliku...', textAlign: TextAlign.center),
            ],
            if (_errors.isNotEmpty) ...[
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(color: Colors.red.shade50, borderRadius: BorderRadius.circular(8), border: Border.all(color: Colors.red.shade200)),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.error, color: Colors.red.shade700, size: 20),
                        const SizedBox(width: 8),
                        Text('Błędy', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red.shade700)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    ..._errors.map((error) => Padding(padding: const EdgeInsets.only(bottom: 4), child: Text(error, style: TextStyle(color: Colors.red.shade700)))),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(onPressed: _isUploading ? null : () => Navigator.of(context).pop(), child: const Text('Anuluj')),
        ElevatedButton(onPressed: canUpload ? _uploadFile : null, child: const Text('Prześlij')),
      ],
    );
  }
}
