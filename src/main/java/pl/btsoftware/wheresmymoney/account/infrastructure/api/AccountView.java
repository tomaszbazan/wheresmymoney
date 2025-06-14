package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import pl.btsoftware.wheresmymoney.account.domain.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountView(UUID id, String name, BigDecimal balance, String currency) {
    public static AccountView from(Account account) {
        var balance = account.balance();
        return new AccountView(account.id().value(), account.name(), balance.amount(), balance.currency());
    }
}
