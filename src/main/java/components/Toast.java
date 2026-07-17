package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public final class Toast {
    private Toast() {
    }

    public static void success(java.awt.Window parent, String message) {
        show(parent, message, Theme.EMERALD);
    }

    public static void error(java.awt.Window parent, String message) {
        show(parent, message, Theme.DANGER);
    }

    public static void info(java.awt.Window parent, String message) {
        show(parent, message, Theme.ROYAL_BLUE);
    }

    private static void show(java.awt.Window parent, String message, Color accent) {
        SwingUtilities.invokeLater(() -> {
            JWindow window = new JWindow(parent);
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.DARK_NAVY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, 6, getHeight(), 12, 12);
                    g2.dispose();
                }
            };
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(14, 20, 14, 20));
            JLabel label = new JLabel(message);
            label.setForeground(Color.WHITE);
            label.setFont(Theme.fontMedium(13));
            panel.add(label, BorderLayout.CENTER);
            window.setContentPane(panel);
            window.pack();
            window.setSize(Math.max(280, window.getWidth()), Math.max(48, window.getHeight()));

            java.awt.Window owner = parent != null ? parent : java.awt.KeyboardFocusManager
                    .getCurrentKeyboardFocusManager().getActiveWindow();
            if (owner != null) {
                int x = owner.getX() + owner.getWidth() - window.getWidth() - 24;
                int y = owner.getY() + owner.getHeight() - window.getHeight() - 24;
                window.setLocation(x, y);
            }
            window.setVisible(true);
            Timer timer = new Timer(2800, e -> {
                window.setVisible(false);
                window.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
