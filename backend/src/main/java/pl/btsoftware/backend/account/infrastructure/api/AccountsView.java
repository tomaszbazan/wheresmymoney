package pl.btsoftware.backend.account.infrastructure.api;

import pl.btsoftware.backend.account.domain.Account;

import java.util.List;

public record AccountsView(List<AccountView> accounts) {
    public static AccountsView from(List<Account> accounts) {
        return new AccountsView(accounts.stream()
                .map(AccountView::from)
                .toList());
    }
}
