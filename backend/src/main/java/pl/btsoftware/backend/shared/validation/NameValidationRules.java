package pl.btsoftware.backend.shared.validation;

import java.util.function.Supplier;

public final class NameValidationRules {
    private static final int MAX_NAME_LENGTH = 100;

    private NameValidationRules() {
    }

    public static void validate(
            String name,
            Supplier<RuntimeException> emptyExceptionSupplier,
            Supplier<RuntimeException> tooLongExceptionSupplier,
            Supplier<RuntimeException> invalidCharactersExceptionSupplier
    ) {
        validateNotEmpty(name, emptyExceptionSupplier);
        validateLength(name, tooLongExceptionSupplier);
        validateCharacters(name, invalidCharactersExceptionSupplier);
    }

    private static void validateNotEmpty(String name, Supplier<RuntimeException> exceptionSupplier) {
        if (exceptionSupplier == null) {
            return;
        }
        if (name == null || name.isBlank()) {
            throw exceptionSupplier.get();
        }
    }

    private static void validateLength(String name, Supplier<RuntimeException> exceptionSupplier) {
        if (exceptionSupplier == null || name == null) {
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw exceptionSupplier.get();
        }
    }

    private static void validateCharacters(String name, Supplier<RuntimeException> exceptionSupplier) {
        if (exceptionSupplier == null || name == null || name.isEmpty()) {
            return;
        }
        if (!NameValidator.isValid(name)) {
            throw exceptionSupplier.get();
        }
    }
}
