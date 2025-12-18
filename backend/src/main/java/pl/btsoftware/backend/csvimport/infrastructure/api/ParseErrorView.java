package pl.btsoftware.backend.csvimport.infrastructure.api;

import pl.btsoftware.backend.csvimport.domain.ParseError;

public record ParseErrorView(int lineNumber, String message) {
    public static ParseErrorView from(ParseError error) {
        return new ParseErrorView(error.lineNumber(), error.message());
    }
}
