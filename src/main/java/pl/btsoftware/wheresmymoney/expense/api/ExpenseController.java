package pl.btsoftware.wheresmymoney.expense.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.wheresmymoney.expense.ExpenseService;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/expenses")
@Slf4j
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExpenseView> getExpenses() {
        return List.of();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseView getExpense(@PathVariable UUID id) {
        log.info("Getting expense with id: {}", id);
        return new ExpenseView(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = CREATED)
    public void createExpense(@RequestBody ExpenseRequest expense) {
        log.info("Creating expense: {}", expense);
        expenseService.createExpense(expense);
    }
}
