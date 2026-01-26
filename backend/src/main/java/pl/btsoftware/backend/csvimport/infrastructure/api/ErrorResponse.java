package pl.btsoftware.backend.csvimport.infrastructure.api;

import pl.btsoftware.backend.csvimport.domain.ErrorType;

public record ErrorResponse(ErrorType errorType, String message) {}
