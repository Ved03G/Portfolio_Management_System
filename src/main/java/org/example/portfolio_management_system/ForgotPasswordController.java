package org.example.portfolio_management_system;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ForgotPasswordController {
    @FXML
    private TextField tfUsername;

    @FXML
    private DatePicker tfDOB; // DatePicker for date of birth

    @FXML
    private PasswordField tfNewPassword;

    @FXML
    private PasswordField tfConfirmPassword;

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String username = tfUsername.getText();
        LocalDate dob = tfDOB.getValue();
        String newPassword = tfNewPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();

        // Check if any fields are empty
        if (username.isEmpty() || dob == null || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Error", "Passwords do not match.");
            return;
        }

        try {
            // Validate user details and update the password
            if (validateUser(username, dob)) {
                updatePassword(username, newPassword);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password reset successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid username or date of birth.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error occurred during password reset: " + e.getMessage());
        }
    }

    // Validate user by username and DOB
    private boolean validateUser(String username, LocalDate dob) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT * FROM users WHERE username = ? AND date_of_birth = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setDate(2, java.sql.Date.valueOf(dob));
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();  // If a record is found, return true
        }
    }

    // Update password in the database
    private void updatePassword(String username, String newPassword) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        String query = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }

    // Method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void Backtologin(ActionEvent event) throws IOException {
        // Load Register.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();

        // Get current stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Set the scene to Register page
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register Page");
        stage.show();
    }
}
