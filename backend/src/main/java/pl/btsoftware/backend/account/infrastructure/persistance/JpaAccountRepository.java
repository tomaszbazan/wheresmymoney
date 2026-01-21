package pl.btsoftware.backend.account.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.users.domain.GroupId;

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
        var existingEntity = repository.findById(account.id().value());
        if (existingEntity.isPresent()) {
            var existing = existingEntity.get();
            entity = new AccountEntity(
                    entity.getId(),
                    entity.getName(),
                    entity.getBalance(),
                    entity.getCurrency(),
                    entity.getCreatedAt(),
                    entity.getCreatedBy(),
                    entity.getOwnedByGroup(),
                    entity.getUpdatedAt(),
                    entity.getUpdatedBy(),
                    existing.getVersion()
            );
        }
        repository.save(entity);
    }

    @Override
    public Optional<Account> findById(AccountId id, GroupId groupId) {
        return repository.findByIdAndOwnedByGroup(id.value(), groupId.value())
                .map(AccountEntity::toDomain);
    }

    @Override
    public void deleteById(AccountId id) {
        repository.deleteById(id.value());
    }

    @Override
    public Optional<Account> findByNameAndCurrency(String name, Currency currency, GroupId groupId) {
        return repository.findByNameAndCurrencyAndOwnedByGroup(name, currency, groupId.value())
                .map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findAllBy(GroupId groupId) {
        return repository.findAllByOwnedByGroup(groupId.value()).stream()
                .map(AccountEntity::toDomain)
                .collect(Collectors.toList());
    }
}
