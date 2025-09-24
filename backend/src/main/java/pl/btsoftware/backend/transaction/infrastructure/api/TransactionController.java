package pl.btsoftware.backend.transaction.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionModuleFacade transactionModuleFacade;
    private final CategoryModuleFacade categoryModuleFacade;

    @PostMapping("/transactions")
    public TransactionView createTransaction(@RequestBody CreateTransactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to create transaction for account: {} by user: {}", request.accountId(), userId);
        var transaction = transactionModuleFacade.createTransaction(request.toCommand(userId));
        return TransactionView.from(transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @GetMapping("/transactions/{id}")
    public TransactionView getTransaction(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get transaction with id: {} by user: {}", id, userId);
        var transaction = transactionModuleFacade.getTransactionById(id, userId);
        return TransactionView.from(transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @GetMapping("/transactions")
    public TransactionsView getAllTransactions(@AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get all transactions by user: {}", userId);
        var transactions = transactionModuleFacade.getAllTransactions(userId);
        return TransactionsView.from(transactions, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @GetMapping("/accounts/{accountId}/transactions") // TODO: Consider renaming
    public TransactionsView getAccountTransactions(@PathVariable UUID accountId, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get transactions for account with id: {} by user: {}", accountId, userId);
        var transactions = transactionModuleFacade.getTransactionsByAccountId(accountId, userId);
        return TransactionsView.from(transactions, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @PutMapping("/transactions/{id}")
    public TransactionView updateTransaction(@PathVariable UUID id, @RequestBody UpdateTransactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to update transaction with id: {} by user: {}", id, userId);
        var transaction = transactionModuleFacade.updateTransaction(request.toCommand(TransactionId.of(id)), userId);
        return TransactionView.from(transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @DeleteMapping("/transactions/{id}")
    public void deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to delete transaction with id: {} by user: {}", id, userId);
        transactionModuleFacade.deleteTransaction(id, userId);
    }
}
