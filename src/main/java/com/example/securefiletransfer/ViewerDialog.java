package com.example.securefiletransfer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ViewerDialog extends JDialog {
    private Timer expiryTimer;
    private final int requestId;
    private final Component parentComponent;

    public ViewerDialog(Window owner, String title, Component view, int requestId, Component parentComponent) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.requestId = requestId;
        this.parentComponent = parentComponent;

        // Check if the request is expired before showing the dialog
        if (!checkValidAccess()) {
            throw new IllegalStateException("Access expired");
        }

        if (view instanceof JComponent) {
            add(new JScrollPane(view));
        } else {
            add(view);
        }

        // Check expiry time and start monitoring
        startExpiryMonitor();

        // Handle dialog closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopExpiryMonitor();
            }
        });
    }

    private void startExpiryMonitor() {
        if (requestId <= 0) {
            return;
        }

        expiryTimer = new Timer(true);
        expiryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkExpiry();
            }
        }, 0, 10000); // Check every 10 seconds
    }

    private void stopExpiryMonitor() {
        if (expiryTimer != null) {
            expiryTimer.cancel();
            expiryTimer = null;
        }
    }

    private boolean checkValidAccess() {
        if (requestId <= 0) {
            return true;
        }

        String sql = "SELECT status, expiry_time FROM requests WHERE id = ? AND " +
                     "status = 'approved' AND (expiry_time IS NULL OR expiry_time > NOW())";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    // Mark as expired if it was previously approved
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE requests SET status = 'expired' WHERE id = ? AND status = 'approved'")) {
                        updateStmt.setInt(1, requestId);
                        updateStmt.executeUpdate();
                    }
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error checking access validity: " + e.getMessage());
            return false;
        }
    }

    private void checkExpiry() {
        String sql = "SELECT status, expiry_time FROM requests WHERE id = ? AND " +
                     "status = 'approved' AND (expiry_time IS NULL OR expiry_time > NOW())";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                // Access has expired
                SwingUtilities.invokeLater(() -> {
                    stopExpiryMonitor();
                    dispose(); // Close the viewer

                    // Update request status
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE requests SET status = 'expired' WHERE id = ?")) {
                        updateStmt.setInt(1, requestId);
                        updateStmt.executeUpdate();

                        // Show expiry message with re-request option
                        int choice = JOptionPane.showConfirmDialog(parentComponent,
                                "Your access to this file has expired.\nWould you like to submit a new request?",
                                "Access Expired",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (choice == JOptionPane.YES_OPTION) {
                            if (parentComponent instanceof UserPanel userPanel) {
                                userPanel.handleRequest();
                            }
                            // AdminPanel does not have handleRequest
                        }

                        // Refresh panels
                        if (parentComponent instanceof UserPanel userPanel) {
                            userPanel.loadFiles();
                        } else if (parentComponent instanceof AdminPanel adminPanel) {
                            adminPanel.loadFiles();
                            adminPanel.loadRequests();
                            adminPanel.loadDashboardData();
                        }

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(parentComponent,
                                "File Has Been Expired: " + ex.getMessage(),
                                "File Expired",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        } catch (SQLException e) {
            System.err.println("Error checking file access expiry: " + e.getMessage());
        }
    }

    public void showDialog() {
        pack();
        if (getWidth() > 1024 || getHeight() > 768) {
            setSize(1024, 768);
        }
        setLocationRelativeTo(parentComponent);
        setVisible(true);
    }
}
