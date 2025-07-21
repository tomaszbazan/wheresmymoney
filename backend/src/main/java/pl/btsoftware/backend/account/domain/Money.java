package pl.btsoftware.backend.account.domain;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static pl.btsoftware.backend.account.domain.Currency.DEFAULT;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        // Ensure amount has exactly 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add balance with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract balance with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money withCurrency(Currency newCurrency) {
        return new Money(this.amount, newCurrency);
    }

    public Money withAmount(BigDecimal newAmount) {
        return new Money(newAmount, this.currency);
    }

    @NotNull
    @Override
    public String toString() {
        return amount.toString() + " " + currency;
    }
}
