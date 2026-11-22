package library;

import java.util.*;

/**
 * Holds the book collection and enforces validation rules.
 * @author OEL Group
 */
public class LibraryManager {
    private final Map<String, Book> books = new LinkedHashMap<>();

    /**
     * Adds a new book after validating the input.
     * @param id     unique ID (must not be empty or duplicate)
     * @param title  title (must not be empty)
     * @param author author (must not be empty)
     * @param status initial status
     * @throws LibraryException if validation fails
     */
    public void addBook(String id, String title, String author, BookStatus status) throws LibraryException {
        id = clean(id);
        if (id.isEmpty()) throw new LibraryException("Book ID is required.", "id");
        if (books.containsKey(id)) throw new LibraryException("A book with ID '" + id + "' already exists.", "id");
        validate(title, author);
        books.put(id, new Book(id, clean(title), clean(author), status));
    }

    /**
     * Updates title and author of an existing book.
     * @param id     ID of the book to update
     * @param title  new title
     * @param author new author
     * @throws LibraryException if the book does not exist or input is invalid
     */
    public void updateBook(String id, String title, String author) throws LibraryException {
        Book b = require(id);
        validate(title, author);
        b.setTitle(clean(title));
        b.setAuthor(clean(author));
    }

    /**
     * Changes the status of a book.
     * @param id     ID of the book
     * @param status new status
     * @throws LibraryException if the book does not exist
     */
    public void setStatus(String id, BookStatus status) throws LibraryException {
        require(id).setStatus(status);
    }

    /**
     * Deletes a book.
     * @param id ID of the book
     * @throws LibraryException if the book does not exist
     */
    public void deleteBook(String id) throws LibraryException {
        require(id);
        books.remove(clean(id));
    }

    /** @return all books in insertion order */
    public List<Book> getAllBooks() { return new ArrayList<>(books.values()); }

    /**
     * Counts books with a given status.
     * @param s status to count
     * @return number of matching books
     */
    public int count(BookStatus s) {
        int n = 0;
        for (Book b : books.values()) if (b.getStatus() == s) n++;
        return n;
    }

    private Book require(String id) throws LibraryException {
        Book b = books.get(clean(id));
        if (b == null) throw new LibraryException("No book found with ID '" + clean(id) + "'.", "id");
        return b;
    }

    private void validate(String title, String author) throws LibraryException {
        if (clean(title).isEmpty()) throw new LibraryException("Title cannot be empty.", "title");
        if (clean(author).isEmpty()) throw new LibraryException("Author cannot be empty.", "author");
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
