package ui;

import components.CardPanel;
import components.PageHeader;
import components.Theme;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class AboutPanel extends JPanel implements MainFrame.RefreshablePanel {

    private PageHeader pageHeader;
    private JScrollPane pageScroll;

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("About", "Product information and technology stack");

        CardPanel card = new CardPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        card.setBorder(new EmptyBorder(UiLayout.SPACE_LG, UiLayout.SPACE_XL, UiLayout.SPACE_LG, UiLayout.SPACE_XL));
        card.setArc(16);
        card.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        // Top Logo + Title
        JPanel headerPanel = new JPanel(new BorderLayout(UiLayout.SPACE_MD, 0));
        headerPanel.setOpaque(false);

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Theme.GOLD);
                int[] xPoints = {24, 40, 40, 24, 8, 8};
                int[] yPoints = {4, 12, 34, 42, 34, 12};
                g2.fillPolygon(xPoints, yPoints, 6);

                g2.setColor(Theme.DARK_NAVY);
                int[] xPoints2 = {24, 35, 35, 24, 13, 13};
                int[] yPoints2 = {9, 15, 31, 37, 31, 15};
                g2.fillPolygon(xPoints2, yPoints2, 6);

                g2.setColor(Theme.GOLD);
                g2.fillOval(20, 20, 8, 8);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(48, 48);
            }
        };
        logoPanel.setOpaque(false);

        JPanel titleTextPanel = new JPanel();
        titleTextPanel.setLayout(new BoxLayout(titleTextPanel, BoxLayout.Y_AXIS));
        titleTextPanel.setOpaque(false);

        JLabel appName = new JLabel("Grand Azure Property Management System");
        appName.setFont(Theme.fontBold(16));
        appName.setForeground(Theme.textPrimary());

        JLabel appVer = new JLabel("Version 1.0.0 (Enterprise Edition)");
        appVer.setFont(Theme.fontRegular(12));
        appVer.setForeground(Theme.textSecondary());

        titleTextPanel.add(appName);
        titleTextPanel.add(Box.createVerticalStrut(2));
        titleTextPanel.add(appVer);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(titleTextPanel, BorderLayout.CENTER);

        // Grid details
        final JPanel grid = new JPanel(new GridLayout(0, 2, UiLayout.SPACE_LG, UiLayout.SPACE_MD));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(UiLayout.SPACE_MD, 0, UiLayout.SPACE_MD, 0));

        addAboutDetail(grid, "System Objective", "Manage properties, inventory rates, customer registrations, database security and business reports.");
        addAboutDetail(grid, "Architecture Design", "Desktop Model-View-Controller (MVC) with direct JDBC access layers.");
        addAboutDetail(grid, "Primary Database", "SQLite (Local Embedded Database) running under WAL transactions.");
        addAboutDetail(grid, "UI Renderer Engine", "Pure Java Swing Custom Paint Components (100% Native).");
        addAboutDetail(grid, "License Protection", "Commercial Enterprise License (CodeAlpha Task 4).");
        addAboutDetail(grid, "Operations & Support", "support@grandazure.com");

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);

        // Footer copyright
        JLabel copyright = new JLabel("© 2026 Grand Azure Hotel & Suites");
        copyright.setFont(Theme.fontRegular(11));
        copyright.setForeground(Theme.textMuted());
        copyright.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(copyright, BorderLayout.SOUTH);

        UiLayout.ViewportWidthPanel content = new UiLayout.ViewportWidthPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, UiLayout.SPACE_MD, 0));

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.setAlignmentX(LEFT_ALIGNMENT);
        column.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        column.add(UiLayout.fullWidth(pageHeader));
        column.add(UiLayout.fullWidth(card));
        content.add(column);

        pageScroll = UiLayout.pageScroll(content);
        add(pageScroll, BorderLayout.CENTER);

        // Add responsiveness handler
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cols = getWidth() < 680 ? 1 : 2;
                GridLayout layout = (GridLayout) grid.getLayout();
                if (layout.getColumns() != cols) {
                    grid.setLayout(new GridLayout(0, cols, UiLayout.SPACE_LG, UiLayout.SPACE_MD));
                    grid.revalidate();
                }
            }
        });
    }

    private void addAboutDetail(JPanel panel, String label, String value) {
        JPanel detail = new JPanel(new BorderLayout(0, UiLayout.SPACE_XS));
        detail.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());

        JLabel val = new JLabel("<html><body style='width: 320px;'>" + value + "</body></html>");
        val.setFont(Theme.fontRegular(13));
        val.setForeground(Theme.textPrimary());

        detail.add(lbl, BorderLayout.NORTH);
        detail.add(val, BorderLayout.CENTER);
        panel.add(detail);
    }

    @Override
    public void refresh() {
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        if (pageScroll != null) {
            pageScroll.getViewport().setBackground(Theme.bgPrimary());
        }
        repaint();
    }
}
