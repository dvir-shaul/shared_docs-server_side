package docSharing.utils;

import docSharing.entity.Permission;
import docSharing.entity.User;
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
     *
     * @param regex - the type we check on from email or password.
     * @param data  - the input to check on.
     */
    public static void validate(String regex, String data) throws IllegalArgumentException, NullPointerException {
        logger.info("in Validations -> validate");

        if (data == null) {
            logger.error("in Validations -> validate -> " + ExceptionMessage.EMPTY_NOTNULL_FIELD);
            throw new NullPointerException(ExceptionMessage.EMPTY_NOTNULL_FIELD.toString());
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        if (!matcher.matches()) {
            logger.error("in Validations -> validate -> " + ExceptionMessage.VALIDATION_FAILED);
            throw new IllegalArgumentException(ExceptionMessage.VALIDATION_FAILED.toString() + data);
        }
    }

    /**
     * validateWrongInputRegister gets called when a new user is register to the app
     * checks if we got all the parameters we need to make register.
     *
     * @param user - a user who trying register to the app.
     * @return - true if one of the parameters is wrong
     */
    public static boolean validateWrongInputRegister(User user) {
        return user.getName() == null || user.getEmail() == null || user.getPassword() == null || user.getId() != null;
    }
    /**
     * validateWrongInputRegister gets called when a new user is register to the app
     * checks if we got all the parameters we need to make register.
     *
     * @param user - a user who trying register to the app.
     * @return - true if one of the parameters is wrong
     */

    /**
     * validateWrongPermissionChange checks if permission input was null.
     * @param permission - new permission
     * @return permission == null
     */
    public static boolean validateWrongPermissionChange( Permission permission) {
        return permission == null;
    }
    /**
     * validateWrongName checks if the name we have is either null or the length is 0.
     *
     * @param name - name to check on.
     * @return - true if name was wrong input.
     */
    public static boolean validateWrongName(String name) {
        return name == null || name.length() == 0;
    }

    /**
     * validateIdNull is checking if the given id is null
     *
     * @param id - id to check.
     * @return id == null
     */
    public static boolean validateIdNull(Long id) {
        return id == null;
    }


    /**
     * validateToken is a function that check given token if it actual valid token and return id.
     *
     * @param token - the token input.
     * @return - id of user.
     */
    public static Long validateToken(String token) {
        logger.info("in Validations -> validateToken");

        if (token == null) {
            logger.error("in Validations -> validateToken -> " + ExceptionMessage.NULL_INPUT);
            throw new NullPointerException(ExceptionMessage.NULL_INPUT.toString());
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7, token.length());
        } else {
            logger.error("in Validations -> validateToken -> " + ExceptionMessage.ILLEGAL_AUTH_HEADER);
            throw new IllegalArgumentException(ExceptionMessage.ILLEGAL_AUTH_HEADER.toString());
        }
        Claims claims = ConfirmationToken.decodeJWT(token);
        return Long.valueOf(claims.getId());
    }
}
