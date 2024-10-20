package org.example.portfolio_management_system;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Transactionview {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/javafxapp2"; // Update as necessary
    private static final String USER = "root"; // Your MySQL username
    private static final String PASSWORD = "Servesh#21"; // Your MySQL password
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    private ListView<Transaction> transactionListView;
    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;

    // Method to load the FXML of each section
    private void switchToPage1(ActionEvent event, String fxmlFile1, String title, Button clickedButton) throws IOException {
        FXMLLoader loader1 = new FXMLLoader(getClass().getResource(fxmlFile1));
        root = loader1.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        resetButtonStyles();
        clickedButton.getStyleClass().add("sidebar-button-active");
    }

    private void resetButtonStyles() {
        List<Button> buttons = List.of(btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile);
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PortfolioManagement.fxml"));
                return loader.load();
            }
        };

        // After the loading is done, switch to the MutualFunds.fxml content
        loadTask.setOnSucceeded(workerStateEvent -> {
            try {
                Parent root = loadTask.getValue();  // Get the loaded FXML root
                currentScene.setRoot(root);  // Replace the current root with the new one
                stage.setTitle("Portfolio Mangement");
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

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage1(event, "SIPManagement.fxml", "SIP Management", btnSIP);
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
        switchToPage1(event, "TransactionHistory.fxml", "Transaction History", btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage1(event, "UserProfile.fxml", "User Profile", btnProfile);
    }

    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId(); // Fetch the logged-in user's ID
    }

    // Method to load transactions from the database
    public void loadTransactionsFromDatabase() {
        int userId = getCurrentUserId(); // Get current user ID
        String query = "SELECT Amount, type1, transaction_date, fund_type, fund_name, units FROM transactions WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId); // Set the current user's ID in the query
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String amount = String.valueOf(rs.getDouble("Amount"));
                String type = rs.getString("type1");
                Date transactionDate = rs.getDate("transaction_date");
                String fundType = rs.getString("fund_type");
                String fundName = rs.getString("fund_name");
                double units = rs.getDouble("units");

                // Add the transaction to the list
                Transaction transaction = new Transaction(amount, type, transactionDate, fundType, fundName, units);
                transactionList.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the items in the ListView
        transactionListView.setItems(transactionList);
    }

    @FXML
    private void initialize() {
        transactionListView.setCellFactory(listView -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setGraphic(null);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("transactioncell.fxml"));
                    try {
                        AnchorPane cell = loader.load();
                        TransactionCellController controller = loader.getController();
                        controller.setTransactionData(transaction);
                        setGraphic(cell);
                        setPadding(new Insets(10));
                        // Add mouse enter event to enlarge the cell
                        cell.setOnMouseEntered(e -> {
                            cell.setScaleX(1.02); // Enlarge cell by 5%
                            cell.setScaleY(1.02);
                        });

                        // Add mouse exit event to reset the cell size
                        cell.setOnMouseExited(e -> {
                            cell.setScaleX(1.0); // Reset to original size
                            cell.setScaleY(1.0);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        });

        // Load transactions from the database and display them in the ListView
        loadTransactionsFromDatabase();
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
}

