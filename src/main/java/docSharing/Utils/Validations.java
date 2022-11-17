package docSharing.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

    public static void validate(String regex, String data) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (!matcher.matches())
            throw new IllegalArgumentException(ExceptionMessage.VALIDATION_FAILED.getMessage() + data);
    }
}
