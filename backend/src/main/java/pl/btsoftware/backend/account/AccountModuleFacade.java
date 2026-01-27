package pl.btsoftware.backend.account;

import java.util.List;
import lombok.AllArgsConstructor;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;
    private final UsersModuleFacade usersModuleFacade;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.createAccount(command);
    }

    public List<Account> getAccounts(UserId userId) {
        return accountService.getAccounts(userId);
    }

    public Account getAccount(AccountId accountId, UserId userId) {
        return accountService.getById(accountId, userId);
    }

    public Account getAccount(AccountId accountId, GroupId groupId) {
        return accountService.getById(accountId, groupId);
    }

    public Account updateAccount(UpdateAccountCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return accountService.updateAccount(command.accountId(), command.name(), user.groupId());
    }

    public void deleteAccount(AccountId accountId, UserId userId) {
        accountService.deleteAccount(accountId, userId);
    }

    public void deposit(AccountId accountId, Money amount, UserId userId) {
        accountService.deposit(accountId, amount, userId);
    }

    public void withdraw(AccountId accountId, Money amount, UserId userId) {
        accountService.withdraw(accountId, amount, userId);
    }
}
