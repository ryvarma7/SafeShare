package com.example.securefiletransfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

public class UITheme {
    // Colors
    public static final Color BACKGROUND_COLOR = new Color(235, 238, 245);  // Soft blue-gray
    public static final Color PANEL_BACKGROUND_COLOR = new Color(248, 250, 252);  // Very light blue tint
    public static final Color HEADER_COLOR = new Color(23, 43, 77);
    public static final Color FONT_COLOR = new Color(66, 82, 110);
    public static final Color PRIMARY_COLOR = new Color(0, 120, 212);
    public static final Color APPROVE_COLOR = new Color(40, 167, 69);
    public static final Color REJECT_COLOR = new Color(220, 53, 69);
    public static final Color VIEW_COLOR = new Color(23, 162, 184);

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Icons (Using Unicode characters as placeholders - replace with actual icons if needed)
    public static final String ICON_LOGOUT = "🚪";
    public static final String ICON_APPROVE = "✓";
    public static final String ICON_REJECT = "✕";
    public static final String ICON_VIEW = "👁";
    public static final String ICON_DETAILS = "ℹ";

    // Borders
    public static final Border PADDED_BORDER = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    public static final Border LINE_BORDER = BorderFactory.createLineBorder(new Color(233, 236, 239));
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            LINE_BORDER,
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    );

    // Utility Methods
    public static void styleButton(JButton button, Color color, String icon) {
        button.setFont(NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (!icon.isEmpty()) {
            button.setText(icon + " " + button.getText());
        }
        button.setPreferredSize(new Dimension(120, 35));
    }

    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setGridColor(new Color(233, 236, 239));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(247, 248, 250));
        table.getTableHeader().setForeground(FONT_COLOR);
        table.setSelectionBackground(new Color(230, 240, 255));  // Lighter blue selection
        table.setSelectionForeground(HEADER_COLOR);

        scrollPane.setBorder(LINE_BORDER);
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND_COLOR);
    }

    public static JPanel createDashboardCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBackground(new Color(252, 253, 255));  // Even lighter tint for cards
        card.setBorder(CARD_BORDER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SMALL_FONT);
        titleLabel.setForeground(FONT_COLOR);

        valueLabel.setFont(HEADER_FONT);
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
}