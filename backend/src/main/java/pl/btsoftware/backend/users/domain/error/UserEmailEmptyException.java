package pl.btsoftware.backend.users.domain.error;

public class UserEmailEmptyException extends UserBusinessException {
    public UserEmailEmptyException() {
        super("User email cannot be empty");
    }
}