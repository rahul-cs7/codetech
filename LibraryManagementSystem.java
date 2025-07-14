import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibraryManagementSystem extends JFrame {
    Connection conn;

    JTextField bookIdField, bookTitleField, userIdField, issueDateField, returnDateField;
    JTextArea outputArea;

    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // UI Setup
        JPanel inputPanel = new JPanel(new GridLayout(7, 2));
        bookIdField = new JTextField();
        bookTitleField = new JTextField();
        userIdField = new JTextField();
        issueDateField = new JTextField(LocalDate.now().toString());
        returnDateField = new JTextField(LocalDate.now().plusDays(7).toString());

        inputPanel.add(new JLabel("Book ID:"));
        inputPanel.add(bookIdField);
        inputPanel.add(new JLabel("Book Title:"));
        inputPanel.add(bookTitleField);
        inputPanel.add(new JLabel("User ID:"));
        inputPanel.add(userIdField);
        inputPanel.add(new JLabel("Issue Date (yyyy-mm-dd):"));
        inputPanel.add(issueDateField);
        inputPanel.add(new JLabel("Return Date (yyyy-mm-dd):"));
        inputPanel.add(returnDateField);

        JButton addBookBtn = new JButton("Add Book");
        JButton issueBookBtn = new JButton("Issue Book");
        JButton returnBookBtn = new JButton("Return Book");

        inputPanel.add(addBookBtn);
        inputPanel.add(issueBookBtn);
        inputPanel.add(returnBookBtn);

        add(inputPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // DB Setup
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS books (id TEXT PRIMARY KEY, title TEXT, available INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS issued_books (book_id TEXT, user_id TEXT, issue_date TEXT, return_date TEXT)");
        } catch (SQLException e) {
            show("Database error: " + e.getMessage());
        }

        // Button Actions
        addBookBtn.addActionListener(e -> addBook());
        issueBookBtn.addActionListener(e -> issueBook());
        returnBookBtn.addActionListener(e -> returnBook());

        setVisible(true);
    }

    void addBook() {
        String id = bookIdField.getText();
        String title = bookTitleField.getText();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO books VALUES (?, ?, 1)");
            ps.setString(1, id);
            ps.setString(2, title);
            ps.executeUpdate();
            show("Book added: " + title);
        } catch (SQLException e) {
            show("Error adding book: " + e.getMessage());
        }
    }

    void issueBook() {
        String id = bookIdField.getText();
        String userId = userIdField.getText();
        String issueDate = issueDateField.getText();
        String returnDate = returnDateField.getText();

        try {
            PreparedStatement check = conn.prepareStatement("SELECT available FROM books WHERE id = ?");
            check.setString(1, id);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt("available") == 1) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO issued_books VALUES (?, ?, ?, ?)");
                ps.setString(1, id);
                ps.setString(2, userId);
                ps.setString(3, issueDate);
                ps.setString(4, returnDate);
                ps.executeUpdate();

                PreparedStatement update = conn.prepareStatement("UPDATE books SET available = 0 WHERE id = ?");
                update.setString(1, id);
                update.executeUpdate();

                show("Book issued to user: " + userId);
            } else {
                show("Book not available.");
            }
        } catch (SQLException e) {
            show("Issue error: " + e.getMessage());
        }
    }

    void returnBook() {
        String id = bookIdField.getText();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT return_date FROM issued_books WHERE book_id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String returnDate = rs.getString("return_date");
                long daysLate = ChronoUnit.DAYS.between(LocalDate.parse(returnDate), LocalDate.now());
                double fine = daysLate > 0 ? daysLate * 2.0 : 0.0;

                PreparedStatement del = conn.prepareStatement("DELETE FROM issued_books WHERE book_id = ?");
                del.setString(1, id);
                del.executeUpdate();

                PreparedStatement update = conn.prepareStatement("UPDATE books SET available = 1 WHERE id = ?");
                update.setString(1, id);
                update.executeUpdate();

                show("Book returned. Late fine: ₹" + fine);
            } else {
                show("Book not issued.");
            }
        } catch (SQLException e) {
            show("Return error: " + e.getMessage());
        }
    }

    void show(String msg) {
        outputArea.append(msg + "\n");
    }

    public static void main(String[] args) {
        new LibraryManagementSystem();
    }
}
