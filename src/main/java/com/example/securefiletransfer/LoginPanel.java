package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class LoginPanel extends JPanel {
    // UI Components
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final String role;
    private final JLabel errorLabel;

    // --- STYLING CONSTANTS (from previous refactor) ---
    private static final Color PRIMARY_COLOR = new Color(0, 128, 128);
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255);
    private static final Color BACKGROUND_WHITE = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    private static final Font FONT_HEADING = new Font("Arial", Font.BOLD, 32);
    private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_LOGO = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_ERROR = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_LINK = new Font("Arial", Font.PLAIN, 14);

    private static final Dimension FIELD_SIZE = new Dimension(400, 45);
    private static final Dimension BUTTON_SIZE = new Dimension(400, 45);

    private static final Insets PADDING_FIELD_LABEL = new Insets(0, 0, 8, 0);
    private static final Insets PADDING_FIELD = new Insets(0, 0, 16, 0);
    private static final Insets PADDING_FORM_TITLE = new Insets(0, 0, 24, 0);
    private static final Insets PADDING_BUTTON_LINKS = new Insets(8, 0, 0, 0);

    private static final Border BORDER_FIELD = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 0, 8, 0)
    );
    private static final Border BORDER_FIELD_FOCUSED = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
        BorderFactory.createEmptyBorder(8, 0, 7, 0)
    );
    private static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(40, 40, 40, 40) // Padding inside the card
    );
    // --- END STYLING CONSTANTS ---

    public LoginPanel(String role) {
        this.role = role;
        this.errorLabel = new JLabel("");
        this.usernameField = createStyledTextField();
        this.passwordField = createStyledPasswordField();
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Main panel is now the background, using GridBagLayout to center the card
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Outer padding

        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Add the Logo (Top-Center)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 30, 0); // Increased padding below logo
        gbc.weightx = 1.0; // This helps center the logo
        JLabel logo = new JLabel("SafeShare");
        logo.setFont(FONT_LOGO);
        logo.setForeground(PRIMARY_COLOR);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        add(logo, gbc);

        // 2. Add the Login Card (Centered)
        JPanel loginCard = createLoginCard();
        
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 1.0; // Fill remaining horizontal space
        gbc.weighty = 1.0; // Fill remaining vertical space
        add(loginCard, gbc);
    }
    
    /**
     * Creates the centered white login card.
     */
    private JPanel createLoginCard() {
        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(BACKGROUND_WHITE);
        card.setBorder(BORDER_CARD);
        // Set a preferred size for the card
        card.setPreferredSize(new Dimension(500, 550));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.insets = PADDING_FORM_TITLE;
        String panelTitle = "user".equals(role) ? "Log In" : "Admin Log In";
        JLabel titleLabel = new JLabel(panelTitle);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(titleLabel, gbc);
            
        // Username field
        gbc.insets = PADDING_FIELD_LABEL;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(FONT_LABEL);
        usernameLabel.setForeground(TEXT_COLOR);
        formPanel.add(usernameLabel, gbc);
            
        gbc.insets = PADDING_FIELD;
        formPanel.add(usernameField, gbc);
            
        // Password field
        gbc.insets = PADDING_FIELD_LABEL;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(FONT_LABEL);
        passwordLabel.setForeground(TEXT_COLOR);
        formPanel.add(passwordLabel, gbc);
            
        gbc.insets = PADDING_FIELD;
        formPanel.add(passwordField, gbc);

        // Error label
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setHorizontalAlignment(SwingConstants.LEFT);
        errorLabel.setFont(FONT_ERROR);
        errorLabel.setVisible(false);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();

        // Add components to card
        card.add(errorLabel, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
            
        return card;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        
        // Sign in button
        JButton loginButton = createStyledButton("Sign in", PRIMARY_COLOR, Color.WHITE);
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);
        
        // Footer panel for links
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Centered
        linksPanel.setBackground(BACKGROUND_WHITE);
        
        if ("user".equals(role)) {
            JLabel accountText = new JLabel("Don't have an account?");
            accountText.setFont(FONT_LINK);
            accountText.setForeground(TEXT_COLOR);
            linksPanel.add(accountText);
            
            JButton registerButton = createLinkButton("Register");
            registerButton.addActionListener(e -> SecureFileTransfer.showPanel("Register"));
            linksPanel.add(registerButton);
        }
        
        // Separator
        linksPanel.add(new JLabel("|"));

        // Add separator and switch button only if needed
        if (linksPanel.getComponentCount() > 0) {
            linksPanel.add(new JLabel(" | "));
        }
        JButton switchButton = createLinkButton(
            "user".equals(role) ? "Admin Login" : "User Login"
        );
        switchButton.addActionListener(e -> 
            SecureFileTransfer.showPanel("user".equals(role) ? "AdminLogin" : "UserLogin")
        );
        linksPanel.add(switchButton);
        
        gbc.insets = PADDING_BUTTON_LINKS;
        panel.add(linksPanel, gbc);
        
        return panel;
    }
    
    // --- STYLING HELPER METHODS ---

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setPreferredSize(BUTTON_SIZE);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFont(FONT_LINK);
        button.setForeground(PRIMARY_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(FIELD_SIZE);
        field.setBorder(createTextFieldBorder(false));
        field.setFont(FONT_BODY);
        field.setBackground(BACKGROUND_WHITE);
        addTextFieldFocusListener(field);
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(FIELD_SIZE);
        field.setBorder(createTextFieldBorder(false));
        field.setFont(FONT_BODY);
        field.setBackground(BACKGROUND_WHITE);
        addTextFieldFocusListener(field);
        return field;
    }
    
    private void addTextFieldFocusListener(JComponent field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(createTextFieldBorder(true));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(createTextFieldBorder(false));
            }
        });
    }
    
    private Border createTextFieldBorder(boolean focused) {
        return focused ? BORDER_FIELD_FOCUSED : BORDER_FIELD;
    }

    // --- END STYLING HELPER METHODS ---
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        String sql = "SELECT id, role FROM users WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    SecureFileTransfer.loginUser(id, username);
                    if ("admin".equals(role)) {
                        SecureFileTransfer.showAdminPanel();
                    } else {
                        SecureFileTransfer.showUserPanel();
                    }
                } else {
                    showError("Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }
    
    public void showError(String message) {
        errorLabel.setText("! " + message);
        errorLabel.setVisible(true);
    }
    
    public void hideError() {
        errorLabel.setVisible(false);
    }
}