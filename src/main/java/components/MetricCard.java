package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Compact KPI metric card for analytics dashboards. */
public class MetricCard extends CardPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final String iconKey;
    private final Color accent;
    private boolean hover;

    public MetricCard(String title, String value, String iconKey, Color accent) {
        this.iconKey = iconKey;
        this.accent = accent == null ? Theme.ROYAL_BLUE : accent;
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(180, 110));
        setMinimumSize(new Dimension(140, 100));
        setCursor(Cursor.getDefaultCursor());
        setArc(14);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color wash = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hover ? 40 : 28);
                g2.setColor(wash);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                NavIcons.paint(g2, iconKey, 8, 8, 18, accent);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }
        };
        iconBadge.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontMedium(12));
        titleLabel.setForeground(Theme.textSecondary());

        top.add(iconBadge, BorderLayout.WEST);
        top.add(titleLabel, BorderLayout.CENTER);

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.fontBold(24));
        valueLabel.setForeground(Theme.textPrimary());

        add(top, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void applyTheme() {
        titleLabel.setForeground(Theme.textSecondary());
        valueLabel.setForeground(Theme.textPrimary());
        repaint();
    }
}
