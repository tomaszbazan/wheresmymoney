package pl.btsoftware.backend.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.Money;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static pl.btsoftware.backend.account.domain.Currency.PLN;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account createAccount(CreateAccountCommand command) {
        var currency = command.currency() == null ? PLN : command.currency();
        if (accountRepository.findByNameAndCurrency(command.name(), currency).isPresent()) {
            throw new AccountAlreadyExistsException();
        }
        
        var account = command.toDomain();
        accountRepository.store(account);
        return account;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Account getById(UUID id) {
        return accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account updateAccount(UUID id, String newName) {
        var account = accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));

        // Check for duplicate name+currency combination (but allow same name for the same account)
        var existingAccount = accountRepository.findByNameAndCurrency(newName, account.balance().currency());
        if (existingAccount.isPresent() && !existingAccount.get().id().equals(account.id())) {
            throw new AccountAlreadyExistsException();
        }

        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }


    public void deleteAccount(UUID id) {
        accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));

        accountRepository.deleteById(id);
    }

    public Account addTransaction(UUID accountId, BigDecimal amount, String transactionType) {
        var account = accountRepository.findById(AccountId.from(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // Calculate balance change based on transaction type
        Money balanceChange = "INCOME".equals(transactionType)
                ? Money.of(amount, account.balance().currency())
                : Money.of(amount.negate(), account.balance().currency());

        var updatedAccount = account.updateBalance(balanceChange);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }
}
