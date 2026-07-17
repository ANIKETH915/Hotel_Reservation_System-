package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

public class StatusBadge extends JComponent {
    private String text;

    public StatusBadge(String text) {
        this.text = text == null ? "" : text;
        setMinimumSize(new Dimension(72, 24));
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        repaint();
    }

    public String getText() {
        return text;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(Theme.fontMedium(11));
        return new Dimension(Math.max(72, metrics.stringWidth(text) + 20), 24);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = Theme.statusColor(text);
        Color bg = new Color(base.getRed(), base.getGreen(), base.getBlue(), 36);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.setColor(base);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.setFont(Theme.fontMedium(11));
        int tw = g2.getFontMetrics().stringWidth(text);
        int x = Math.max(8, (getWidth() - tw) / 2);
        int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
        g2.drawString(text, x, y);
        g2.dispose();
    }
}
