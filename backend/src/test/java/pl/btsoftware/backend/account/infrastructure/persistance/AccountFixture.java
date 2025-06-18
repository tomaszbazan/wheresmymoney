package pl.btsoftware.backend.account.infrastructure.persistance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.ExpenseRepository;

@Service
public class AccountFixture {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public void deleteAll() {
        // First delete all expenses
        expenseRepository.findAll().forEach(expense ->
                expenseRepository.deleteById(expense.id().value())
        );

        // Then delete all accounts
        accountRepository.findAll().stream().map(Account::id).forEach(id ->
                accountRepository.deleteById(id.value())
        );
    }
}
