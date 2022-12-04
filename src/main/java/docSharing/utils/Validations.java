package docSharing.utils;

import docSharing.entity.GeneralItem;
import docSharing.service.AuthService;
import io.jsonwebtoken.Claims;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

    public static void validate(String regex, String data) throws NullPointerException, IllegalArgumentException{
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

//    public static Boolean validateAction(AuthService service, GeneralItem item, String token) {
//        Long userId;
//        try {
//            userId = service.validateToken(token);
//        } catch (NullPointerException e) {
//            return false;
//        }
//        if (userId != item.getUserId()) return false;
//        return true;
//    }

    public static Long validateToken(String token) {
        if (token == null) {
            throw new NullPointerException(ExceptionMessage.NULL_INPUT.toString());
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7, token.length());
        } else {
            throw new IllegalArgumentException(ExceptionMessage.ILLEGAL_AUTH_HEADER.toString());
        }
        Claims claims = ConfirmationToken.decodeJWT(token);
        return Long.valueOf(claims.getId());
    }
}
