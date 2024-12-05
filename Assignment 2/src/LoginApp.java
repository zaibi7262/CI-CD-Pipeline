import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginApp extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mysql";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Zaibi72622002@";

    public LoginApp() {
        setTitle("Login Screen");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));

        // Email Label and Text Field
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        // Password Label and Password Field
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        panel.add(loginButton);

        add(panel);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Email and Password fields cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String userName = authenticateUser(email, password);
                if (userName != null) {
                    JOptionPane.showMessageDialog(null, "Welcome, " + userName + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                // Catch connection issues or other unexpected exceptions
                JOptionPane.showMessageDialog(null, "System error: " + ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String authenticateUser(String email, String password) throws Exception {
        String userName = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT Name FROM User WHERE LOWER(Email) = LOWER(?) AND Password = ?";
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
            throw new Exception("Failed to connect to the database."); // Rethrow with a meaningful message
        }
        return userName;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginApp loginApp = new LoginApp();
            loginApp.setVisible(true);
        });
    }
}
