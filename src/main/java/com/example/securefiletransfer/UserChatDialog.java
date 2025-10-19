package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class UserChatDialog extends JDialog {
    private final int userId;
    private final JTextArea chatArea;
    private final JTextField messageField;

    public UserChatDialog(JFrame parent, int userId) {
        super(parent, "Chat with Admin", true);
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));
        setSize(400, 500);
        setLocationRelativeTo(parent);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        chatArea.setText("");
        String sql = "SELECT u.full_name as sender, m.message, m.timestamp " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "WHERE (sender_id = ? AND receiver_id = 1) OR (sender_id = 1 AND receiver_id = ?) " +
                     "ORDER BY m.timestamp ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                chatArea.append(rs.getString("sender") + ": " + rs.getString("message") + "\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading messages: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, 1, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, msg);
            stmt.executeUpdate();
            messageField.setText("");
            loadMessages();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error sending message: " + e.getMessage());
        }
    }
}
