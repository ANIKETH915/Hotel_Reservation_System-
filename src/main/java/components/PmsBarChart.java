package components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class PmsBarChart extends CardPanel {

    private final String title;
    private final List<BarItem> items = new ArrayList<>();

    // Animation progress
    private double animProgress = 0.0;
    private Timer animTimer;

    // Hover index
    private int hoveredBarIndex = -1;

    public PmsBarChart(String title) {
        this.title = title;
        setArc(16);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHoverBar(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredBarIndex = -1;
                repaint();
            }
        });
    }

    public static class BarItem {
        public final String label;
        public final int value;
        public final Color startColor;
        public final Color endColor;
        public int x;
        public int y;
        public int w;
        public int h;

        public BarItem(String label, int value, Color startColor, Color endColor) {
            this.label = label;
            this.value = value;
            this.startColor = startColor;
            this.endColor = endColor;
        }
    }

    public void setData(List<BarItem> data) {
        this.items.clear();
        this.items.addAll(data);
        triggerAnimation();
    }

    private void triggerAnimation() {
        animProgress = 0.0;
        if (animTimer != null && animTimer.isRunning()) {
            animTimer.stop();
        }
        animTimer = new Timer(15, e -> {
            animProgress = Math.min(1.0, animProgress + 0.05);
            repaint();
            if (animProgress >= 1.0) {
                animTimer.stop();
            }
        });
        animTimer.start();
    }

    private void checkHoverBar(int mx, int my) {
        int oldIndex = hoveredBarIndex;
        hoveredBarIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            BarItem item = items.get(i);
            if (mx >= item.x && mx <= item.x + item.w &&
                my >= item.y && my <= item.y + item.h) {
                hoveredBarIndex = i;
                break;
            }
        }

        if (hoveredBarIndex != oldIndex) {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 8;
        int h = getHeight() - 8;

        // Draw title
        g2.setFont(Theme.fontBold(13));
        g2.setColor(Theme.textPrimary());
        g2.drawString(title, 20, 28);

        if (items.isEmpty()) {
            g2.setFont(Theme.fontRegular(13));
            g2.setColor(Theme.textMuted());
            String placeholder = "Parsing booking metrics...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, (w - fm.stringWidth(placeholder)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Layout bounds
        int paddingLeft = 40;
        int paddingRight = 40;
        int paddingTop = 60;
        int paddingBottom = 40;

        int chartW = w - paddingLeft - paddingRight;
        int chartH = h - paddingTop - paddingBottom;

        // Find max value
        int maxVal = 0;
        for (BarItem item : items) {
            if (item.value > maxVal) {
                maxVal = item.value;
            }
        }
        maxVal = Math.max(5, (int) Math.ceil(maxVal * 1.2)); // 20% headroom

        // Draw helper grid lines
        g2.setFont(Theme.fontRegular(10));
        g2.setColor(Theme.textMuted());
        int gridCount = 3;
        for (int i = 0; i <= gridCount; i++) {
            int yGrid = paddingTop + chartH - (i * chartH / gridCount);
            int valGrid = i * maxVal / gridCount;
            
            g2.setColor(new Color(Theme.border().getRed(), Theme.border().getGreen(), Theme.border().getBlue(), 50));
            g2.drawLine(paddingLeft, yGrid, paddingLeft + chartW, yGrid);
            
            g2.setColor(Theme.textMuted());
            g2.drawString(String.valueOf(valGrid), 15, yGrid + 4);
        }

        // Draw Bars
        int barCount = items.size();
        int gap = 30;
        int barW = (chartW - (gap * (barCount - 1))) / barCount;

        for (int i = 0; i < barCount; i++) {
            BarItem item = items.get(i);
            boolean isHovered = (i == hoveredBarIndex);

            item.w = barW;
            // Height animation
            double animatedVal = item.value * animProgress;
            item.h = (int) Math.round(animatedVal * chartH / maxVal);
            item.x = paddingLeft + i * (barW + gap);
            item.y = paddingTop + chartH - item.h;

            // Draw Bar Background (very faint track)
            g2.setColor(new Color(Theme.border().getRed(), Theme.border().getGreen(), Theme.border().getBlue(), 35));
            g2.fillRoundRect(item.x, paddingTop, item.w, chartH, 12, 12);

            if (item.h > 0) {
                // Draw bar with gradient
                GradientPaint gp = new GradientPaint(
                        item.x, item.y, item.startColor,
                        item.x, item.y + item.h, item.endColor
                );
                g2.setPaint(gp);
                g2.fillRoundRect(item.x, item.y, item.w, item.h, 12, 12);

                // Add a glow ring on hover
                if (isHovered) {
                    g2.setStroke(new java.awt.BasicStroke(2.0f));
                    g2.setColor(Theme.GOLD);
                    g2.drawRoundRect(item.x - 1, item.y - 1, item.w + 2, item.h + 2, 12, 12);
                }
            }

            // Draw Value label at top of bar
            g2.setFont(Theme.fontBold(11));
            g2.setColor(isHovered ? Theme.GOLD : Theme.textPrimary());
            String valLabel = String.valueOf(item.value);
            FontMetrics fm = g2.getFontMetrics();
            int labelY = item.y - 6;
            if (labelY < paddingTop - 4) {
                labelY = paddingTop - 4;
            }
            g2.drawString(valLabel, item.x + (item.w - fm.stringWidth(valLabel)) / 2, labelY);

            // Draw Category label at bottom
            g2.setFont(Theme.fontRegular(11));
            g2.setColor(Theme.textSecondary());
            fm = g2.getFontMetrics();
            g2.drawString(item.label, item.x + (item.w - fm.stringWidth(item.label)) / 2, paddingTop + chartH + 20);
        }

        g2.dispose();
    }

    public void applyTheme() {
        repaint();
    }
}
