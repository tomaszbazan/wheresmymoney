package pl.btsoftware.wheresmymoney.expense.api;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.wheresmymoney.expense.ExpenseService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void shouldReturnListOfExpenses() throws Exception {
        // when & then
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnSingleExpenseForProvidedId() throws Exception {
        // when & then
        mockMvc.perform(get("/expenses/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateExpense() throws Exception {
        // given
        @Language("JSON") var content = """
                {
                    "id": "5f015914-5bda-40dc-9f4c-9c1cbdd37043"
                }
                """;

        // when & then
        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());

        Mockito.verify(expenseService, Mockito.times(1)).createExpense(new ExpenseRequest(UUID.fromString("5f015914-5bda-40dc-9f4c-9c1cbdd37043")));
        Mockito.verifyNoMoreInteractions(expenseService);
    }

    @Test
    void shouldUpdateExpense() {
    }

    @Test
    void shouldDeleteExpense() {
    }

}