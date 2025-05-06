package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.AccountFixture;
import pl.btsoftware.wheresmymoney.configuration.IntegrationTest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
public class AccountControllerTest {

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
    void shouldReturnListOfAccounts() throws Exception {
        // given
        var accountId1 = createAccount("Checking Account");
        var accountId2 = createAccount("Savings Account");

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(2)))
                .andExpect(jsonPath("$.accounts[0].id").value(accountId1))
                .andExpect(jsonPath("$.accounts[0].name").value("Checking Account"))
                .andExpect(jsonPath("$.accounts[0].balance").value(0))
                .andExpect(jsonPath("$.accounts[0].currency").value("PLN"))
                .andExpect(jsonPath("$.accounts[1].id").value(accountId2))
                .andExpect(jsonPath("$.accounts[1].name").value("Savings Account"))
                .andExpect(jsonPath("$.accounts[1].balance").value(0))
                .andExpect(jsonPath("$.accounts[1].currency").value("PLN"));
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(0)));
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        // given
        var accountId = createAccount("New account");

        // and
        UpdateAccountRequest request = new UpdateAccountRequest("Updated Account");
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(put("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.name").value("Updated Account"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("PLN"));
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        // given
        var accountId = createAccount("Account to delete");

        // when
        mockMvc.perform(delete("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(0)));
    }

    private String createAccount(String Savings_Account) throws Exception {
        var createAccountRequest = new CreateAccountRequest(Savings_Account);
        var createAccountResponse = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(createAccountResponse.getResponse().getContentAsString()).get("id").asText();
    }
}
