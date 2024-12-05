import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class LoginAppTest {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/softwaretesting";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zaibi72622002@";

    // Test data
    private static final String VALID_EMAIL = "testuser@example.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String INVALID_EMAIL = "invalid@example.com";
    private static final String INVALID_PASSWORD = "wrongpassword";


    static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Create Users table
            String createTable = """
                CREATE TABLE IF NOT EXISTS User (
                    Email VARCHAR(255) PRIMARY KEY,
                    Name VARCHAR(255),
                    Password VARCHAR(255)
                );
            """;
            conn.createStatement().execute(createTable);

            // Insert test user
            String insertUser = """
                INSERT INTO User (Email, Name, Password)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE Password=VALUES(Password);
            """;
            try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
                stmt.setString(1, VALID_EMAIL);
                stmt.setString(2, "Test User");
                stmt.setString(3, VALID_PASSWORD);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }


    void testValidLogin() {
        String userName = authenticateUser(VALID_EMAIL, VALID_PASSWORD);
        assertNotNull(userName, "User should be found with valid credentials.");
        assertEquals("Test User", userName, "Username should match expected.");
    }

    @Test
    void testInvalidLogin() {
        String userName = authenticateUser(INVALID_EMAIL, INVALID_PASSWORD);
        assertNull(userName, "No user should be found with invalid credentials.");
    }


    void testEmptyInput() {
        String userName = authenticateUser("", "");
        assertNull(userName, "No user should be found with empty credentials.");
    }


    void testDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            assertNotNull(conn, "Database connection should be established.");
        } catch (Exception e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @Test
    void testSQLInjectionPrevention() {
        String maliciousInput = "' OR '1'='1";
        String userName = authenticateUser(maliciousInput, maliciousInput);
        assertNull(userName, "SQL Injection should not bypass authentication.");
    }

    // Helper method for authentication logic
    private String authenticateUser(String email, String password) {
        String userName = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT Name FROM User WHERE Email = ? AND Password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userName = rs.getString("Name");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userName;
    }

    static void teardownDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Drop Users table
            String dropTable = "DROP TABLE IF EXISTS User;";
            conn.createStatement().execute(dropTable);
        } catch (Exception e) {
            System.err.println("Database teardown failed: " + e.getMessage());
        }
    }
}
