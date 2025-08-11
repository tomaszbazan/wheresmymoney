package pl.btsoftware.backend.users.domain.error;

public abstract class UserBusinessException extends RuntimeException {
    protected UserBusinessException(String message) {
        super(message);
    }
}