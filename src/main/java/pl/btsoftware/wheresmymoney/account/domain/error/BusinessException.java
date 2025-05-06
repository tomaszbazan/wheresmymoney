package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Base exception class for all business exceptions in the domain.
 * Includes an error code and a description.
 */
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;

    /**
     * Creates a new business exception with the specified error code and message.
     *
     * @param errorCode the error code
     * @param message the error message
     */
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code of this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}