package org.example.portfolio_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class SipDataCellController {

    @FXML
    private Label sipNameLabel;
    @FXML
    private Label frequencyLabel;
    @FXML
    private Label periodLabel;
    @FXML
    private Label investedAmountLabel;
    @FXML
    private Label totalUnitsLabel;
    @FXML
    private Label currentAmountLabel;
    @FXML
    private Label returnsLabel;
    @FXML
    private SipData sipData;

    @FXML
    private SIPManagementController sipman;


    // Initialize the controller (Optional, if needed)
// Set the SIP data to the cell
    public void setSipData(SipData sipData) {
        this.sipData = sipData;
        if (sipData != null) {
            sipNameLabel.setText(sipData.getSipName());
            frequencyLabel.setText(sipData.getFrequency());
            periodLabel.setText(sipData.getPeriod()); // Calculated period
            investedAmountLabel.setText(String.format("₹%.2f", sipData.getInvestedAmount()));
            totalUnitsLabel.setText(String.format("%.2f", sipData.getTotalUnits()));

            // Current amount and returns
            double currentAmount = sipData.getCurrentAmount();
            double investedAmount = sipData.getInvestedAmount();
            double returns = sipData.getReturns();

            currentAmountLabel.setText(String.format("₹%.2f", currentAmount));
            returnsLabel.setText(String.format("₹%.2f", returns));

            // Set color based on profit or loss
            if (currentAmount > investedAmount) {
                currentAmountLabel.setStyle("-fx-text-fill: green;"); // Profit
                returnsLabel.setStyle("-fx-text-fill: green;"); // Profit
            } else if (currentAmount < investedAmount) {
                currentAmountLabel.setStyle("-fx-text-fill: red;"); // Loss
                returnsLabel.setStyle("-fx-text-fill: red;"); // Loss
            }
            else{
                currentAmountLabel.setStyle("-fx-text-fill: #a5a3a3;");
                returnsLabel.setStyle("-fx-text-fill: #a5a3a3;");
            }
        }
    }
    // Set the SIP data to the cell
    public void handlesell(ActionEvent actionEvent) {

        System.out.println("handleSell method called");

        if (sipData == null) {
            showAlert("Error", "No SIP Data", "No SIP data available for selling.");
            return; // Exit if no data
        }


        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sell Fund");
        dialog.setHeaderText("Sell Fund: " + sipData.getSipName());
        dialog.setContentText("Total Units Available: " + sipData.getTotalUnits() +
                "\nEnter the Units You Want To Sell:");

        dialog.showAndWait().ifPresent(unitStr -> {
            try {
                double unitsToSell = Double.parseDouble(unitStr);
                double availableUnits = sipData.getTotalUnits();
                double investedAmount = unitsToSell* sipData.getNav();

                // Check for sufficient units
                if (unitsToSell > availableUnits) {
                    showAlert("Error", "Invalid Input", "You Do Not Have Sufficient Units!");
                    return;
                }

                double remainingUnits = availableUnits - unitsToSell;
                double nav = sipData.getNav(); // Assuming SIP data has NAV
                double amountInvested = remainingUnits * nav;

                int userId = getCurrentUserId();
                // Store transaction and update database
                deleteFundFromDatabase(amountInvested, remainingUnits, sipData.getSip_id(),userId);
                storeSaleTransaction(amountInvested, unitsToSell, sipData.getSipName(),userId);
              //updatePortfolio(getCurrentUserId(),sipData.getFundId(),sipData.getSipName(),remainingUnits);


                // Update portfolio after the sale
                 updatePortfolio(userId, sipData.getFundId(), sipData.getSipName(), remainingUnits,amountInvested);
                // Success alert
                showAlert("Success", "Success", "You have successfully sold " + unitsToSell +
                        " units for ₹ " + investedAmount +
                        ". Remaining Units: " + remainingUnits +
                        ", Remaining Amount: " + amountInvested);
                reloadSIPManagementScreen();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid Input", "Please enter a valid number for the amount.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    private void storeSaleTransaction(double saleAmount, double unitsSold, String fundName, int userId) throws SQLException {
        String insertSQL = "INSERT INTO transactions (amount, units, type1, transaction_date, fund_name,fund_type,user_id) VALUES (?, ?, 'Sell', ?, ?,'SIP',?) ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setDouble(1, saleAmount);
            stmt.setDouble(2, unitsSold);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setString(4, fundName);
            stmt.setInt(5, userId);

            stmt.executeUpdate();
        }
    }

    private void reloadSIPManagementScreen() throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage) investedAmountLabel.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }
    }
    // Utility method for showing alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void deleteFundFromDatabase(double amount, double units, int sip_id, int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String deleteQuery = "UPDATE sip Set total_units=?,sip_amount=? where sip_id=? and user_id=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);

            ps.setDouble(1, units);// Set the scheme code of the selected fund
            ps.setDouble(2, amount);
            ps.setInt(3, sip_id);
            ps.setInt(4, userId);
            ps.executeUpdate();  // Execute the deletion

            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String deleteQuery = "DELETE FROM sip WHERE sip.total_units=? and user_id=?";
        try(Connection conn=DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, 0);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePortfolio(int userId, String fund_id, String fundname, double totalUnits,double amountInvested) throws SQLException {
        try {
            Connection connection = DatabaseConnection.getConnection();

            // Get the total invested amount and current value for this user from mutual_funds
            String sumQuery = "SELECT SUM(amount_invested) AS totalAmountInvested, SUM(current_value) AS totalCurrentValue " +
                    "FROM mutual_funds WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sumQuery);
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                double totalAmountInvested = amountInvested;
                double totalCurrentValue = totalUnits* sipData.getNav();

                // Check if a portfolio entry for this user and fund already exists
                String checkQuery = "SELECT * FROM portfolio WHERE user_id = ? AND scheme_code = ?";
                PreparedStatement checkPs = connection.prepareStatement(checkQuery);
                checkPs.setInt(1, userId);
                checkPs.setString(2, fund_id);
                ResultSet checkResultSet = checkPs.executeQuery();

                if (checkResultSet.next()) {
                    // If entry exists, update the existing portfolio record
                    String updateQuery = "UPDATE portfolio SET amount_invested = ?, current_value = ?, units = ?, fund_name = ? " +
                            "WHERE user_id = ? AND scheme_code = ?";
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setDouble(1, totalAmountInvested);
                    updatePs.setDouble(2, totalCurrentValue);
                    updatePs.setDouble(3, totalUnits);
                    updatePs.setString(4, fundname);
                    updatePs.setInt(5, userId);
                    updatePs.setString(6, fund_id);
                    updatePs.executeUpdate();
                    updatePs.close();
                } else {
                    // If no entry exists, you might want to insert a new record (optional)
                    String insertQuery = "INSERT INTO portfolio (user_id, amount_invested, current_value, type, scheme_code, fund_name, units) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                    insertPs.setInt(1, userId);
                    insertPs.setDouble(2, totalAmountInvested);
                    insertPs.setDouble(3, totalCurrentValue);
                    insertPs.setString(4, "SIP");
                    insertPs.setString(5, fund_id);
                    insertPs.setString(6, fundname);
                    insertPs.setDouble(7, totalUnits);
                    insertPs.executeUpdate();
                    insertPs.close();
                }

                checkResultSet.close();
                checkPs.close();
            }

            resultSet.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
