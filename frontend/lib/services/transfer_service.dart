import 'package:http/http.dart' as http;

import '../models/transfer.dart';
import 'auth_service.dart';
import 'http_client.dart';

abstract class TransferService {
  Future<Transfer> createTransfer({required String sourceAccountId, required String targetAccountId, required double sourceAmount, double? targetAmount, String? description});

  Future<List<Transfer>> getTransfers();
}

class RestTransferService implements TransferService {
  final ApiClient _apiClient;

  RestTransferService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);

  @override
  Future<Transfer> createTransfer({
    required String sourceAccountId,
    required String targetAccountId,
    required double sourceAmount,
    double? targetAmount,
    String? description,
  }) async {
    final Map<String, dynamic> body = {'sourceAccountId': sourceAccountId, 'targetAccountId': targetAccountId, 'sourceAmount': sourceAmount};

    if (targetAmount != null) {
      body['targetAmount'] = targetAmount;
    }

    if (description != null) {
      body['description'] = description;
    }

    return await _apiClient.post<Transfer>('/transfers', body, Transfer.fromJson);
  }

  @override
  Future<List<Transfer>> getTransfers() async {
    return await _apiClient.getList<Transfer>('/transfers', 'transfers', Transfer.fromJson);
  }
}
