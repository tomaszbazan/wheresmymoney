package pl.btsoftware.backend.shared.validation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NameValidationRulesTest {

    private static Stream<Arguments> invalidNameTestCases() {
        return Stream.of(
                arguments(null, ValidationError.EMPTY),
                arguments("", ValidationError.EMPTY),
                arguments("   ", ValidationError.EMPTY),
                arguments("a".repeat(101), ValidationError.TOO_LONG),
                arguments("Invalid\nName", ValidationError.INVALID_CHARACTERS));
    }

    private static Stream<Arguments> validNameTestCases() {
        return Stream.of(
                arguments("ValidName"),
                arguments("Name123"),
                arguments("Name with spaces"),
                arguments("Name-with_symbols@#!?."),
                arguments("PolishĄĆĘŁŃÓŚŹŻ"),
                arguments("a".repeat(100)));
    }

    @ParameterizedTest
    @MethodSource("invalidNameTestCases")
    void shouldThrowExceptionForInvalidName(String invalidName, ValidationError expectedError) {
        var emptyException = new TestEmptyException();
        var tooLongException = new TestTooLongException();
        var invalidCharsException = new TestInvalidCharactersException();

        assertThatThrownBy(() -> NameValidationRules.validate(
                        invalidName, () -> emptyException, () -> tooLongException, () -> invalidCharsException))
                .isInstanceOf(expectedError.getExceptionClass());
    }

    @ParameterizedTest
    @MethodSource("validNameTestCases")
    void shouldAcceptValidName(String validName) {
        var emptyException = new TestEmptyException();
        var tooLongException = new TestTooLongException();
        var invalidCharsException = new TestInvalidCharactersException();

        NameValidationRules.validate(
                validName, () -> emptyException, () -> tooLongException, () -> invalidCharsException);

        assertThat(validName).isNotNull();
    }

    private static Stream<String> emptyNameTestCases() {
        return Stream.of(null, "", "   ");
    }

    private static Stream<String> tooLongNameTestCases() {
        return Stream.of("a".repeat(101));
    }

    private static Stream<String> invalidCharactersNameTestCases() {
        return Stream.of("Invalid\nName", "Name\tTab", "Name<>Special");
    }

    @ParameterizedTest
    @MethodSource("emptyNameTestCases")
    void shouldSkipEmptyValidationWhenSupplierIsNull(String emptyName) {
        var tooLongException = new TestTooLongException();
        var invalidCharsException = new TestInvalidCharactersException();

        assertThatCode(() -> NameValidationRules.validate(
                        emptyName, null, () -> tooLongException, () -> invalidCharsException))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("tooLongNameTestCases")
    void shouldSkipLengthValidationWhenSupplierIsNull(String tooLongName) {
        var emptyException = new TestEmptyException();
        var invalidCharsException = new TestInvalidCharactersException();

        assertThatCode(() -> NameValidationRules.validate(
                        tooLongName, () -> emptyException, null, () -> invalidCharsException))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidCharactersNameTestCases")
    void shouldSkipCharactersValidationWhenSupplierIsNull(String nameWithInvalidChars) {
        var emptyException = new TestEmptyException();
        var tooLongException = new TestTooLongException();

        assertThatCode(() -> NameValidationRules.validate(
                        nameWithInvalidChars, () -> emptyException, () -> tooLongException, null))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidNameTestCases")
    void shouldSkipAllValidationsWhenAllSuppliersAreNull(String invalidName, ValidationError expectedError) {
        assertThatCode(() -> NameValidationRules.validate(invalidName, null, null, null))
                .doesNotThrowAnyException();
    }

    private enum ValidationError {
        EMPTY(TestEmptyException.class),
        TOO_LONG(TestTooLongException.class),
        INVALID_CHARACTERS(TestInvalidCharactersException.class);

        private final Class<? extends RuntimeException> exceptionClass;

        ValidationError(Class<? extends RuntimeException> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public Class<? extends RuntimeException> getExceptionClass() {
            return exceptionClass;
        }
    }

    private static class TestEmptyException extends RuntimeException {}

    private static class TestTooLongException extends RuntimeException {}

    private static class TestInvalidCharactersException extends RuntimeException {}
}
