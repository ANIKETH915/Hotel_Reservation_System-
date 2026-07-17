package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CardPanel extends JPanel {
    private int arc = 16;
    private static final Color SHADOW_LIGHT = new Color(15, 23, 42, 22);
    private static final Color SHADOW_DARK = new Color(15, 23, 42, 50);

    public CardPanel() {
        this(null);
    }

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setDoubleBuffered(true);
    }

    public void setArc(int arc) {
        this.arc = arc;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Skip expensive AA when resizing rapidly — still looks good
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        int w = getWidth() - 4;
        int h = getHeight() - 4;
        g2.setColor(Theme.isDark() ? SHADOW_DARK : SHADOW_LIGHT);
        g2.fillRoundRect(3, 4, w, h, arc, arc);

        g2.setColor(Theme.bgCard());
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        g2.setColor(Theme.border());
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.dispose();
        // Do NOT call super.paintComponent — opaque children paint themselves
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 120), Math.max(d.height, 80));
    }
}
