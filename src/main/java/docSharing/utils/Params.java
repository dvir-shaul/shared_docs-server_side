package docSharing.utils;

/**
 * enum class with the parameters that will be sent as parameter in a given url
 * this enum class is used in the filter permission process.
 */
public enum Params {
    FOLDER_ID("id"),
    PARENT_FOLDER_ID("parentFolderId"),
    DOCUMENT_ID("documentId");
//    FOLDER_ID("id");


    private final String text;
    Params(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
    }
