package pl.btsoftware.wheresmymoney.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;

import java.util.List;
import java.util.UUID;

import static pl.btsoftware.wheresmymoney.account.domain.AccountId.from;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account createAccount(AccountModuleFacade.CreateAccountCommand command) {
        var account = command.toDomain();
        accountRepository.store(account);
        return account;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Account getById(UUID id) {
        return accountRepository.findById(from(id)).orElseThrow();
    }
}