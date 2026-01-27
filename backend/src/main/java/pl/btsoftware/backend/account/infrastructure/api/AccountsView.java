package pl.btsoftware.backend.account.infrastructure.api;

import java.util.List;
import pl.btsoftware.backend.account.domain.Account;

public record AccountsView(List<AccountView> accounts) {
    public AccountsView(List<AccountView> accounts) {
        this.accounts = accounts != null ? List.copyOf(accounts) : List.of();
    }

    public static AccountsView from(List<Account> accounts) {
        return new AccountsView(accounts.stream().map(AccountView::from).toList());
    }
}
