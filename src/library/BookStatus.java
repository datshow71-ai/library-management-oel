package library;

/**
 * Lending status of a book.
 * @author OEL Group
 */
public enum BookStatus {
    AVAILABLE("Available"), ISSUED("Issued"), RESERVED("Reserved");

    private final String label;

    BookStatus(String label) { this.label = label; }

    /** @return human-readable label */
    public String getLabel() { return label; }
}
