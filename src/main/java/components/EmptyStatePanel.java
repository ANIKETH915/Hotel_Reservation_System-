package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class EmptyStatePanel extends JPanel {
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private StyledButton actionButton;
    private String iconKey = "empty";

    public EmptyStatePanel(String title, String subtitle) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 24, 40, 24));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 48;
                int x = (getWidth() - s) / 2;
                int y = 8;
                g2.setColor(new Color(Theme.ROYAL_BLUE.getRed(), Theme.ROYAL_BLUE.getGreen(),
                        Theme.ROYAL_BLUE.getBlue(), 28));
                g2.fillOval(x, y, s, s);
                NavIcons.paint(g2, iconKey, x + 12, y + 12, 24, Theme.ROYAL_BLUE);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 72);
            }
        };
        icon.setOpaque(false);

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Theme.fontBold(16));
        titleLabel.setForeground(Theme.textPrimary());

        subtitleLabel = new JLabel("<html><body style='text-align:center;width:280px'>"
                + (subtitle == null ? "" : subtitle) + "</body></html>", SwingConstants.CENTER);
        subtitleLabel.setFont(Theme.fontRegular(13));
        subtitleLabel.setForeground(Theme.textSecondary());

        JPanel text = new JPanel(new BorderLayout(0, 6));
        text.setOpaque(false);
        text.add(titleLabel, BorderLayout.NORTH);
        text.add(subtitleLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.setOpaque(false);
        actions.setName("actions");

        center.add(icon, BorderLayout.NORTH);
        center.add(text, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    public void setIconKey(String key) {
        this.iconKey = key;
        repaint();
    }

    public void setAction(String text, Runnable onClick) {
        JPanel actions = null;
        for (java.awt.Component c : ((JPanel) getComponent(0)).getComponents()) {
            if (c instanceof JPanel p && "actions".equals(p.getName())) {
                actions = p;
                break;
            }
        }
        if (actions == null) {
            return;
        }
        actions.removeAll();
        actionButton = new StyledButton(text, StyledButton.Style.PRIMARY);
        actionButton.addActionListener(e -> onClick.run());
        actions.add(actionButton);
        revalidate();
        repaint();
    }

    public void applyTheme() {
        titleLabel.setForeground(Theme.textPrimary());
        subtitleLabel.setForeground(Theme.textSecondary());
        repaint();
    }
}
