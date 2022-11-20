package docSharing.utils;

import docSharing.entity.File;
import docSharing.service.AuthService;

import javax.naming.AuthenticationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

    public static void validate(String regex, String data) {
        if (data == null)
            throw new NullPointerException(ExceptionMessage.EMPTY_NOTNULL_FIELD.toString());

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        if (!matcher.matches())
            throw new IllegalArgumentException(ExceptionMessage.VALIDATION_FAILED.toString() + data);
    }

//    public static void authorizeTokenToUser(Long userIdByToken, Long requestedUserId) throws AuthenticationException {
//        if (userIdByToken != requestedUserId)
//            throw new AuthenticationException(ExceptionMessage.UNAUTHORIZED.toString());
//    }

    public static Boolean validateAction(AuthService service, File item, String token) {
        Long userId;
        try {
            userId = service.validateToken(token);
        } catch (NullPointerException e) {
            return false;
        }
        if (userId != item.getUserId()) return false;
        return true;
    }
}
