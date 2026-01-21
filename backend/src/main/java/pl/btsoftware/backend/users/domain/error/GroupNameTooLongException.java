package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class GroupNameTooLongException extends BusinessException {
    private static final String ERROR_CODE = "GROUP_NAME_TOO_LONG";
    private static final String MESSAGE = "Group name cannot be longer than 100 characters";

    public GroupNameTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
