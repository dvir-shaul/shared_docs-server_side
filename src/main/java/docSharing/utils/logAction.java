package docSharing.utils;

public enum logAction {
    INSERT("insert"),
    DELETE("delete");
    private final String text;

    logAction(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
