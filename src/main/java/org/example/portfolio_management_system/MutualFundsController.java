package org.example.portfolio_management_system;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class MutualFundsController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    // Sidebar buttons
    @FXML
    private TableView<MutualFund2> investmentTable;
    @FXML
    private TextField searchSchemeCode1, searchSchemeName1;
    @FXML
    private TableColumn<MutualFund2, String> schemeCodeColumn;
    @FXML
    private TableColumn<MutualFund2, String> schemeNameColumn;
    @FXML
    private TableColumn<MutualFund2, Double> navColumn;
    @FXML
    private TableColumn<MutualFund2, Double> amountInvestedColumn;
    @FXML
    private TableColumn<MutualFund2, Double> currentValueColumn;
    @FXML
    private TableColumn<MutualFund2, Double> costperunitColumn;
    @FXML
    private TableColumn<MutualFund2, Double> unitsColumn;

    private ObservableList<MutualFund2> mutualFundsList = FXCollections.observableArrayList();

    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;

    // Table and search fields
    @FXML
    private TextField searchSchemeCode, searchSchemeName;

    @FXML
    private TableView<MutualFund> mutualFundTable;

    @FXML
    private TableColumn<MutualFund, String> columnSchemeCode, columnSchemeName;

    private ObservableList<MutualFund> mutualFundData = FXCollections.observableArrayList();
    private MutualFund selectedFund;
    private double currentNav = 0.0;
    private double costperunit = 0.0;

    // Initialize method to load data into the table and handle sidebar
    @FXML
    public void initialize() {
        if (schemeCodeColumn != null) {
            schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        } else {
            System.out.println("Error: schemeCodeColumn is null");
        }
        // Initialize table columns
        schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        schemeNameColumn.setCellValueFactory(new PropertyValueFactory<>("schemeName"));
        navColumn.setCellValueFactory(new PropertyValueFactory<>("nav"));
        amountInvestedColumn.setCellValueFactory(new PropertyValueFactory<>("amountInvested"));
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        costperunitColumn.setCellValueFactory(new PropertyValueFactory<>("costPerUnit"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));

        // Load the data from database
        loadTableData();


        columnSchemeCode.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        columnSchemeName.setCellValueFactory(new PropertyValueFactory<>("schemeName"));

        loadMutualFunds();  // Load data when initializing
        // Handle table row selection
        mutualFundTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFund = newSelection;
            }
        });
    }
    public void loadTableData() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            int userId = getCurrentUserId(); // Get the current user ID

            String query = "SELECT scheme_code, fund_name, nav, amount_invested, current_value, units, costperunit FROM mutual_funds WHERE user_id = " + userId;
            ResultSet resultSet = statement.executeQuery(query);

            mutualFundsList.clear(); // Clear the list before loading new data

            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                String schemeName = resultSet.getString("fund_name");
                double nav = resultSet.getDouble("nav");
                double amountInvested = resultSet.getDouble("amount_invested");
                double currentValue = resultSet.getDouble("current_value");
                double units = resultSet.getDouble("units");
                double costPerUnit = resultSet.getDouble("costperunit");

                MutualFund2 mutualFund = new MutualFund2(schemeCode, schemeName, nav, amountInvested, currentValue, units, costPerUnit);
                mutualFundsList.add(mutualFund);
            }

            investmentTable.setItems(mutualFundsList);

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch mutual funds from API and populate the table
    private void loadMutualFunds() {
        MutualFundFetcher fetcher = new MutualFundFetcher();
        JSONArray mutualFunds = fetcher.fetchData();

        if (mutualFunds != null) {
            for (Object obj : mutualFunds) {
                JSONObject fund = (JSONObject) obj;

                // Handle schemeCode conversion from Long to String
                Object schemeCodeObj = fund.get("schemeCode");
                String schemeCode = schemeCodeObj instanceof Long ? String.valueOf(schemeCodeObj) : (String) schemeCodeObj;

                mutualFundData.add(new MutualFund(
                        schemeCode,
                        (String) fund.get("schemeName")
                ));
            }
        }

        mutualFundTable.setItems(mutualFundData);
    }


    // Filter table data based on search input
    @FXML
    public void onSearch() {
        String schemeCodeFilter = searchSchemeCode.getText().toLowerCase();
        String schemeNameFilter = searchSchemeName.getText().toLowerCase();

        ObservableList<MutualFund> filteredData = FXCollections.observableArrayList();

        for (MutualFund fund : mutualFundData) {
            if (fund.getSchemeCode().toLowerCase().contains(schemeCodeFilter)
                    && fund.getSchemeName().toLowerCase().contains(schemeNameFilter)) {
                filteredData.add(fund);
            }
        }

        mutualFundTable.setItems(filteredData);
    }
    @FXML
    public void onInvestmentSearch() {
        String schemeCodeFilter = searchSchemeCode1.getText().toLowerCase();
        String schemeNameFilter = searchSchemeName1.getText().toLowerCase();

        ObservableList<MutualFund2> filteredData = FXCollections.observableArrayList();

        for (MutualFund2 fund : mutualFundsList) {
            if (fund.getSchemeCode().toLowerCase().contains(schemeCodeFilter)
                    && fund.getSchemeName().toLowerCase().contains(schemeNameFilter)) {
                filteredData.add(fund);
            }
        }

        investmentTable.setItems(filteredData);
    }
    private void reloadSMFManagementScreen() throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MutualFunds.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage)searchSchemeCode.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }
    }

    @FXML
    public void fetchNAVData() {
        if (selectedFund != null) {
            MutualFundFetcher fetcher = new MutualFundFetcher();
            JSONObject fundData = fetcher.fetchFundDataBySchemeCode(selectedFund.getSchemeCode());

            if (fundData != null) {
                JSONObject data = (JSONObject) ((JSONArray) fundData.get("data")).get(0);
                String date = (String) data.get("date");
                currentNav = Double.parseDouble((String) data.get("nav")); // Save NAV for later calculations
                costperunit = currentNav;
                // Show the fetched NAV and date to the user (e.g., in an alert)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("NAV Data");
                alert.setHeaderText("NAV Information");
                alert.setContentText("Date: " + date + "\nNAV: " + currentNav);
                alert.showAndWait();
            }
        } else {
            // Show error if no fund is selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Mutual Fund Selected");
            alert.setContentText("Please select a mutual fund first.");
            alert.showAndWait();
        }
    }
    // Add selected mutual fund to investment
    @FXML
    public void addToInvestment() {
        if (selectedFund != null) {
            if (currentNav == 0.0) {
                // Ensure that NAV is fetched before allowing investment
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("NAV Not Fetched");
                alert.setContentText("Please fetch the NAV data before making an investment.");
                alert.showAndWait();
                return;
            }

            // Show input dialog to ask how much the user wants to invest
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Investment Amount");
            dialog.setHeaderText("Enter the amount you want to invest:");
            dialog.setContentText("Amount:");

            // Get user input for investment amount
            dialog.showAndWait().ifPresent(amountStr -> {
                try {
                    double amount = Double.parseDouble(amountStr);
                    double units = amount / currentNav; // Calculate units based on NAV
                    units = Math.round(units * 100.0) / 100.0; // Round to 2 decimal places
                    double currentValue = units * currentNav;
                    // Save investment details to the database
                    saveInvestmentToDatabase(amount, units, currentValue,costperunit);
                    // Show investment information
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Investment Successful");
                    alert.setHeaderText("Investment Details");
                    alert.setContentText(
                            "You have invested Rs. " + amount + " in " + selectedFund.getSchemeName() + ".\n\n" +
                                    "You will receive " + units + " units based on the current NAV of " + currentNav + "."
                    );

                    alert.showAndWait();
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number for the amount.");
                    alert.showAndWait();
                }
            });
        } else {
            // Show error if no fund is selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Mutual Fund Selected");
            alert.setContentText("Please select a mutual fund first.");
            alert.showAndWait();
        }
    }

    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    public void updatePortfolio(int userId,String scheme_code,String fund_name,Double units) {
        try {
            Connection connection = DatabaseConnection.getConnection();

            // Query to get the sum of amount_invested and current_value
            String sumQuery = "SELECT amount_invested, current_value " +
                    "FROM mutual_funds WHERE user_id = ? order by fund_id desc limit 1";

            PreparedStatement ps = connection.prepareStatement(sumQuery);
            ps.setInt(1, userId);

            // Execute query
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                // Retrieve the total amount invested and current value from the result set
                double totalAmountInvested = resultSet.getDouble("amount_invested");
                double totalCurrentValue = resultSet.getDouble("current_value");


                // Ensure that values are non-zero before updating
                if (totalAmountInvested > 0 && totalCurrentValue > 0) {
                    // Insert or update the portfolio record in the database
                    String insertQuery = "INSERT INTO portfolio (user_id, amount_invested, current_value, scheme_code, fund_name, units,type) " +
                            "VALUES (?, ?, ?, ?, ?, ?,'Mutual Funds') " +
                            "ON DUPLICATE KEY UPDATE " +
                            "amount_invested = VALUES(amount_invested), " +
                            "current_value = VALUES(current_value)";

                    PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                    insertPs.setInt(1, userId);
                    insertPs.setDouble(2, totalAmountInvested);
                    insertPs.setDouble(3, totalCurrentValue);
                    insertPs.setString(4, scheme_code);
                    insertPs.setString(5, fund_name);
                    insertPs.setDouble(6, units);


                    // Execute the update
                    insertPs.executeUpdate();
                    insertPs.close();
                } else {
                    System.out.println("No investments found to update the portfolio.");
                }
            }

            // Close result set and connection
            resultSet.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updatePortfolio1(int userId, String fundName, Double remainingUnits, double remainingAmountInvested) {
        try {
            Connection connection = DatabaseConnection.getConnection();

            // Get the scheme code based on the fund_name
            String getSchemeCodeQuery = "SELECT scheme_code FROM mutual_funds WHERE fund_name = ?";
            PreparedStatement schemeCodePs = connection.prepareStatement(getSchemeCodeQuery);
            schemeCodePs.setString(1, fundName);
            ResultSet schemeCodeResultSet = schemeCodePs.executeQuery();

            String fundId = null;
            if (schemeCodeResultSet.next()) {
                fundId = schemeCodeResultSet.getString("scheme_code");
            }

            // Calculate the new current value based on the remaining units
            double newCurrentValue = calculateCurrentValue(fundId, remainingUnits);

            // If remaining units are greater than 0, update the portfolio
            if (remainingUnits > 0) {
                String updateQuery = "UPDATE portfolio SET amount_invested = ?, current_value = ?, units = ? WHERE user_id = ? AND fund_name = ?";
                PreparedStatement updatePs = connection.prepareStatement(updateQuery);

                updatePs.setDouble(1, remainingAmountInvested);
                updatePs.setDouble(2, newCurrentValue);
                updatePs.setDouble(3, remainingUnits);
                updatePs.setInt(4, userId);
                updatePs.setString(5, fundName);

                updatePs.executeUpdate();
                updatePs.close();
            } else {
                // If units become zero, you can either delete the portfolio entry or update it with zero values
                String deleteQuery = "DELETE FROM portfolio WHERE user_id = ? AND fund_name = ?";
                PreparedStatement deletePs = connection.prepareStatement(deleteQuery);
                deletePs.setInt(1, userId);
                deletePs.setString(2, fundName);

                deletePs.executeUpdate();
                deletePs.close();
            }

            schemeCodeResultSet.close();
            schemeCodePs.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to calculate the current value based on NAV and units
    public double calculateCurrentValue(String fund_id, double units) {
        double currentNAV = getCurrentNAV(fund_id);  // Fetch current NAV for the fund
        return currentNAV * units;  // Calculate current value
    }
    // Method to fetch the current NAV of a mutual fund
    public double getCurrentNAV(String fund_id) {
        double currentNAV = 0.0;

        try {
            // Example: Fetch NAV from the database
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT nav FROM mutual_funds WHERE scheme_code = ?";  // Adjust based on your database schema
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, fund_id);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                currentNAV = resultSet.getDouble("nav");  // Assuming nav column has the current NAV
            }

            resultSet.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return currentNAV;
    }


    private void saveInvestmentToDatabase(double amountInvested, double units, double currentValue, double costperunit) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Prepare the insert query for mutual fund investment
            String insertQuery = "INSERT INTO mutual_funds (user_id, fund_name, amount_invested, current_value, investment_date, scheme_code, nav, units, costperunit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            int userId = getCurrentUserId();
            savetotransactions(amountInvested, units, selectedFund.getSchemeName(), "Buy", userId);
            // Get the current user ID
            ps.setInt(1, userId);
            ps.setString(2, selectedFund.getSchemeName());
            ps.setDouble(3, amountInvested);
            ps.setDouble(4, currentValue);
            ps.setTimestamp(5, new Timestamp(new Date().getTime()));
            ps.setString(6, selectedFund.getSchemeCode());
            ps.setDouble(7, currentNav);
            ps.setDouble(8, units);
            ps.setDouble(9, costperunit);

            // Execute the insert query
            ps.executeUpdate();
            ps.close();

            // Debug: Print out investment details for verification
            System.out.println("Investment saved: " + amountInvested + " in fund " + selectedFund.getSchemeName());

            // Update the portfolio after a successful investment
            updatePortfolio(userId,selectedFund.getSchemeCode(),selectedFund.getSchemeName(),units);

            // Close the connection
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void savetotransactions(double amountInvested, double units, String fundName, String type, int userId) {
        String insertSQL = "INSERT INTO transactions (user_id, Amount, units, type1, transaction_date, fund_name, fund_type) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            // Save to transactions table for the specific user
            stmt.setInt(1, userId);  // Add the user_id to the query
            stmt.setDouble(2, amountInvested);
            stmt.setDouble(3, units);
            stmt.setString(4, type);
            stmt.setString(5, String.valueOf(java.sql.Date.valueOf(LocalDate.now())));
            stmt.setString(6, fundName);
            stmt.setString(7, "Mutual Fund");

            stmt.executeUpdate();
            System.out.println("Mutual fund transaction successfully stored in the database for user ID: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error storing mutual fund transaction: " + e.getMessage());
        }
    }





    // Method to handle the selling of a fund
    @FXML
    public void handleSellFund(ActionEvent event) throws IOException {
        MutualFund2 selectedFund = investmentTable.getSelectionModel().getSelectedItem();

        if (selectedFund != null) {
            // Confirm with the user before selling
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sell Fund");
            dialog.setHeaderText("Sell Fund: " + selectedFund.getSchemeName());
            dialog.setHeaderText("Total Units Available: " + selectedFund.getUnits());
            dialog.setContentText("Enter the Units You Want To Sell:");

            dialog.showAndWait().ifPresent(unitStr -> {
                try {
                    double unitsToSell = Double.parseDouble(unitStr);
                    double totalUnitsAvailable = selectedFund.getUnits();
                    double nav = selectedFund.getNav();

                    if (unitsToSell > totalUnitsAvailable) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid Input");
                        alert.setContentText("You do not have sufficient units!");
                        alert.showAndWait();
                        return;
                    }

                    // Calculate remaining units and updated amount invested
                    double remainingUnits = totalUnitsAvailable - unitsToSell;
                    double remainingAmountInvested = remainingUnits * nav;

                    // Get the current user ID
                    int userId = getCurrentUserId();

                    // Update portfolio and mutual_funds table
                    updatePortfolio1(userId, selectedFund.getSchemeName(), remainingUnits, remainingAmountInvested);
                    deleteFundFromDatabase(remainingAmountInvested, remainingUnits, nav, selectedFund.getSchemeCode(), userId);

                    // Log the transaction as a sell operation
                    savetotransactions(remainingAmountInvested, remainingUnits, selectedFund.getSchemeName(), "Sell", userId);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Success");
                    alert.setContentText("You have successfully sold " + unitsToSell + " units for ₹ " + (unitsToSell * nav) +
                            ". Remaining Units: " + remainingUnits + ", Remaining Amount: ₹ " + remainingAmountInvested);
                    alert.showAndWait();

                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number for the units.");
                    alert.showAndWait();
                }
            });
        }

        reloadSMFManagementScreen();
    }



    private void deleteFundFromDatabase(double remainingAmount, double remainingUnits, double nav, String schemeCode, int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();

            // If remaining units are greater than zero, update the record for the specific user
            if (remainingUnits > 0) {
                String updateQuery = "UPDATE mutual_funds SET units = ?, amount_invested = ?, current_value = ? WHERE scheme_code = ? AND user_id = ?";
                PreparedStatement ps = connection.prepareStatement(updateQuery);

                ps.setDouble(1, remainingUnits);
                ps.setDouble(2, remainingAmount);
                ps.setDouble(3, remainingUnits * nav);  // New current value based on remaining units
                ps.setString(4, schemeCode);
                ps.setInt(5, userId);  // Ensure it applies only for this user

                ps.executeUpdate();  // Execute the update
                ps.close();
            } else {
                // If remaining units are zero, delete the record for the specific user
                String deleteQuery = "DELETE FROM mutual_funds WHERE scheme_code = ? AND user_id = ?";
                PreparedStatement ps = connection.prepareStatement(deleteQuery);

                ps.setString(1, schemeCode);
                ps.setInt(2, userId);  // Ensure it deletes only for this user

                ps.executeUpdate();  // Execute the delete
                ps.close();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Method to load the FXML of each section
    private void switchToPage(ActionEvent event, String fxmlFile, String title, Button clickedButton) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        clickedButton.getStyleClass().add("sidebar-button-active");
    }

    // Sidebar button handlers
    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage(event, "PortfolioManagement.fxml", "Portfolio Management", btnPortfolio);
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage(event, "SIPManagement.fxml", "SIP Management", btnSIP);
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage(event, "MutualFunds.fxml", "Mutual Funds", btnMutualFunds);
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage(event, "ReportsAnalytics.fxml", "Reports & Analytics", btnReports);
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage(event, "TransactionHistory.fxml", "Transaction History", btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage(event, "UserProfile.fxml", "User Profile", btnProfile);
    }
}
