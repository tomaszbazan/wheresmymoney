package pl.btsoftware.backend.account.domain.error;

public class AccountAccessDeniedException extends BusinessException {
    public AccountAccessDeniedException() {
        super("ACCOUNT_ACCESS_DENIED", "Access denied to account");
    }
}