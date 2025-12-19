package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.csvimport.domain.ErrorType.INVALID_DATE_FORMAT;

class ParseErrorTest {

    @Test
    void shouldCreateParseErrorWithAllParameters() {
        // given
        var type = INVALID_DATE_FORMAT;
        var lineNumber = 5;
        var details = "Invalid date format: 2023-13-45";

        // when
        var error = new ParseError(type, lineNumber, details);

        // then
        assertThat(error.type()).isEqualTo(INVALID_DATE_FORMAT);
        assertThat(error.lineNumber()).isEqualTo(5);
        assertThat(error.details()).isEqualTo("Invalid date format: 2023-13-45");
    }

    @Test
    void shouldRejectNullErrorType() {
        // when & then
        assertThatThrownBy(() -> new ParseError(null, 1, "details"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullDetails() {
        // when & then
        assertThatThrownBy(() -> new ParseError(INVALID_DATE_FORMAT, 1, null))
                .isInstanceOf(NullPointerException.class);
    }
}
