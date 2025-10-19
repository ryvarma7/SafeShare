package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class UserPanel extends JPanel {
    private final JTable filesTable;
    private final DefaultTableModel filesTableModel;

    public UserPanel() {
        setLayout(new BorderLayout(10, 15));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(UITheme.PADDED_BORDER);

        // ---------------- Header ----------------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, " + SecureFileTransfer.getUsername());
        welcomeLabel.setFont(UITheme.TITLE_FONT);
        welcomeLabel.setForeground(UITheme.HEADER_COLOR);

        JButton logoutButton = new JButton("Logout");
        UITheme.styleButton(logoutButton, UITheme.REJECT_COLOR, ""); // No icon

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ---------------- Table Section ----------------
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        tablePanel.setBorder(UITheme.PADDED_BORDER);

        JLabel tableTitle = new JLabel("Available Files for Request");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(UITheme.FONT_COLOR);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        filesTableModel = new DefaultTableModel(new String[]{"File ID", "Filename", "Your Request Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        filesTable = new JTable(filesTableModel);
        JScrollPane scrollPane = new JScrollPane(filesTable);
        UITheme.styleTable(filesTable, scrollPane);
        filesTable.getColumn("Your Request Status").setCellRenderer(new StatusCellRenderer());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // ---------------- Buttons Panel ----------------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);

        JButton requestButton = new JButton("Request File");
        JButton viewButton = new JButton("View File");
        JButton chatButton = new JButton("Chat with Admin");

        UITheme.styleButton(requestButton, UITheme.PRIMARY_COLOR, "");
        UITheme.styleButton(viewButton, UITheme.APPROVE_COLOR, "");
        UITheme.styleButton(chatButton, UITheme.PRIMARY_COLOR, "");

        buttonPanel.add(requestButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(chatButton);

        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);

        // ---------------- Event Listeners ----------------
        requestButton.addActionListener(e -> handleRequest());
        viewButton.addActionListener(e -> handleViewFile());
        logoutButton.addActionListener(e -> SecureFileTransfer.logoutUser());
        chatButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new UserChatDialog(parentFrame, SecureFileTransfer.getUserId()).setVisible(true);
        });

        loadFiles();
    }

    // ---------------- Handle File Request ----------------
    public void handleRequest() {
        int selectedRow = filesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a file to request.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int fileId = (int) filesTableModel.getValueAt(selectedRow, 0);
        String status = (String) filesTableModel.getValueAt(selectedRow, 2);

        // Allow re-request for expired or rejected
        if ("Expired".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Would you like to submit a new request for this file?",
                    "Re-request File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        } else if (!"Not Requested".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "You already have an active request for this file.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Integer previousRequestId = null;
        String checkSql = "SELECT id FROM requests WHERE user_id = ? AND file_id = ? ORDER BY request_date DESC LIMIT 1";
        String insertSql = "INSERT INTO requests (user_id, file_id, status, previous_request_id) VALUES (?, ?, 'pending', ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Check previous request
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, SecureFileTransfer.getUserId());
                checkStmt.setInt(2, fileId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) previousRequestId = rs.getInt("id");
            }

            // Insert new request
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, SecureFileTransfer.getUserId());
                insertStmt.setInt(2, fileId);
                insertStmt.setObject(3, previousRequestId);
                insertStmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "File request sent to the administrator.",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            loadFiles();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error submitting request: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- Handle File View ----------------
    private void handleViewFile() {
        int selectedRow = filesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a file to view.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) filesTableModel.getValueAt(selectedRow, 2);

        if ("Expired".equalsIgnoreCase(status)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Your access to this file has expired. Would you like to submit a new request?",
                    "Access Expired", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) handleRequest();
            return;
        } else if (!"Approved".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "You don't have permission to view this file. Please request access first.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int fileId = (int) filesTableModel.getValueAt(selectedRow, 0);
        String key = JOptionPane.showInputDialog(this, "Enter the key provided by the admin to view the file:");
        if (key == null || key.trim().isEmpty()) return;

        String sql = """
            SELECT r.id, r.expiry_time FROM requests r
            WHERE r.user_id = ? AND r.file_id = ? AND r.request_key = ?
            AND r.status = 'approved' AND (r.expiry_time IS NULL OR r.expiry_time > NOW())
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, SecureFileTransfer.getUserId());
            stmt.setInt(2, fileId);
            stmt.setString(3, key.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Check expiry warning
                    java.sql.Timestamp expiryTime = rs.getTimestamp("expiry_time");
                    if (expiryTime != null) {
                        long timeLeft = expiryTime.getTime() - System.currentTimeMillis();
                        long minutesLeft = timeLeft / (60 * 1000);
                        if (timeLeft < 3600000) {
                            JOptionPane.showMessageDialog(this,
                                    String.format("Warning: Access expires in %d minutes", minutesLeft + 1),
                                    "Access Expiring Soon", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    FileHandler.viewFileInDialog(this, fileId);
                } else {
                    handleInvalidOrExpiredKey(conn, fileId, key);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error verifying access: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- Handle Invalid or Expired Key ----------------
    private void handleInvalidOrExpiredKey(Connection conn, int fileId, String key) throws SQLException {
        String checkExpiredSql = "SELECT expiry_time FROM requests WHERE user_id = ? AND file_id = ? AND request_key = ? AND status = 'approved'";
        try (PreparedStatement expiredStmt = conn.prepareStatement(checkExpiredSql)) {
            expiredStmt.setInt(1, SecureFileTransfer.getUserId());
            expiredStmt.setInt(2, fileId);
            expiredStmt.setString(3, key.trim());
            ResultSet expiredRs = expiredStmt.executeQuery();

            if (expiredRs.next()) {
                String updateSql = "UPDATE requests SET status = 'expired' WHERE user_id = ? AND file_id = ? AND request_key = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, SecureFileTransfer.getUserId());
                    updateStmt.setInt(2, fileId);
                    updateStmt.setString(3, key.trim());
                    updateStmt.executeUpdate();
                }

                int choice = JOptionPane.showConfirmDialog(this,
                        "Your access has expired. Would you like to submit a new request for this file?",
                        "Access Expired", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) handleRequest();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid key. View access failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        loadFiles();
    }

    // ---------------- Load Files ----------------
    public void loadFiles() {
        filesTableModel.setRowCount(0);
        String sql = """
            SELECT f.id, f.filename,
            CASE WHEN r.expiry_time < NOW() AND r.status = 'approved' THEN 'Expired'
                 ELSE r.status END AS status
            FROM files f
            LEFT JOIN requests r ON f.id = r.file_id AND r.user_id = ?
            ORDER BY f.upload_date DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, SecureFileTransfer.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                filesTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("status") == null ? "Not Requested" : rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading files: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
