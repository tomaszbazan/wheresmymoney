package pl.btsoftware.wheresmymoney.account.infrastructure.persistance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;

@Service
public class AccountFixture {
    @Autowired
    private AccountRepository accountRepository;

    public void deleteAll() {
        accountRepository.findAll().stream().map(Account::id).forEach(id -> accountRepository.deleteById(id.value()));
    }
}
