package pl.btsoftware.backend.shared.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class ColorInvalidFormatException extends BusinessException {
    private static final String ERROR_CODE = "COLOR_INVALID_FORMAT";
    private static final String MESSAGE = "Color must be in format #AABBCC, got: %s";

    public ColorInvalidFormatException(String value) {
        super(ERROR_CODE, MESSAGE.formatted(value));
    }
}
