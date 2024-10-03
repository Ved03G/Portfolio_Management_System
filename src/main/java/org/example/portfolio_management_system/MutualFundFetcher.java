package org.example.portfolio_management_system;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class MutualFundFetcher {

    // Fetch fund data by scheme code (latest NAV)
    public JSONObject fetchFundDataBySchemeCode(String schemeCode) {
        try {
            URL url = new URL("https://api.mfapi.in/mf/" + schemeCode + "/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Enable GZIP Compression
            conn.setRequestProperty("Accept-Encoding", "gzip");

            // Use larger buffer for reading data
            InputStreamReader reader = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader, 16384); // 8KB buffer

            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(bufferedReader);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch all mutual fund data
    public JSONArray fetchData() {
        try {
            URL url = new URL("https://api.mfapi.in/mf");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Enable GZIP Compression
            conn.setRequestProperty("Accept-Encoding", "gzip");

            // Use larger buffer for reading data
            InputStreamReader reader = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader, 8192); // 8KB buffer

            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(bufferedReader);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Display mutual fund data in application
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
