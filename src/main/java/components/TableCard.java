package components;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Card wrapper around a table (or empty-state overlay) for consistent list screens.
 */
public class TableCard extends CardPanel {

    public TableCard(TableEmptyOverlay overlay) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(2, 2, 6, 2));
        setArc(14);
        add(overlay, BorderLayout.CENTER);
    }

    public TableCard(JScrollPane scroll) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(2, 2, 6, 2));
        setArc(14);
        add(scroll, BorderLayout.CENTER);
    }

    public TableCard(String title, TableEmptyOverlay overlay) {
        super(new BorderLayout(0, UiLayout.SPACE_SM));
        setBorder(new EmptyBorder(UiLayout.SPACE_MD, UiLayout.SPACE_MD, UiLayout.SPACE_MD + 2, UiLayout.SPACE_MD));
        setArc(14);
        javax.swing.JLabel label = new javax.swing.JLabel(title);
        label.setFont(Theme.fontMedium(14));
        label.setForeground(Theme.textPrimary());
        add(label, BorderLayout.NORTH);
        add(overlay, BorderLayout.CENTER);
        putClientProperty("titleLabel", label);
    }

    public void applyTheme() {
        Object title = getClientProperty("titleLabel");
        if (title instanceof javax.swing.JLabel label) {
            label.setForeground(Theme.textPrimary());
        }
        repaint();
    }
}
