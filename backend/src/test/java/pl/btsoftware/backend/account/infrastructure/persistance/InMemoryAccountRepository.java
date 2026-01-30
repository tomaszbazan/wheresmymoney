package pl.btsoftware.backend.account.infrastructure.persistance;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@Profile("test")
public class InMemoryAccountRepository implements AccountRepository {
    private final HashMap<AccountId, Account> database = new HashMap<>();

    @Override
    public void store(Account account) {
        database.put(account.id(), account);
    }

    @Override
    public Optional<Account> findById(AccountId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id))
                .filter(account -> account.ownedBy().equals(groupId));
    }

    @Override
    public void deleteById(AccountId id) {
        database.remove(id);
    }

    @Override
    public Optional<Account> findByNameAndCurrency(String name, Currency currency, GroupId groupId) {
        return database.values().stream()
                .filter(account -> account.name().equals(name)
                        && account.balance().currency().equals(currency)
                        && account.ownedBy().equals(groupId))
                .findFirst();
    }

    @Override
    public List<Account> findAllBy(GroupId groupId) {
        return database.values().stream()
                .filter(account -> account.ownedBy().equals(groupId))
                .toList();
    }
}
