package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND", "User not found");
    }
}
