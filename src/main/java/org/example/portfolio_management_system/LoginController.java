//package org.example.portfolio_management_system;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.stage.Stage;
//import javafx.scene.control.Alert.AlertType;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.io.IOException;
//import java.util.regex.Pattern;
//public class LoginController {
//
//    @FXML
//    private TextField tfusername;
//    @FXML
//    private PasswordField tfpass;
//    @FXML
//    private TextField tfpassVisible;
//    @FXML
//    private Button ShowPassword;
//    @FXML
//    private Button btnLogin;
//
//
//    // Event handler for the login button
//    @FXML
//    private void handleLoginButtonClick(ActionEvent event) {
//        String username = tfusername.getText();
//        String password = tfpass.getText();
//
//        try {
//            // Check if credentials are valid using database validation
//            if (validateLogin(username, password)) {
//                // Redirect to Client Dashboard if login is successful
//                loadClientDashboard();
//            } else {
//                // Show an alert if login fails
//                Alert alert = new Alert(AlertType.ERROR);
//                alert.setTitle("Login Failed");
//                alert.setHeaderText(null);
//                alert.setContentText("Invalid username or password, Please try again.");
//                alert.showAndWait();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Alert alert = new Alert(AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText(null);
//            alert.setContentText("An error occurred while trying to log in. Please try again.");
//            alert.showAndWait();
//        }
//    }
//
//    // Method to validate login with the database
//    private boolean validateLogin(String username, String password) throws SQLException {
//        Connection connection = DatabaseConnection.getConnection();
//
//        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
//        try (PreparedStatement statement = connection.prepareStatement(query)) {
//            statement.setString(1, username);
//            statement.setString(2, password);  // Use hashed password in real scenarios
//
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) {
//                System.out.println("Login successful!");
//                int userId = resultSet.getInt("userid");
//                UserSession.getInstance(userId);
//                return true;  // User found and role matches
//            } else {
//                System.out.println("Invalid username, password.");
//                return false;  // User not found or role doesn't match
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (connection != null) {
//                connection.close();
//            }
//        }
//    }
//
//
//    // Method to load the Client Dashboard page after successful login
//    private void loadClientDashboard() {
//        try {
//            // Load the Client Dashboard FXML
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("PortfolioManagement.fxml"));
//            Parent root = loader.load();
//
//            // Get the current stage and set the scene to Client Dashboard
//            Stage stage = (Stage) btnLogin.getScene().getWindow();
//            Scene scene = new Scene(root);
//            stage.setScene(scene);
//            stage.setTitle("Dashboard");
//            stage.show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Event handler for the register button
//    @FXML
//    private void HandleRegisterClick(ActionEvent event) throws IOException {
//        // Load Register.fxml
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
//        Parent root = loader.load();
//
//        // Get current stage
//        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
//
//        // Set the scene to Register page
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.setTitle("Register Page");
//        stage.show();
//    }
//    @FXML
//    private void loadforgotpswd(ActionEvent event) throws IOException {
//        // Load Register.fxml
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("ForgotPassword.fxml"));
//        Parent root = loader.load();
//
//        // Get current stage
//        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
//
//        // Set the scene to Register page
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.setTitle("Register Page");
//        stage.show();
//    }
//
//    @FXML
//    private void initialize() {
//        // Set the initial state
//        tfpassVisible.setVisible(false);
//        tfpassVisible.managedProperty().bind(tfpassVisible.visibleProperty());
//
//        // Show/Hide password functionality
//        ShowPassword.setOnAction(event -> {
//            if (tfpass.isVisible()) {
//                tfpassVisible.setText(tfpass.getText());
//                tfpass.setVisible(false);
//                tfpassVisible.setVisible(true);
//                ShowPassword.setText("Hide Password");
//            } else {
//                tfpass.setText(tfpassVisible.getText());
//                tfpassVisible.setVisible(false);
//                tfpass.setVisible(true);
//                ShowPassword.setText("Show Password");
//            }
//        });}
//}
package org.example.portfolio_management_system;

import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField tfusername;
    @FXML
    private PasswordField tfpass;
    @FXML
    private TextField tfpassVisible;
    @FXML
    private Button ShowPassword;
    @FXML
    private Button btnLogin;

    // Event handler for the login button
    @FXML
    private void handleLoginButtonClick(ActionEvent event) {
        String username = tfusername.getText();
        String password = tfpass.getText();

        try {
            // Check if credentials are valid using database validation
            if (validateLogin(username, password)) {
                // Redirect to Client Dashboard if login is successful
                loadClientDashboard();
            } else {
                // Show an alert if login fails
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid username or password, Please try again.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while trying to log in. Please try again.");
            alert.showAndWait();
        }
    }

    // Method to validate login with the database
    private boolean validateLogin(String username, String password) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);  // Use hashed password in real scenarios

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Login successful!");
                int userId = resultSet.getInt("userid");

                // Set a new user session
                UserSession.setInstance(userId);  // This ensures the session is created for the new user
                return true;  // User found and role matches
            } else {
                System.out.println("Invalid username, password.");
                return false;  // User not found or role doesn't match
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    // Method to load the Client Dashboard page after successful login
    private void loadClientDashboard() {
        try {
            // Create a ProgressIndicator and display it during the loading process
            ProgressIndicator progressIndicator = new ProgressIndicator();
            StackPane loadingPane = new StackPane(progressIndicator);

            // Get the current stage and show the progress indicator
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene loadingScene = new Scene(loadingPane, 1100, 700);
            stage.setScene(loadingScene);
            stage.show();

            // Create a task to load the Client Dashboard in the background
            Task<Parent> loadDashboardTask = new Task<>() {
                @Override
                protected Parent call() throws IOException {
                    // Load the Client Dashboard FXML
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("PortfolioManagement.fxml"));
                    return loader.load();
                }
            };

            // Set the dashboard scene once the loading is complete
            loadDashboardTask.setOnSucceeded(workerStateEvent -> {
                Parent root = loadDashboardTask.getValue();
                Scene dashboardScene = new Scene(root);
                stage.setScene(dashboardScene);
                stage.setTitle("Dashboard");
                stage.show();
            });

            // Handle loading errors
            loadDashboardTask.setOnFailed(workerStateEvent -> {
                Throwable exception = loadDashboardTask.getException();
                exception.printStackTrace();
                // Optionally, you can show an error message or dialog to the user
            });

            // Start the task in a separate thread
            new Thread(loadDashboardTask).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Event handler for the register button
    @FXML
    private void HandleRegisterClick(ActionEvent event) throws IOException {
        // Load Register.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
        Parent root = loader.load();

        // Get current stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Set the scene to Register page
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register Page");
        stage.show();
    }

    @FXML
    private void loadforgotpswd(ActionEvent event) throws IOException {
        // Load ForgotPassword.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ForgotPassword.fxml"));
        Parent root = loader.load();

        // Get current stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Set the scene to Forgot Password page
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Forgot Password Page");
        stage.show();
    }

    @FXML
    private void initialize() {
        // Set the initial state
        tfpassVisible.setVisible(false);
        tfpassVisible.managedProperty().bind(tfpassVisible.visibleProperty());

        // Show/Hide password functionality
        ShowPassword.setOnAction(event -> {
            if (tfpass.isVisible()) {
                tfpassVisible.setText(tfpass.getText());
                tfpass.setVisible(false);
                tfpassVisible.setVisible(true);
                ShowPassword.setText("Hide Password");
            } else {
                tfpass.setText(tfpassVisible.getText());
                tfpassVisible.setVisible(false);
                tfpass.setVisible(true);
                ShowPassword.setText("Show Password");
            }
        });
        handlehover(btnLogin);
    }
    private void handlehover(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);

           scaleIn.setToX(1.1); // Enlarge button by 10%
           scaleIn.setToY(1.1);


        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);

        scaleOut.setToX(1.0); // Enlarge button by 10%
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.playFromStart());
        button.setOnMouseExited(e -> scaleOut.playFromStart());
    }
}
