package org.example.portfolio_management_system;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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


    private static final String PROXY_HOST = "10.0.1.6"; // Replace with your proxy host
    private static final int PROXY_PORT = 8030;
    @FXML
    public void initialize() {
        loadMutualFunds();
        setupSearchFilter();
        loadFrequencyOptions();
    }
    private HttpClient createHttpClient() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        return HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, PROXY_PORT)))
                .build();
    }


    public boolean isFundClosed(String fundId) {
        try {
            HttpClient client = createHttpClient(); // Use the proxy-enabled HttpClient
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mfapi.in/mf/" + fundId))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray dataArray = jsonObject.getAsJsonArray("data");

            if (dataArray.size() > 0) {
                JsonObject lastEntry = dataArray.get(0).getAsJsonObject();
                String dateString = lastEntry.get("date").getAsString();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate lastRecordedDate = LocalDate.parse(dateString, formatter);

                LocalDate currentDate = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(lastRecordedDate, currentDate);

                return daysBetween > 30;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Load mutual funds into the ObservableList
    private void loadMutualFunds() {
        HttpClient client = createHttpClient(); // Use the proxy-enabled HttpClient
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
            fundListView.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading mutual funds: " + e.getMessage());
        }
    }

    // Set up search filter for mutual funds
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

                // Fetch NAV and display in Alert
                String nav = String.valueOf(fetchNAVForFund(selectedFund.getsipid()));  // Method to fetch NAV
                if (nav != null) {
                    showNavAlert(selectedFund.getSchemeName(), nav); // Show NAV in Alert
                } else {
                    showAlert("Error", "Failed to fetch NAV for the selected fund.");
                }
            }
        });
    }
    private void showNavAlert(String schemeName, String nav) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("NAV Information");
        alert.setHeaderText(null);
        alert.setContentText("The current NAV for " + schemeName + " is: " + nav);
        alert.showAndWait();
    }


    // Fetch NAV for a specific fund using proxy settings
    private double fetchNAVForFund(String fundId) {
        double navValue = 0.0;

        try {
            HttpClient client = createHttpClient(); // Use the proxy-enabled HttpClient
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mfapi.in/mf/" + fundId + "/nav"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (jsonObject.has("data")) {
                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                if (dataArray.size() > 0) {
                    JsonObject navEntry = dataArray.get(0).getAsJsonObject();
                    navValue = navEntry.get("nav").getAsDouble();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching NAV for fund: " + e.getMessage());
        }

        return navValue;
    }

    // Handle SIP investment using proxy settings
    public void handleSIPInvestment(String fundId, double sipAmount) {
        try {
            double navValue = fetchNAVForFund(fundId); // Fetch NAV using proxy settings
            if (navValue > 0) {
                double unitsPurchased = sipAmount / navValue;
                System.out.println("SIP Investment for Fund ID " + fundId + ": " + unitsPurchased + " units purchased.");
                // Add any additional logic for SIP handling (e.g., storing the investment)
            } else {
                System.out.println("Invalid NAV value retrieved for Fund ID: " + fundId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error handling SIP investment: " + e.getMessage());
        }
    }

    // Load frequency options for SIP
    private void loadFrequencyOptions() {
        ObservableList<String> frequencyOptions = FXCollections.observableArrayList("Monthly", "Quarterly", "Yearly");
        frequencyChoiceBox.setItems(frequencyOptions);
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
        String insertSQL = "INSERT INTO sip (user_id, fund_id, sip_amount, total_units, start_date, end_date, frequency, fund_Name) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            int userid = getCurrentUserId();
            updatePortfolio(userid,sipAmount,fund.getsipid(),fund_Name,totalUnits);

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

    private void storeTransactionData(MutualFund fund ,int userId ,double sipAmount, double totalUnits, String schemename) {

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
    public void updatePortfolio(int userId, double amountInvested, String fundId, String fundName, double totalUnits) {
        try {
            // Get the connection
            Connection connection = DatabaseConnection.getConnection();

            double currentValue = amountInvested;

            // Insert into portfolio table
            String insertQuery = "INSERT INTO portfolio (user_id, amount_invested, current_value, type, scheme_code, fund_name, units) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement insertPs = connection.prepareStatement(insertQuery);
            insertPs.setInt(1, userId);               // Set user ID
            insertPs.setDouble(2, amountInvested);    // Set amount invested
            insertPs.setDouble(3, currentValue);      // Set calculated current value
            insertPs.setString(4, "SIP");             // Set type (assuming this is always "SIP")
            insertPs.setString(5, fundId);            // Set scheme code (fund_id)
            insertPs.setString(6, fundName);          // Set fund name
            insertPs.setDouble(7, totalUnits);        // Set total units

            // Execute the insert query
            insertPs.executeUpdate();

            // Close the resources
            insertPs.close();
            connection.close();

            System.out.println("Portfolio updated successfully for user: " + userId + ", fund: " + fundName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
