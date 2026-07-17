package components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class StyledButton extends JButton {
    public enum Style { PRIMARY, SECONDARY, DANGER, GHOST, GOLD }

    private final Style style;
    private boolean hover;

    public StyledButton(String text, Style style) {
        super(text);
        this.style = style;
        setFocusPainted(true);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.fontMedium(13));
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

    public StyledButton(String text) {
        this(text, Style.PRIMARY);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = Math.max(104, metrics.stringWidth(getText() == null ? "" : getText()) + 32);
        return new Dimension(width, 38);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        java.awt.Color fill;
        java.awt.Color text;
        switch (style) {
            case SECONDARY -> {
                fill = hover ? Theme.border() : Theme.bgCard();
                text = Theme.textPrimary();
                g2.setColor(Theme.border());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            case DANGER -> {
                fill = hover ? Theme.DANGER.darker() : Theme.DANGER;
                text = java.awt.Color.WHITE;
            }
            case GOLD -> {
                fill = hover ? Theme.GOLD.darker() : Theme.GOLD;
                text = Theme.DARK_NAVY;
            }
            case GHOST -> {
                fill = hover ? Theme.tableHover() : new java.awt.Color(0, 0, 0, 0);
                text = Theme.ROYAL_BLUE;
            }
            default -> {
                fill = hover ? Theme.ROYAL_BLUE.brighter() : Theme.ROYAL_BLUE;
                text = java.awt.Color.WHITE;
            }
        }

        if (style != Style.GHOST && style != Style.SECONDARY) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        } else if (style == Style.SECONDARY) {
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
        } else if (hover) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }

        if (isFocusOwner()) {
            g2.setColor(Theme.GOLD);
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
        }

        g2.setColor(text);
        g2.setFont(getFont());
        Font font = getFont();
        FontMetrics fm = g2.getFontMetrics(font);
        String label = getText() == null ? "" : getText();
        int textWidth = fm.stringWidth(label);
        int x = Math.max(12, (getWidth() - textWidth) / 2);
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.clipRect(8, 0, Math.max(0, getWidth() - 16), getHeight());
        g2.drawString(label, x, y);
        g2.dispose();
    }
}
