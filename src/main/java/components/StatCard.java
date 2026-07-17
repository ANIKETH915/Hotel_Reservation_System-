package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class StatCard extends CardPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel hintLabel;
    private Color accent;
    private double progress = -1;

    public StatCard(String title, String value, Color accent) {
        setLayout(new BorderLayout(0, 8));
        this.accent = accent;
        setBorder(new EmptyBorder(18, 18, 14, 18));
        setMinimumSize(new Dimension(160, 112));

        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontMedium(12));
        titleLabel.setForeground(Theme.textSecondary());

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.fontBold(26));
        valueLabel.setForeground(Theme.textPrimary());

        hintLabel = new JLabel(" ");
        hintLabel.setFont(Theme.fontRegular(11));
        hintLabel.setForeground(Theme.textMuted());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titleLabel, BorderLayout.WEST);

        JPanel accentDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(StatCard.this.accent);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(14, 14);
            }
        };
        accentDot.setOpaque(false);
        top.add(accentDot, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.setOpaque(false);
        footer.add(hintLabel, BorderLayout.NORTH);
        footer.add(new ProgressTrack(), BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setHint(String hint) {
        hintLabel.setText(hint == null ? " " : hint);
    }

    public void setProgress(double progress) {
        this.progress = progress;
        repaint();
    }

    public void applyTheme() {
        titleLabel.setForeground(Theme.textSecondary());
        valueLabel.setForeground(Theme.textPrimary());
        hintLabel.setForeground(Theme.textMuted());
        repaint();
    }

    private class ProgressTrack extends JPanel {
        ProgressTrack() {
            setOpaque(false);
            setPreferredSize(new Dimension(1, 6));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (progress < 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barW = getWidth();
            g2.setColor(Theme.border());
            g2.fillRoundRect(0, 0, barW, 6, 6, 6);
            int filled = (int) Math.round(barW * Math.min(1.0, Math.max(0, progress)));
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, filled, 6, 6, 6);
            g2.dispose();
        }
    }
}
