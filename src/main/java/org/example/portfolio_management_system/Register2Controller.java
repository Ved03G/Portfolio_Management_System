package org.example.portfolio_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Register2Controller {

    @FXML
    private TextField tfAddress, tfPAN, tfBankDetails, ifscField;

    @FXML
    private Button btnRegister;

    // Event handler for the submit button
    @FXML
    public void handleSubmit() {
        // Retrieve the current username from the UserSession2 singleton
        String username = UserSession2.getInstance().getusername();
        String address = tfAddress.getText();
        String pan = tfPAN.getText();
        String bank_account_number = tfBankDetails.getText();
        String ifsc_code = ifscField.getText();

        // Check if any fields are empty
        if (address.isEmpty() || pan.isEmpty() || bank_account_number.isEmpty() || ifsc_code.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return;
        }

        try {
            // Register the user into the database with bank details
            registerUser(username, address, pan, bank_account_number, ifsc_code);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "There was an error during registration: " + e.getMessage());
        }
    }

    // Method to register the user into the database
    public void registerUser(String username, String address, String pan, String bank_account_number, String ifsc_code) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        // Call the stored procedure to update the user's details
        String sql = "{CALL update_user_details(?, ?, ?, ?, ?)}";
        try (PreparedStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, address);
            stmt.setString(3, pan);
            stmt.setString(4, bank_account_number);
            stmt.setString(5, ifsc_code);
            stmt.executeUpdate();
        }

        // Show success alert and redirect to the login page
        showAlert(Alert.AlertType.INFORMATION, "Registration Success", "Registration Successful");
        try {
            // Load the Login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
        // Load Login.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Login Page");
        stage.show();
    }
}
