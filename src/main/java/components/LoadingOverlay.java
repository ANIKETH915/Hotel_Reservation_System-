package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LoadingOverlay extends JPanel {

    private final JLabel messageLabel;

    public LoadingOverlay() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setVisible(false);

        messageLabel = new JLabel("Loading...");
        messageLabel.setFont(Theme.fontMedium(16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(new EmptyBorder(16, 32, 16, 32));

        JPanel pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.add(messageLabel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(pill, gbc);
    }

    public void showOverlay(String message) {
        messageLabel.setText(message != null ? message : "Loading...");
        setVisible(true);
        revalidate();
        repaint();
    }

    public void showOverlay() {
        showOverlay("Loading...");
    }

    public void hideOverlay() {
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
