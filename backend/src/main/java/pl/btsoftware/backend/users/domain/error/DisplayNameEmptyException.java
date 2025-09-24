package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class DisplayNameEmptyException extends BusinessException {
    public DisplayNameEmptyException() {
        super("DISPLAY_NAME_EMPTY", "Display name cannot be empty");
    }
}
