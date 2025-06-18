package pl.btsoftware.backend.account.domain.error;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}