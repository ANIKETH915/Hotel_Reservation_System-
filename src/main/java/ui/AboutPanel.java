package ui;

import components.CardPanel;
import components.Theme;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AboutPanel extends JPanel implements MainFrame.RefreshablePanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        JLabel title = new JLabel("About Grand Azure");
        title.setFont(Theme.fontBold(22));
        title.setForeground(Theme.textPrimary());

        CardPanel card = new CardPanel();
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        addLine(card, "Grand Azure Hotel Reservation System");
        addLine(card, "Version 1.0.0");
        addLine(card, " ");
        addLine(card, "CodeAlpha Java Internship — Task 4");
        addLine(card, "Property Management System for hotel operations");
        addLine(card, " ");
        addLine(card, "Technology Stack:");
        addLine(card, "  • Java 17");
        addLine(card, "  • Swing (Custom UI Components)");
        addLine(card, "  • MySQL Database");
        addLine(card, "  • Maven Build System");
        addLine(card, " ");
        addLine(card, "Features: Room & customer management, bookings,");
        addLine(card, "payments, reports, CSV import/export, and database backup.");
        addLine(card, " ");
        addLine(card, "© 2026 Grand Azure Hotel & Suites");

        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(card, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    private void addLine(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.fontRegular(14));
        lbl.setForeground(Theme.textPrimary());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);
    }

    @Override
    public void refresh() {
        // static content
    }
}
