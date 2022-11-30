package docSharing.utils;

/**
 * enum class with Exception Messages to send more accurate errors.
 */
public enum ExceptionMessage {

    MULTIPLE_PRIMARY_KEY("A table can not contain multiple primary keys."),
    MULTIPLE_AUTO_INCREMENT("A table can not contain multiple auto increment fields."),
    TRUNCATE("Couldn't truncate the table properly."),
    ILLEGAL_SQL_QUERY("Sql query is not legal."),
    DUPLICATED_UNIQUE_FIELD("Can not create a duplicate of a unique field: "),
    NULL_INPUT("cannot set the content of the argument to null"),
    EMPTY_NOTNULL_FIELD("Not null fields must be filled out before creation"),
    FIELDS_OF_OBJECT("Something went wrong when tried to get object's fields..."),
    RUNTIME("Runtime exception"),
    NEGATIVE_FIELD("This field can not be negative"),
    TOO_SHORT_STRING("This field can not be as short as inserted"),
    CREATE_TABLE("Couldn't create the table properly."),
    NO_PRIMARY_KEY_FOUND("No primary key found."),
    ACCOUNT_EXISTS("This user email already exists: "),
    ACCOUNT_DOES_NOT_EXISTS("This user email does not exists in database: "),
    ILLEGAL_AUTH_HEADER("Authorization header is not legal"),

    DOCUMENT_EXISTS("This document ID already exist in the database: "),
    DOCUMENT_DOES_NOT_EXISTS("This document ID does not exist in the database: "),
    NOT_MATCH("Error: email or password does not match"),
    NO_USER_IN_DATABASE("Could not locate this user in the database."),
    NO_ACCOUNT_IN_DATABASE("Could not locate this email in the database."),
    NO_DOCUMENT_IN_DATABASE("Could not locate this document in the database."),

    NO_FOLDER_IN_DATABASE("Could not locate this folder in the database."),

    NO_USER_IN_DOCUMENT_IN_DATABASE("Could not locate the find the user that uses this document in the database."),
    UNAUTHORIZED("You are unauthorized to create such a action"),
    FOLDER_EXISTS("This folder ID already exist in the database: "),
    FOLDER_DOES_NOT_EXISTS("This folder ID does not exist in the database: "),

    VALIDATION_FAILED("Could not approve the given information: "),
    UNAUTHORIZED_USER("You don't have the permission to do that action: "),
    WRONG_SEARCH("Something in the request wasn't properly written, try again"),

    CIRCULAR_FOLDERS("The destination folder is subfolder of the source folder"),
    CANT_ASSIGN_PERMISSION("Document ID or user ID or Permission does not exist in the database: "),
    USER_NOT_ACTIVATED("The user is not activated"),
    USER_IS_NOT_THE_ADMIN("The user is not the admin of the document");

    private final String message;

    private ExceptionMessage(final String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }
}