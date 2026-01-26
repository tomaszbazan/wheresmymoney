package pl.btsoftware.backend.migration.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.btsoftware.backend.migration.application.MigrationSyncService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/migration/sync")
@RequiredArgsConstructor
@Slf4j
public class MigrationSyncController {

    private final MigrationSyncService migrationSyncService;

    @PostMapping("/transactions")
    public ResponseEntity<SyncResultResponse> syncTransaction(@RequestBody SyncTransactionRequest request) {
        log.info("Received transaction sync request for old ID: {}", request.oldId());
        SyncResultResponse response = migrationSyncService.syncTransaction(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/transactions/batch")
    public ResponseEntity<List<SyncResultResponse>> syncTransactionsBatch(@RequestBody List<SyncTransactionRequest> requests) {
        log.info("Received batch transaction sync request with {} items", requests.size());
        List<SyncResultResponse> responses = requests.stream()
                .map(migrationSyncService::syncTransaction)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/accounts")
    public ResponseEntity<SyncResultResponse> syncAccount(@RequestBody SyncAccountRequest request) {
        log.info("Received account sync request for old ID: {}", request.oldId());
        SyncResultResponse response = migrationSyncService.syncAccount(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/accounts/batch")
    public ResponseEntity<List<SyncResultResponse>> syncAccountsBatch(@RequestBody List<SyncAccountRequest> requests) {
        log.info("Received batch account sync request with {} items", requests.size());
        List<SyncResultResponse> responses = requests.stream()
                .map(migrationSyncService::syncAccount)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/categories")
    public ResponseEntity<SyncResultResponse> syncCategory(@RequestBody SyncCategoryRequest request) {
        log.info("Received category sync request for old ID: {}", request.oldId());
        SyncResultResponse response = migrationSyncService.syncCategory(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/categories/batch")
    public ResponseEntity<List<SyncResultResponse>> syncCategoriesBatch(@RequestBody List<SyncCategoryRequest> requests) {
        log.info("Received batch category sync request with {} items", requests.size());
        List<SyncResultResponse> responses = requests.stream()
                .map(migrationSyncService::syncCategory)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
