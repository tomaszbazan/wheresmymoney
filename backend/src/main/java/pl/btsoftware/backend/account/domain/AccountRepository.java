package pl.btsoftware.backend.account.domain;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void store(Account account);
    Optional<Account> findById(AccountId id, GroupId groupId);
    List<Account> findAllBy(GroupId groupId);
    Optional<Account> findByNameAndCurrency(String name, Currency currency, GroupId groupId);
    void deleteById(AccountId id);
}
