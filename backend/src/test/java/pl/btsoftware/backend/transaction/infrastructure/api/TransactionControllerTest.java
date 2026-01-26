package pl.btsoftware.backend.transaction.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.config.WebConfig;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.shared.pagination.PaginationValidator;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.JwtTokenFixture.createTokenFor;

@WebMvcTest(controllers = TransactionController.class)
@Import(WebConfig.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionModuleFacade transactionModuleFacade;

    @MockBean
    private CategoryModuleFacade categoryModuleFacade;

    @MockBean
    private PaginationValidator paginationValidator;

    @BeforeEach
    void setUp() {
        when(categoryModuleFacade.getCategoryById(any(CategoryId.class), any(UserId.class)))
                .thenAnswer(invocation -> {
                    var categoryId = invocation.getArgument(0, CategoryId.class);
                    return Instancio.of(Category.class)
                            .set(field(Category::id), categoryId)
                            .set(field(Category::name), "Sample Category")
                            .create();
                });
        when(paginationValidator.validatePageSize(any(Integer.class)))
                .thenAnswer(invocation -> Math.min(invocation.getArgument(0, Integer.class), 100));
    }

    @Test
    void shouldReturnTransactionById() throws Exception {
        // given
        var transactionId = randomUUID();
        var accountId = randomUUID();
        var userId = new UserId("user123");
        var transaction = createTransaction(transactionId, accountId, new BigDecimal("100.50"), "Test transaction", TransactionType.INCOME);

        when(transactionModuleFacade.getTransactionById(transactionId, userId)).thenReturn(transaction);

        // when & then
        mockMvc.perform(get("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.category.id").value(transaction.categoryId().value().toString()))
                .andExpect(jsonPath("$.category.name").value("Sample Category"))
                .andExpect(jsonPath("$.transactionDate").value(transaction.transactionDate().toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();
        var userId = new UserId("user123");

        when(transactionModuleFacade.getTransactionById(nonExistentId, userId))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        // when & then
        mockMvc.perform(get("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transaction not found")));
    }

    @Test
    void shouldReturnListOfAllTransactions() throws Exception {
        // given
        var userId = new UserId("user123");
        var transaction1 = createTransaction(randomUUID(), randomUUID(), new BigDecimal("100.00"), "Transaction 1", TransactionType.INCOME);
        var transaction2 = createTransaction(randomUUID(), randomUUID(), new BigDecimal("50.00"), "Transaction 2", TransactionType.EXPENSE);

        var page = new PageImpl<>(List.of(transaction1, transaction2), PageRequest.of(0, 20), 2);
        when(transactionModuleFacade.getAllTransactionsPaginated(any(UserId.class), any())).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[0].description").value("Transaction 1"))
                .andExpect(jsonPath("$.transactions[1].description").value("Transaction 2"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist() throws Exception {
        // given
        var userId = new UserId("user123");
        var emptyPage = new PageImpl<Transaction>(emptyList(), PageRequest.of(0, 20), 0);
        when(transactionModuleFacade.getAllTransactionsPaginated(any(UserId.class), any())).thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.transactions", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldReturnAccountTransactions() throws Exception {
        // given
        var accountId = randomUUID();
        var userId = new UserId("user123");
        var transaction1 = createTransaction(randomUUID(), accountId, new BigDecimal("200.00"), "Account Transaction 1", TransactionType.INCOME);
        var transaction2 = createTransaction(randomUUID(), accountId, new BigDecimal("75.00"), "Account Transaction 2", TransactionType.EXPENSE);

        when(transactionModuleFacade.getTransactionsByAccountId(accountId, userId)).thenReturn(List.of(transaction1, transaction2));

        // when & then
        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
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

        when(transactionModuleFacade.updateTransaction(any(UpdateTransactionCommand.class), any(UserId.class))).thenReturn(updatedTransaction);

        var updateRequest = new UpdateTransactionRequest(Money.of(new BigDecimal("150.00"), PLN), "Updated transaction", randomUUID());
        String json = objectMapper.writeValueAsString(updateRequest);

        // when & then
        mockMvc.perform(put("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.description").value("Updated transaction"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();

        when(transactionModuleFacade.updateTransaction(any(UpdateTransactionCommand.class), any(UserId.class)))
                .thenThrow(new IllegalArgumentException("Transaction not found"));

        var updateRequest = new UpdateTransactionRequest(Money.of(new BigDecimal("100.00"), PLN), "Test", randomUUID());

        // when & then
        mockMvc.perform(put("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(createTokenFor("user123")))
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
                Money.of(new BigDecimal("100.50"), PLN),
                "Test transaction",
                now(),
                "INCOME",
                randomUUID()
        );

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.category.id").value(transaction.categoryId().value().toString()))
                .andExpect(jsonPath("$.category.name").value("Sample Category"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        // given
        var transactionId = randomUUID();

        // when & then
        mockMvc.perform(delete("/api/transactions/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTransaction() throws Exception {
        // given
        var nonExistentId = randomUUID();
        var userId = new UserId("user123");

        doThrow(new IllegalArgumentException("Transaction not found"))
                .when(transactionModuleFacade).deleteTransaction(nonExistentId, userId);

        // when & then
        mockMvc.perform(delete("/api/transactions/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(createTokenFor("user123")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transaction not found")));
    }

    private Transaction createTransaction(UUID transactionId, UUID accountId, BigDecimal amount, String description, TransactionType type) {
        var auditInfo = AuditInfo.create("user123", randomUUID());
        var accountIdObj = new AccountId(accountId);
        var money = Money.of(amount, PLN);
        var transactionDate = now();
        var hashCalculator = new pl.btsoftware.backend.transaction.domain.TransactionHashCalculator();
        var hash = hashCalculator.calculateHash(accountIdObj, money, description, transactionDate, type);
        return new Transaction(
                new TransactionId(transactionId),
                accountIdObj,
                money,
                type,
                description,
                CategoryId.generate(),
                transactionDate,
                hash,
                auditInfo,
                auditInfo,
                Tombstone.active()
        );
    }
}
