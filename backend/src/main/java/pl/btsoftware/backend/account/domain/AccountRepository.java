package pl.btsoftware.backend.account.domain;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void store(Account account);
    Optional<Account> findById(AccountId id);
    List<Account> findAll();

    void deleteById(AccountId id);
    Optional<Account> findByNameAndCurrency(String name, Currency currency);
}