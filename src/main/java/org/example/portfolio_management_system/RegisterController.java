
package org.example.portfolio_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField tfUsername, tfEmail, tfPhoneNumber;

    @FXML
    private DatePicker tfDOB;

    @FXML
    private Button GetStarted;
    @FXML
    private PasswordField tfPassword, confirmPasswordField;
    @FXML
    private TextField tfPasswordVisible, confirmPasswordVisible;

    @FXML
    private CheckBox showPasswordCheckBox, showConfirmPasswordCheckBox;


    @FXML
    public void initialize() {
        // Sync the visible password field with the hidden one initially
        tfPasswordVisible.textProperty().bindBidirectional(tfPassword.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    // Event handler for the submit button
    @FXML
    public void handleSubmit() {
        String username = tfUsername.getText();
        String password = tfPassword.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = tfEmail.getText();
        String phone_number = tfPhoneNumber.getText();

        // Fetch the date from DatePicker
        LocalDate dob = tfDOB.getValue();
        // Check if any fields are empty
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || phone_number.isEmpty() || dob == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return;
        }
        // Input validation
        if (!validatePassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            return;
        }

        if (!checkPasswordMatch(password, confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The passwords do not match. Please re-enter the password.");
            return;
        }

        try {
            if (checkUserExists(username, password, dob)) {
                if (isRegistrationIncomplete(username)) {
                    showAlert(Alert.AlertType.WARNING, "Incomplete Registration",
                            "You have already registered but not completed the process.\n" +
                                    "Please fill in your bank details to finish registration.");
                    redirectToBankDetailsForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Username Taken",
                            "This username is already taken.\n" +
                                    "Please choose another username.");
                }
                return;
            }


            registerUser(username, password, email, phone_number, dob);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "There was an error during registration: " + e.getMessage());
        }
    }



    // Method to toggle visibility of the password field
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (showPasswordCheckBox.isSelected()) {
            tfPassword.setVisible(false);
            tfPassword.setManaged(false);
            tfPasswordVisible.setVisible(true);
            tfPasswordVisible.setManaged(true);
        } else {
            tfPassword.setVisible(true);
            tfPassword.setManaged(true);
            tfPasswordVisible.setVisible(false);
            tfPasswordVisible.setManaged(false);
        }
    }

    // Method to toggle visibility of the confirm password field
    @FXML
    private void toggleConfirmPasswordVisibility(ActionEvent event) {
        if (showConfirmPasswordCheckBox.isSelected()) {
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordVisible.setVisible(true);
            confirmPasswordVisible.setManaged(true);
        } else {
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordVisible.setVisible(false);
            confirmPasswordVisible.setManaged(false);
        }
    }
    private boolean checkUserExists(String username, String password, LocalDate dob) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND date_of_birth = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setDate(3, java.sql.Date.valueOf(dob));

            return statement.executeQuery().next();  // Returns true if user exists
        }
    }
    private boolean isRegistrationIncomplete(String username) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        String query = "SELECT address,bank_account_number,ifsc_code,pan FROM users WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            UserSession2.getInstance(username);

            if (resultSet.next()) {
                return resultSet.getString("bank_account_number") == null;  // Check if bank details are missing
            }
        }
        return false;
    }

    private void redirectToBankDetailsForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Register2.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) GetStarted.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Complete Registration - Bank Details");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Method to register the user into the database and link to agent or client table
    public void registerUser(String username, String password, String email, String phone_number, LocalDate dob) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        // Insert into the users table
        String userQuery = "INSERT INTO users (username, password, email, phone_number, date_of_birth) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setString(4, phone_number);
            statement.setDate(5, java.sql.Date.valueOf(dob));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // Set the session after successful registration
            UserSession2.getInstance(username);

            try {
                // Redirect to the next screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Register2.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) GetStarted.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Complete Registration - Bank Details");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // Method to validate password strength
    private boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        return pattern.matcher(password).matches();
    }

    // Method to check if password and confirm password match
    private boolean checkPasswordMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
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
