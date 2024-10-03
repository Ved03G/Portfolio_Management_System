package org.example.portfolio_management_system;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class MutualFundFetcher {

    private String proxyHost;
    private int proxyPort;

    // Constructor to accept proxy details
    public MutualFundFetcher(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    // Create connection using proxy settings (always uses proxy unless no proxy is specified)
    private HttpURLConnection createConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        Proxy proxy = null;

        // If proxy details are provided, set the proxy
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != 0) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }

        HttpURLConnection conn;
        if (proxy != null) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setRequestMethod("GET");
        return conn;
    }

    // Fetch fund data by scheme code with proxy
    public JSONObject fetchFundDataBySchemeCode(String schemeCode) {
        try {
            HttpURLConnection conn = createConnection("https://api.mfapi.in/mf/" + schemeCode);
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch all API data with proxy
    public JSONArray fetchData() {
        try {
            HttpURLConnection conn = createConnection("https://api.mfapi.in/mf");
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JSONParser parser = new JSONParser();
                return (JSONArray) parser.parse(reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Display fetched data in the application
    public void displayFundsInApplication() {
        JSONArray mutualFunds = fetchData();
        if (mutualFunds != null) {
            for (Object obj : mutualFunds) {
                JSONObject fund = (JSONObject) obj;
                System.out.println("Scheme Name: " + fund.get("schemeName"));
                System.out.println("NAV: " + fund.get("nav"));
                System.out.println("Date: " + fund.get("date"));
                System.out.println("Scheme Type: " + fund.get("schemeType"));
                System.out.println("-------------------");
            }
        }
    }
}
