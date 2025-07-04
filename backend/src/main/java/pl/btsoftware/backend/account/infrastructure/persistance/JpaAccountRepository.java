package pl.btsoftware.backend.account.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.AccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository repository;

    @Override
    public void store(Account account) {
        AccountEntity entity = AccountEntity.fromDomain(account);
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
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Account> findByNameAndCurrency(String name, String currency) {
        return repository.findByNameAndCurrency(name, currency)
                .map(AccountEntity::toDomain);
    }
}
