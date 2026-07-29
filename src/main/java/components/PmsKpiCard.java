package components;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class PmsKpiCard extends CardPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel trendLabel;
    private final String iconKey;
    private final Color accentColor;

    private boolean isHovered = false;
    private double hoverProgress = 0.0;
    private Timer hoverTimer;

    // Counter animation variables
    private double targetValue = 0;
    private double currentValue = 0;
    private Timer counterTimer;
    private String prefix = "";
    private String suffix = "";
    private boolean isCurrency = false;
    private boolean isPercentage = false;

    public PmsKpiCard(String title, String initialValue, String iconKey, Color accent, String trendText, boolean positiveTrend) {
        this.iconKey = iconKey;
        this.accentColor = accent == null ? Theme.ROYAL_BLUE : accent;
        
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(18, 20, 16, 20));
        setArc(20);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Title
        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.fontBold(10));
        titleLabel.setForeground(Theme.textSecondary());

        // Value
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(Theme.fontDisplay(24));
        valueLabel.setForeground(Theme.textPrimary());

        // Trend Panel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        trendLabel = new JLabel("");
        trendLabel.setFont(Theme.fontMedium(11));
        setTrend(trendText, positiveTrend);
        footer.add(trendLabel);

        // Header Panel (Title + Icon)
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.CENTER);

        // Icon Badge
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded badge with glow
                Color wash = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), isHovered ? 45 : 24);
                g2.setColor(wash);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Draw border
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                NavIcons.paint(g2, PmsKpiCard.this.iconKey, 8, 8, 18, accentColor);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }
        };
        iconBadge.setOpaque(false);
        header.add(iconBadge, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Hover animation timer (smooth coordinate shift & shadow depth transition)
        hoverTimer = new Timer(15, e -> {
            if (isHovered) {
                hoverProgress = Math.min(1.0, hoverProgress + 0.15);
            } else {
                hoverProgress = Math.max(0.0, hoverProgress - 0.15);
            }
            repaint();
            if (hoverProgress == 0.0 || hoverProgress == 1.0) {
                hoverTimer.stop();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                hoverTimer.start();
            }
        });

        setValue(initialValue);
    }

    public void setTrend(String trendText, boolean positive) {
        if (trendText == null || trendText.isBlank()) {
            trendLabel.setText("• stable activity");
            trendLabel.setForeground(Theme.textMuted());
        } else {
            String arrow = positive ? "▲ " : "▼ ";
            trendLabel.setText(arrow + trendText);
            trendLabel.setForeground(positive ? Theme.EMERALD : Theme.DANGER);
        }
    }

    public void setValue(String valStr) {
        if (valStr == null) {
            valueLabel.setText("-");
            return;
        }

        // Parse numerical values to run animated counter
        try {
            String clean = valStr.trim();
            isCurrency = clean.contains("$") || clean.contains("₹") || clean.contains("€") || clean.toLowerCase().contains("usd");
            isPercentage = clean.contains("%");
            
            // Extract numeric parts
            prefix = "";
            suffix = "";
            
            if (clean.startsWith("$")) { prefix = "$"; clean = clean.substring(1); }
            else if (clean.startsWith("₹")) { prefix = "₹"; clean = clean.substring(1); }
            else if (clean.startsWith("€")) { prefix = "€"; clean = clean.substring(1); }
            
            if (clean.endsWith("%")) { suffix = "%"; clean = clean.substring(0, clean.length() - 1); }
            else if (clean.endsWith(" rooms")) { suffix = " rooms"; clean = clean.substring(0, clean.length() - 6); }
            else if (clean.endsWith(" guests")) { suffix = " guests"; clean = clean.substring(0, clean.length() - 7); }

            clean = clean.replace(",", "").trim();
            double target = Double.parseDouble(clean);
            animateTo(target);
        } catch (Exception ex) {
            // Fallback for non-numeric values
            valueLabel.setText(valStr);
        }
    }

    private void animateTo(double target) {
        targetValue = target;
        currentValue = 0; // Reset animation
        
        if (counterTimer != null && counterTimer.isRunning()) {
            counterTimer.stop();
        }

        final int durationMs = 600;
        final int intervalMs = 20;
        final double steps = (double) durationMs / intervalMs;
        final double increment = targetValue / steps;

        counterTimer = new Timer(intervalMs, e -> {
            currentValue += increment;
            if (currentValue >= targetValue) {
                currentValue = targetValue;
                counterTimer.stop();
            }
            updateValueDisplay();
        });
        counterTimer.start();
    }

    private void updateValueDisplay() {
        if (isCurrency) {
            valueLabel.setText(prefix + String.format(Locale.US, "%,.0f", currentValue) + suffix);
        } else if (isPercentage) {
            valueLabel.setText(prefix + String.format(Locale.US, "%.1f", currentValue) + suffix);
        } else {
            valueLabel.setText(prefix + String.format(Locale.US, "%,.0f", currentValue) + suffix);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = getArc();

        // 3D Lift coordinate calculation
        int shiftY = (int) Math.round(hoverProgress * -4);

        // Draw shadow
        Color shadowColor = Theme.isDark() ? new Color(0, 0, 0, 110) : new Color(15, 23, 42, 28);
        int shadowOffset = 4 + (int) Math.round(hoverProgress * 3);
        int shadowBlur = 8 + (int) Math.round(hoverProgress * 6);
        
        for (int i = 0; i < shadowBlur; i++) {
            float alpha = 0.05f * (1.0f - (float) i / shadowBlur);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(shadowColor);
            g2.fillRoundRect(4 + i / 2, 4 + shiftY + shadowOffset + i / 2, w - 8 - i, h - 8 - i, arc, arc);
        }
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Draw card background
        g2.setColor(Theme.bgCard());
        g2.fillRoundRect(0, shiftY, w - 1, h - 1, arc, arc);

        // Draw left gradient accent bar
        GradientPaint gp = new GradientPaint(0, shiftY, accentColor, 0, shiftY + h, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
        g2.setPaint(gp);
        g2.fillRoundRect(0, shiftY, 5, h - 1, 6, 6);
        // Clean up the inner rounded side of the bar
        g2.fillRect(3, shiftY, 2, h - 1);

        // Draw gold glow border on hover
        if (hoverProgress > 0) {
            Color glowColor = new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), (int) (hoverProgress * 80));
            g2.setColor(glowColor);
            g2.drawRoundRect(0, shiftY, w - 1, h - 1, arc, arc);
        } else {
            g2.setColor(Theme.border());
            g2.drawRoundRect(0, shiftY, w - 1, h - 1, arc, arc);
        }

        g2.dispose();
    }

    public void applyTheme() {
        titleLabel.setForeground(Theme.textSecondary());
        valueLabel.setForeground(Theme.textPrimary());
        repaint();
    }
}
