package pl.btsoftware.backend.account;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static pl.btsoftware.backend.account.domain.AccountId.generate;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.createAccount(command);
    }

    public List<Account> getAccounts() {
        return accountService.getAccounts();
    }

    public Account getAccount(UUID accountId) {
        return accountService.getById(accountId);
    }

    public record CreateAccountCommand(String name, @Nullable Currency currency) {
        public Account toDomain() {
            return new Account(generate(), name, currency);
        }
    }

    public record UpdateAccountCommand(UUID accountId, String name) {
        public UpdateAccountCommand {
            if (accountId == null) {
                throw new IllegalArgumentException("Account id cannot be null");
            }
            if (name == null || name.isBlank()) {
                throw new AccountNameEmptyException();
            }
        }
    }

    public Account updateAccount(UpdateAccountCommand command) {
        return accountService.updateAccount(command.accountId(), command.name());
    }

    public void deleteAccount(UUID accountId) {
        accountService.deleteAccount(accountId);
    }

    public void addTransaction(UUID accountId, BigDecimal amount, String transactionType) {
        accountService.addTransaction(accountId, amount, transactionType);
    }
}
