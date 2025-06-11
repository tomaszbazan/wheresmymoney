package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade.CreateExpenseCommand;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade.UpdateExpenseCommand;
import pl.btsoftware.wheresmymoney.account.domain.error.BusinessException;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@AllArgsConstructor
public class ExpenseController {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    private final AccountModuleFacade accountModuleFacade;


    @GetMapping("/{id}")
    public ExpenseView getExpense(@PathVariable UUID id) {
        logger.info("Received request to get expense with id: {}", id);
        return ExpenseView.from(accountModuleFacade.getExpense(id));
    }

    @GetMapping
    public ExpensesView searchExpense(@RequestParam(required = false) UUID accountId) {
        if (accountId != null) {
            logger.info("Received request to get expenses for account with id: {}", accountId);
            return ExpensesView.from(accountModuleFacade.getExpensesByAccountId(accountId));
        } else {
            logger.info("Received request to get all expenses");
            return ExpensesView.from(accountModuleFacade.getExpenses());
        }
    }

    @PostMapping
    public ResponseEntity<ExpenseView> createExpense(@RequestBody CreateExpenseRequest request) {
        logger.info("Received request to create expense for account with id: {}, amount: {}, description: {}, currency: {}",
                request.accountId(), request.amount(), request.description(), request.currency());
        var expense = accountModuleFacade.createExpense(
            new CreateExpenseCommand(
                request.accountId(),
                request.amount(),
                request.description(),
                    request.date(),
                    request.currency()
            )
        );
        return new ResponseEntity<>(ExpenseView.from(expense), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseView> updateExpense(
        @PathVariable UUID id,
        @RequestBody UpdateExpenseRequest request
    ) {
        logger.info("Received request to update expense with id: {}, amount: {}, description: {}", 
            id, request.amount(), request.description());
        var expense = accountModuleFacade.updateExpense(
            new UpdateExpenseCommand(
                id,
                request.amount(),
                request.description(),
                request.date()
            )
        );
        return ResponseEntity.ok(ExpenseView.from(expense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        logger.info("Received request to delete expense with id: {}", id);
        accountModuleFacade.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        logger.error("Business exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
