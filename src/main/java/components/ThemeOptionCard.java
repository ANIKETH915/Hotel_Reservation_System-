package components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Selectable Light / Dark theme option tile. */
public class ThemeOptionCard extends JPanel {

    private final boolean darkOption;
    private final String title;
    private final String subtitle;
    private boolean selected;
    private boolean hover;
    private Runnable onSelect;

    public ThemeOptionCard(boolean darkOption, String title, String subtitle) {
        this.darkOption = darkOption;
        this.title = title;
        this.subtitle = subtitle;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(160, 72));
        setMinimumSize(new Dimension(140, 68));
        setBorder(new EmptyBorder(0, 0, 0, 0));
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

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSelect != null) {
                    onSelect.run();
                }
            }
        });
    }

    public void setOnSelect(Runnable onSelect) {
        this.onSelect = onSelect;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isDarkOption() {
        return darkOption;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = selected
                ? (Theme.isDark() ? new Color(30, 58, 138, 70) : new Color(219, 234, 254))
                : (hover ? Theme.tableHover() : Theme.bgCard());
        Color border = selected ? Theme.ROYAL_BLUE : Theme.border();

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        if (selected) {
            g2.setColor(Theme.GOLD);
            g2.fillRoundRect(getWidth() - 18, 10, 8, 8, 8, 8);
        }

        // Icon badge
        int ix = 14;
        int iy = (getHeight() - 28) / 2;
        Color badge = darkOption ? Theme.DARK_NAVY : new Color(0xFE, 0xF3, 0xC7);
        g2.setColor(badge);
        g2.fillRoundRect(ix, iy, 28, 28, 8, 8);
        if (darkOption) {
            g2.setColor(Theme.GOLD);
            g2.fillOval(ix + 8, iy + 8, 12, 12);
            g2.setColor(Theme.DARK_NAVY);
            g2.fillOval(ix + 12, iy + 6, 10, 10);
        } else {
            g2.setColor(new Color(0xF5, 0x9E, 0x0B));
            g2.fillOval(ix + 8, iy + 8, 12, 12);
        }

        g2.setColor(Theme.textPrimary());
        g2.setFont(Theme.fontMedium(13));
        g2.drawString(title, ix + 40, iy + 14);
        g2.setColor(Theme.textSecondary());
        g2.setFont(Theme.fontRegular(11));
        g2.drawString(subtitle, ix + 40, iy + 30);
        g2.dispose();
    }
}
