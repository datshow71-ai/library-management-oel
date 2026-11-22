package library;

/**
 * Signals a validation or lookup problem in the library.
 * @author OEL Group
 */
public class LibraryException extends Exception {
    private final String field;

    /**
     * @param message user-facing description of the problem
     * @param field   the form field responsible ("id", "title", "author")
     */
    public LibraryException(String message, String field) {
        super(message);
        this.field = field;
    }

    /** @return the name of the offending form field */
    public String getField() { return field; }
}
