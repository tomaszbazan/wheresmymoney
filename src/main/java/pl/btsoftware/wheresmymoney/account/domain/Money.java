package pl.btsoftware.wheresmymoney.account.domain;

import org.jetbrains.annotations.NotNull;
import pl.btsoftware.wheresmymoney.account.domain.error.InvalidCurrencyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

    public static final String DEFAULT_CURRENCY = "PLN";
    public static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("PLN", "EUR", "USD", "GBP");

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new InvalidCurrencyException();
        }

        // Ensure amount has exactly 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money of(BigDecimal amount, String currency) {
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

    public Money withCurrency(String newCurrency) {
        if (!SUPPORTED_CURRENCIES.contains(newCurrency)) {
            throw new IllegalArgumentException("Unsupported currency: " + newCurrency + ". Supported currencies are: " + SUPPORTED_CURRENCIES);
        }
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
