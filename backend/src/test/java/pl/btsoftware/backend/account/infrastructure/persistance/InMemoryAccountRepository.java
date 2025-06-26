package pl.btsoftware.backend.account.infrastructure.persistance;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.AccountRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("test")
public class InMemoryAccountRepository implements AccountRepository {
    private final HashMap<UUID, Account> database = new HashMap<>();

    @Override
    public void store(Account account) {
        database.put(account.id().value(), account);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(database.get(id.value()));
    }

    @Override
    public List<Account> findAll() {
        return database.values().stream().toList();
    }

    @Override
    public void deleteById(UUID id) {
        database.remove(id);
    }

    @Override
    public Optional<Account> findByNameAndCurrency(String name, String currency) {
        return database.values().stream()
                .filter(account -> account.name().equals(name) && account.balance().currency().equals(currency))
                .findFirst();
    }
}
