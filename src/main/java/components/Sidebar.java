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

        NavItemPanel(String label, String iconKey) {
            this.label = label;
            this.iconKey = iconKey;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(240, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(new Color(Theme.ROYAL_BLUE.getRed(), Theme.ROYAL_BLUE.getGreen(),
                        Theme.ROYAL_BLUE.getBlue(), 55));
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                g2.setColor(Theme.GOLD);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 2, 2);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
            }

            Color iconColor = active ? Theme.GOLD : new Color(0x94, 0xA3, 0xB8);
            NavIcons.paint(g2, iconKey, 22, 13, 20, iconColor);

            g2.setColor(active ? Color.WHITE : new Color(0xCB, 0xD5, 0xE1));
            g2.setFont(active ? Theme.fontMedium(13) : Theme.fontRegular(13));
            g2.drawString(label, 52, 28);
            g2.dispose();
        }
    }
}
