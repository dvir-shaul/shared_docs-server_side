package docSharing.utils;

public enum ExceptionMessage {

    MULTIPLE_PRIMARY_KEY("A table can not contain multiple primary keys."),
    MULTIPLE_AUTO_INCREMENT("A table can not contain multiple auto increment fields."),
    TRUNCATE("Couldn't truncate the table properly."),
    ILLEGAL_SQL_QUERY("Sql query is not legal."),
    DUPLICATED_UNIQUE_FIELD("Can not create a duplicate of a unique field: "),
    EMPTY_NOTNULL_FIELD("Not null fields must be filled out before creation"),
    FIELDS_OF_OBJECT("Something went wrong when tried to get object's fields..."),
    RUNTIME("Runtime exception"),
    NEGATIVE_FIELD("This field can not be negative"),
    TOO_SHORT_STRING("This field can not be as short as inserted"),
    CREATE_TABLE("Couldn't create the table properly."),
    NO_PRIMARY_KEY_FOUND("No primary key found."),
    ACCOUNT_EXISTS("This user email already exists: "),
    NOT_MATCH("Error: email or password does not match"),
    NO_ACCOUNT_IN_DATABASE("Could not locate this email in the database."),
    UNAUTHORIZED("You are unauthorized to create such a action"),
    FOLDER_EXISTS("This folder ID does not exist in the database: "),
    VALIDATION_FAILED("Could not approve the given information: ");

    private final String message;

    private ExceptionMessage(final String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }
}