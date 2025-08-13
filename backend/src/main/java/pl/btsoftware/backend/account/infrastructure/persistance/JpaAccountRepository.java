package pl.btsoftware.backend.account.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository repository;

    @Override
    public void store(Account account) {
        var entity = AccountEntity.fromDomain(account);
        repository.save(entity);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return repository.findById(id.value())
                .map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream()
                .map(AccountEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(AccountId id) {
        repository.deleteById(id.value());
    }

    @Override
    public Optional<Account> findByNameAndCurrency(String name, Currency currency) {
        return repository.findByNameAndCurrency(name, currency)
                .map(AccountEntity::toDomain);
    }
}
