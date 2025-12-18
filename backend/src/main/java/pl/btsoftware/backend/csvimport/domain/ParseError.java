package pl.btsoftware.backend.csvimport.domain;

import static java.util.Objects.requireNonNull;

public record ParseError(int lineNumber, String message) {
    public ParseError {
        requireNonNull(message, "Error message cannot be null");
    }
}
