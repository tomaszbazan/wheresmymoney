package pl.btsoftware.backend.csvimport.infrastructure.api;

import pl.btsoftware.backend.csvimport.domain.ErrorType;
import pl.btsoftware.backend.csvimport.domain.ParseError;

public record ParseErrorView(ErrorType type, int lineNumber, String details) {
    public static ParseErrorView from(ParseError error) {
        return new ParseErrorView(error.type(), error.lineNumber(), error.details());
    }
}
