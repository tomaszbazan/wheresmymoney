package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountModuleFacade accountModuleFacade;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void shouldReturnListOfAccounts() throws Exception {
//        // given
//        List<AccountDTO> accounts = Arrays.asList(
//                new AccountDTO(1L, "Checking Account", 1000.0),
//                new AccountDTO(2L, "Savings Account", 5000.0)
//        );
//        when(accountModuleFacade.getAccounts()).thenReturn(accounts);
//
//        // when & then
//        mockMvc.perform(get("/api/accounts")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].name").value("Checking Account"))
//                .andExpect(jsonPath("$[0].balance").value(1000.0))
//                .andExpect(jsonPath("$[1].id").value(2))
//                .andExpect(jsonPath("$[1].name").value("Savings Account"))
//                .andExpect(jsonPath("$[1].balance").value(5000.0));
//    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() throws Exception {
        // Given
        when(accountModuleFacade.getAccounts()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accounts", hasSize(0)));
    }

     @Test
     void shouldReturnInternalServerErrorWhenExceptionOccurs() throws Exception {
         // Given
         when(accountModuleFacade.getAccounts()).thenThrow(new RuntimeException("Simulated error"));

         // When & Then
         mockMvc.perform(get("/api/accounts")
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isInternalServerError());
     }

    @Test
    void shouldCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("New Account");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(accountModuleFacade).createAccount(new AccountModuleFacade.CreateAccountCommand("New Account"));
    }
}
