package org.example.portfolio_management_system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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
import java.util.List;
import java.util.Objects;

public class SIPManagementController {

    @FXML
    public ListView<SipData> sipListView;
    @FXML
    public TextField searchField;
    @FXML
    public Button btnAddSIP;
    @FXML
    public Button btnSellSIP;
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Button btnPortfolio,btnSIP,btnMutualFunds,btnReports,btnTransactions,btnProfile;
    @FXML
    private Label navLabel;

    @FXML
    private ObservableList<SipData> sipDataList = FXCollections.observableArrayList();
    private ObservableList<SipData> originalSipDataList = FXCollections.observableArrayList(); // For filtering

    //    private static final String DB_URL = "jdbc:mysql://localhost:3306/mutualfundsdb";
//    private static final String DB_USER = "root";
//    private static final String DB_PASSWORD = "Servesh#21";
    // Method to load the FXML of each section
    private void switchToPage2(ActionEvent event, String fxmlFile, String title,Button clickedButton) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        resetButtonStyles();
        clickedButton.getStyleClass().add("sidebar-button-active");
    }
    private void resetButtonStyles() {
        java.util.List<Button> buttons = List.of(btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile);
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage2(event, "PortfolioManagement.fxml", "Portfolio Management", btnPortfolio);
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage2(event, "SIPManagement.fxml", "SIP Management",btnSIP);
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage2(event, "MutualFunds.fxml", "Mutual Funds",btnMutualFunds);
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage2(event, "ReportsAnalytics.fxml", "Reports & Analytics",btnReports);
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage2(event, "TransactionHistory.fxml", "Transaction History",btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage2(event, "UserProfile.fxml", "User Profile",btnProfile);
    }
    public void handleStartsipclick(ActionEvent event) throws IOException {
        // Load the addsip.fxml file
        Parent root = FXMLLoader.load(getClass().getResource("addsip.fxml"));

        // Create a new Stage for the "Start SIP" window
        Stage sipStage = new Stage();
        sipStage.setTitle("Start New SIP");

        // Set the scene for the new stage
        Scene sipScene = new Scene(root);
        sipStage.setScene(sipScene);

        // Show the new stage (window)
        sipStage.show();
        closesipmanagement();

    }

    private void closesipmanagement() {
        Stage stage = (Stage) sipListView.getScene().getWindow();
        stage.close();
    }


    @FXML
    private void initialize() {

//        sipListView.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        // Set cell factory

        sipListView.setCellFactory(listView -> new ListCell<SipData>() {
            private final FXMLLoader loader = new FXMLLoader(getClass().getResource("sipdatacell.fxml"));
            private final AnchorPane cell;


            {
                try {
                    cell = loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                cell.setOnMouseEntered(event -> cell.setStyle("-fx-background-color: #e0e0e0;"));
//                cell.setOnMouseExited(event -> cell.setStyle("-fx-background-color: transparent;"));
                cell.setOnMouseEntered(event -> {
                    cell.setScaleX(1.02); // Scale 5% larger
                    cell.setScaleY(1.02);
                });

                cell.setOnMouseExited(event -> {
                    cell.setScaleX(1); // Reset to normal size
                    cell.setScaleY(1);
                });

            }


            @Override
            protected void updateItem(SipData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    SipDataCellController controller = loader.getController();
                    controller.setSipData(item); // Ensure this is set correctly
                    setGraphic(cell);
                }
                setPadding(new Insets(10));
            }
        });

        // Load SIP data from the database
        loadSipData();

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSipData(newValue));
        addHoverEffect(btnPortfolio);
        addHoverEffect(btnSIP);
        addHoverEffect(btnMutualFunds);
        addHoverEffect(btnReports);
        addHoverEffect(btnTransactions);
        addHoverEffect(btnProfile);
    }
    private void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.1); // Enlarge button by 10%
            button.setScaleY(1.1);
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1.0); // Reset to original size
            button.setScaleY(1.0);
        });
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    // Load SIP data from the database
    private void loadSipData() {
        int userid=getCurrentUserId();
        String query = "SELECT sip_id,fund_Name, frequency, start_date, end_date, sip_amount, total_units,fund_id FROM sip where user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int sipId = rs.getInt("sip_id");
                String sipName = rs.getString("fund_Name");
                String frequency = rs.getString("frequency");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double investedAmount = rs.getDouble("sip_amount");
                double totalUnits = rs.getDouble("total_units");
                String fundId = rs.getString("fund_id");
                double currentAmount = fetchCurrentAmount(sipName, totalUnits);
                double returns = currentAmount - investedAmount;
                double nav = getCurrentNavForFund(sipName);

                SipData sipData = new SipData(sipId, sipName, frequency, startDate, endDate, investedAmount, totalUnits, currentAmount, returns, nav,fundId);
                sipDataList.add(sipData);
            }
            originalSipDataList.setAll(sipDataList); // Store original data for filtering
            sipListView.setItems(sipDataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sipListView.setItems(sipDataList);
    }

    // Fetch the current amount based on total units and current NAV
    private double fetchCurrentAmount(String sipName, double totalUnits) {
        double nav = getCurrentNavForFund(sipName); // Assuming sipName is used as fundId for NAV lookup
        return totalUnits * nav;
    }

    // Fetch the current NAV for the SIP (This method should call your API or database)
    private double fetchNavForFund(String fundId) {
        String navApiUrl = "https://api.mfapi.in/mf/" + fundId;

        // Define your proxy settings
        String proxyHost = "10.0.1.6"; // Replace with your proxy host
        int proxyPort = 8030; // Replace with your proxy port

        // Create a proxy instance
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

        // Create an HttpClient with proxy settings
        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort))) // Set the proxy
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(navApiUrl))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode navNode = rootNode.path("data").get(0).path("nav");

            return navNode.asDouble();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // Fetch fund ID from fund name
    private String getFundIdFromName(String fundName) {
        String query = "SELECT fund_id FROM sip WHERE fund_Name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fundName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("fund_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double getCurrentNavForFund(String fundName) {
        String fundId = getFundIdFromName(fundName);
        if (fundId == null) {
            return 0.0;
        }
        return fetchNavForFund(fundId);
    }

    // Filter SIP data based on search input
    private void filterSipData(String searchText) {
        ObservableList<SipData> filteredList = FXCollections.observableArrayList();
        for (SipData data : sipListView.getItems()) {
            if (data.getSipName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(data);
            }
        }
        sipListView.setItems(filteredList);
    }



}
