package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CannotRemoveLastGroupMemberException extends BusinessException {
    private static final String ERROR_CODE = "CANNOT_REMOVE_LAST_GROUP_MEMBER";
    private static final String MESSAGE = "Cannot remove last member from group";

    public CannotRemoveLastGroupMemberException() {
        super(ERROR_CODE, MESSAGE);
    }
}
