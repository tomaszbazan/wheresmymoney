package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class GroupNameEmptyException extends BusinessException {
    public GroupNameEmptyException() {
        super("GROUP_NAME_EMPTY", "Group name cannot be empty");
    }
}
