package org.example.portfolio_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

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
    }

    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    public void loadPortfolioData(int userId) {
        try {
            Connection connection = getConnection();

            // SQL query to sum the amount_invested and current_value for the particular user
            String query = "SELECT SUM(amount_invested) as totalamount, SUM(current_value) as currenttotal FROM portfolio WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            calculateGains(userId);
            // Execute the query and fetch the summed results
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double amountInvested = resultSet.getDouble("totalamount");
                double currentValue = resultSet.getDouble("currenttotal");

                // Formatting the amounts to 4 decimal places
                String formattedAmountInvested = String.format("%.4f", amountInvested);
                String formattedCurrentValue = String.format("%.4f", currentValue);

                // Convert the formatted strings back to double (if required)
                double roundedCurrentValue = Double.parseDouble(formattedCurrentValue);
                double roundedAmountInvested = Double.parseDouble(formattedAmountInvested);

                // Set the values to the respective text areas
                amountInvestedTextArea.setText(String.valueOf(roundedAmountInvested));
                currentValueTextArea.setText(String.valueOf(roundedCurrentValue));
            }

            // Close the resources
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void calculateGains(int user_id) {
        double totalGain = 0.0;
        double totalAmountInvested = 0.0;

        try {
            Connection connection = getConnection();

            // Query to sum the total amount invested and current value for the user
            String query = "SELECT SUM(amount_invested) AS totalAmountInvested, SUM(current_value) AS totalCurrentValue " +
                    "FROM portfolio WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, user_id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                totalAmountInvested = resultSet.getDouble("totalAmountInvested");
                double totalCurrentValue = resultSet.getDouble("totalCurrentValue");

                // Calculate total gain
                totalGain = totalCurrentValue - totalAmountInvested;

                // Calculate gain percentage if amount invested is not zero
                if (totalAmountInvested != 0) {
                    double totalGainPercentage = (totalGain / totalAmountInvested) * 100;
                    totalGainLabel.setText(String.format("       %.5f%%", totalGainPercentage));
                } else {
                    totalGainLabel.setText("         N/A");
                }

                // Display unrealized gain
                if (totalAmountInvested != 0) {
                    unrealizedGainLabel.setText(String.format("       %.5f", totalGain));
                } else {
                    unrealizedGainLabel.setText("        N/A");
                }
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
        switchToPage(event, "PortfolioManagement.fxml", "Portfolio Management");
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage(event, "SIPManagement.fxml", "SIP Management");
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage(event, "MutualFunds.fxml", "Mutual Funds");
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage(event, "ReportsAnalytics.fxml", "Reports & Analytics");
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage(event, "TransactionHistory.fxml", "Transaction History");
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage(event, "UserProfile.fxml", "User Profile");
    }
    // In your controller class

}
