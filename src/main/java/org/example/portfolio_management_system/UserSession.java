package org.example.portfolio_management_system;

public class UserSession {
    private static UserSession instance;
    private int userId;

    private UserSession(int userId) {
        this.userId = userId;
    }

    // Always create a new session for each login
    public static void setInstance(int userId) {
        instance = new UserSession(userId);
    }

    // Get the current session instance
    public static UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User not logged in");
        }
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    // Optional method to clear the session if needed
    public static void clearSession() {
        instance = null;
    }
}


