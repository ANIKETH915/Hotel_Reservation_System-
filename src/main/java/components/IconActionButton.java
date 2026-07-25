package components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Fixed-size action button with a painted leading icon — for analytics toolbars.
 */
public class IconActionButton extends JButton {

    public enum Tone { NEUTRAL, PRIMARY, GOLD }

    private static final int WIDTH = 200;
    private static final int HEIGHT = 40;
    private static final int ICON = 16;

    private final String iconKey;
    private final Tone tone;
    private boolean hover;
    private boolean active;

    public IconActionButton(String text, String iconKey) {
        this(text, iconKey, Tone.NEUTRAL);
    }

    public IconActionButton(String text, String iconKey, Tone tone) {
        super(text);
        this.iconKey = iconKey;
        this.tone = tone;
        setFocusPainted(true);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Theme.fontMedium(12));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
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

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill;
        Color border;
        Color text;
        Color icon;

        if (tone == Tone.GOLD) {
            fill = hover || active ? Theme.GOLD.darker() : Theme.GOLD;
            border = fill;
            text = Theme.DARK_NAVY;
            icon = Theme.DARK_NAVY;
        } else if (tone == Tone.PRIMARY || active) {
            fill = hover ? Theme.ROYAL_BLUE.brighter() : Theme.ROYAL_BLUE;
            border = fill;
            text = Color.WHITE;
            icon = Theme.GOLD;
        } else {
            fill = hover ? Theme.tableHover() : Theme.bgCard();
            border = active ? Theme.ROYAL_BLUE : Theme.border();
            text = Theme.textPrimary();
            icon = active ? Theme.ROYAL_BLUE : Theme.textSecondary();
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        if (isFocusOwner()) {
            g2.setColor(Theme.GOLD);
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
        }

        int iconY = (getHeight() - ICON) / 2;
        NavIcons.paint(g2, iconKey, 14, iconY, ICON, icon);

        g2.setColor(text);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String label = getText() == null ? "" : getText();
        int textX = 14 + ICON + 10;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.clipRect(textX, 0, Math.max(0, getWidth() - textX - 12), getHeight());
        g2.drawString(label, textX, textY);
        g2.dispose();
    }
}
