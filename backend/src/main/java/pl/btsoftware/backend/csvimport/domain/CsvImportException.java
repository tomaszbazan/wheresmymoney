package pl.btsoftware.backend.csvimport.domain;

import lombok.Getter;

@Getter
public class CsvImportException extends RuntimeException {
    private final ErrorType errorType;

    public CsvImportException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
