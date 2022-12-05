package docSharing.utils;

import io.jsonwebtoken.Claims;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {
    private static Logger logger = LogManager.getLogger(Validations.class.getName());

    /**
     * validate is called from AuthController when we need to validate a given input such as email or password,
     * the validation process is according to enum regex we created.
     * @param regex - the type we check on from email or password.
     * @param data - the input to check on.
     */
    public static void validate(String regex, String data) throws IllegalArgumentException,NullPointerException {
        logger.info("in Validations -> validate");

        if (data == null){
            logger.error("in Validations -> validate -> "+ExceptionMessage.EMPTY_NOTNULL_FIELD);
            throw new NullPointerException(ExceptionMessage.EMPTY_NOTNULL_FIELD.toString());
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        if (!matcher.matches()){
            logger.error("in Validations -> validate -> "+ExceptionMessage.VALIDATION_FAILED);
            throw new IllegalArgumentException(ExceptionMessage.VALIDATION_FAILED.toString() + data);
        }
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

    /**
     * validateToken is a function that check given token if it actual valid token and return id.
     * @param token - the token input.
     * @return - id of user.
     */
    public static Long validateToken(String token) {
        logger.info("in Validations -> validateToken");

        if (token == null) {
            logger.error("in Validations -> validateToken -> "+ExceptionMessage.NULL_INPUT);
            throw new NullPointerException(ExceptionMessage.NULL_INPUT.toString());
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7, token.length());
        } else {
            logger.error("in Validations -> validateToken -> "+ExceptionMessage.ILLEGAL_AUTH_HEADER);
            throw new IllegalArgumentException(ExceptionMessage.ILLEGAL_AUTH_HEADER.toString());
        }
        Claims claims = ConfirmationToken.decodeJWT(token);
        return Long.valueOf(claims.getId());
    }
}
