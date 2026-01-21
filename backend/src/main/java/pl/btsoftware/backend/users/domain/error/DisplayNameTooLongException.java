package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class DisplayNameTooLongException extends BusinessException {
    private static final String ERROR_CODE = "DISPLAY_NAME_TOO_LONG";
    private static final String MESSAGE = "Display name cannot be longer than 100 characters";

    public DisplayNameTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
