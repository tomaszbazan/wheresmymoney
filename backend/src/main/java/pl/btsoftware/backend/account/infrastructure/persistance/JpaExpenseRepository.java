package pl.btsoftware.backend.account.infrastructure.persistance;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Expense;
import pl.btsoftware.backend.account.domain.ExpenseId;
import pl.btsoftware.backend.account.domain.ExpenseRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("!test")
@AllArgsConstructor
public class JpaExpenseRepository implements ExpenseRepository {

    private final ExpenseJpaRepository repository;

    @Override
    public void store(Expense expense) {
        ExpenseEntity entity = ExpenseEntity.fromDomain(expense);
        repository.save(entity);
    }

    @Override
    public Optional<Expense> findById(ExpenseId id) {
        return repository.findById(id.value())
                .map(ExpenseEntity::toDomain);
    }

    @Override
    public List<Expense> findAll() {
        return repository.findAll().stream()
                .map(ExpenseEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByAccountId(AccountId accountId) {
        return repository.findByAccountId(accountId.value()).stream()
                .map(ExpenseEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
