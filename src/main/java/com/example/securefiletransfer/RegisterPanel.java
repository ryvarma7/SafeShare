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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;

public class RegisterPanel extends JPanel {
    private final JTextField nameField, emailField, mobileField, usernameField;
    private final JPasswordField passwordField;
    private final PasswordStrengthDots strengthDots;
    private final JLabel strengthLabel;
    
    // --- STYLING CONSTANTS (Matched with LoginPanel) ---
    private static final Color PRIMARY_COLOR = new Color(0, 128, 128);
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255);
    private static final Color BACKGROUND_WHITE = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color MUTED_COLOR = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    private static final Font FONT_HEADING = new Font("Arial", Font.BOLD, 32);
    private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_NOTE = new Font("Arial", Font.ITALIC, 13);
    private static final Font FONT_LOGO = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_LINK = new Font("Arial", Font.PLAIN, 14);

    private static final Dimension FIELD_SIZE = new Dimension(800, 45);
    private static final Dimension BUTTON_SIZE = new Dimension(800, 45);

    private static final Insets PADDING_FIELD_LABEL = new Insets(0, 0, 10, 0);
    private static final Insets PADDING_FIELD = new Insets(0, 0, 20, 0);
    private static final Insets PADDING_FORM_TITLE = new Insets(0, 0, 30, 0);
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
        BorderFactory.createEmptyBorder(60, 100, 60, 100) // Increased padding inside the card
    );
    // --- END STYLING CONSTANTS ---
    
    public RegisterPanel() {
        // Initialize all final fields first
        this.nameField = createStyledTextField();
        this.emailField = createStyledTextField();
        this.mobileField = createStyledTextField();
        // Add mobile number validation
        ((javax.swing.text.PlainDocument) this.mobileField.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                // Only allow digits
                String filtered = string.replaceAll("[^0-9]", "");
                // Check if adding this string would exceed 10 digits
                if (fb.getDocument().getLength() + filtered.length() <= 10) {
                    super.insertString(fb, offset, filtered, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                // Only allow digits
                String filtered = text.replaceAll("[^0-9]", "");
                // Check if the replacement would exceed 10 digits
                if (fb.getDocument().getLength() - length + filtered.length() <= 10) {
                    super.replace(fb, offset, length, filtered, attrs);
                }
            }
        });

        // Add a tooltip instead of placeholder
        this.mobileField.setToolTipText("Enter 10 digit mobile number");
        this.usernameField = createStyledTextField();
        this.passwordField = createStyledPasswordField();
        this.strengthDots = new PasswordStrengthDots();
        this.strengthLabel = new JLabel("");
        
        // Main panel setup
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

        // 2. Add the Register Card (Centered)
        JPanel registerCard = createRegisterCard();
        
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 1.0; // Fill remaining horizontal space
        gbc.weighty = 1.0; // Fill remaining vertical space
        add(registerCard, gbc);
        
        // Initialize initial state
        updatePasswordStrength();
    }

    /**
     * Creates the centered white card for registration.
     */
    private JPanel createRegisterCard() {
        // This panel will be the white card
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BACKGROUND_WHITE);
        card.setBorder(BORDER_CARD);
        // Allow the card to expand with the window while maintaining minimum size
        card.setMinimumSize(new Dimension(800, 700));
        card.setPreferredSize(new Dimension(1000, 800));

        // Registration form panel (this holds the actual fields)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_WHITE);
        // Add padding around the form itself
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Configure form panel for optimal scrolling
        formPanel.setPreferredSize(new Dimension(800, 700));
        formPanel.setDoubleBuffered(true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.insets = PADDING_FORM_TITLE;
        JLabel titleLabel = new JLabel("Create your Account");
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        formPanel.add(titleLabel, gbc);

        // Full Name
        gbc.insets = PADDING_FIELD_LABEL;
        formPanel.add(createLabel("Full Name:"), gbc);
        gbc.insets = PADDING_FIELD;
        formPanel.add(nameField, gbc);

        // Email
        gbc.insets = PADDING_FIELD_LABEL;
        formPanel.add(createLabel("Email:"), gbc);
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(emailField, gbc);
        // Email caution
        gbc.insets = PADDING_FIELD;
        JLabel emailNote = new JLabel("Used for OTP verification");
        emailNote.setFont(FONT_NOTE);
        emailNote.setForeground(MUTED_COLOR);
        formPanel.add(emailNote, gbc);

        // Mobile Number
        gbc.insets = PADDING_FIELD_LABEL;
        formPanel.add(createLabel("Mobile Number:"), gbc);
        gbc.insets = PADDING_FIELD;
        formPanel.add(mobileField, gbc);



        // Username
        gbc.insets = PADDING_FIELD_LABEL;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.insets = PADDING_FIELD;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.insets = PADDING_FIELD_LABEL;
        formPanel.add(createLabel("Password:"), gbc);
        // Show/hide toggle
        JButton toggleButton = createLinkButton("Show");
        toggleButton.addActionListener(e -> {
            if (passwordField.getEchoChar() != '\u0000') {
                passwordField.setEchoChar((char)0);
                toggleButton.setText("Hide");
            } else {
                passwordField.setEchoChar('•');
                toggleButton.setText("Show");
            }
        });
        
        JPanel passwordFieldPanel = new JPanel(new BorderLayout());
        passwordFieldPanel.setBackground(BACKGROUND_WHITE);
        passwordFieldPanel.add(passwordField, BorderLayout.CENTER);
        passwordFieldPanel.add(toggleButton, BorderLayout.EAST);
        
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(passwordFieldPanel, gbc);

        // Password strength indicator
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        strengthPanel.setOpaque(false);
        strengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        strengthPanel.add(strengthDots);
        strengthPanel.add(strengthLabel);
        gbc.insets = PADDING_FORM_TITLE; // More padding
        gbc.anchor = GridBagConstraints.LINE_START;
        formPanel.add(strengthPanel, gbc);
        
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePasswordStrength(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePasswordStrength(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        // Add a scroll pane in case the form is too long
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setBackground(BACKGROUND_WHITE);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Optimize scrolling speed and smoothness
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.getVerticalScrollBar().setBlockIncrement(64);
        formScrollPane.setDoubleBuffered(true);
        
        // Make sure the form panel uses available space
        formPanel.setPreferredSize(new Dimension(800, 700));
        
        // Configure scroll pane to expand properly
        formScrollPane.setPreferredSize(null); // Let it be determined by content
        formScrollPane.getViewport().setOpaque(false);
        formScrollPane.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE); // Faster scroll mode
        
        // Add the scroll pane to fill the card
        card.add(formScrollPane, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridwidth = GridBagConstraints.REMAINDER;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.insets = new Insets(0, 0, 8, 0);
        
        // Sign up button
        JButton registerButton = createStyledButton("Sign up", PRIMARY_COLOR, Color.WHITE);
        registerButton.addActionListener(e -> handleRegister());
        panel.add(registerButton, buttonGbc);
        
        // Back to login link
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linksPanel.setOpaque(false);
        
        JLabel accountText = new JLabel("Already have an account?");
        accountText.setFont(FONT_LINK);
        accountText.setForeground(TEXT_COLOR);
        linksPanel.add(accountText);
        
        JButton backButton = createLinkButton("Sign in");
        backButton.addActionListener(e -> SecureFileTransfer.showPanel("UserLogin"));
        linksPanel.add(backButton);
        
        buttonGbc.insets = PADDING_BUTTON_LINKS;
        panel.add(linksPanel, buttonGbc);

        return panel;
    }

    private void updatePasswordStrength() {
        if (strengthDots == null || strengthLabel == null) {
            return; // Exit if components aren't initialized yet
        }
        
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
            record PasswordStrength(String text, Color color) {}
            var result = switch (score) {
                case 1 -> new PasswordStrength("Very Weak", new Color(220, 53, 69));
                case 2 -> new PasswordStrength("Weak", new Color(240, 100, 50));
                case 3 -> new PasswordStrength("Medium", new Color(255, 193, 7));
                case 4 -> new PasswordStrength("Strong", new Color(132, 204, 22));
                case 5 -> new PasswordStrength("Very Strong", new Color(40, 167, 69));
                default -> new PasswordStrength("Very Weak", new Color(220, 53, 69));
            };
            text = result.text();
            textColor = result.color();
        }

        strengthLabel.setText(text);
        strengthLabel.setForeground(textColor);
    }
    
    // --- STYLING HELPER METHODS ---
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_COLOR);
        return label;
    }

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
    
    private void handleRegister() {
        String fullName = nameField.getText();
        String email = emailField.getText();
        String mobile = mobileField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mobileNumber = mobileField.getText();
        if (mobileNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a mobile number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mobileNumber.length() != 10) {
            JOptionPane.showMessageDialog(this, "Mobile number must be exactly 10 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (strengthDots.getStrengthLevel() < 3) {
            JOptionPane.showMessageDialog(this, "Password is too weak. Please create a stronger password.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (full_name, email, mobile, username, password, role) VALUES (?, ?, ?, ?, ?, 'user')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, mobile);
            statement.setString(4, username);
            statement.setString(5, password);
            statement.executeUpdate();
            
            // Clear all fields for new registration
            nameField.setText("");
            emailField.setText("");
            mobileField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            
            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
            SecureFileTransfer.showPanel("UserLogin");
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}