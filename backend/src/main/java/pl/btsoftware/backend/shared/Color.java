package pl.btsoftware.backend.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public record Color(String value) {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public Color {
        if (isNull(value)) {
            throw new IllegalArgumentException("Color value cannot be null");
        }
        if (!HEX_COLOR_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Color must be in format #AABBCC, got: " + value);
        }
    }

    @JsonCreator
    public static Color of(String value) {
        return new Color(value);
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
