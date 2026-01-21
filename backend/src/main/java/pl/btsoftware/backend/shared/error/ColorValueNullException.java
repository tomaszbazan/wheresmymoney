package pl.btsoftware.backend.shared.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class ColorValueNullException extends BusinessException {
    private static final String ERROR_CODE = "COLOR_VALUE_NULL";
    private static final String MESSAGE = "Color value cannot be null";

    public ColorValueNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}
