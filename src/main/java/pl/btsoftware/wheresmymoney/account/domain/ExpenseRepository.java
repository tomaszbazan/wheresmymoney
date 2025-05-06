package pl.btsoftware.wheresmymoney.account.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {
    void store(Expense expense);
    Optional<Expense> findById(ExpenseId id);
    List<Expense> findAll();
    List<Expense> findByAccountId(AccountId accountId);
    void deleteById(UUID id);
}