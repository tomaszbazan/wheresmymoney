package pl.btsoftware.backend.users.domain.error;

public class DisplayNameEmptyException extends UserBusinessException {
    public DisplayNameEmptyException() {
        super("Display name cannot be empty");
    }
}