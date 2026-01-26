package pl.btsoftware.backend.migration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.migration.application.MigrationSyncService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MigrationSyncController.class)
class MigrationSyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MigrationSyncService migrationSyncService;

    @Test
    void shouldSyncTransactionSuccessfully() throws Exception {
        SyncTransactionRequest request = new SyncTransactionRequest(
                1,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(150.00),
                LocalDateTime.now(),
                "Test expense",
                "EXPENSE",
                1
        );

        UUID newId = UUID.randomUUID();
        SyncResultResponse response = new SyncResultResponse(true, newId, "Success", 1);

        when(migrationSyncService.syncTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/migration/sync/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newId").value(newId.toString()))
                .andExpect(jsonPath("$.oldId").value(1));
    }

    @Test
    void shouldReturnBadRequestOnTransactionSyncFailure() throws Exception {
        SyncTransactionRequest request = new SyncTransactionRequest(
                1,
                100,
                200,
                10,
                20,
                BigDecimal.valueOf(150.00),
                LocalDateTime.now(),
                "Test expense",
                "EXPENSE",
                1
        );

        SyncResultResponse response = new SyncResultResponse(false, null, "Error: Missing mapping", 1);

        when(migrationSyncService.syncTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/migration/sync/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error: Missing mapping"));
    }

    @Test
    void shouldSyncTransactionsBatch() throws Exception {
        SyncTransactionRequest request1 = new SyncTransactionRequest(
                1, 100, 200, 10, 20, BigDecimal.valueOf(150.00),
                LocalDateTime.now(), "Test 1", "EXPENSE", 1
        );
        SyncTransactionRequest request2 = new SyncTransactionRequest(
                2, 100, 200, 10, 20, BigDecimal.valueOf(200.00),
                LocalDateTime.now(), "Test 2", "EXPENSE", 1
        );

        List<SyncTransactionRequest> requests = List.of(request1, request2);

        UUID newId1 = UUID.randomUUID();
        UUID newId2 = UUID.randomUUID();

        when(migrationSyncService.syncTransaction(any()))
                .thenReturn(new SyncResultResponse(true, newId1, "Success", 1))
                .thenReturn(new SyncResultResponse(true, newId2, "Success", 2));

        mockMvc.perform(post("/api/v1/migration/sync/transactions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[1].success").value(true));
    }

    @Test
    void shouldSyncAccountSuccessfully() throws Exception {
        SyncAccountRequest request = new SyncAccountRequest(
                1,
                "Test Account",
                BigDecimal.valueOf(1000.00),
                1,
                20,
                10,
                "Test comment",
                LocalDateTime.now()
        );

        UUID newId = UUID.randomUUID();
        SyncResultResponse response = new SyncResultResponse(true, newId, "Success", 1);

        when(migrationSyncService.syncAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/migration/sync/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newId").value(newId.toString()));
    }

    @Test
    void shouldSyncCategorySuccessfully() throws Exception {
        SyncCategoryRequest request = new SyncCategoryRequest(
                1,
                "Food",
                "EXPENSE",
                "Food expenses",
                "#FF0000",
                null,
                20,
                10,
                LocalDateTime.now()
        );

        UUID newId = UUID.randomUUID();
        SyncResultResponse response = new SyncResultResponse(true, newId, "Success", 1);

        when(migrationSyncService.syncCategory(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/migration/sync/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newId").value(newId.toString()));
    }
}
