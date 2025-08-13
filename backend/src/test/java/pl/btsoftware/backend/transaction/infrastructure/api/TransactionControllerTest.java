package pl.btsoftware.backend.transaction.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.btsoftware.backend.shared.Currency.PLN;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionModuleFacade transactionModuleFacade;

    @Test
    void shouldReturnTransactionById() throws Exception {
        // given
        var transactionId = randomUUID();
        var accountId = randomUUID();
        var transaction = createTransaction(transactionId, accountId, new BigDecimal("100.50"), "Test transaction", TransactionType.INCOME);

        when(transactionModuleFacade.getTransactionById(transactionId)).thenReturn(transaction);

        // when & then
        mockMvc.perform(get("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();

        when(transactionModuleFacade.getTransactionById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        // when & then
        mockMvc.perform(get("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transaction not found")));
    }

    @Test
    void shouldReturnListOfAllTransactions() throws Exception {
        // given
        var transaction1 = createTransaction(randomUUID(), randomUUID(), new BigDecimal("100.00"), "Transaction 1", TransactionType.INCOME);
        var transaction2 = createTransaction(randomUUID(), randomUUID(), new BigDecimal("50.00"), "Transaction 2", TransactionType.EXPENSE);

        when(transactionModuleFacade.getAllTransactions()).thenReturn(List.of(transaction1, transaction2));

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[0].description").value("Transaction 1"))
                .andExpect(jsonPath("$.transactions[1].description").value("Transaction 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist() throws Exception {
        // given
        when(transactionModuleFacade.getAllTransactions()).thenReturn(emptyList());

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions", hasSize(0)));
    }

    @Test
    void shouldReturnAccountTransactions() throws Exception {
        // given
        var accountId = randomUUID();
        var transaction1 = createTransaction(randomUUID(), accountId, new BigDecimal("200.00"), "Account Transaction 1", TransactionType.INCOME);
        var transaction2 = createTransaction(randomUUID(), accountId, new BigDecimal("75.00"), "Account Transaction 2", TransactionType.EXPENSE);

        when(transactionModuleFacade.getTransactionsByAccountId(accountId)).thenReturn(List.of(transaction1, transaction2));

        // when & then
        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.transactions[1].accountId").value(accountId.toString()));
    }

    @Test
    void shouldUpdateTransaction() throws Exception {
        // given
        var transactionId = randomUUID();
        var accountId = randomUUID();
        var updatedTransaction = createTransaction(transactionId, accountId, new BigDecimal("150.00"), "Updated transaction", TransactionType.INCOME);

        when(transactionModuleFacade.updateTransaction(eq(transactionId), any(BigDecimal.class), any(String.class), any(String.class)))
                .thenReturn(updatedTransaction);

        var updateRequest = new UpdateTransactionRequest(new BigDecimal("150.00"), "Updated transaction", "Updated Category");
        String json = objectMapper.writeValueAsString(updateRequest);

        // when & then
        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.description").value("Updated transaction"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();

        when(transactionModuleFacade.updateTransaction(eq(nonExistentId), any(BigDecimal.class), any(String.class), any(String.class)))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        var updateRequest = new UpdateTransactionRequest(new BigDecimal("100.00"), "Test", "Test");

        // when & then
        mockMvc.perform(put("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transaction not found")));
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        // given
        var accountId = randomUUID();
        var transactionId = randomUUID();
        var transaction = createTransaction(transactionId, accountId, new BigDecimal("100.50"), "Test transaction", TransactionType.INCOME);

        when(transactionModuleFacade.createTransaction(any())).thenReturn(transaction);

        var createRequest = new CreateTransactionRequest(
                accountId,
                new BigDecimal("100.50"),
                "Test transaction",
                OffsetDateTime.now(ZoneOffset.UTC),
                "INCOME",
                "Salary",
                "PLN"
        );

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        // given
        var transactionId = randomUUID();

        // when & then
        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();

        doThrow(new IllegalArgumentException("Transaction not found"))
                .when(transactionModuleFacade).deleteTransaction(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transaction not found")));
    }

    private Transaction createTransaction(UUID transactionId, UUID accountId, BigDecimal amount, String description, TransactionType type) {
        return new Transaction(
                new TransactionId(transactionId),
                new AccountId(accountId),
                Money.of(amount, PLN),
                type,
                description,
                "Salary",
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC),
                Tombstone.active()
        );
    }
}