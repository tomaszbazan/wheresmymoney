package pl.btsoftware.backend.transaction.infrastructure.api;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.shared.pagination.PaginationValidator;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.transaction.domain.TransactionSearchCriteria;
import pl.btsoftware.backend.users.domain.UserId;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionModuleFacade transactionModuleFacade;
    private final CategoryModuleFacade categoryModuleFacade;
    private final PaginationValidator paginationValidator;

    @PostMapping("/transactions")
    public TransactionView createTransaction(
            @RequestBody @Valid CreateTransactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to create transaction for account: {} by user: {}", request.accountId(), userId);
        var transaction = transactionModuleFacade.createTransaction(request.toCommand(userId));
        return TransactionView.from(
                transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @GetMapping("/transactions/{id}")
    public TransactionView getTransaction(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get transaction with id: {} by user: {}", id, userId);
        var transaction = transactionModuleFacade.getTransactionById(id, userId);
        return TransactionView.from(
                transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @GetMapping("/transactions")
    public TransactionsPaginatedView getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Set<TransactionType> types,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Set<UUID> accountIds,
            @RequestParam(required = false) Set<UUID> categoryIds,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "transactionDate,desc") String[] sort,
            @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get paginated transactions (page={}, size={}) by user: {}", page, size, userId);

        var validatedSize = paginationValidator.validatePageSize(size);

        Sort finalSort = Sort.unsorted();
        if (sort != null && sort.length > 0) {
            String property = sort[0];
            Sort.Direction direction = Sort.Direction.DESC;

            if (sort.length > 1) {
                direction = Sort.Direction.fromString(sort[1]);
            } else if (property.contains(",")) {
                String[] parts = property.split(",");
                property = parts[0];
                if (parts.length > 1) {
                    direction = Sort.Direction.fromString(parts[1]);
                }
            }
            finalSort = Sort.by(direction, property);
        }

        // Add secondary sort by createdAt for stability
        finalSort = finalSort.and(Sort.by("createdAt").descending());

        var pageable = PageRequest.of(page, validatedSize, finalSort);

        var criteria = TransactionSearchCriteria.from(
                types, dateFrom, dateTo, minAmount, maxAmount, accountIds, categoryIds, description);
        var transactionsPage = transactionModuleFacade.getAllTransactions(criteria, userId, pageable);

        return TransactionsPaginatedView.from(
                transactionsPage, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @PutMapping("/transactions/{id}")
    public TransactionView updateTransaction(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateTransactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to update transaction with id: {} by user: {}", id, userId);
        var transaction = transactionModuleFacade.updateTransaction(request.toCommand(TransactionId.of(id)), userId);
        return TransactionView.from(
                transaction, categoryId -> categoryModuleFacade.getCategoryById(categoryId, userId));
    }

    @DeleteMapping("/transactions/{id}")
    public void deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to delete transaction with id: {} by user: {}", id, userId);
        transactionModuleFacade.deleteTransaction(id, userId);
    }

    @PostMapping("/transactions/bulk")
    public BulkCreateTransactionResponse bulkCreateTransactions(
            @RequestBody @Valid BulkCreateTransactionRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info(
                "Received request to bulk create {} transactions for account: {} by user: {}",
                request.transactions().size(),
                request.accountId(),
                userId);
        var result = transactionModuleFacade.bulkCreateTransactions(request.toCommands(userId), userId);
        return BulkCreateTransactionResponse.from(result);
    }
}
