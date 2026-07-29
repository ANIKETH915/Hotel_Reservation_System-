package components;

import java.awt.BasicStroke;
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
import java.awt.geom.GeneralPath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import utils.CurrencyUtil;

public class PmsLineChart extends CardPanel {

    private final String title;
    private final List<DataPoint> dataPoints = new ArrayList<>();
    private final Color lineColor;

    // Animation sweep
    private double animationProgress = 0.0;
    private Timer animationTimer;

    // Hover detection
    private int hoveredPointIndex = -1;
    private java.awt.Point mousePosition = null;

    public PmsLineChart(String title, Color lineColor) {
        this.title = title;
        this.lineColor = lineColor == null ? Theme.ROYAL_BLUE : lineColor;
        setArc(16);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                checkHoverPoint(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredPointIndex = -1;
                mousePosition = null;
                repaint();
            }
        });
    }

    public static class DataPoint {
        public final LocalDate date;
        public final double value;
        public final String formattedValue;
        public int x;
        public int y;

        public DataPoint(LocalDate date, double value) {
            this.date = date;
            this.value = value;
            this.formattedValue = CurrencyUtil.format(BigDecimal.valueOf(value));
        }
    }

    public void setData(List<LocalDate> dates, List<Double> values) {
        dataPoints.clear();
        for (int i = 0; i < Math.min(dates.size(), values.size()); i++) {
            dataPoints.add(new DataPoint(dates.get(i), values.get(i)));
        }
        triggerAnimation();
    }

    private void triggerAnimation() {
        animationProgress = 0.0;
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationTimer = new Timer(15, e -> {
            animationProgress = Math.min(1.0, animationProgress + 0.05);
            repaint();
            if (animationProgress >= 1.0) {
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }

    private void checkHoverPoint(int mx, int my) {
        int oldIndex = hoveredPointIndex;
        hoveredPointIndex = -1;

        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint dp = dataPoints.get(i);
            int distSq = (mx - dp.x) * (mx - dp.x) + (my - dp.y) * (my - dp.y);
            if (distSq < 100) { // Hover radius of 10px
                hoveredPointIndex = i;
                break;
            }
        }

        if (hoveredPointIndex != oldIndex) {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paint standard CardPanel background & shadow
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 8;
        int h = getHeight() - 8;

        // Draw title
        g2.setFont(Theme.fontBold(13));
        g2.setColor(Theme.textPrimary());
        g2.drawString(title, 20, 28);

        if (dataPoints.isEmpty()) {
            g2.setFont(Theme.fontRegular(13));
            g2.setColor(Theme.textMuted());
            String placeholder = "Gathering transaction records...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, (w - fm.stringWidth(placeholder)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Layout bounds
        int paddingLeft = 60;
        int paddingRight = 30;
        int paddingTop = 60;
        int paddingBottom = 40;

        int chartW = w - paddingLeft - paddingRight;
        int chartH = h - paddingTop - paddingBottom;

        // Find min/max values
        double maxValue = 0;
        for (DataPoint dp : dataPoints) {
            if (dp.value > maxValue) {
                maxValue = dp.value;
            }
        }
        maxValue = Math.max(100, maxValue * 1.15); // Add 15% headroom

        // Grid lines & axis labels
        g2.setFont(Theme.fontRegular(10));
        g2.setColor(Theme.textMuted());
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = paddingTop + chartH - (i * chartH / gridLines);
            double val = i * maxValue / gridLines;

            // Draw grid line
            g2.setColor(new Color(Theme.border().getRed(), Theme.border().getGreen(), Theme.border().getBlue(), 50));
            g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);

            // Draw label
            g2.setColor(Theme.textMuted());
            g2.drawString(formatShort(val), 12, y + 4);
        }

        // Calculate chart points coordinates
        int size = dataPoints.size();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 0; i < size; i++) {
            DataPoint dp = dataPoints.get(i);
            dp.x = paddingLeft + (i * chartW / (size - 1));
            // Animate point height using progress factor
            double animatedVal = dp.value * animationProgress;
            dp.y = paddingTop + chartH - (int) Math.round(animatedVal * chartH / maxValue);

            // Draw X labels
            g2.drawString(dp.date.format(df), dp.x - 18, paddingTop + chartH + 20);
        }

        // Draw area path (gradient fill under line)
        GeneralPath area = new GeneralPath();
        area.moveTo(dataPoints.get(0).x, paddingTop + chartH);
        for (int i = 0; i < size; i++) {
            area.lineTo(dataPoints.get(i).x, dataPoints.get(i).y);
        }
        area.lineTo(dataPoints.get(size - 1).x, paddingTop + chartH);
        area.closePath();

        GradientPaint fillGp = new GradientPaint(
                0, paddingTop, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 60),
                0, paddingTop + chartH, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0)
        );
        g2.setPaint(fillGp);
        g2.fill(area);

        // Draw line path (glow + line)
        GeneralPath path = new GeneralPath();
        path.moveTo(dataPoints.get(0).x, dataPoints.get(0).y);
        for (int i = 1; i < size; i++) {
            path.lineTo(dataPoints.get(i).x, dataPoints.get(i).y);
        }

        // Subtle glow effect
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 40));
        g2.draw(path);

        // Solid path line
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(lineColor);
        g2.draw(path);

        // Draw data points
        for (int i = 0; i < size; i++) {
            DataPoint dp = dataPoints.get(i);
            boolean isHovered = (i == hoveredPointIndex);
            int circleR = isHovered ? 12 : 7;
            
            // Outer glow circle
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), isHovered ? 100 : 40));
            g2.fillOval(dp.x - circleR / 2, dp.y - circleR / 2, circleR, circleR);

            // Core center circle
            g2.setColor(Theme.bgCard());
            g2.fillOval(dp.x - 3, dp.y - 3, 6, 6);
            g2.setColor(lineColor);
            g2.drawOval(dp.x - 3, dp.y - 3, 6, 6);
        }

        // Draw tooltip popup on hover
        if (hoveredPointIndex >= 0 && mousePosition != null) {
            DataPoint dp = dataPoints.get(hoveredPointIndex);
            
            String tipText = dp.formattedValue;
            String tipDate = dp.date.format(df);
            
            g2.setFont(Theme.fontBold(11));
            int textW1 = g2.getFontMetrics().stringWidth(tipText);
            g2.setFont(Theme.fontRegular(10));
            int textW2 = g2.getFontMetrics().stringWidth(tipDate);
            
            int boxW = Math.max(textW1, textW2) + 20;
            int boxH = 40;
            int boxX = Math.min(w - boxW - 10, Math.max(10, dp.x - boxW / 2));
            int boxY = dp.y - boxH - 12;

            // Draw tooltip box
            g2.setColor(Theme.DARK_NAVY);
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
            g2.setColor(Theme.GOLD);
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

            // Draw tooltip text
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.fontRegular(10));
            g2.drawString(tipDate, boxX + 10, boxY + 16);
            g2.setFont(Theme.fontBold(11));
            g2.setColor(Theme.GOLD);
            g2.drawString(tipText, boxX + 10, boxY + 31);
        }

        g2.dispose();
    }

    private String formatShort(double val) {
        String sym = "$";
        String code = CurrencyUtil.getCurrency();
        if ("EUR".equalsIgnoreCase(code)) sym = "€";
        else if ("INR".equalsIgnoreCase(code)) sym = "₹";
        
        if (val >= 1000) {
            return sym + String.format(java.util.Locale.US, "%.1fk", val / 1000.0);
        }
        return sym + String.format(java.util.Locale.US, "%.0f", val);
    }

    public void applyTheme() {
        repaint();
    }
}
