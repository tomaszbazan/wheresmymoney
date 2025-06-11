package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import pl.btsoftware.wheresmymoney.account.domain.Money;

public record CreateAccountRequest(String name, String currency) {
    public CreateAccountRequest {
        if (currency == null || currency.isBlank()) {
            currency = Money.DEFAULT_CURRENCY;
        }
    }

    public CreateAccountRequest(String name) {
        this(name, Money.DEFAULT_CURRENCY);
    }
}
