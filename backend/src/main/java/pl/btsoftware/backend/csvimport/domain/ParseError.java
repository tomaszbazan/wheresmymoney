package pl.btsoftware.backend.csvimport.domain;

import static java.util.Objects.requireNonNull;

public record ParseError(ErrorType type, int lineNumber, String details) {
    public ParseError {
        requireNonNull(type, "Error type cannot be null");
        requireNonNull(details, "Details cannot be null");
    }
}
