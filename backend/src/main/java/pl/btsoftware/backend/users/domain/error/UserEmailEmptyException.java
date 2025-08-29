package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class UserEmailEmptyException extends BusinessException {
    public UserEmailEmptyException() {
        super("USER_EMAIL_EMPTY", "User email cannot be empty");
    }
}