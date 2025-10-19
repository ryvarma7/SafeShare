package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class RegisterPanel extends JPanel {
    private final JTextField nameField, emailField, mobileField, usernameField;
    private final JTextArea addressArea;
    private final JPasswordField passwordField;
    private final PasswordStrengthDots strengthDots;
    private final JLabel strengthLabel;

    public RegisterPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND_COLOR);

        // --- Left Branding Panel ---
        add(UITheme.createBrandingPanel("Create Your Account"), BorderLayout.WEST);

        // --- Right Form Panel ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        add(rightPanel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.PANEL_BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 221, 221)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.HEADER_COLOR);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 15, 10);
        formPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 1;
        gbc.gridy++;

        // Full Name
        addLabelAndField("Full Name:", nameField = new JTextField(25), formPanel, gbc);
        gbc.gridy++;

        // Email
        addLabelAndField("Email:", emailField = new JTextField(25), formPanel, gbc);

        // OTP caution under email
        gbc.gridy++;
        JLabel emailCaution = new JLabel("*This email is used for OTPs");
        emailCaution.setFont(UITheme.LABEL_FONT.deriveFont(Font.ITALIC, 11f));
        emailCaution.setForeground(Color.GRAY);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(emailCaution, gbc);

        gbc.gridy++;
        // Mobile
        addLabelAndField("Mobile No:", mobileField = new JTextField(25), formPanel, gbc);

        gbc.gridy++;
        // Address
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(addressLabel, gbc);
        addressArea = new JTextArea(3, 25);
        addressArea.setLineWrap(true);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(new JScrollPane(addressArea), gbc);

        gbc.gridy++;
        // Username
        addLabelAndField("Username:", usernameField = new JTextField(25), formPanel, gbc);
        addPlaceholder(usernameField, "*Used for login*");

        gbc.gridy++;
        // Password
        addLabelAndField("Password:", passwordField = new JPasswordField(25), formPanel, gbc);
        //addPlaceholder(passwordField, "*Used for login*");

        // Password strength indicator
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        strengthPanel.setOpaque(false);
        strengthDots = new PasswordStrengthDots();
        strengthLabel = new JLabel("");
        strengthLabel.setFont(UITheme.LABEL_FONT.deriveFont(Font.BOLD));
        strengthPanel.add(strengthDots);
        strengthPanel.add(strengthLabel);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 5, 10);
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(strengthPanel, gbc);

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePasswordStrength(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePasswordStrength(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        JButton registerButton = new JButton("Register Account");
        UITheme.styleButton(registerButton, UITheme.PRIMARY_COLOR, "");
        JButton backButton = new JButton("Back to Login");
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        rightPanel.add(formPanel, new GridBagConstraints());
        registerButton.addActionListener(e -> handleRegister());
        backButton.addActionListener(e -> SecureFileTransfer.showPanel("UserLogin"));

        updatePasswordStrength();
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        strengthDots.setStrengthLevel(score);

        Color textColor = Color.GRAY;
        String text = "";

        if (!password.isEmpty()) {
            switch (score) {
                case 1: text = "Very Weak"; textColor = new Color(220, 53, 69); break;
                case 2: text = "Weak"; textColor = new Color(240, 100, 50); break;
                case 3: text = "Medium"; textColor = new Color(255, 193, 7); break;
                case 4: text = "Strong"; textColor = new Color(132, 204, 22); break;
                case 5: text = "Very Strong"; textColor = new Color(40, 167, 69); break;
                default: text = "Very Weak"; textColor = new Color(220, 53, 69);
            }
        }

        strengthLabel.setText(text);
        strengthLabel.setForeground(textColor);
    }

    private void addLabelAndField(String labelText, JComponent field, JPanel panel, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(field, gbc);
    }

    private void addPlaceholder(JTextComponent field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void handleRegister() {
        String fullName = nameField.getText();
        String email = emailField.getText();
        String mobile = mobileField.getText();
        String address = addressArea.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // All fields required validation
        if (fullName.isEmpty() || email.isEmpty() || mobile.isEmpty() || address.isEmpty() || username.isEmpty() || password.isEmpty() ||
            username.equals("*Used for login*")) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (strengthDots.getStrengthLevel() < 3) {
            JOptionPane.showMessageDialog(this, "Password is too weak. Please create a stronger password.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (full_name, email, mobile, address, username, password, role) VALUES (?, ?, ?, ?, ?, ?, 'user')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, mobile);
            statement.setString(4, address);
            statement.setString(5, username);
            statement.setString(6, password);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
            SecureFileTransfer.showPanel("UserLogin");
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
