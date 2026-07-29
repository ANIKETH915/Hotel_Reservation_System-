package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import service.SettingsService;

public class Sidebar extends JPanel {

    private static final String[][] NAV_ITEMS = {
            {"Dashboard", "dashboard"},
            {"Rooms", "rooms"},
            {"Customers", "customers"},
            {"Bookings", "bookings"},
            {"Payments", "payments"},
            {"Reports", "reports"},
            {"Settings", "settings"},
            {"About", "about"}
    };

    private final Consumer<String> onNavigate;
    private final Map<String, NavItemPanel> itemPanels = new LinkedHashMap<>();
    private final JLabel nameLabel = new JLabel("Grand Azure");
    private final JLabel subLabel = new JLabel("Property Management");
    private String activeKey = "Dashboard";

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(Theme.bgSidebar());
        setPreferredSize(new Dimension(240, 0));
        setBorder(new EmptyBorder(0, 0, 16, 0));

        add(createHeader(), BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(8, 0, 0, 0));

        for (String[] item : NAV_ITEMS) {
            NavItemPanel panel = new NavItemPanel(item[0], item[1]);
            panel.setToolTipText(item[0] + " (Ctrl+" + shortcutIndex(item[0]) + ")");
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setActive(item[0]);
                    onNavigate.accept(item[0]);
                }
            });
            itemPanels.put(item[0], panel);
            nav.add(panel);
            nav.add(Box.createVerticalStrut(2));
        }

        nav.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(8));

        NavItemPanel logout = new NavItemPanel("Logout", "logout");
        logout.setToolTipText("Logout (Ctrl+L)");
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onNavigate.accept("Logout");
            }
        });
        itemPanels.put("Logout", logout);
        nav.add(logout);

        add(nav, BorderLayout.CENTER);
        setActive("Dashboard");
        refreshBranding();
    }

    private int shortcutIndex(String key) {
        return switch (key) {
            case "Dashboard" -> 1;
            case "Rooms" -> 2;
            case "Customers" -> 3;
            case "Bookings" -> 4;
            case "Payments" -> 5;
            case "Reports" -> 6;
            case "Settings" -> 7;
            case "About" -> 8;
            default -> 0;
        };
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 20, 24, 16));

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.GOLD);
                g2.fillOval(0, 0, 44, 44);
                g2.setColor(Theme.DARK_NAVY);
                NavIcons.paint(g2, "rooms", 10, 10, 24, Theme.DARK_NAVY);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(44, 44);
            }
        };
        logoPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        nameLabel.setFont(Theme.fontBold(14));
        nameLabel.setForeground(Color.WHITE);
        subLabel.setFont(Theme.fontRegular(11));
        subLabel.setForeground(new Color(0x94, 0xA3, 0xB8));

        textPanel.add(nameLabel);
        textPanel.add(subLabel);

        header.add(logoPanel, BorderLayout.WEST);
        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    public void refreshBranding() {
        try {
            refreshBranding(new SettingsService().getHotelName());
        } catch (Exception e) {
            refreshBranding(null);
        }
    }

    public void refreshBranding(String hotel) {
        if (hotel != null && hotel.contains("&")) {
            nameLabel.setText(hotel.substring(0, hotel.indexOf('&')).trim());
            subLabel.setText(hotel.substring(hotel.indexOf('&') + 1).trim());
        } else if (hotel != null && hotel.length() > 18) {
            nameLabel.setText(hotel.substring(0, 18) + "…");
            subLabel.setText("Property Management");
        } else {
            nameLabel.setText(hotel == null || hotel.isBlank() ? "Hotel PMS" : hotel);
            subLabel.setText("Property Management");
        }
        repaint();
    }

    public void setActive(String key) {
        if ("Logout".equals(key)) {
            return;
        }
        activeKey = key;
        for (Map.Entry<String, NavItemPanel> entry : itemPanels.entrySet()) {
            if (!"Logout".equals(entry.getKey())) {
                entry.getValue().setActive(entry.getKey().equals(key));
            }
        }
        repaint();
    }

    public String getActiveKey() {
        return activeKey;
    }

    private static class NavItemPanel extends JPanel {
        private final String label;
        private final String iconKey;
        private boolean active;
        private boolean hover;

        private double activeProgress = 0.0;
        private double hoverProgress = 0.0;
        private final Timer transitionTimer;

        NavItemPanel(String label, String iconKey) {
            this.label = label;
            this.iconKey = iconKey;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(240, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setBorder(new EmptyBorder(0, 12, 0, 12));

            transitionTimer = new Timer(15, e -> {
                boolean changed = false;
                if (active) {
                    if (activeProgress < 1.0) {
                        activeProgress = Math.min(1.0, activeProgress + 0.15);
                        changed = true;
                    }
                } else {
                    if (activeProgress > 0.0) {
                        activeProgress = Math.max(0.0, activeProgress - 0.15);
                        changed = true;
                    }
                }

                if (hover) {
                    if (hoverProgress < 1.0) {
                        hoverProgress = Math.min(1.0, hoverProgress + 0.15);
                        changed = true;
                    }
                } else {
                    if (hoverProgress > 0.0) {
                        hoverProgress = Math.max(0.0, hoverProgress - 0.15);
                        changed = true;
                    }
                }

                if (changed) {
                    repaint();
                } else {
                    ((javax.swing.Timer)e.getSource()).stop();
                }
            });

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    if (!transitionTimer.isRunning()) transitionTimer.start();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    if (!transitionTimer.isRunning()) transitionTimer.start();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            if (!transitionTimer.isRunning()) transitionTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Draw glowing gradient background wash
            if (activeProgress > 0) {
                // Navy blue background wash
                Color wash = new Color(Theme.ROYAL_BLUE.getRed(), Theme.ROYAL_BLUE.getGreen(), Theme.ROYAL_BLUE.getBlue(), (int) (55 * activeProgress));
                g2.setColor(wash);
                g2.fillRoundRect(8, 4, w - 16, h - 8, 10, 10);
                
                // Gold outer glow border
                Color glow = new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), (int) (60 * activeProgress));
                g2.setColor(glow);
                g2.drawRoundRect(8, 4, w - 16, h - 8, 10, 10);

                // Gold vertical indicator bar (animates height from 0 to full)
                g2.setColor(Theme.GOLD);
                int barHeight = (int) Math.round((h - 20) * activeProgress);
                g2.fillRoundRect(3, 10 + (h - 20 - barHeight) / 2, 4, barHeight, 2, 2);
            } else if (hoverProgress > 0) {
                // Hover transparent grey background wash
                Color hoverWash = new Color(255, 255, 255, (int) (16 * hoverProgress));
                g2.setColor(hoverWash);
                g2.fillRoundRect(8, 4, w - 16, h - 8, 10, 10);
            }

            // 2. Icon color transition (grey -> gold)
            Color iconColor;
            if (activeProgress > 0) {
                // Mix gray and gold based on progress
                int r = (int) ((0x94 * (1.0 - activeProgress)) + (Theme.GOLD.getRed() * activeProgress));
                int gr = (int) ((0xA3 * (1.0 - activeProgress)) + (Theme.GOLD.getGreen() * activeProgress));
                int b = (int) ((0xB8 * (1.0 - activeProgress)) + (Theme.GOLD.getBlue() * activeProgress));
                iconColor = new Color(r, gr, b);
            } else {
                iconColor = new Color(0x94, 0xA3, 0xB8);
            }

            // Paint Nav icon
            NavIcons.paint(g2, iconKey, 22, 13, 20, iconColor);

            // 3. Text color transition (grey -> white)
            Color textColor;
            if (activeProgress > 0) {
                int r = (int) ((0xCB * (1.0 - activeProgress)) + (255 * activeProgress));
                int gr = (int) ((0xD5 * (1.0 - activeProgress)) + (255 * activeProgress));
                int b = (int) ((0xE1 * (1.0 - activeProgress)) + (255 * activeProgress));
                textColor = new Color(r, gr, b);
                g2.setFont(Theme.fontMedium(13));
            } else {
                textColor = new Color(0xCB, 0xD5, 0xE1);
                g2.setFont(Theme.fontRegular(13));
            }

            g2.setColor(textColor);
            g2.drawString(label, 52, 28);
            g2.dispose();
        }
    }
}
