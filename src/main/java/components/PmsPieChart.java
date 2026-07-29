package components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class PmsPieChart extends CardPanel {

    private final String title;
    private final List<Segment> segments = new ArrayList<>();
    private double occupancyRate = 0.0;

    // Animation progress
    private double animProgress = 0.0;
    private Timer animTimer;

    // Interactive segment hover
    private int hoveredSegmentIndex = -1;

    public PmsPieChart(String title) {
        this.title = title;
        setArc(16);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHoverSegment(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredSegmentIndex = -1;
                repaint();
            }
        });
    }

    public static class Segment {
        public final String name;
        public final int count;
        public final Color color;
        public double percentage;
        public double startAngle;
        public double sweepAngle;

        public Segment(String name, int count, Color color) {
            this.name = name;
            this.count = count;
            this.color = color;
        }
    }

    public void setOccupancyRate(double rate) {
        this.occupancyRate = rate;
    }

    public void setSegmentsData(List<Segment> data) {
        this.segments.clear();
        int total = 0;
        for (Segment s : data) {
            total += s.count;
        }

        if (total > 0) {
            double currentAngle = 90.0; // Start at 12 o'clock
            for (Segment s : data) {
                s.percentage = (double) s.count / total;
                s.startAngle = currentAngle;
                s.sweepAngle = -s.percentage * 360.0; // Counter-clockwise
                currentAngle += s.sweepAngle;
                this.segments.add(s);
            }
        }
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

    private void checkHoverSegment(int mx, int my) {
        int w = getWidth() - 8;
        int h = getHeight() - 8;
        int size = Math.min(w, h) - 100;
        int cx = w / 2;
        int cy = h / 2 + 10;

        int dx = mx - cx;
        int dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        int outerR = size / 2;
        int innerR = outerR - 26; // Donut thickness

        int oldIndex = hoveredSegmentIndex;
        hoveredSegmentIndex = -1;

        if (dist >= innerR && dist <= outerR) {
            // Calculate angle in degrees [0, 360]
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) {
                angle += 360.0;
            }
            
            // Map mouse angle to segment start/sweep
            // Note: sweep angles are negative
            for (int i = 0; i < segments.size(); i++) {
                Segment s = segments.get(i);
                double start = -s.startAngle; // Adjust to normal trigonometric angle
                if (start < 0) start += 360;
                double end = -(s.startAngle + s.sweepAngle);
                if (end < 0) end += 360;

                // Adjust comparisons for wrap-around
                boolean inRange = false;
                double clickAngle = angle;
                
                double sAngleNorm = (360 - s.startAngle) % 360;
                double eAngleNorm = (360 - (s.startAngle + s.sweepAngle)) % 360;
                if (sAngleNorm < 0) sAngleNorm += 360;
                if (eAngleNorm < 0) eAngleNorm += 360;
                
                if (sAngleNorm < eAngleNorm) {
                    if (clickAngle >= sAngleNorm && clickAngle <= eAngleNorm) {
                        inRange = true;
                    }
                } else {
                    if (clickAngle >= sAngleNorm || clickAngle <= eAngleNorm) {
                        inRange = true;
                    }
                }

                if (inRange) {
                    hoveredSegmentIndex = i;
                    break;
                }
            }
        }

        if (hoveredSegmentIndex != oldIndex) {
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

        if (segments.isEmpty()) {
            g2.setFont(Theme.fontRegular(13));
            g2.setColor(Theme.textMuted());
            String placeholder = "Analysing room registry...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, (w - fm.stringWidth(placeholder)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Donut dimensions
        int diameter = Math.min(w, h) - 110;
        int cx = w / 2;
        int cy = h / 2 + 10;
        int x = cx - diameter / 2;
        int y = cy - diameter / 2;

        // Draw Donut segments
        for (int i = 0; i < segments.size(); i++) {
            Segment s = segments.get(i);
            boolean isHovered = (i == hoveredSegmentIndex);

            g2.setColor(s.color);
            int arcX = x;
            int arcY = y;

            // Explode segment slightly outwards on hover
            if (isHovered) {
                double midAngle = Math.toRadians(s.startAngle + s.sweepAngle / 2);
                int offsetDist = 6;
                arcX += (int) (Math.cos(midAngle) * offsetDist);
                arcY += (int) (Math.sin(midAngle) * offsetDist);
            }

            g2.fillArc(arcX, arcY, diameter, diameter, (int) Math.round(s.startAngle), (int) Math.round(s.sweepAngle * animProgress));
        }

        // Draw inner cutout to turn the pie into a donut
        int innerCutoutD = diameter - 52;
        int cutX = cx - innerCutoutD / 2;
        int cutY = cy - innerCutoutD / 2;
        g2.setColor(Theme.bgCard());
        g2.fillOval(cutX, cutY, innerCutoutD, innerCutoutD);

        // Draw border inside donut cutout
        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(Theme.border());
        g2.drawOval(cutX, cutY, innerCutoutD, innerCutoutD);
        g2.drawOval(x, y, diameter, diameter);

        // Draw occupancy percentage text in the center
        String pctText = String.format("%.1f%%", occupancyRate);
        g2.setFont(Theme.fontDisplay(18));
        g2.setColor(Theme.textPrimary());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pctText, cx - fm.stringWidth(pctText) / 2, cy + 3);

        g2.setFont(Theme.fontRegular(10));
        g2.setColor(Theme.textSecondary());
        String sub = "Occupied";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 16);

        // Draw custom legend at the bottom of the card
        int legendX = 14;
        int legendY = h - 22;
        g2.setFont(Theme.fontRegular(11));

        for (Segment s : segments) {
            g2.setColor(s.color);
            g2.fillRoundRect(legendX, legendY - 8, 8, 8, 3, 3);
            g2.setColor(Theme.textPrimary());
            String label = s.name + " (" + s.count + ")";
            g2.drawString(label, legendX + 13, legendY - 1);

            int width = g2.getFontMetrics().stringWidth(label) + 26;
            legendX += width;
        }

        g2.dispose();
    }

    public void applyTheme() {
        repaint();
    }
}
