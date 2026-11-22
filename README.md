# Athenaeum – Library Book Management System (Java Swing)

## Run (IntelliJ / NetBeans / Eclipse)
Create a Java project, copy `src/library/*.java` into it, run `library.LibraryApp` (JDK 11+).

## Command line
    javac -d out src/library/*.java
    java -cp out library.LibraryApp

## Javadoc
    javadoc -d docs -author -sourcepath src library

## Git workflow (suggested commits)
    git init
    git add src/library/Book.java src/library/BookStatus.java && git commit -m "Add Book model and status enum"
    git add src/library/LibraryException.java src/library/LibraryManager.java && git commit -m "Add LibraryManager with validation"
    git add src/library/LibraryApp.java && git commit -m "Add animated Swing GUI"
    git add README.md docs && git commit -m "Add README and generated Javadoc"
    git branch -M main
    git remote add origin <your-repo-url> && git push -u origin main
    git status ; git log --oneline ; git diff

## Test cases (expected → what to check in the UI)
| Test | Expected |
|---|---|
| Add valid book | Green toast, row slides in, stats count up |
| Empty title | Red toast "Title cannot be empty", title field shakes |
| Duplicate ID | Red toast "already exists", ID field shakes |
| View all books | All rows listed |
| Empty library (delete all) | "The library is empty" message |
| Update existing book | Details change, green toast |
| Update non-existing ID | Red toast "No book found", handled gracefully |
| Delete existing book | Row removed, counts update |
