package components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Simple painted icons — no external assets required. */
public final class NavIcons {
    private NavIcons() {
    }

    public static void paint(Graphics2D g, String key, int x, int y, int size, Color color) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(Math.max(1.4f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        String k = key == null ? "" : key.toLowerCase();
        switch (k) {
            case "dashboard" -> paintDashboard(g2, x, y, size);
            case "rooms" -> paintBed(g2, x, y, size);
            case "customers" -> paintPerson(g2, x, y, size);
            case "bookings" -> paintCalendar(g2, x, y, size);
            case "payments" -> paintCard(g2, x, y, size);
            case "reports" -> paintChart(g2, x, y, size);
            case "settings" -> paintGear(g2, x, y, size);
            case "about" -> paintInfo(g2, x, y, size);
            case "logout" -> paintLogout(g2, x, y, size);
            case "export" -> paintExport(g2, x, y, size);
            case "backup" -> paintBackup(g2, x, y, size);
            case "revenue", "daily" -> paintRevenue(g2, x, y, size);
            case "monthly" -> paintMonthly(g2, x, y, size);
            case "summary" -> paintSummary(g2, x, y, size);
            case "utilization" -> paintUtilization(g2, x, y, size);
            default -> paintEmpty(g2, x, y, size);
        }
        g2.dispose();
    }

    private static void paintDashboard(Graphics2D g, int x, int y, int s) {
        int gapa = Math.max(2, s / 8);
        int half = (s - gapa) / 2;
        g.fill(new RoundRectangle2D.Float(x, y, half, half, 3, 3));
        g.fill(new RoundRectangle2D.Float(x + half + gapa, y, half, half, 3, 3));
        g.fill(new RoundRectangle2D.Float(x, y + half + gapa, half, half, 3, 3));
        g.fill(new RoundRectangle2D.Float(x + half + gapa, y + half + gapa, half, half, 3, 3));
    }

    private static void paintBed(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x, y + s * 0.35f, s, s * 0.45f, 4, 4));
        g.draw(new Line2D.Float(x, y + s * 0.55f, x + s, y + s * 0.55f));
        g.fill(new RoundRectangle2D.Float(x + s * 0.12f, y + s * 0.18f, s * 0.28f, s * 0.2f, 3, 3));
    }

    private static void paintPerson(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + s * 0.3f, y + s * 0.08f, s * 0.4f, s * 0.4f));
        g.draw(new RoundRectangle2D.Float(x + s * 0.15f, y + s * 0.55f, s * 0.7f, s * 0.38f, s * 0.35f, s * 0.35f));
    }

    private static void paintCalendar(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 1, y + s * 0.18f, s - 2, s * 0.72f, 4, 4));
        g.draw(new Line2D.Float(x + 1, y + s * 0.4f, x + s - 1, y + s * 0.4f));
        g.draw(new Line2D.Float(x + s * 0.28f, y + s * 0.05f, x + s * 0.28f, y + s * 0.28f));
        g.draw(new Line2D.Float(x + s * 0.72f, y + s * 0.05f, x + s * 0.72f, y + s * 0.28f));
    }

    private static void paintCard(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 1, y + s * 0.22f, s - 2, s * 0.56f, 4, 4));
        g.fill(new Rectangle2D.Float(x + 1, y + s * 0.36f, s - 2, s * 0.14f));
        g.draw(new Line2D.Float(x + s * 0.2f, y + s * 0.62f, x + s * 0.55f, y + s * 0.62f));
    }

    private static void paintChart(Graphics2D g, int x, int y, int s) {
        g.draw(new Line2D.Float(x + 2, y + s - 2, x + 2, y + 2));
        g.draw(new Line2D.Float(x + 2, y + s - 2, x + s - 2, y + s - 2));
        g.fill(new Rectangle2D.Float(x + s * 0.22f, y + s * 0.45f, s * 0.14f, s * 0.35f));
        g.fill(new Rectangle2D.Float(x + s * 0.45f, y + s * 0.28f, s * 0.14f, s * 0.52f));
        g.fill(new Rectangle2D.Float(x + s * 0.68f, y + s * 0.18f, s * 0.14f, s * 0.62f));
    }

    private static void paintGear(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + s * 0.28f, y + s * 0.28f, s * 0.44f, s * 0.44f));
        g.draw(new Ellipse2D.Float(x + s * 0.38f, y + s * 0.38f, s * 0.24f, s * 0.24f));
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(i * 60);
            double cx = x + s / 2.0;
            double cy = y + s / 2.0;
            double x1 = cx + Math.cos(a) * s * 0.28;
            double y1 = cy + Math.sin(a) * s * 0.28;
            double x2 = cx + Math.cos(a) * s * 0.48;
            double y2 = cy + Math.sin(a) * s * 0.48;
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }

    private static void paintInfo(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + 2, y + 2, s - 4, s - 4));
        g.fill(new Ellipse2D.Float(x + s * 0.45f, y + s * 0.28f, s * 0.12f, s * 0.12f));
        g.draw(new Line2D.Float(x + s * 0.5f, y + s * 0.48f, x + s * 0.5f, y + s * 0.72f));
    }

    private static void paintLogout(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 2, y + 3, s * 0.55f, s - 6, 4, 4));
        g.draw(new Line2D.Float(x + s * 0.45f, y + s * 0.5f, x + s - 2, y + s * 0.5f));
        g.draw(new Line2D.Float(x + s * 0.7f, y + s * 0.32f, x + s - 2, y + s * 0.5f));
        g.draw(new Line2D.Float(x + s * 0.7f, y + s * 0.68f, x + s - 2, y + s * 0.5f));
    }

    private static void paintEmpty(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 2, y + 2, s - 4, s - 4, 6, 6));
        g.draw(new Line2D.Float(x + s * 0.3f, y + s * 0.5f, x + s * 0.7f, y + s * 0.5f));
    }

    private static void paintExport(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 2, y + s * 0.45f, s - 4, s * 0.42f, 4, 4));
        g.draw(new Line2D.Float(x + s * 0.5f, y + 2, x + s * 0.5f, y + s * 0.55f));
        g.draw(new Line2D.Float(x + s * 0.3f, y + s * 0.28f, x + s * 0.5f, y + s * 0.5f));
        g.draw(new Line2D.Float(x + s * 0.7f, y + s * 0.28f, x + s * 0.5f, y + s * 0.5f));
    }

    private static void paintBackup(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + 3, y + 2, s - 6, s * 0.28f));
        g.draw(new Line2D.Float(x + 3, y + s * 0.16f, x + 3, y + s * 0.7f));
        g.draw(new Line2D.Float(x + s - 3, y + s * 0.16f, x + s - 3, y + s * 0.7f));
        g.draw(new Ellipse2D.Float(x + 3, y + s * 0.55f, s - 6, s * 0.28f));
        g.draw(new Ellipse2D.Float(x + 3, y + s * 0.32f, s - 6, s * 0.28f));
    }

    private static void paintRevenue(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + 2, y + 2, s - 4, s - 4));
        g.draw(new Line2D.Float(x + s * 0.5f, y + s * 0.28f, x + s * 0.5f, y + s * 0.72f));
        g.draw(new Line2D.Float(x + s * 0.35f, y + s * 0.38f, x + s * 0.65f, y + s * 0.38f));
        g.draw(new Line2D.Float(x + s * 0.35f, y + s * 0.55f, x + s * 0.65f, y + s * 0.55f));
    }

    private static void paintMonthly(Graphics2D g, int x, int y, int s) {
        paintCalendar(g, x, y, s);
        g.fill(new Rectangle2D.Float(x + s * 0.28f, y + s * 0.52f, s * 0.16f, s * 0.16f));
        g.fill(new Rectangle2D.Float(x + s * 0.52f, y + s * 0.52f, s * 0.16f, s * 0.16f));
    }

    private static void paintSummary(Graphics2D g, int x, int y, int s) {
        g.draw(new RoundRectangle2D.Float(x + 2, y + 2, s - 4, s - 4, 4, 4));
        g.draw(new Line2D.Float(x + s * 0.25f, y + s * 0.32f, x + s * 0.75f, y + s * 0.32f));
        g.draw(new Line2D.Float(x + s * 0.25f, y + s * 0.5f, x + s * 0.75f, y + s * 0.5f));
        g.draw(new Line2D.Float(x + s * 0.25f, y + s * 0.68f, x + s * 0.6f, y + s * 0.68f));
    }

    private static void paintUtilization(Graphics2D g, int x, int y, int s) {
        g.draw(new Ellipse2D.Float(x + 2, y + 2, s - 4, s - 4));
        g.fillArc(x + 4, y + 4, s - 8, s - 8, 90, -200);
    }
}
