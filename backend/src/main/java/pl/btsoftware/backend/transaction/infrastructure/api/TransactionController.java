package pl.btsoftware.backend.transaction.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionModuleFacade transactionModuleFacade;

    @GetMapping("/transactions/{id}")
    public TransactionView getTransaction(@PathVariable UUID id) {
        log.info("Received request to get transaction with id: {}", id);
        var transaction = transactionModuleFacade.getTransactionById(id);
        return TransactionView.from(transaction);
    }

    @GetMapping("/transactions")
    public TransactionsView getAllTransactions() {
        log.info("Received request to get all transactions");
        var transactions = transactionModuleFacade.getAllTransactions();
        return TransactionsView.from(transactions);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public TransactionsView getAccountTransactions(@PathVariable UUID accountId) {
        log.info("Received request to get transactions for account with id: {}", accountId);
        var transactions = transactionModuleFacade.getTransactionsByAccountId(accountId);
        return TransactionsView.from(transactions);
    }

    @PutMapping("/transactions/{id}")
    public TransactionView updateTransaction(@PathVariable UUID id, @RequestBody UpdateTransactionRequest request) {
        log.info("Received request to update transaction with id: {}", id);
        var transaction = transactionModuleFacade.updateTransaction(id, request.amount(), request.description(), request.category());
        return TransactionView.from(transaction);
    }

    @DeleteMapping("/transactions/{id}")
    public void deleteTransaction(@PathVariable UUID id) {
        log.info("Received request to delete transaction with id: {}", id);
        transactionModuleFacade.deleteTransaction(id);
    }
}