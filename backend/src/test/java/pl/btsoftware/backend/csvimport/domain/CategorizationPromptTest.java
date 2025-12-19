package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategorizationPromptTest {

    @Test
    void shouldRejectNullJsonPrompt() {
        assertThrows(NullPointerException.class, () -> new CategorizationPrompt(null));
    }

    @Test
    void shouldRejectBlankJsonPrompt() {
        assertThrows(IllegalArgumentException.class, () -> new CategorizationPrompt(""));
        assertThrows(IllegalArgumentException.class, () -> new CategorizationPrompt("   "));
    }

    @Test
    void shouldAcceptValidJsonPrompt() {
        var validPrompt = "{\"test\": \"value\"}";
        var prompt = new CategorizationPrompt(validPrompt);
        assertEquals(validPrompt, prompt.jsonPrompt());
    }
}
