package pl.btsoftware.backend.shared.validation;

import java.util.regex.Pattern;

public class NameValidator {
    // Allow: letters, digits, polish chars, space, -, _, ., @, #, !, ?, :, comma
    public static final Pattern VALID_CHARACTERS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9ąĄćĆęĘłŁńŃóÓśŚźŹżŻ \\-_@#!?\\.:,]+$");
    public static final Pattern ALL_NON_VALID_CHARACTERS_PATTERN =
            Pattern.compile("[^a-zA-Z0-9ąĄćĆęĘłŁńŃóÓśŚźŹżŻ \\-_@#!?\\.:,]");

    public static boolean isValid(String name) {
        if (name == null) {
            return false;
        }
        return VALID_CHARACTERS_PATTERN.matcher(name).matches();
    }
}
