package library;

/**
 * A single library book.
 * @author OEL Group
 */
public class Book {
    private final String id;
    private String title;
    private String author;
    private BookStatus status;

    /**
     * Creates a book.
     * @param id     unique identifier
     * @param title  book title
     * @param author book author
     * @param status initial status
     */
    public Book(String id, String title, String author, BookStatus status) {
        this.id = id; this.title = title; this.author = author; this.status = status;
    }

    /** @return the unique ID */
    public String getId() { return id; }
    /** @return the title */
    public String getTitle() { return title; }
    /** @param title new title */
    public void setTitle(String title) { this.title = title; }
    /** @return the author */
    public String getAuthor() { return author; }
    /** @param author new author */
    public void setAuthor(String author) { this.author = author; }
    /** @return the current status */
    public BookStatus getStatus() { return status; }
    /** @param status new status */
    public void setStatus(BookStatus status) { this.status = status; }

    @Override
    public String toString() { return id + " | " + title + " | " + author + " | " + status.getLabel(); }
}
