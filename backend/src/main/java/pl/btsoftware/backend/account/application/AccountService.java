package pl.btsoftware.backend.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.domain.error.CannotDeleteAccountWithTransactionsException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UsersModuleFacade usersModuleFacade;

    public Account createAccount(CreateAccountCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());
        var currency = command.currency() == null ? Currency.DEFAULT : command.currency();
        accountRepository.findByNameAndCurrency(command.name(), currency, user.groupId()).ifPresent(account -> {
            throw new AccountAlreadyExistsException();
        });

        var account = command.toDomain(user);
        accountRepository.store(account);
        return account;
    }

    public List<Account> getAccounts(UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return accountRepository.findAllBy(user.groupId());
    }

    public Account getById(AccountId id, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return accountRepository.findById(id, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account getById(AccountId id, GroupId groupId) {
        return accountRepository.findById(id, groupId)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account updateAccount(AccountId accountId, String newName, GroupId groupId) {
        var account = accountRepository.findById(accountId, groupId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var existingAccount = accountRepository.findByNameAndCurrency(newName, account.balance().currency(), account.ownedBy());
        if (existingAccount.isPresent() && !existingAccount.get().id().equals(account.id())) {
            throw new AccountAlreadyExistsException();
        }

        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }

    public void deleteAccount(AccountId accountId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.hasAnyTransaction()) {
            throw new CannotDeleteAccountWithTransactionsException();
        }

        accountRepository.deleteById(accountId);
    }

    public void addTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.addTransaction(transactionId, amount, transactionType);
        accountRepository.store(updatedAccount);
    }

    public void removeTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.removeTransaction(transactionId, amount, transactionType);
        accountRepository.store(updatedAccount);
    }

    public void changeTransaction(AccountId accountId, TransactionId transactionId, Money oldAmount, Money newAmount, TransactionType transactionType, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.changeTransaction(transactionId, oldAmount, newAmount, transactionType);
        accountRepository.store(updatedAccount);
    }
}
