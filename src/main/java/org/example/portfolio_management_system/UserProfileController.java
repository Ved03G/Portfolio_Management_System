package org.example.portfolio_management_system;

import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.io.IOException;
import javafx.fxml.Initializable;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField usernameField, phoneField, addressField, bankAccountField, ifscField, emailField, panField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = getCurrentUserId();
        loadUserData(userId);// Call the method when the view is initialized
        addHoverEffect(btnPortfolio);
        addHoverEffect(btnSIP);
        addHoverEffect(btnMutualFunds);
        addHoverEffect(btnReports);
        addHoverEffect(btnTransactions);
        addHoverEffect(btnProfile);

    }
    private void addHoverEffect(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);

        scaleIn.setToX(1.1); // Enlarge button by 10%
        scaleIn.setToY(1.1);


        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);

        scaleOut.setToX(1.0); // Enlarge button by 10%
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.playFromStart());
        button.setOnMouseExited(e -> scaleOut.playFromStart());
    }
    // Method to load user data
    public void loadUserData(int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT username, password, phone_number, address, date_of_birth, bank_account_number, ifsc_code, email, pan FROM users WHERE userid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                usernameField.setText(resultSet.getString("username"));
                passwordField.setText(resultSet.getString("password"));
                phoneField.setText(resultSet.getString("phone_number"));
                addressField.setText(resultSet.getString("address"));
                dobPicker.setValue(resultSet.getDate("date_of_birth").toLocalDate());
                bankAccountField.setText(resultSet.getString("bank_account_number"));
                ifscField.setText(resultSet.getString("ifsc_code"));
                emailField.setText(resultSet.getString("email"));
                panField.setText(resultSet.getString("pan"));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Method to save updated profile data
    @FXML
    private void saveProfile() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String updateQuery = "UPDATE users SET password = ?, phone_number = ?, address = ?, date_of_birth = ?, bank_account_number = ?, ifsc_code = ?, email = ?, pan = ? WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);

            preparedStatement.setString(1, passwordField.getText());
            preparedStatement.setString(2, phoneField.getText());
            preparedStatement.setString(3, addressField.getText());
            preparedStatement.setDate(4, Date.valueOf(dobPicker.getValue()));
            preparedStatement.setString(5, bankAccountField.getText());
            preparedStatement.setString(6, ifscField.getText());
            preparedStatement.setString(7, emailField.getText());
            preparedStatement.setString(8, panField.getText());
            preparedStatement.setString(9, usernameField.getText());

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully!");
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Edit profile method (can enable fields for editing if necessary)
    @FXML
    private void editProfile() {
        phoneField.setEditable(true);
        addressField.setEditable(true);
        passwordField.setEditable(true);
        dobPicker.setDisable(false);
        bankAccountField.setEditable(true);
        ifscField.setEditable(true);
        emailField.setEditable(true);
        panField.setEditable(true);
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    // Method to load the FXML of each section
    private void switchToPage(ActionEvent event, String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage(event, "PortfolioManagement.fxml", "Portfolio Management");
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage(event, "SIPManagement.fxml", "SIP Management");
    }



    public void handlemutualfundclick(ActionEvent event) throws IOException {
        // Get the current stage and scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene currentScene = stage.getScene();
        Parent currentRoot = currentScene.getRoot();

        // Create a StackPane to hold the current content and the ProgressIndicator
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(currentRoot);

        // Create the ProgressIndicator and add it to the StackPane
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100, 100);  // Set size if needed
        stackPane.getChildren().add(progressIndicator);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);  // Center the ProgressIndicator

        // Replace the current root with the StackPane (which includes the loading indicator)
        currentScene.setRoot(stackPane);

        // Run the loading process in a background thread using Task
        Task<Parent> loadTask = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                // Load the MutualFunds.fxml file (this happens in the background thread)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MutualFunds.fxml"));
                return loader.load();
            }
        };

        // After the loading is done, switch to the MutualFunds.fxml content
        loadTask.setOnSucceeded(workerStateEvent -> {
            try {
                Parent root = loadTask.getValue();  // Get the loaded FXML root
                currentScene.setRoot(root);  // Replace the current root with the new one
                stage.setTitle("Mutual Funds");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Handle any exceptions that occur during loading
        loadTask.setOnFailed(workerStateEvent -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();  // Handle the exception (log it or show an error)
        });

        // Start the background thread to load the FXML
        new Thread(loadTask).start();
    }




    public void handlereportsclick(ActionEvent event) throws IOException {
        // Get the current stage and root
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene currentScene = stage.getScene();
        Parent currentRoot = currentScene.getRoot();

        // Create a StackPane to hold the current content and the ProgressIndicator
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(currentRoot);

        // Create the ProgressIndicator and add it to the StackPane
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100, 100);  // Set size if needed
        stackPane.getChildren().add(progressIndicator);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);  // Center the ProgressIndicator

        // Replace the current root with the StackPane (which includes the loading indicator)
        currentScene.setRoot(stackPane);

        // Run the loading process in a background thread using Task
        Task<Parent> loadTask = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                // Load the FXML file (this happens in the background thread)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportsAnalytics.fxml"));
                return loader.load();
            }
        };

        // After the loading is done, switch to the ReportsAnalytics.fxml content
        loadTask.setOnSucceeded(workerStateEvent -> {
            try {
                Parent root = loadTask.getValue();  // Get the loaded FXML root
                currentScene.setRoot(root);  // Replace the current root with the new one
                stage.setTitle("Reports & Analytics");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Handle any exceptions that occur during loading
        loadTask.setOnFailed(workerStateEvent -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();  // Handle the exception (log it or show an error)
        });

        // Start the background thread to load the FXML
        new Thread(loadTask).start();
    }


    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage(event, "TransactionHistory.fxml", "Transaction History");
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage(event, "UserProfile.fxml", "User Profile");
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
