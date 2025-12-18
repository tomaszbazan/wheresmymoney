package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParseErrorTest {

    @Test
    void shouldCreateParseError() {
        // given
        var lineNumber = 5;
        var message = "Invalid date format";

        // when
        var error = new ParseError(lineNumber, message);

        // then
        assertThat(error.lineNumber()).isEqualTo(5);
        assertThat(error.message()).isEqualTo("Invalid date format");
    }

    @Test
    void shouldRejectNullMessage() {
        // when & then
        assertThatThrownBy(() -> new ParseError(1, null))
                .isInstanceOf(NullPointerException.class);
    }
}
