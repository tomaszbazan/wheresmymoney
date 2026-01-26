package pl.btsoftware.backend.shared.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class InvalidExchangeRateException extends BusinessException {
    private static final String ERROR_CODE = "INVALID_EXCHANGE_RATE";
    private static final String MESSAGE = "Exchange rate must be positive";

    public InvalidExchangeRateException() {
        super(ERROR_CODE, MESSAGE);
    }
}
