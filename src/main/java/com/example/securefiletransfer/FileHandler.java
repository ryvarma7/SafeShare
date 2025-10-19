package com.example.securefiletransfer;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class FileHandler {
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());

    public static void viewFileInDialog(Component parent, int fileId) {
        SwingWorker<Object[], Void> viewerWorker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                String sql = "SELECT filename, file_data, expiry_time FROM files WHERE id = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, fileId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String filename = rs.getString("filename");
                            Blob blob = rs.getBlob("file_data");
                            byte[] fileBytes = blob.getBytes(1, (int) blob.length());
                            Timestamp expiry = rs.getTimestamp("expiry_time");
                            return new Object[]{filename, fileBytes, expiry};
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    if (result == null || result.length != 3) {
                        JOptionPane.showMessageDialog(parent, 
                            "Could not retrieve file data from database.", 
                            "View Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String filename = ((String) result[0]).toLowerCase();
                    byte[] fileBytes = (byte[]) result[1];
                    Timestamp expiryTimestamp = (Timestamp) result[2];

                    // --- Check expiry ---
                    if (!(parent instanceof AdminPanel)) { // Non-admin users
                        if (expiryTimestamp != null && expiryTimestamp.before(Timestamp.valueOf(LocalDateTime.now()))) {
                            JOptionPane.showMessageDialog(parent,
                                "Access denied. The file has expired.",
                                "Expired",
                                JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    Component viewComponent;
                    if (filename.endsWith(".png") || filename.endsWith(".jpg") ||
                        filename.endsWith(".jpeg") || filename.endsWith(".gif")) {
                        viewComponent = createImageComponent(fileBytes);
                    } else if (filename.endsWith(".txt")) {
                        viewComponent = createTextComponent(fileBytes);
                    } else if (filename.endsWith(".pdf")) {
                        viewComponent = createPDFComponent(fileBytes);
                    } else if (filename.endsWith(".doc") || filename.endsWith(".docx")) {
                        viewComponent = createWordComponent(fileBytes);
                    } else {
                        JOptionPane.showMessageDialog(parent, 
                            "Preview is not available for this file type.", 
                            "Cannot View File", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    int requestId;
                    if (parent instanceof AdminPanel) {
                        requestId = 0; // Admin bypass
                    } else {
                        // Verify the user has an approved, non-expired request
                        requestId = -1;
                        try {
                            String requestSql = "SELECT r.id FROM requests r " +
                                                "WHERE r.user_id = ? AND r.file_id = ? AND r.status = 'approved' " +
                                                "AND (r.expiry_time IS NULL OR r.expiry_time > NOW())";
                            try (Connection conn = DatabaseManager.getConnection();
                                 PreparedStatement stmt = conn.prepareStatement(requestSql)) {
                                stmt.setInt(1, SecureFileTransfer.getUserId());
                                stmt.setInt(2, fileId);
                                try (ResultSet rs = stmt.executeQuery()) {
                                    if (rs.next()) requestId = rs.getInt("id");
                                }
                            }
                        } catch (SQLException e) {
                            LOGGER.log(Level.WARNING, "Error getting request ID for file access", e);
                        }

                        if (requestId == -1) {
                            JOptionPane.showMessageDialog(parent,
                                "You do not have permission to view this file or your access has expired.",
                                "Access Denied",
                                JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    if (viewComponent != null) {
                        ViewerDialog dialog = new ViewerDialog(
                            SwingUtilities.getWindowAncestor(parent),
                            "View: " + filename,
                            viewComponent,
                            requestId,
                            parent
                        );
                        dialog.showDialog();
                    }

                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error viewing file", e);
                    JOptionPane.showMessageDialog(parent, 
                        "Error viewing file: " + e.getCause().getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        viewerWorker.execute();
    }

    private static Component createImageComponent(byte[] fileBytes) {
        ImageIcon imageIcon = new ImageIcon(fileBytes);
        if (imageIcon.getIconWidth() == -1) return null;
        return new JLabel(imageIcon);
    }

    private static Component createTextComponent(byte[] fileBytes) {
        String content = new String(fileBytes);
        JTextArea textArea = new JTextArea(content, 20, 60);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private static Component createPDFComponent(byte[] fileBytes) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            JPanel pdfPanel = new JPanel(new GridLayout(0, 1, 0, 10));
            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 100);
                ImageIcon icon = new ImageIcon(image);
                if (icon.getIconWidth() > 800) {
                    Image scaled = icon.getImage().getScaledInstance(800, -1, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                }
                pdfPanel.add(new JLabel(icon));
            }
            return pdfPanel;
        } catch (Exception e) {
            return null;
        }
    }

    private static Component createWordComponent(byte[] fileBytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            JTextArea textArea = new JTextArea(content.toString(), 20, 60);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            return textArea;
        } catch (Exception e) {
            return null;
        }
    }

    public static void downloadFile(int fileId, File saveLocation) {
        String sql = "SELECT file_data, expiry_time FROM files WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fileId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp expiry = rs.getTimestamp("expiry_time");
                    if (expiry != null && expiry.before(Timestamp.valueOf(LocalDateTime.now()))) {
                        JOptionPane.showMessageDialog(null,
                            "Cannot download. The file has expired.",
                            "Expired",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Blob blob = rs.getBlob("file_data");
                    try (InputStream in = blob.getBinaryStream();
                         FileOutputStream out = new FileOutputStream(saveLocation)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    JOptionPane.showMessageDialog(null, "File downloaded to:\n" + saveLocation.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error downloading file", e);
            JOptionPane.showMessageDialog(null, "Error downloading file: " + e.getMessage());
        }
    }
}
