package pl.btsoftware.backend.users.domain.error;

public class GroupNameEmptyException extends UserBusinessException {
    public GroupNameEmptyException() {
        super("Group name cannot be empty");
    }
}