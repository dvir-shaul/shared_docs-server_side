package docSharing.utils;

public enum Regex {
    NAME("([a-zA-ZÀ-ÿ][-,a-z. ']+[ ]*)+"),

    PHONE_NUMBER("/^05\\d([-]{0,1})\\d{7}$/"),
    EMAIL("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"),
    PASSWORD("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"),
    ID("^[0-9]*"),
    FILE_NAME("^[A-za-z0-9 .]{1,255}$");

    private final String regex;

    private Regex(final String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
