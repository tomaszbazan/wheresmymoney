package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class GroupMustHaveAtLeastOneMemberException extends BusinessException {
    private static final String ERROR_CODE = "GROUP_MUST_HAVE_AT_LEAST_ONE_MEMBER";
    private static final String MESSAGE = "Group must have at least one member";

    public GroupMustHaveAtLeastOneMemberException() {
        super(ERROR_CODE, MESSAGE);
    }
}
