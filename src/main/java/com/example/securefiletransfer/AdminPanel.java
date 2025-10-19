package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class AdminPanel extends JPanel {
    private final JTable requestsTable;
    private final DefaultTableModel requestsTableModel;
    private final JTable filesTable;
    private final DefaultTableModel filesTableModel;
    private final JLabel totalFilesLabel, pendingRequestsLabel, approvedRequestsLabel;

    public AdminPanel() {
        setLayout(new BorderLayout(10, 15));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(UITheme.PADDED_BORDER);

        // --- Header ---
        // Changed to BorderLayout to correctly align title left and buttons right
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Administrator Dashboard");
        welcomeLabel.setFont(UITheme.TITLE_FONT);
        welcomeLabel.setForeground(UITheme.HEADER_COLOR);

        JButton logoutButton = new JButton("Logout");

        // Style button
        UITheme.styleButton(logoutButton, UITheme.REJECT_COLOR, UITheme.ICON_LOGOUT);

        // This panel correctly holds the buttons aligned to the right
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topButtonPanel.setOpaque(false);
        topButtonPanel.add(logoutButton);

        // Add components to headerPanel using BorderLayout
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(topButtonPanel, BorderLayout.EAST);
        
        // Add the finished header to the main panel
        add(headerPanel, BorderLayout.NORTH);

        // --- Dashboard Cards ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        cardsPanel.setOpaque(false);
        totalFilesLabel = new JLabel("0");
        pendingRequestsLabel = new JLabel("0");
        approvedRequestsLabel = new JLabel("0");
        cardsPanel.add(UITheme.createDashboardCard("Total Files", totalFilesLabel, new Color(23, 162, 184)));
        cardsPanel.add(UITheme.createDashboardCard("Pending Requests", pendingRequestsLabel, new Color(255, 193, 7)));
        cardsPanel.add(UITheme.createDashboardCard("Approved Today", approvedRequestsLabel, new Color(40, 167, 69)));

        // --- Requests Panel ---
        JPanel requestsPanel = new JPanel(new BorderLayout(10, 10));
        requestsPanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        requestsPanel.setBorder(UITheme.PADDED_BORDER);
        JLabel tableTitle = new JLabel("Manage File Access Requests");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(UITheme.FONT_COLOR);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        requestsPanel.add(tableTitle, BorderLayout.NORTH);

        requestsTableModel = new DefaultTableModel(new String[]{"Req ID", "Username", "Filename", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestsTable = new JTable(requestsTableModel);
        JScrollPane requestsScrollPane = new JScrollPane(requestsTable);
        UITheme.styleTable(requestsTable, requestsScrollPane);
        requestsTable.getColumn("Status").setCellRenderer(new StatusCellRenderer());
        requestsPanel.add(requestsScrollPane, BorderLayout.CENTER);

        JPanel requestButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        requestButtons.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton viewUserDetailsButton = new JButton("User Details");
        UITheme.styleButton(approveButton, UITheme.APPROVE_COLOR, UITheme.ICON_APPROVE);
        UITheme.styleButton(rejectButton, UITheme.REJECT_COLOR, UITheme.ICON_REJECT);
        UITheme.styleButton(viewUserDetailsButton, UITheme.VIEW_COLOR, UITheme.ICON_DETAILS);
        requestButtons.add(approveButton);
        requestButtons.add(rejectButton);
        requestButtons.add(viewUserDetailsButton);
        requestsPanel.add(requestButtons, BorderLayout.SOUTH);

        // --- Files Panel ---
        JPanel filesPanel = new JPanel(new BorderLayout(10, 10));
        filesPanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        filesPanel.setBorder(UITheme.PADDED_BORDER);
        JLabel filesTitle = new JLabel("Uploaded Files");
        filesTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        filesTitle.setForeground(UITheme.FONT_COLOR);
        filesTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        filesPanel.add(filesTitle, BorderLayout.NORTH);

        filesTableModel = new DefaultTableModel(new String[]{"File ID", "Filename", "Uploaded By", "Upload Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        filesTable = new JTable(filesTableModel);
        JScrollPane filesScrollPane = new JScrollPane(filesTable);
        UITheme.styleTable(filesTable, filesScrollPane);
        filesPanel.add(filesScrollPane, BorderLayout.CENTER);

        // --- File Management Buttons ---
        JPanel filesButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filesButtonPanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        JButton fileUploadButton = new JButton("Upload File");
        JButton viewFileButton = new JButton("View File");
        JButton deleteFileButton = new JButton("Delete File");
        UITheme.styleButton(fileUploadButton, UITheme.PRIMARY_COLOR, UITheme.ICON_APPROVE);
        UITheme.styleButton(viewFileButton, UITheme.VIEW_COLOR, UITheme.ICON_VIEW);
        UITheme.styleButton(deleteFileButton, UITheme.REJECT_COLOR, UITheme.ICON_REJECT);
        filesButtonPanel.add(fileUploadButton);
        filesButtonPanel.add(viewFileButton);
        filesButtonPanel.add(deleteFileButton);
        filesPanel.add(filesButtonPanel, BorderLayout.SOUTH);

        // --- Layout ---
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(requestsPanel);
        contentPanel.add(filesPanel);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(contentPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Button Actions ---
        fileUploadButton.addActionListener(e -> handleUpload());
        viewFileButton.addActionListener(e -> handleViewFile());
        deleteFileButton.addActionListener(e -> handleDeleteFile());
        approveButton.addActionListener(e -> handleApprove());
        rejectButton.addActionListener(e -> handleReject());
        viewUserDetailsButton.addActionListener(e -> handleViewUserDetails());
        logoutButton.addActionListener(e -> SecureFileTransfer.logoutUser());

        // --- Load Data ---
        refreshAllData(); // Load all data initially
    }

    private void refreshAllData() {
        updateExpiredRequests();
        loadDashboardData();
        loadRequests();
        loadFiles();
    }

    private void updateExpiredRequests() {
        String sql = "UPDATE requests " +
                "SET status = 'expired', request_key = NULL " +
                "WHERE (status = 'approved' OR status = 'approved-expired') " + 
                "AND expiry_time IS NOT NULL AND expiry_time < NOW()";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error updating expired requests: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewUserDetails() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) requestsTableModel.getValueAt(selectedRow, 1);
        String sql = "SELECT full_name, email, mobile FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userDetails = "<html><b>Name:</b> " + rs.getString("full_name") + "<br/>" +
                            "<b>Email:</b> " + rs.getString("email") + "<br/>" +
                            "<b>Mobile:</b> " + rs.getString("mobile") + "</html>";
                    JOptionPane.showMessageDialog(this, new JLabel(userDetails), "Details for " + username, JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleApprove() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request to approve",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) requestsTableModel.getValueAt(selectedRow, 0);

        JPanel expiryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JLabel hoursLabel = new JLabel("Hours:");
        JLabel minutesLabel = new JLabel("Minutes:");
        JTextField hoursField = new JTextField("0", 5);
        JTextField minutesField = new JTextField("0", 5);
        JCheckBox noLimitCheck = new JCheckBox("No Time Limit");

        expiryPanel.add(hoursLabel);
        expiryPanel.add(hoursField);
        expiryPanel.add(minutesLabel);
        expiryPanel.add(minutesField);
        expiryPanel.add(new JLabel(""));
        expiryPanel.add(noLimitCheck);

        int option = JOptionPane.showConfirmDialog(this, expiryPanel,
                "Set Access Expiry Time", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        java.sql.Timestamp expiryTimestamp = null;
        if (!noLimitCheck.isSelected()) {
            int hours = Integer.parseInt(hoursField.getText().trim());
            int minutes = Integer.parseInt(minutesField.getText().trim());
            if (hours > 0 || minutes > 0) {
                long expiryMillis = System.currentTimeMillis() + (hours * 3600 + minutes * 60) * 1000L;
                expiryTimestamp = new java.sql.Timestamp(expiryMillis);
            }
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String otp = generateOTP();
            String updateSql = "UPDATE requests SET status = 'approved', request_key = ?, expiry_time = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, otp);
                if (expiryTimestamp != null)
                    stmt.setTimestamp(2, expiryTimestamp);
                else
                    stmt.setNull(2, java.sql.Types.TIMESTAMP);
                stmt.setInt(3, requestId);
                stmt.executeUpdate();
            }

            String emailSql = "SELECT u.email FROM requests r JOIN users u ON r.user_id = u.id WHERE r.id = ?";
            String userEmail = null;
            try (PreparedStatement stmt = conn.prepareStatement(emailSql)) {
                stmt.setInt(1, requestId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) userEmail = rs.getString("email");
                }
            }

            if (userEmail != null) {
                String expiryText = (expiryTimestamp != null)
                        ? "This OTP will expire at: " + expiryTimestamp.toString()
                        : "No time limit set (OTP does not expire).";

                String message = "Your file access request has been approved.\n\n"
                        + "OTP to view the file: " + otp + "\n"
                        + expiryText + "\n\nPlease use this OTP in the secure portal.";

                EmailSender.sendEmail(userEmail, "File Access Approved - OTP Inside", message);
                JOptionPane.showMessageDialog(this,
                        "Request approved successfully!\nOTP sent to: " + userEmail,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            refreshAllData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error approving request: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String generateOTP() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return otp.toString();
    }

    private void handleReject() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to reject", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int requestId = (int) requestsTableModel.getValueAt(selectedRow, 0);
        String sql = "UPDATE requests SET status = 'rejected' WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            stmt.executeUpdate();
            refreshAllData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error rejecting request: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Upload");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Allowed Files (*.txt, *.pdf, *.doc, *.docx, *.png, *.jpg, *.jpeg, *.gif)",
                "txt", "pdf", "doc", "docx", "png", "jpg", "jpeg", "gif");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String sql = "INSERT INTO files (filename, file_data, upload_date, uploaded_by) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 FileInputStream fis = new FileInputStream(selectedFile)) {

                stmt.setString(1, selectedFile.getName());
                stmt.setBinaryStream(2, fis, selectedFile.length());
                stmt.setInt(3, SecureFileTransfer.getUserId());
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "File uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error uploading file: " + ex.getMessage(), "Upload Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadDashboardData() {
        String sql = "SELECT (SELECT COUNT(*) FROM files) as total_files, " +
                "(SELECT COUNT(*) FROM requests WHERE status = 'pending') as pending_requests, " +
                "(SELECT COUNT(*) FROM requests WHERE status = 'approved' AND DATE(request_date) = CURDATE()) as approved_today";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                totalFilesLabel.setText(String.valueOf(rs.getInt("total_files")));
                pendingRequestsLabel.setText(String.valueOf(rs.getInt("pending_requests")));
                approvedRequestsLabel.setText(String.valueOf(rs.getInt("approved_today")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadRequests() {
        requestsTableModel.setRowCount(0);
        String sql = "SELECT r.id, u.username, f.filename, r.status FROM requests r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN files f ON r.file_id = f.id ORDER BY r.request_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requestsTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("filename"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading requests: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFiles() {
        filesTableModel.setRowCount(0);
        String sql = "SELECT f.id, f.filename, u.username as uploader, f.upload_date FROM files f JOIN users u ON f.uploaded_by = u.id ORDER BY f.upload_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                filesTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("uploader"),
                        rs.getTimestamp("upload_date").toString()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading files: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteFile() {
        int selectedRow = filesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a file to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fileId = (int) filesTableModel.getValueAt(selectedRow, 0);
        String fileName = (String) filesTableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete file: " + fileName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM files WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fileId);
            stmt.executeUpdate();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "File deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting file: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewFile() {
        int selectedRow = filesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a file to view.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fileId = (int) filesTableModel.getValueAt(selectedRow, 0);
        SecureFileTransfer.viewFile(fileId);
    }
}