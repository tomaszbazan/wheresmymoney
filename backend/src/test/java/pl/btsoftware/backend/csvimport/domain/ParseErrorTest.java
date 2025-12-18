package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParseErrorTest {

    @Test
    void shouldCreateParseError() {
        var error = new ParseError(5, "Invalid date format");

        assertEquals(5, error.lineNumber());
        assertEquals("Invalid date format", error.message());
    }

    @Test
    void shouldRejectNullMessage() {
        assertThrows(NullPointerException.class, () -> new ParseError(1, null));
    }
}
