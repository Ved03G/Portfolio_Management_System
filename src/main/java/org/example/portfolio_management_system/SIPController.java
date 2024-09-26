package org.example.portfolio_management_system;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

public class SIPController {

    @FXML
    private TextField fundSearchField;
    @FXML
    private ListView<MutualFund> fundListView;
    @FXML
    private TextField sipAmountField;
    @FXML
    private ChoiceBox<String> frequencyChoiceBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label navLabel;
    @FXML
    private Label totalUnitsLabel;

    private ObservableList<MutualFund> mutualFunds = FXCollections.observableArrayList();
    private FilteredList<MutualFund> filteredMutualFunds;

    @FXML
    public void initialize() {
        loadMutualFunds();
        setupSearchFilter();
        loadFrequencyOptions();
    }

    // Load mutual funds into the ObservableList
    private void loadMutualFunds() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mfapi.in/mf"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            for (JsonNode fundNode : rootNode) {
                String fundId = fundNode.path("schemeCode").asText();
                String schemeName = fundNode.path("schemeName").asText();
                String fundType = fundNode.path("fundType").asText();
                String category = fundNode.path("category").asText();
                mutualFunds.add(new MutualFund(fundId, schemeName, fundType, category, ""));
            }

            filteredMutualFunds = new FilteredList<>(mutualFunds, p -> true);
            fundListView.setItems(filteredMutualFunds);

            fundListView.setVisible(false); // Initially hide ListView

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading mutual funds: " + e.getMessage());
        }
    }

    // Set up search filter for mutual funds
    private void setupSearchFilter() {
        fundSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                filteredMutualFunds.setPredicate(fund -> false); // Hide ListView if search is empty
                fundListView.setVisible(false);
            } else {
                filteredMutualFunds.setPredicate(fund -> fund.getSchemeName().toLowerCase().contains(newValue.toLowerCase()));
                fundListView.setVisible(!filteredMutualFunds.isEmpty()); // Show ListView if there are results
            }
        });

        // Handle selection from ListView
        fundListView.setOnMouseClicked((MouseEvent event) -> {
            MutualFund selectedFund = fundListView.getSelectionModel().getSelectedItem();
            if (selectedFund != null) {
                fundSearchField.setText(selectedFund.getSchemeName());
                fundListView.setVisible(false); // Hide ListView after selection
            }
        });
    }

    // Load frequency options for SIP
    private void loadFrequencyOptions() {
        ObservableList<String> frequencyOptions = FXCollections.observableArrayList("Monthly", "Quarterly", "Yearly");
        frequencyChoiceBox.setItems(frequencyOptions);
    }

    // Handle SIP investment
    public void handleSIPInvestment() {
        String selectedFundName = fundSearchField.getText();
        MutualFund selectedFund = findFundByName(selectedFundName);

        if (selectedFund == null) {
            System.out.println("No valid fund selected.");
            navLabel.setText("No valid fund selected.");
            return;
        }

        String sipAmountText = sipAmountField.getText();
        if (sipAmountText == null || sipAmountText.isEmpty()) {
            System.out.println("SIP amount is empty.");
            navLabel.setText("Please enter SIP amount.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String frequency = frequencyChoiceBox.getValue();

        if (startDate == null || endDate == null || frequency == null) {
            System.out.println("Start date, end date, or frequency is not selected.");
            navLabel.setText("Select all SIP details.");
            return;
        }

        try {
            double sipAmount = Double.parseDouble(sipAmountText);
            String fundId = selectedFund.getsipid();

            String fundDataUrl = "https://api.mfapi.in/mf/" + fundId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fundDataUrl))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            MutualFund fundWithNav = parseFundDetails(jsonResponse);
            if (fundWithNav == null || fundWithNav.getNav() == null) {
                System.out.println("Failed to fetch NAV.");
                navLabel.setText("Failed to fetch NAV.");
                return;
            }

            double nav = Double.parseDouble(fundWithNav.getNav());
            double totalUnits = sipAmount / nav;

            navLabel.setText(String.format("NAV: %.2f", nav));
            totalUnitsLabel.setText(String.format("Total Units: %.2f", totalUnits));

            String schemeName = fundWithNav.getSchemeName();

            // Get the current user's ID
            int userId = getCurrentUserId();

            // Store SIP details and transactions with the current user's ID
            storeSIPDetails(userId, selectedFund, sipAmount, totalUnits, startDate, endDate, frequency, schemeName);
            storeTransactionData(userId, sipAmount, totalUnits, schemeName);

        } catch (NumberFormatException e) {
            System.out.println("Invalid SIP amount entered.");
            navLabel.setText("Invalid SIP amount.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error fetching NAV data: " + e.getMessage());
            navLabel.setText("Error fetching NAV.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during SIP investment: " + e.getMessage());
            navLabel.setText("Error during SIP investment.");
        }

        Stage stage = (Stage) navLabel.getScene().getWindow();
        try {
            reloadSIPManagementScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadSIPManagementScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));
        Parent newRoot = loader.load();
        Stage currentStage = (Stage) sipAmountField.getScene().getWindow();
        if (currentStage != null) {
            currentStage.setScene(new Scene(newRoot));
            currentStage.show();
        } else {
            System.out.println("Current stage is null");
        }
    }

    private MutualFund parseFundDetails(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject metaObject = jsonObject.getAsJsonObject("meta");
        String schemeName = metaObject.has("scheme_name") ? metaObject.get("scheme_name").getAsString() : "Unknown Scheme";
        String fundId = metaObject.has("scheme_code") ? metaObject.get("scheme_code").getAsString() : "Unknown Scheme";
        String fundType = metaObject.has("scheme_type") ? metaObject.get("scheme_type").getAsString() : "Unknown Scheme";
        String category = metaObject.has("scheme_category") ? metaObject.get("scheme_category").getAsString() : "Unknown Scheme";
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        String nav = "Unknown NAV";
        if (dataArray.size() > 0) {
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            nav = dataObject.has("nav") ? dataObject.get("nav").getAsString() : "Unknown NAV";
        }
        return new MutualFund(fundId, schemeName, fundType, category, nav);
    }



    // Helper method to find MutualFund by name
    private MutualFund findFundByName(String schemeName) {
        for (MutualFund fund : mutualFunds) {
            if (fund.getSchemeName().equals(schemeName)) {
                return fund;
            }
        }
        return null;
    }
    public class MutualFund {
        private String sipid;
        private String schemeName;
        private String fundType;
        private String category;
        private String nav;

        public MutualFund(String sipid, String schemeName, String fundType, String category, String nav) {
            this.sipid = sipid;
            this.schemeName = schemeName;
            this.fundType = fundType;
            this.category = category;
            this.nav = nav;
        }



        // Getters
        public String getsipid() {
            return sipid;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public String getNav() {
            return nav;
        }

        // Override toString() to display fund name in ComboBox
        @Override
        public String toString() {
            return schemeName;
        }

    }
    public void backtosipManagement(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage) sipAmountField.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }

    }
    private void storeSIPDetails(int userId,MutualFund fund, double sipAmount, double totalUnits, LocalDate startDate, LocalDate endDate, String frequency, String fund_Name) {
        String insertSQL = "INSERT INTO sip (user_id, fund_id, sip_amount, total_units, start_date, end_date, frequency, fund_Name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            int userid = getCurrentUserId();
            updatePortfolio(userid,fund.getsipid(),fund_Name,totalUnits);

            // Set values for the SQL statement
            stmt.setInt(1, userId);

            stmt.setString(2, fund.getsipid());
            stmt.setDouble(3, sipAmount);
            stmt.setDouble(4, totalUnits);
            stmt.setDate(5, java.sql.Date.valueOf(startDate));
            stmt.setDate(6, java.sql.Date.valueOf(endDate));
            stmt.setString(7, frequency);
            stmt.setString(8, fund_Name);


            // Execute the update and confirm
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("SIP data successfully stored in the database.");
            } else {
                System.out.println("No SIP data inserted. Check the values.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Prints detailed error trace
            System.out.println("Error storing SIP data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("General error: " + e.getMessage());
        }
    }

    private void storeTransactionData(int userId ,double sipAmount, double totalUnits, String schemename) {

        String insertSQL = "INSERT INTO transactions (user_id,Amount, units, type1, transaction_date, fund_name, fund_type) VALUES (?,?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            // Set values for the SQL statement
            stmt.setInt(1, userId);
            stmt.setDouble(2, sipAmount);
            stmt.setDouble(3, totalUnits);
            stmt.setString(4, "Buy");
            stmt.setString(5, String.valueOf(java.sql.Date.valueOf(LocalDate.now())));
            stmt.setString(6, schemename);
            stmt.setString(7, "SIP");

            // Execute the update and confirm
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Transaction data successfully stored in the database.");
            } else {
                System.out.println("No transaction data inserted. Check the values.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Prints detailed error trace
            System.out.println("Error storing transaction data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("General error: " + e.getMessage());
        }
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    public void updatePortfolio(int userId,String fund_id,String fundname,double totalUnits) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sumQuery = "SELECT SUM(amount_invested) AS totalAmountInvested, SUM(current_value) AS totalCurrentValue " +
                    "FROM mutual_funds WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sumQuery);
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                double totalAmountInvested = resultSet.getDouble("totalAmountInvested");
                double totalCurrentValue = resultSet.getDouble("totalCurrentValue");

                String insertQuery = "INSERT INTO portfolio (user_id, amount_invested, current_value,type,scheme_code,fund_name,units) VALUES (?, ?, ?,?,?,?,?)";
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

            resultSet.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
