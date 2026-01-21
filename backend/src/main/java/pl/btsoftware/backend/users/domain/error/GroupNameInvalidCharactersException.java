package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class GroupNameInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "GROUP_NAME_INVALID_CHARACTERS";
    private static final String MESSAGE = "Group name contains invalid characters";

    public GroupNameInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
