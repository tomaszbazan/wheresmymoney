package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategorizationPromptTest {

    @Test
    void shouldRejectNullJsonPrompt() {
        assertThatThrownBy(() -> new CategorizationPrompt(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlankJsonPrompt() {
        assertThatThrownBy(() -> new CategorizationPrompt(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CategorizationPrompt("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptValidJsonPrompt() {
        var validPrompt = "{\"test\": \"value\"}";
        var prompt = new CategorizationPrompt(validPrompt);
        assertThat(prompt.jsonPrompt()).isEqualTo(validPrompt);
    }
}
