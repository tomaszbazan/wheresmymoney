package pl.btsoftware.wheresmymoney.account;

import lombok.AllArgsConstructor;
import pl.btsoftware.wheresmymoney.account.application.AccountService;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.domain.AccountId;

import java.util.List;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;

    public void createAccount(CreateAccountCommand command) {
        accountService.createAccount(command);
    }

    public List<Account> getAccounts() {
        return accountService.getAccounts();
    }

    public record CreateAccountCommand(String name) {
        public CreateAccountCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Account name cannot be null or blank");
            }
        }

        public Account toDomain() {
            return new Account(AccountId.generate(), name);
        }
    }
}
