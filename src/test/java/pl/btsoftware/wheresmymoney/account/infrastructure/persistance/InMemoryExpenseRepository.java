package pl.btsoftware.wheresmymoney.account.infrastructure.persistance;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.wheresmymoney.account.domain.AccountId;
import pl.btsoftware.wheresmymoney.account.domain.Expense;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseId;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("test")
public class InMemoryExpenseRepository implements ExpenseRepository {
    private final HashMap<UUID, Expense> database = new HashMap<>();

    @Override
    public void store(Expense expense) {
        database.put(expense.id().value(), expense);
    }

    @Override
    public Optional<Expense> findById(ExpenseId id) {
        return Optional.ofNullable(database.get(id.value()));
    }

    @Override
    public List<Expense> findAll() {
        return database.values().stream().toList();
    }

    @Override
    public List<Expense> findByAccountId(AccountId accountId) {
        return database.values().stream()
                .filter(expense -> expense.accountId().value().equals(accountId.value()))
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        database.remove(id);
    }
}
