package pl.btsoftware.backend.csvimport.domain;

public class CsvValidationException extends CsvParsingException {
    public CsvValidationException(String message) {
        super(message);
    }
}
