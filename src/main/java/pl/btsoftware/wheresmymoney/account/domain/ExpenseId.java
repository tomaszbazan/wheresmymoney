package pl.btsoftware.wheresmymoney.account.domain;

import java.util.UUID;

public record ExpenseId(UUID value) {
    public static ExpenseId generate() {
        return new ExpenseId(UUID.randomUUID());
    }

    public static ExpenseId from(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Expense id cannot be null");
        }
        return new ExpenseId(id);
    }
}