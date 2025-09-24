package pl.btsoftware.backend.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColorTest {

    @Test
    void shouldCreateValidColorWithUpperCaseHex() {
        // given & when
        var color = Color.of("#AABBCC");

        // then
        assertThat(color.value()).isEqualTo("#AABBCC");
        assertThat(color.toString()).isEqualTo("#AABBCC");
    }

    @Test
    void shouldCreateValidColorWithLowerCaseHex() {
        // given & when
        var color = Color.of("#aabbcc");

        // then
        assertThat(color.value()).isEqualTo("#aabbcc");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#000000",
            "#FFFFFF",
            "#123456",
            "#abcdef",
            "#ABCDEF",
            "#9a8b7c"
    })
    void shouldAcceptValidHexColors(String colorValue) {
        // when & then
        var color = Color.of(colorValue);
        assertThat(color.value()).isEqualTo(colorValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "AABBCC",      // missing #
            "#AABBG",      // invalid character G (too short)
            "#AABBCCDD",   // too long
            "#AAB",        // too short
            "#GGHHII",     // invalid characters
            "",            // empty
            " #AABBCC",    // leading space
            "#AABBCC ",    // trailing space
            "#aabbcc\n",   // newline
            "rgb(255,255,255)", // not hex format
            "red",         // color name
            "#"            // just hash
    })
    void shouldRejectInvalidHexColors(String invalidColor) {
        // when & then
        assertThatThrownBy(() -> Color.of(invalidColor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Color must be in format #AABBCC, got: " + invalidColor);
    }

    @Test
    void shouldRejectNullColor() {
        // when & then
        assertThatThrownBy(() -> Color.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Color value cannot be null");
    }

    @Test
    void shouldHaveCorrectEquality() {
        // given
        var color1 = Color.of("#FF0000");
        var color2 = Color.of("#FF0000");
        var color3 = Color.of("#00FF00");

        // then
        assertThat(color1).isEqualTo(color2);
        assertThat(color1).isNotEqualTo(color3);
        assertThat(color1.hashCode()).isEqualTo(color2.hashCode());
    }
}
