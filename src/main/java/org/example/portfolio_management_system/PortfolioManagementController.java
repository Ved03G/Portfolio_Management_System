package org.example.portfolio_management_system;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.zip.GZIPInputStream;

import static org.example.portfolio_management_system.DatabaseConnection.getConnection;

public class PortfolioManagementController {

    @FXML
    private TableView<Portfolio> investmentTable;

    @FXML
    private TableColumn<MutualFund2, String> schemeCodeColumn;
    @FXML
    private TableColumn<MutualFund2, String> schemeNameColumn;
    @FXML
    private TableColumn<MutualFund2, Double> amountInvestedColumn;
    @FXML
    private TableColumn<MutualFund2, Double> currentValueColumn;
    @FXML
    private TableColumn<MutualFund2, Double> typeColumn;
    @FXML
    private TableColumn<MutualFund2, Double> unitsColumn;
    @FXML
    private TextArea amountInvestedTextArea;
    @FXML
    private TextArea currentValueTextArea;
    @FXML
    private Label totalGainLabel;
    @FXML
    private Label unrealizedGainLabel;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;

    private ObservableList<Portfolio> portfoliosList = FXCollections.observableArrayList();

    public void initialize() {
        if (schemeCodeColumn != null) {
            schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        }
        schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        schemeNameColumn.setCellValueFactory(new PropertyValueFactory<>("schemeName"));
        amountInvestedColumn.setCellValueFactory(new PropertyValueFactory<>("amountInvested"));
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));


        loadTableData();
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

    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    public void loadPortfolioData(int userId) {
        try {
            Connection connection = getConnection();

            // SQL query to fetch scheme_code, amount_invested, and units for the user
            String query = "SELECT scheme_code, SUM(amount_invested) as totalamount, SUM(units) as totalunits FROM portfolio WHERE user_id = ? GROUP BY scheme_code";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            // Execute the query and fetch the result
            ResultSet resultSet = preparedStatement.executeQuery();

            double totalAmountInvested = 0;
            double totalCurrentValue = 0;

            // Iterate through the result set and fetch current value from API for each scheme
            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                double amountInvested = resultSet.getDouble("totalamount");
                double totalUnits = resultSet.getDouble("totalunits");

                // Fetch the current NAV from the API using the schemeCode
                double currentNAV = fetchNAVFromAPI(schemeCode);

                // Calculate the current value for this scheme
                double currentValue = totalUnits * currentNAV;

                // Sum up the total invested and current values
                totalAmountInvested += amountInvested;
                totalCurrentValue += currentValue;
            }

            // Formatting the amounts to 4 decimal places
            String formattedAmountInvested = String.format("%.4f", totalAmountInvested);
            String formattedCurrentValue = String.format("%.4f", totalCurrentValue);

            // Convert the formatted strings back to double (if required)
            double roundedCurrentValue = Double.parseDouble(formattedCurrentValue);
            double roundedAmountInvested = Double.parseDouble(formattedAmountInvested);

            // Set the values to the respective text areas
            amountInvestedTextArea.setText(String.valueOf(roundedAmountInvested));
            currentValueTextArea.setText(String.valueOf(roundedCurrentValue));

            // Calculate gains (call the method to display gains)
            calculateGains(userId);

            // Close the resources
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private double fetchNAVFromAPI(String schemeCode) {
        double nav = 0;
        try {
            // API endpoint with the schemeCode
            String apiUrl = "https://api.mfapi.in/mf/" + schemeCode;

            // Create a connection to the API
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Request GZIP compressed content
            connection.setRequestProperty("Accept-Encoding", "gzip");

            // Check if the response is GZIP compressed
            InputStream inputStream;
            if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                inputStream = new GZIPInputStream(connection.getInputStream());
            } else {
                inputStream = connection.getInputStream();
            }

            // Use a buffered reader with a 16KB buffer
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), 16 * 1024);
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close the connections
            in.close();
            connection.disconnect();

            // Parse the JSON response to get the NAV value
            JSONObject jsonResponse = new JSONObject(content.toString());
            nav = jsonResponse.getJSONArray("data").getJSONObject(0).getDouble("nav"); // Assuming 'nav' is the field

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nav;
    }


    public void calculateGains(int userId) {
        double totalGain = 0.0;
        double totalAmountInvested = 0.0;
        double totalCurrentValue = 0.0;

        try {
            Connection connection = getConnection();

            // SQL query to fetch scheme_code, amount_invested, and units for the user
            String query = "SELECT scheme_code, SUM(amount_invested) as totalamount, SUM(units) as totalunits " +
                    "FROM portfolio WHERE user_id = ? GROUP BY scheme_code";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            // Execute the query and fetch the result
            ResultSet resultSet = preparedStatement.executeQuery();

            // Loop through all rows to fetch the current NAV from the API for each scheme_code
            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                double amountInvested = resultSet.getDouble("totalamount");
                double totalUnits = resultSet.getDouble("totalunits");

                // Fetch the current NAV from the API for this scheme_code
                double currentNAV = fetchNAVFromAPI(schemeCode);

                // Calculate the current value for this investment
                double currentValue = totalUnits * currentNAV;

                // Add to the total amount invested and total current value
                totalAmountInvested += amountInvested;
                totalCurrentValue += currentValue;
            }

            // Calculate total gain
            totalGain = totalCurrentValue - totalAmountInvested;

            // Update the gain percentage if the amount invested is not zero
            if (totalAmountInvested != 0) {
                double totalGainPercentage = (totalGain / totalAmountInvested) * 100;
                totalGainLabel.setText(String.format("       %.5f%%", totalGainPercentage));
            } else {
                totalGainLabel.setText("         N/A");
            }

            // Display unrealized gain if amount invested is not zero
            if (totalAmountInvested != 0) {
                unrealizedGainLabel.setText(String.format("       %.5f", totalGain));
            } else {
                unrealizedGainLabel.setText("        N/A");
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void loadTableData() {
        try {
            Connection connection = getConnection();
            String query = "SELECT scheme_code, fund_name, amount_invested, current_value, units, type FROM portfolio WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, getCurrentUserId());
            ResultSet resultSet = preparedStatement.executeQuery();
            portfoliosList.clear();

            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                String schemeName = resultSet.getString("fund_name");
                double amountInvested = resultSet.getDouble("amount_invested");
                double currentValue = resultSet.getDouble("current_value");
                double units = resultSet.getDouble("units");
                String type = resultSet.getString("type");

                Portfolio portfolio = new Portfolio(schemeCode, schemeName, amountInvested, currentValue, units, type);
                portfoliosList.add(portfolio);
            }
            investmentTable.setItems(portfoliosList);
            int userId = getCurrentUserId();
            loadPortfolioData(userId);

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
    // In your controller class

}
