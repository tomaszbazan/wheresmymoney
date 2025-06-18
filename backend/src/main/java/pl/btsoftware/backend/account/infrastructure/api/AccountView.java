package pl.btsoftware.backend.account.infrastructure.api;

import pl.btsoftware.backend.account.domain.Account;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountView(UUID id, String name, BigDecimal balance, String currency, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static AccountView from(Account account) {
        var balance = account.balance();
        return new AccountView(account.id().value(), account.name(), balance.amount(), balance.currency(), account.createdAt(), account.updatedAt());
    }
}
