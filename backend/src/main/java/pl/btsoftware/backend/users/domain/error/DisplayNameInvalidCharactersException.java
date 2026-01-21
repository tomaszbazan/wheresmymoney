package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class DisplayNameInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "DISPLAY_NAME_INVALID_CHARACTERS";
    private static final String MESSAGE = "Display name contains invalid characters";

    public DisplayNameInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
