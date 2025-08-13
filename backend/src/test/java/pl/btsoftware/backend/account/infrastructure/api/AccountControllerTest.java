package pl.btsoftware.backend.account.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountModuleFacade accountModuleFacade;

    @Test
    void shouldReturnListOfAccounts() throws Exception {
        // given
        var accountId1 = randomUUID();
        var accountId2 = randomUUID();
        var account1 = createAccount(accountId1, "Checking Account", PLN);
        var account2 = createAccount(accountId2, "Savings Account", PLN);

        when(accountModuleFacade.getAccounts()).thenReturn(List.of(account1, account2));

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                .andExpect(jsonPath("$.accounts[0].id").value(accountId1.toString()))
                .andExpect(jsonPath("$.accounts[0].name").value("Checking Account"))
                .andExpect(jsonPath("$.accounts[0].balance").value(0))
                .andExpect(jsonPath("$.accounts[0].currency").value("PLN"))
                .andExpect(jsonPath("$.accounts[1].id").value(accountId2.toString()))
                .andExpect(jsonPath("$.accounts[1].name").value("Savings Account"))
                .andExpect(jsonPath("$.accounts[1].balance").value(0))
                .andExpect(jsonPath("$.accounts[1].currency").value("PLN"));
    }

    private static Stream<Arguments> incorrectName() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("  "),
                Arguments.of("a".repeat(101))
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() throws Exception {
        // given
        when(accountModuleFacade.getAccounts()).thenReturn(emptyList());

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(0)));
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        // given
        var accountId = randomUUID();

        // when & then
        mockMvc.perform(delete("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        // given
        var accountId = randomUUID();
        var updatedAccount = createAccount(accountId, "Updated Account", PLN);

        when(accountModuleFacade.updateAccount(any(UpdateAccountCommand.class)))
                .thenReturn(updatedAccount);

        UpdateAccountRequest request = new UpdateAccountRequest("Updated Account");
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(put("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Account"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("PLN"));
    }

    @Test
    void shouldCreateAccount() throws Exception {
        // given
        var accountId = randomUUID();
        var createdAccount = createAccount(accountId, "Test Account", EUR);

        when(accountModuleFacade.createAccount(any(CreateAccountCommand.class)))
                .thenReturn(createdAccount);

        var createAccountRequest = """
                {
                    "name": "Test Account",
                    "currency": "EUR"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Test Account"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @ParameterizedTest
    @MethodSource("incorrectName")
    void shouldReturnBadRequestWhenCreatingAccountWithEmptyName(String name) throws Exception {
        // given
        var createAccountRequest = new CreateAccountRequest(name, PLN);

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAccountWithNullName() throws Exception {
        // given
        var createAccountRequest = new CreateAccountRequest(null, PLN);

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAccountCreationWithUnsupportedCurrency() throws Exception {
        // given
        var createAccountRequest = """
                {
                    "name": "Test Account",
                    "currency": "JPY"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAccountById() throws Exception {
        // given
        var accountId = randomUUID();
        var account = createAccount(accountId, "Test Account", PLN);

        when(accountModuleFacade.getAccount(accountId)).thenReturn(account);

        // when & then
        mockMvc.perform(get("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Test Account"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("PLN"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentAccount() throws Exception {
        // given
        var nonExistentId = randomUUID();

        when(accountModuleFacade.getAccount(nonExistentId))
                .thenThrow(new AccountNotFoundException(nonExistentId));

        // when & then
        mockMvc.perform(get("/api/accounts/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found with id: " + nonExistentId)));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentAccount() throws Exception {
        // given
        var nonExistentId = randomUUID();

        when(accountModuleFacade.updateAccount(any(UpdateAccountCommand.class)))
                .thenThrow(new AccountNotFoundException(nonExistentId));
        
        var updateRequest = new UpdateAccountRequest("Updated Name");

        // when & then
        mockMvc.perform(put("/api/accounts/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found with id: " + nonExistentId)));
    }

    @ParameterizedTest
    @MethodSource("incorrectName")
    void shouldReturnBadRequestWhenUpdateAccountWithEmptyName(String name) throws Exception {
        // given
        var updateAccountRequest = new UpdateAccountRequest(name);

        // when & then
        mockMvc.perform(put("/api/accounts/" + randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingAccountWithNullName() throws Exception {
        // given
        var updateAccountRequest = new UpdateAccountRequest(null);

        // when & then
        mockMvc.perform(put("/api/accounts/" + randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentAccount() throws Exception {
        // given
        var nonExistentId = randomUUID();

        doThrow(new AccountNotFoundException(nonExistentId))
                .when(accountModuleFacade).deleteAccount(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/accounts/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found with id: " + nonExistentId)));
    }

    private Account createAccount(UUID id, String name, Currency currency) {
        return new Account(
                new AccountId(id),
                name,
                Money.of(BigDecimal.ZERO, currency),
                new ArrayList<>(),
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
