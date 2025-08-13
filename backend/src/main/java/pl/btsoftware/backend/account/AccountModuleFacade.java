package pl.btsoftware.backend.account;

import lombok.AllArgsConstructor;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;

import java.util.List;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.createAccount(command);
    }

    public List<Account> getAccounts() {
        return accountService.getAccounts();
    }

    public Account getAccount(AccountId accountId) {
        return accountService.getById(accountId);
    }

    public Account updateAccount(UpdateAccountCommand command) {
        return accountService.updateAccount(command.accountId(), command.name());
    }

    public void deleteAccount(AccountId accountId) {
        accountService.deleteAccount(accountId);
    }

    public void addTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType) {
        accountService.addTransaction(accountId, transactionId, amount, transactionType);
    }

    public void removeTransaction(AccountId accountId, TransactionId transactionId, Money amount, TransactionType transactionType) {
        accountService.removeTransaction(accountId, transactionId, amount, transactionType);
    }

    public void changeTransaction(AccountId accountId, TransactionId transactionId, Money oldAmount, Money newAmount, TransactionType transactionType) {
        accountService.changeTransaction(accountId, transactionId, oldAmount, newAmount, transactionType);
    }
}
