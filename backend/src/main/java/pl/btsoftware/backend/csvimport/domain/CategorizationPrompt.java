package pl.btsoftware.backend.csvimport.domain;

import static java.util.Objects.requireNonNull;

public record CategorizationPrompt(String jsonPrompt) {
    public CategorizationPrompt {
        requireNonNull(jsonPrompt, "JSON prompt cannot be null");
        if (jsonPrompt.isBlank()) {
            throw new IllegalArgumentException("JSON prompt cannot be blank");
        }
    }
}
