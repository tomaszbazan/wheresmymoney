package pl.btsoftware.backend.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountHasTransactionsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UsersModuleFacade usersModuleFacade;
    private final TransactionQueryFacade transactionQueryFacade;
    private final AuditModuleFacade auditModuleFacade;

    public Account createAccount(CreateAccountCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());
        var currency = command.currency() == null ? Currency.DEFAULT : command.currency();
        accountRepository.findByNameAndCurrency(command.name(), currency, user.groupId()).ifPresent(account -> {
            throw new AccountAlreadyExistsException();
        });

        var account = command.toDomain(user);
        accountRepository.store(account);
        auditModuleFacade.logAccountCreated(account.id(), account.name(), command.userId(), user.groupId());
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

        var currency = account.balance().currency();
        var existingAccount = accountRepository.findByNameAndCurrency(newName, currency, account.ownedBy());
        if (existingAccount.isPresent()) {
            throw new AccountAlreadyExistsException();
        }

        var oldName = account.name();
        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        auditModuleFacade.logAccountUpdated(accountId, oldName, newName, updatedAccount.lastUpdatedBy(), groupId);
        return updatedAccount;
    }

    public void deleteAccount(AccountId accountId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var hasTransactions = transactionQueryFacade.hasTransactions(accountId, user.groupId());
        if (hasTransactions) {
            throw new AccountHasTransactionsException();
        }

        accountRepository.deleteById(accountId);
        auditModuleFacade.logAccountDeleted(accountId, account.name(), userId, user.groupId());
    }

    public void deposit(AccountId accountId, Money amount, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.deposit(amount);
        accountRepository.store(updatedAccount);
        auditModuleFacade.logAccountDeposit(accountId, account.name(), userId, user.groupId(), amount);
    }

    public void withdraw(AccountId accountId, Money amount, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account = accountRepository.findById(accountId, user.groupId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        var updatedAccount = account.withdraw(amount);
        accountRepository.store(updatedAccount);
        auditModuleFacade.logAccountWithdraw(accountId, account.name(), userId, user.groupId(), amount);
    }
}