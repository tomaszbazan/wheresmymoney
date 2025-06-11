package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import pl.btsoftware.wheresmymoney.account.domain.Money;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateExpenseRequest(UUID accountId, BigDecimal amount, String description, OffsetDateTime date,
                                   String currency) {
    public CreateExpenseRequest {
        if (currency == null || currency.isBlank()) {
            currency = Money.DEFAULT_CURRENCY;
        }
    }

    public CreateExpenseRequest(UUID accountId, BigDecimal amount, String description, OffsetDateTime date) {
        this(accountId, amount, description, date, Money.DEFAULT_CURRENCY);
    }
}
