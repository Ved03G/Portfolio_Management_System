package org.example.portfolio_management_system;

public class Portfolio {
    private String schemeCode;
    private String schemeName;
    private double amountInvested;
    private double currentValue;
    private double units;
    private String type;

    // Constructor, getters, and setters
    public Portfolio(String schemeCode, String schemeName, double amountInvested, double currentValue, double units, String type) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.amountInvested = amountInvested;
        this.currentValue = currentValue;
        this.units = units;
        this.type = type;
    }
    public String getSchemeCode() {
        return schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public double getUnits() {
        return units;
    }

    public String getType() {
        return type;
    }


    // Add getters and setters as needed
}
