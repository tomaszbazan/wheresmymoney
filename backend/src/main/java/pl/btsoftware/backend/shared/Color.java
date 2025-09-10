package pl.btsoftware.backend.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record Color(String value) {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public Color {
        if (value == null) {
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

    @NotNull
    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}