package ui;

import components.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class SplashScreen extends JWindow {

    private final Runnable onComplete;
    private final JProgressBar progressBar;

    public SplashScreen(Runnable onComplete) {
        this.onComplete = onComplete;

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.DARK_NAVY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(40, 48, 40, 48));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.GOLD);
                g2.fillOval(0, 0, 80, 80);
                g2.setColor(Theme.DARK_NAVY);
                g2.setFont(Theme.fontBold(28));
                g2.drawString("GA", 22, 50);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 80);
            }
        };
        logo.setOpaque(false);

        JLabel title = new JLabel("Grand Azure");
        title.setFont(Theme.fontDisplay(32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Hotel Reservation System");
        subtitle.setFont(Theme.fontRegular(14));
        subtitle.setForeground(new Color(0x94, 0xA3, 0xB8));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(320, 8));
        progressBar.setForeground(Theme.GOLD);
        progressBar.setBackground(new Color(0x1E, 0x29, 0x3B));
        progressBar.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        center.add(logo, gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        center.add(title, gbc);

        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(0, 0, 32, 0);
        center.add(subtitle, gbc);

        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        center.add(progressBar, gbc);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
        setSize(480, 320);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void showSplash() {
        setVisible(true);
    }

    public void setProgress(int value) {
        progressBar.setValue(Math.max(0, Math.min(value, 100)));
    }

    public void complete() {
        setProgress(100);
        setVisible(false);
        dispose();
        if (onComplete != null) {
            SwingUtilities.invokeLater(onComplete);
        }
    }
}
