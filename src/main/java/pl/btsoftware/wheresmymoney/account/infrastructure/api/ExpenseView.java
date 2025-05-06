package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import pl.btsoftware.wheresmymoney.account.domain.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseView(UUID id, UUID accountId, BigDecimal amount, String description, LocalDateTime date) {
    public static ExpenseView from(Expense expense) {
        return new ExpenseView(
            expense.id().value(),
            expense.accountId().value(),
            expense.amount(),
            expense.description(),
            expense.date()
        );
    }
}