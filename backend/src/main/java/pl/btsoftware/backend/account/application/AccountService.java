package pl.btsoftware.backend.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.domain.error.CannotDeleteAccountWithTransactionsException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;

import java.util.List;

import static pl.btsoftware.backend.shared.Currency.PLN;

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

    public Account getById(AccountId id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account updateAccount(AccountId accountId, String newName) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var existingAccount = accountRepository.findByNameAndCurrency(newName, account.balance().currency());
        if (existingAccount.isPresent() && !existingAccount.get().id().equals(account.id())) {
            throw new AccountAlreadyExistsException();
        }

        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }

    public void deleteAccount(AccountId accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.hasAnyTransaction()) {
            throw new CannotDeleteAccountWithTransactionsException();
        }

        accountRepository.deleteById(accountId);
    }

    public void addTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.addTransaction(transactionId, amount, transactionType);
        accountRepository.store(updatedAccount);
    }

    public void removeTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.removeTransaction(transactionId, amount, transactionType);
        accountRepository.store(updatedAccount);
    }

    public void changeTransaction(AccountId accountId, TransactionId transactionId, Money oldAmount, Money newAmount, TransactionType transactionType) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.changeTransaction(transactionId, oldAmount, newAmount, transactionType);
        accountRepository.store(updatedAccount);
    }
}
