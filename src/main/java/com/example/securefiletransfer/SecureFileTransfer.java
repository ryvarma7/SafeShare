package com.example.securefiletransfer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

public class SecureFileTransfer {

    private static JFrame frame;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    private static int userId = -1;
    private static String username = "";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf.");
        }

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("SafeShare - Secure File Transfer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(850, 700));

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            mainPanel.setBackground(UITheme.BACKGROUND_COLOR);

            mainPanel.add(new LoginPanel("user"), "UserLogin");
            mainPanel.add(new LoginPanel("admin"), "AdminLogin");
            mainPanel.add(new RegisterPanel(), "Register");
            
            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            showPanel("UserLogin");
        });
    }
    
    public static void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public static void loginUser(int id, String name) {
        userId = id;
        username = name;
    }
    public static void openChatWindow() {
        JFrame chatFrame = new JFrame("Admin Chat");
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chatFrame.setSize(500, 600);
        chatFrame.setLocationRelativeTo(frame); // center relative to main frame

        ChatPanel chatPanel = new ChatPanel(); // your existing ChatPanel
        chatFrame.add(chatPanel);
        chatFrame.setVisible(true);
    }


    public static void logoutUser() {
        userId = -1;
        username = "";
        for(Component c : mainPanel.getComponents()){
            if(c instanceof AdminPanel || c instanceof UserPanel){
                mainPanel.remove(c);
            }
        }
        showPanel("UserLogin");
    }
    
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }

    public static void showAdminPanel() {
        mainPanel.add(new AdminPanel(), "Admin");
        cardLayout.show(mainPanel, "Admin");
    }
    
    public static void showUserPanel() {
        mainPanel.add(new UserPanel(), "User");
        cardLayout.show(mainPanel, "User");
    }

    /**
     * Opens a file from the database using its ID.
     * Creates a temporary file and opens it with the default system application.
     */
    public static void viewFile(int fileId) {
        String sql = "SELECT filename, file_data FROM files WHERE id = ?";
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fileId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("filename");
                    InputStream fileData = rs.getBinaryStream("file_data");

                    // Determine file extension
                    String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : null;
                    File tempFile = File.createTempFile("securefile_", suffix);
                    tempFile.deleteOnExit();

                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileData.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(tempFile);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                "Cannot open file on this system.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "File not found.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Error opening file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
