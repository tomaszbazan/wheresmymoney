package pl.btsoftware.wheresmymoney.account.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    void store(Account account);
    Optional<Account> findById(AccountId id);
    List<Account> findAll();
    void deleteById(UUID id);
}