package pl.btsoftware.backend.account.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Money;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        var account1 = createAccount(accountId1, "Checking Account", "PLN");
        var account2 = createAccount(accountId2, "Savings Account", "PLN");

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

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() throws Exception {
        // given
        when(accountModuleFacade.getAccounts()).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(0)));
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        // given
        var accountId = randomUUID();
        var updatedAccount = createAccount(accountId, "Updated Account", "PLN");

        when(accountModuleFacade.updateAccount(any(AccountModuleFacade.UpdateAccountCommand.class)))
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
    void shouldDeleteAccount() throws Exception {
        // given
        var accountId = randomUUID();

        // when & then
        mockMvc.perform(delete("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateAccount() throws Exception {
        // given
        var accountId = randomUUID();
        var createdAccount = createAccount(accountId, "Test Account", "EUR");

        when(accountModuleFacade.createAccount(any(AccountModuleFacade.CreateAccountCommand.class)))
                .thenReturn(createdAccount);

        var createAccountRequest = new CreateAccountRequest("Test Account", "EUR");

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Test Account"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAccountWithInvalidData() throws Exception {
        // given
        when(accountModuleFacade.createAccount(any(AccountModuleFacade.CreateAccountCommand.class)))
                .thenThrow(new AccountNameEmptyException());
        
        var createAccountRequest = new CreateAccountRequest("", "PLN");

        // when & then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Account name cannot be empty")));
    }

    @Test
    void shouldGetAccountById() throws Exception {
        // given
        var accountId = randomUUID();
        var account = createAccount(accountId, "Test Account", "PLN");

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

        when(accountModuleFacade.updateAccount(any(AccountModuleFacade.UpdateAccountCommand.class)))
                .thenThrow(new AccountNotFoundException(nonExistentId));
        
        var updateRequest = new UpdateAccountRequest("Updated Name");

        // when & then
        mockMvc.perform(put("/api/accounts/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Account not found with id: " + nonExistentId)));
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

    private Account createAccount(UUID id, String name, String currency) {
        return new Account(
                new AccountId(id),
                name,
                Money.of(BigDecimal.ZERO, currency),
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
