package com.example.securefiletransfer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/secure_file_transfer";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Sql@2112";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    //  Initialize DB — keeps your existing logic
    private static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            String checkColumn = "SELECT COUNT(*) FROM information_schema.columns " +
                                 "WHERE table_schema = 'secure_file_transfer' " +
                                 "AND table_name = 'requests' " +
                                 "AND column_name = 'expiry_time'";
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(checkColumn);
            rs.next();
            if (rs.getInt(1) == 0) {
                String alterTable = "ALTER TABLE requests " +
                                    "ADD COLUMN expiry_time TIMESTAMP NULL, " +
                                    "ADD COLUMN previous_request_id INT NULL, " +
                                    "MODIFY COLUMN status ENUM('pending', 'approved', 'approved-expired', 'rejected', 'expired') " +
                                    "NOT NULL DEFAULT 'pending'";
                stmt.executeUpdate(alterTable);

                String addForeignKey = "ALTER TABLE requests " +
                                     "ADD CONSTRAINT fk_previous_request " +
                                     "FOREIGN KEY (previous_request_id) " +
                                     "REFERENCES requests(id) ON DELETE SET NULL";
                stmt.executeUpdate(addForeignKey);
            }
        }
    }

    //  Connection with retries
    public static Connection getConnection() throws SQLException {
        int retries = 3;
        SQLException lastException = null;
        
        while (retries > 0) {
            try {
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                conn.setAutoCommit(true);
                return conn;
            } catch (SQLException e) {
                lastException = e;
                retries--;
                if (retries > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection attempt interrupted", ie);
                    }
                }
            }
        }
        throw new SQLException("Failed to get database connection after 3 attempts", lastException);
    }

    //  Fetch user email by ID
    public String getUserEmailById(int userId) {
        String email = null;
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user email", e);
        }
        return email;
    }

    //  Fetch user ID from a request
    public int getUserIdFromRequest(int requestId) {
        String sql = "SELECT user_id FROM requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user ID from request", e);
        }
        return -1;
    }

    //  Generate a 6-character OTP
    public static String generateOTP() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            otp.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return otp.toString();
    }

    // save OTP (request_key) and expiry
    public boolean saveRequestKey(int requestId, String requestKey, Timestamp expiryTime) {
        String sql = "UPDATE requests SET request_key = ?, expiry_time = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, requestKey);
            if (expiryTime != null)
                stmt.setTimestamp(2, expiryTime);
            else
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            stmt.setInt(3, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating request status", e);
        }
        return false;
    }

    //  Approve request and send OTP to user’s registered email
    public boolean approveRequest(int requestId, Timestamp expiryTime) {
        int userId = getUserIdFromRequest(requestId);
        if (userId == -1) {
            System.out.println("User not found for request ID: " + requestId);
            return false;
        }

        // Generate request_key
        String requestKey = generateOTP();
        if (!saveRequestKey(requestId, requestKey, expiryTime)) {
            System.out.println("Failed to save request_key.");
            return false;
        }

        // Fetch user email
        String userEmail = getUserEmailById(userId);
        if (userEmail == null) {
            System.out.println("Email not found for user ID: " + userId);
            return false;
        }

        // Send email
        String subject = "Your File Access OTP";
        String message = "Hello,\n\nYour OTP to access the approved file is: " + requestKey +
                         (expiryTime != null ? "\nIt will expire at: " + expiryTime.toString() : "\nNo expiry set.") +
                         "\n\n– SafeSecure Team";

        EmailSender.sendEmail(userEmail, subject, message);
        System.out.println("OTP sent successfully to: " + userEmail);
        return true;
    }

    // verify OTP
    public boolean verifyRequestKey(int requestId, String enteredKey) {
        String sql = "SELECT request_key FROM requests WHERE id = ? AND (expiry_time IS NULL OR expiry_time > NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedKey = rs.getString("request_key");
                return storedKey != null && storedKey.equals(enteredKey);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validating request key", e);
        }
        return false;
    }
}
