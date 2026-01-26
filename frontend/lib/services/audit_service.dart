import 'package:http/http.dart' as http;

import '../models/audit_entity_type.dart';
import '../models/audit_log.dart';
import '../models/audit_operation.dart';
import 'auth_service.dart';
import 'http_client.dart';

class AuditLogsResponse {
  final List<AuditLog> auditLogs;
  final int page;
  final int size;
  final int totalElements;
  final int totalPages;

  AuditLogsResponse({required this.auditLogs, required this.page, required this.size, required this.totalElements, required this.totalPages});

  factory AuditLogsResponse.fromJson(Map<String, dynamic> json) {
    final auditLogsJson = json['auditLogs'] as List<dynamic>;
    final auditLogs = auditLogsJson.map((log) => AuditLog.fromJson(log as Map<String, dynamic>)).toList();

    return AuditLogsResponse(
      auditLogs: auditLogs,
      page: json['page'] as int,
      size: json['size'] as int,
      totalElements: json['totalElements'] as int,
      totalPages: json['totalPages'] as int,
    );
  }
}

class AuditLogQuery {
  final List<AuditEntityType>? entityTypes;
  final String? entityId;
  final List<AuditOperation>? operations;
  final String? performedBy;
  final DateTime? fromDate;
  final DateTime? toDate;
  final int page;
  final int size;

  AuditLogQuery({this.entityTypes, this.entityId, this.operations, this.performedBy, this.fromDate, this.toDate, this.page = 0, this.size = 50});

  Map<String, String> toQueryParameters() {
    final params = <String, String>{};

    if (entityTypes != null && entityTypes!.isNotEmpty) {
      params['entityType'] = entityTypes!.map((e) => e.name).join(',');
    }

    if (entityId != null) {
      params['entityId'] = entityId!;
    }

    if (operations != null && operations!.isNotEmpty) {
      params['operation'] = operations!.map((e) => e.name).join(',');
    }

    if (performedBy != null) {
      params['performedBy'] = performedBy!;
    }

    if (fromDate != null) {
      params['fromDate'] = fromDate!.toIso8601String();
    }

    if (toDate != null) {
      params['toDate'] = toDate!.toIso8601String();
    }

    params['page'] = page.toString();
    params['size'] = size.toString();

    return params;
  }
}

abstract class AuditService {
  Future<AuditLogsResponse> getAuditLogs(AuditLogQuery query);

  Future<AuditLog> getAuditLogById(String id);
}

class RestAuditService implements AuditService {
  final ApiClient _apiClient;

  RestAuditService({AuthService? authService, http.Client? httpClient}) : _apiClient = ApiClient(authService ?? AuthService(), httpClient: httpClient);

  @override
  Future<AuditLogsResponse> getAuditLogs(AuditLogQuery query) async {
    final queryParams = query.toQueryParameters();
    final uri = Uri.parse('/audit-logs').replace(queryParameters: queryParams);
    final response = await _apiClient.get(uri.toString(), AuditLogsResponse.fromJson);
    return response;
  }

  @override
  Future<AuditLog> getAuditLogById(String id) async {
    return await _apiClient.get<AuditLog>('/audit-logs/$id', AuditLog.fromJson);
  }
}
