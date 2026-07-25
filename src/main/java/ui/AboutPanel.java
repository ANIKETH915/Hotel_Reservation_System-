package ui;

import components.CardPanel;
import components.PageHeader;
import components.Theme;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class AboutPanel extends JPanel implements MainFrame.RefreshablePanel {

    private PageHeader pageHeader;
    private JScrollPane pageScroll;
    private JLabel[] bodyLabels;

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("About", "Product information and technology stack");

        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UiLayout.SPACE_LG, UiLayout.SPACE_XL, UiLayout.SPACE_LG, UiLayout.SPACE_XL));
        card.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));

        String[] lines = {
                "Grand Azure Hotel Reservation System",
                "Version 1.0.0",
                "",
                "CodeAlpha Java Internship — Task 4",
                "Property Management System for hotel operations",
                "",
                "Technology Stack",
                "  • Java 17",
                "  • Swing (Custom UI Components)",
                "  • MySQL Database",
                "  • Maven Build System",
                "",
                "Features: Room & customer management, bookings,",
                "payments, reports, CSV import/export, and database backup.",
                "",
                "© 2026 Grand Azure Hotel & Suites"
        };

        bodyLabels = new JLabel[lines.length];
        for (int i = 0; i < lines.length; i++) {
            bodyLabels[i] = addLine(card, lines[i], i == 0 || "Technology Stack".equals(lines[i]));
        }

        UiLayout.ViewportWidthPanel content = new UiLayout.ViewportWidthPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, UiLayout.SPACE_MD, 0));

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setAlignmentX(LEFT_ALIGNMENT);
        column.setMaximumSize(new Dimension(720, Integer.MAX_VALUE));
        column.add(UiLayout.fullWidth(pageHeader));
        column.add(UiLayout.fullWidth(card));
        content.add(column);

        pageScroll = UiLayout.pageScroll(content);
        add(pageScroll, BorderLayout.CENTER);
    }

    private JLabel addLine(JPanel panel, String text, boolean emphasize) {
        JLabel lbl = new JLabel(text.isEmpty() ? " " : text);
        lbl.setFont(emphasize ? Theme.fontMedium(15) : Theme.fontRegular(14));
        lbl.setForeground(Theme.textPrimary());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(text.isEmpty() ? UiLayout.SPACE_XS : 1, 0, 1, 0));
        panel.add(lbl);
        if (emphasize) {
            panel.add(Box.createVerticalStrut(2));
        }
        return lbl;
    }

    @Override
    public void refresh() {
        // static content
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        if (bodyLabels != null) {
            for (JLabel label : bodyLabels) {
                label.setForeground(Theme.textPrimary());
            }
        }
        if (pageScroll != null) {
            pageScroll.getViewport().setBackground(Theme.bgPrimary());
        }
        repaint();
    }
}
