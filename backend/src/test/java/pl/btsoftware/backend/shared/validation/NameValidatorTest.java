package pl.btsoftware.backend.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NameValidatorTest {

    @Test
    void shouldReturnFalseForNullName() {
        assertThat(NameValidator.isValid(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ValidName",
                "Valid Name",
                "Valid Name 123",
                "Valid-Name",
                "Valid_Name",
                "Valid.Name",
                "Valid@Name",
                "Valid#Name",
                "Valid!Name",
                "Valid?Name",
                "ąĄćĆęĘłŁńŃóÓśŚźŹżŻ",
                "Konto Główne",
                "Test-Account_123",
                "Account@Email.com",
                "Budget#2024"
            })
    void shouldReturnTrueForValidNames(String name) {
        assertThat(NameValidator.isValid(name)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Invalid\nName",
                "Invalid\tName",
                "Invalid\\Name",
                "Invalid/Name",
                "Invalid<Name",
                "Invalid>Name",
                "Invalid|Name",
                "Invalid;Name",
                "Invalid:Name",
                "Invalid'Name",
                "Invalid\"Name",
                "Invalid*Name",
                "Invalid&Name",
                "Invalid%Name",
                "Invalid$Name",
                "Invalid+Name",
                "Invalid=Name",
                "Invalid[Name",
                "Invalid]Name",
                "Invalid{Name",
                "Invalid}Name"
            })
    void shouldReturnFalseForInvalidNames(String name) {
        assertThat(NameValidator.isValid(name)).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyString() {
        assertThat(NameValidator.isValid("")).isFalse();
    }

    @Test
    void shouldReturnTrueForSingleCharacter() {
        assertThat(NameValidator.isValid("A")).isTrue();
    }

    @Test
    void shouldReturnTrueForNumbersOnly() {
        assertThat(NameValidator.isValid("12345")).isTrue();
    }

    @Test
    void shouldReturnTrueForPolishCharacters() {
        assertThat(NameValidator.isValid("Łódź")).isTrue();
        assertThat(NameValidator.isValid("Kraków")).isTrue();
        assertThat(NameValidator.isValid("Żywiec")).isTrue();
    }

    @Test
    void shouldReturnTrueForMixedValidCharacters() {
        assertThat(NameValidator.isValid("Account-123_Test@Home.pl!?#")).isTrue();
    }
}
