package org.example.portfolio_management_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportsAnalyticsController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private PieChart portfolioPieChart;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private LineChart<String, Number> mutualFundLineChart;

    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    public void initialize() {
        // Initialize PieChart data
     populatePieChart();

        // Call to show the line chart
        List<String> fundIds = getFundIdsFromDatabase();

        // Assuming you want to display data for the last 5 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(5);

        // Fetch and display the daily current values
        showDailyCurrentValueChart(lineChart, fundIds, startDate, endDate);
        showDailyCurrentValueChartForMutualFunds(mutualFundLineChart, fundIds, startDate, endDate);
        // Add hover effects to buttons
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

    private List<String> getFundIdsFromDatabase() {
        Set<String> fundIds = new HashSet<>();
        int userId = UserSession.getInstance().getUserId();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21")) {
            // Mutual Funds Query
            String mutualFundsQuery = "SELECT scheme_code FROM mutual_funds WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(mutualFundsQuery)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSetMutualFunds = preparedStatement.executeQuery()) {
                    while (resultSetMutualFunds.next()) {
                        fundIds.add(resultSetMutualFunds.getString("scheme_code"));
                    }
                }
            }

            // SIP Query
            String sipQuery = "SELECT fund_id FROM sip WHERE user_id = ?";
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(sipQuery)) {
                preparedStatement1.setInt(1, userId);
                try (ResultSet resultSetSip = preparedStatement1.executeQuery()) {
                    while (resultSetSip.next()) {
                        fundIds.add(resultSetSip.getString("fund_id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(fundIds);
    }


    // Fetch data from the database and populate the PieChart
    // Method to populate the PieChart with data from the portfolio table (SIP and Mutual Funds combined)
    private void populatePieChart() {
        portfolioPieChart.getData().clear(); // Clear existing data from the PieChart
        int userId = UserSession.getInstance().getUserId(); // Get the user ID from the session
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21")) {

            // Query to get the total amount invested grouped by type (SIP or Mutual Funds)
            String query = "SELECT type, SUM(amount_invested) AS Count FROM portfolio WHERE user_id = ? GROUP BY type";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            // Iterate through the result set and add the data to the PieChart
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                double amountInvested = resultSet.getDouble("Count");
                portfolioPieChart.getData().add(new PieChart.Data(type, amountInvested));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Fetch NAV for a specific date from the API
    // Fetch NAV for a specific date from the API, or get the closest available NAV
    public double getNavForDate(String fundId, String date) {
        double nav = 0.0;
        String apiUrl = "https://api.mfapi.in/mf/" + fundId;

        // Set proxy settings
        System.setProperty("http.proxyHost", "10.0.1.6"); // Replace with your proxy host
        System.setProperty("http.proxyPort", "8030"); // Replace with your proxy port
//        System.setProperty("https.proxyHost", "your.proxy.host"); // Replace with your proxy host
//        System.setProperty("https.proxyPort", "your_proxy_port"); // Replace with your proxy port

        try {
            // Create the URL connection to the API
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check for a valid response code from the API
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the response from the API
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray data = jsonResponse.getJSONArray("data");

                // Variables to store the nearest available NAV
                String closestDate = null;
                double closestNav = 0.0;

                // Loop through the data array to find the NAV for the specified date or the closest one
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.getJSONObject(i);
                    String navDate = entry.getString("date");
                    double currentNav = entry.getDouble("nav");

                    // If NAV for the exact date is found, return it
                    if (navDate.equals(date)) {
                        return currentNav;
                    }

                    // Check if the current date is closer than the previous closest date
                    if (closestDate == null || isCloserDate(date, navDate, closestDate)) {
                        closestDate = navDate;
                        closestNav = currentNav;
                    }
                }

                // If no exact match, return the closest available NAV
                if (closestDate != null) {
                    System.out.println("Using NAV from closest date: " + closestDate);
                    nav = closestNav;
                } else {
                    System.out.println("No NAV data available for or near the requested date: " + date);
                }

            } else {
                System.out.println("Error: Received HTTP response code " + responseCode);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error: Invalid fund ID or data not found for URL: " + apiUrl);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nav;
    }


    // Helper method to determine which date is closer to the target date
    private boolean isCloserDate(String targetDate, String newDate, String currentClosestDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Parse the dates
        LocalDate target = LocalDate.parse(targetDate, formatter);
        LocalDate newLocalDate = LocalDate.parse(newDate, formatter);
        LocalDate closestLocalDate = LocalDate.parse(currentClosestDate, formatter);

        // Return true if the new date is closer to the target date than the current closest date
        return Math.abs(target.toEpochDay() - newLocalDate.toEpochDay()) < Math.abs(target.toEpochDay() - closestLocalDate.toEpochDay());
    }


    // Method to populate the current value line chart
    // Method to populate the current value and invested value line chart for SIPs
    public void showDailyCurrentValueChart(LineChart<String, Number> lineChart, List<String> fundIds, LocalDate startDate, LocalDate endDate) {
        lineChart.getData().clear();

        // Series for total current value
        XYChart.Series<String, Number> totalValueSeries = new XYChart.Series<>();
        totalValueSeries.setName("Total Current Value (SIPs)");

        // Series for total invested value
        XYChart.Series<String, Number> investedValueSeries = new XYChart.Series<>();
        investedValueSeries.setName("Total Invested Value (SIPs)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Iterate through each date from startDate to endDate
        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            double totalCurrentValue = 0.0;
            double totalInvestedValue = 0.0;
            boolean hasInvestment = false;

            // Check each fund ID
            for (String fundId : fundIds) {
                // Get invested amount and total units for the current fund from SIPs
                double investedAmountSIP = getInvestedAmountForFund(fundId);
                double totalUnitsSIP = getTotalUnitsInvestedForFund(fundId);

                // Fetch the NAV for the current date for SIP
                double navSIP = getNavForDate(fundId, currentDate.format(formatter));

                // Only consider SIP fund if it was purchased on or before the current date
                if (isFundPurchasedOnOrBefore(fundId, currentDate) && totalUnitsSIP > 0) {
                    if (navSIP != 0.0) {
                        double currentValueSIP = navSIP * totalUnitsSIP;
                        totalCurrentValue += currentValueSIP;
                    } else {
                        totalCurrentValue += investedAmountSIP; // Use invested amount if NAV is 0
                    }
                    totalInvestedValue += investedAmountSIP; // Track invested amount
                    hasInvestment = true; // At least one SIP investment exists for the day
                }
            }

            // Add data to the chart only if there was an investment on that day
            if (hasInvestment) {
                totalValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalCurrentValue));
                investedValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalInvestedValue));
            }
        }

        // Add both series to the line chart
        lineChart.getData().add(totalValueSeries);
        lineChart.getData().add(investedValueSeries);
    }

    // Method to populate the current value and invested value line chart for Mutual Funds
    public void showDailyCurrentValueChartForMutualFunds(LineChart<String, Number> lineChart, List<String> fundIds, LocalDate startDate, LocalDate endDate) {
        lineChart.getData().clear();

        // Series for total current value
        XYChart.Series<String, Number> totalValueSeries = new XYChart.Series<>();
        totalValueSeries.setName("Total Current Value (Mutual Funds)");

        // Series for total invested value
        XYChart.Series<String, Number> investedValueSeries = new XYChart.Series<>();
        investedValueSeries.setName("Total Invested Value (Mutual Funds)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Iterate through each date from startDate to endDate
        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            double totalCurrentValue = 0.0;
            double totalInvestedValue = 0.0;
            boolean hasInvestment = false;

            // Check each fund ID
            for (String fundId : fundIds) {
                double investedAmountMF = getInvestedAmount(fundId);
                double totalUnitsMF = getTotalValueForFund(fundId);
                double navMF = getNavForDate(fundId, currentDate.format(formatter));

                // Only consider mutual fund if it was purchased on or before the current date
                if (isMutualFundPurchasedOnOrBefore(fundId, currentDate) && totalUnitsMF > 0) {
                    if (navMF != 0.0) {
                        double currentValueMF = navMF * totalUnitsMF;
                        totalCurrentValue += currentValueMF;
                    } else {
                        totalCurrentValue += investedAmountMF;
                    }
                    totalInvestedValue += investedAmountMF; // Track invested amount
                    hasInvestment = true;
                }
            }

            // Add data to the chart only if there was an investment on that day
            if (hasInvestment) {
                totalValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalCurrentValue));
                investedValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalInvestedValue));
            }
        }

        // Add both series to the line chart
        lineChart.getData().add(totalValueSeries);
        lineChart.getData().add(investedValueSeries);
    }


    // Method to check if the fund was purchased on or before a given date
    private boolean isFundPurchasedOnOrBefore(String fundId, LocalDate date) {
        boolean purchased = false;
        int userId = UserSession.getInstance().getUserId();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM sip WHERE fund_id = ? AND start_date <= ? and user_id=?")) {
            statement.setString(1, fundId);
            statement.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // Use database format
            statement.setInt(3, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                purchased = resultSet.getInt("count") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchased;
    }

    // Method to check if the mutual fund was purchased on or before a given date
    private boolean isMutualFundPurchasedOnOrBefore(String fundId, LocalDate date) {
        boolean purchased = false;
        int userId = UserSession.getInstance().getUserId();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM mutual_funds WHERE scheme_code = ? AND mutual_funds.investment_date <= ? and user_id=?")) {
            statement.setString(1, fundId);
            statement.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));// Use database format
            statement.setInt(3, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                purchased = resultSet.getInt("count") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchased;
    }

    // Get the invested amount for a given fund ID from SIPs
    private double getInvestedAmountForFund(String fundId) {
        double amount = 0.0;
        int userId = UserSession.getInstance().getUserId();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(sip_amount) AS total FROM sip WHERE fund_id = ? and user_id=?")) {
            statement.setString(1, fundId);
            statement.setInt(2, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                amount = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }
    private double getInvestedAmount(String fundId) {
        double amount = 0.0;
        int userId = UserSession.getInstance().getUserId();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(amount_invested) AS total FROM mutual_funds WHERE scheme_code = ? and user_id=?")) {
            statement.setString(1, fundId);
            statement.setInt(2, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                amount = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }
    private double getTotalValueForFund(String fundId) {

        double totalUnits = 0.0;
        int userId = UserSession.getInstance().getUserId();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(units) AS total FROM mutual_funds WHERE scheme_code = ? AND user_id=?")) {
            statement.setString(1, fundId);
            statement.setInt(2, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                totalUnits = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalUnits;
    }

    // Get the total units invested for a given fund ID from SIPs
    private double getTotalUnitsInvestedForFund(String fundId) {
        double totalUnits = 0.0;
        int userId = UserSession.getInstance().getUserId();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp2", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(sip.total_units) AS total FROM sip WHERE fund_id = ? and user_id=?")) {
            statement.setString(1, fundId);
            statement.setInt(2, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                totalUnits = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalUnits;
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
}
