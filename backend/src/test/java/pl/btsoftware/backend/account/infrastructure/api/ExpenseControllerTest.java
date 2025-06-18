package pl.btsoftware.backend.account.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.infrastructure.persistance.AccountFixture;
import pl.btsoftware.backend.configuration.IntegrationTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountFixture accountFixture;

    @BeforeEach
    void setUp() {
        accountFixture.deleteAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoExpensesExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses", hasSize(0)));
    }

    @Test
    void shouldCreateExpenseAndReturnIt() throws Exception {
        // given
        String accountId = createAccount("Test Account");

        // and
        OffsetDateTime now = OffsetDateTime.now();
        CreateExpenseRequest request = new CreateExpenseRequest(
                UUID.fromString(accountId),
                new BigDecimal("100.00"),
                "Test Expense",
                now,
                "PLN"
        );
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.currency").value("PLN"));
    }

    @Test
    void shouldReturnExpenseById() throws Exception {
        // given
        String accountId = createAccount("Test Account");
        String expenseId = createExpense(accountId, new BigDecimal("150.00"), "Test Expense");

        // when & then
        mockMvc.perform(get("/api/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.currency").value("PLN"));
    }

    @Test
    void shouldReturnExpensesByAccountId() throws Exception {
        // given
        String accountId = createAccount("Test Account");
        createExpense(accountId, new BigDecimal("150.00"), "Expense 1");
        createExpense(accountId, new BigDecimal("250.00"), "Expense 2");

        // when & then
        mockMvc.perform(get("/api/expenses?accountId=" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses", hasSize(2)))
                .andExpect(jsonPath("$.expenses[0].accountId").value(accountId))
                .andExpect(jsonPath("$.expenses[0].description").value("Expense 1"))
                .andExpect(jsonPath("$.expenses[0].amount").value(150.00))
                .andExpect(jsonPath("$.expenses[0].currency").value("PLN"))
                .andExpect(jsonPath("$.expenses[1].accountId").value(accountId))
                .andExpect(jsonPath("$.expenses[1].description").value("Expense 2"))
                .andExpect(jsonPath("$.expenses[1].amount").value(250.00))
                .andExpect(jsonPath("$.expenses[1].currency").value("PLN"));
    }

    @Test
    void shouldUpdateExpense() throws Exception {
        // given
        String accountId = createAccount("Test Account");
        String expenseId = createExpense(accountId, new BigDecimal("150.00"), "Original Description");

        // and
        OffsetDateTime updatedDate = OffsetDateTime.now();
        UpdateExpenseRequest request = new UpdateExpenseRequest(
                new BigDecimal("200.00"),
                "Updated Description",
                updatedDate
        );
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(put("/api/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.currency").value("PLN"));
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        // given
        String accountId = createAccount("Test Account");
        String expenseId = createExpense(accountId, new BigDecimal("150.00"), "Test Expense");

        // when
        mockMvc.perform(delete("/api/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/api/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorWhenCreatingExpenseWithDifferentCurrencyThanAccount() throws Exception {
        // given
        String accountId = createAccount("EUR Account", "EUR");

        // and
        OffsetDateTime now = OffsetDateTime.now();
        CreateExpenseRequest request = new CreateExpenseRequest(
                UUID.fromString(accountId),
                new BigDecimal("100.00"),
                "Test Expense with Different Currency",
                now,
                "PLN"
        );
        String json = objectMapper.writeValueAsString(request);

        // when
        var result = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // then
        // Verify no expenses were created
        mockMvc.perform(get("/api/expenses?accountId=" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenses", hasSize(0)));
    }

    private String createAccount(String accountName) throws Exception {
        return createAccount(accountName, "PLN");
    }

    private String createAccount(String accountName, String currency) throws Exception {
        var createAccountRequest = new CreateAccountRequest(accountName, currency);
        var createAccountResponse = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(createAccountResponse.getResponse().getContentAsString()).get("id").asText();
    }

    private String createExpense(String accountId, BigDecimal amount, String description) throws Exception {
        CreateExpenseRequest request = new CreateExpenseRequest(
                UUID.fromString(accountId),
                amount,
                description,
                OffsetDateTime.now(),
                "PLN"
        );
        var createExpenseResponse = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(createExpenseResponse.getResponse().getContentAsString()).get("id").asText();
    }
}
