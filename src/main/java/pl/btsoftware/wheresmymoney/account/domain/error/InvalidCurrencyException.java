package pl.btsoftware.wheresmymoney.account.domain.error;

import pl.btsoftware.wheresmymoney.account.domain.Money;

public class InvalidCurrencyException extends BusinessException {
    private static final String ERROR_CODE = "INVALID_CURRENCY";
    private static final String MESSAGE = "Invalid currency. Supported currencies are: %s";

    public InvalidCurrencyException() {
        super(ERROR_CODE, MESSAGE.formatted(String.join(", ", Money.SUPPORTED_CURRENCIES)));
    }
}