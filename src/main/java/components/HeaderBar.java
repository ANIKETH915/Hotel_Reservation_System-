package components;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import service.SettingsService;
import utils.DateUtil;
import utils.SessionManager;
import utils.UiExec;
import ui.MainFrame;

public class HeaderBar extends JPanel {

    private final JLabel hotelLabel;
    private final JLabel sectionLabel;
    private final JLabel dateLabel;
    private final JLabel clockLabel;
    private final JLabel profileNameLabel;
    private final ModernTextField searchField;
    private final JPanel avatarCircle;
    
    private final Timer clockTimer;
    private final UiExec.Coalescer searchCoalescer = new UiExec.Coalescer(120);
    private Consumer<String> onSearch;
    private MainFrame mainFrame;

    private final JPanel refreshBtn;
    private final JPanel themeBtn;
    private final JPanel bellBtn;
    private NotificationPopup notificationPopup;

    private int unreadNotificationsCount = 3; // Initial mock count

    public HeaderBar() {
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.bgHeader());
        setPreferredSize(new Dimension(0, 80));
        setOpaque(false); // Enable glassmorphism painting in paintComponent

        // Left Area: Logo + Hotel Name + Current Section
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(12, 10, 12, 0));

        // 1. Hotel Logo (Crest)
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gold shield shape
                g2.setColor(Theme.GOLD);
                int[] xPoints = {18, 30, 30, 18, 6, 6};
                int[] yPoints = {4, 10, 26, 32, 26, 10};
                g2.fillPolygon(xPoints, yPoints, 6);
                
                // Highlight inside
                g2.setColor(Theme.DARK_NAVY);
                int[] xPoints2 = {18, 26, 26, 18, 10, 10};
                int[] yPoints2 = {8, 13, 23, 28, 23, 13};
                g2.fillPolygon(xPoints2, yPoints2, 6);

                // Small gold crown symbol in center
                g2.setColor(Theme.GOLD);
                g2.fillOval(15, 15, 6, 6);
                
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };
        logoPanel.setOpaque(false);
        leftPanel.add(logoPanel);

        // Name and Section text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        hotelLabel = new JLabel("Grand Azure");
        hotelLabel.setFont(Theme.fontBold(15));
        hotelLabel.setForeground(Theme.textPrimary());

        sectionLabel = new JLabel("Dashboard Overview");
        sectionLabel.setFont(Theme.fontMedium(11));
        sectionLabel.setForeground(Theme.GOLD);

        textPanel.add(hotelLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(sectionLabel);
        leftPanel.add(textPanel);

        // Center Area: Styled Search Box
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        centerPanel.setOpaque(false);

        searchField = new ModernTextField(28) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.textMuted());
                    g2.setFont(Theme.fontRegular(12));
                    g2.drawString("Search Guests / Booking / Room...", 14, 24);
                    g2.dispose();
                }
            }
        };
        searchField.setPreferredSize(new Dimension(340, 38));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                if (onSearch != null) {
                    String text = searchField.getText();
                    searchCoalescer.request(() -> onSearch.accept(text));
                }
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });
        centerPanel.add(searchField);

        // Right Area: Action Buttons, Clock, User Profile
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(14, 0, 14, 16));

        // Date and Time Widget
        JPanel clockWidget = new JPanel();
        clockWidget.setLayout(new BoxLayout(clockWidget, BoxLayout.Y_AXIS));
        clockWidget.setOpaque(false);
        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(Theme.fontBold(13));
        clockLabel.setForeground(Theme.textPrimary());
        dateLabel = new JLabel("Jul 29, 2026");
        dateLabel.setFont(Theme.fontRegular(10));
        dateLabel.setForeground(Theme.textSecondary());
        clockWidget.add(clockLabel);
        clockWidget.add(dateLabel);
        rightPanel.add(clockWidget);

        // Separator
        rightPanel.add(createDivider());

        // Refresh Button
        refreshBtn = createIconButton("refresh");
        refreshBtn.setToolTipText("Refresh Dashboard");
        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                triggerActivePanelRefresh();
            }
        });
        rightPanel.add(refreshBtn);

        // Theme Toggle
        themeBtn = createIconButton("theme");
        themeBtn.setToolTipText("Toggle Theme Style");
        themeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ThemeManager.toggle();
            }
        });
        rightPanel.add(themeBtn);

        // Notification Bell
        bellBtn = new JPanel() {
            private boolean hover = false;
            {
                setPreferredSize(new Dimension(36, 36));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        unreadNotificationsCount = 0; // Clear badge on click
                        repaint();
                        if (notificationPopup == null && mainFrame != null) {
                            notificationPopup = new NotificationPopup(mainFrame);
                        }
                        if (notificationPopup != null) {
                            notificationPopup.showPopup(bellBtn, 0, getHeight());
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle background on hover
                if (hover) {
                    g2.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), 35));
                    g2.fillOval(0, 0, 36, 36);
                }

                // Draw Bell Icon outline
                g2.setColor(hover ? Theme.GOLD : Theme.textPrimary());
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                g2.drawRoundRect(12, 10, 12, 11, 4, 4);
                g2.drawLine(9, 21, 27, 21);
                g2.fillArc(15, 21, 6, 4, 180, 180);

                // Draw notification badge (red dot)
                if (unreadNotificationsCount > 0) {
                    g2.setColor(Theme.DANGER);
                    g2.fillOval(23, 7, 7, 7);
                }
                
                g2.dispose();
            }
        };
        bellBtn.setToolTipText("Operations Alerts");
        rightPanel.add(bellBtn);

        // Separator
        rightPanel.add(createDivider());

        // User Profile Avatar and Name
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profilePanel.setOpaque(false);
        profilePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        profileNameLabel = new JLabel("Admin User");
        profileNameLabel.setFont(Theme.fontMedium(12));
        profileNameLabel.setForeground(Theme.textPrimary());

        avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle avatar with gold border
                g2.setColor(Theme.GOLD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.bgCard());
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                
                // Draw initial inside circle
                g2.setColor(Theme.textPrimary());
                g2.setFont(Theme.fontBold(13));
                String name = profileNameLabel.getText();
                String initial = name.isEmpty() ? "A" : String.valueOf(name.charAt(0));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, 22);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };
        avatarCircle.setOpaque(false);

        profilePanel.add(profileNameLabel);
        profilePanel.add(avatarCircle);
        rightPanel.add(profilePanel);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        refreshProfile();
        loadHotelName();
        updateClock();
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private void triggerActivePanelRefresh() {
        if (mainFrame != null) {
            mainFrame.navigate(mainFrame.getTitle().contains("Rooms") ? "Rooms" : "Dashboard");
            // Also refresh other panel if needed, but navigate will refresh dashboard panel automatically
            mainFrame.repaint();
        }
    }

    private JPanel createIconButton(String type) {
        return new JPanel() {
            private boolean hover = false;
            {
                setPreferredSize(new Dimension(36, 36));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (hover) {
                    g2.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), 35));
                    g2.fillOval(0, 0, 36, 36);
                }

                Color c = hover ? Theme.GOLD : Theme.textPrimary();
                g2.setColor(c);
                g2.setStroke(new java.awt.BasicStroke(1.8f));

                if ("refresh".equals(type)) {
                    // Circular refresh arrow
                    g2.drawArc(10, 10, 16, 16, 45, 270);
                    // Arrow cap
                    g2.drawLine(20, 9, 23, 12);
                    g2.drawLine(23, 12, 19, 15);
                } else if ("theme".equals(type)) {
                    // Sun / Moon drawing depending on current theme
                    if (Theme.isDark()) {
                        // Sun icon
                        g2.drawOval(13, 13, 10, 10);
                        for (int i = 0; i < 8; i++) {
                            double angle = i * Math.PI / 4;
                            int x1 = (int) (18 + Math.cos(angle) * 7);
                            int y1 = (int) (18 + Math.sin(angle) * 7);
                            int x2 = (int) (18 + Math.cos(angle) * 10);
                            int y2 = (int) (18 + Math.sin(angle) * 10);
                            g2.drawLine(x1, y1, x2, y2);
                        }
                    } else {
                        // Moon icon
                        g2.drawArc(12, 12, 12, 12, -70, 240);
                        g2.drawArc(15, 12, 9, 12, -70, 240);
                    }
                }
                
                g2.dispose();
            }
        };
    }

    private JPanel createDivider() {
        JPanel div = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.border());
                g.drawLine(1, 4, 1, getHeight() - 4);
            }
        };
        div.setPreferredSize(new Dimension(3, 26));
        div.setOpaque(false);
        return div;
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    public void setSectionTitle(String section) {
        sectionLabel.setText(section == null ? " " : section + " OVERVIEW");
        searchField.setToolTipText("Search " + (section == null ? "this section" : section.toLowerCase()));
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public void refreshProfile() {
        var admin = SessionManager.getCurrentAdmin();
        if (admin != null) {
            profileNameLabel.setText(admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
        } else {
            profileNameLabel.setText("System Administrator");
        }
        if (avatarCircle != null) {
            avatarCircle.repaint();
        }
    }

    public void loadHotelName() {
        try {
            applyHotelName(new SettingsService().getHotelName());
        } catch (Exception e) {
            applyHotelName("Grand Azure");
        }
    }

    public void applyHotelName(String name) {
        String displayName = name == null || name.isBlank() ? "Grand Azure" : name.trim();
        hotelLabel.setToolTipText(displayName);
        hotelLabel.setText(ellipsize(displayName, 24));
    }

    public void applyTheme() {
        setBackground(Theme.bgHeader());
        hotelLabel.setForeground(Theme.textPrimary());
        sectionLabel.setForeground(Theme.GOLD);
        dateLabel.setForeground(Theme.textSecondary());
        clockLabel.setForeground(Theme.textPrimary());
        profileNameLabel.setForeground(Theme.textPrimary());
        searchField.applyTheme();
        if (refreshBtn != null) refreshBtn.repaint();
        if (themeBtn != null) themeBtn.repaint();
        if (bellBtn != null) bellBtn.repaint();
        if (avatarCircle != null) avatarCircle.repaint();
        repaint();
    }

    public void stopClock() {
        clockTimer.stop();
    }

    private void updateClock() {
        dateLabel.setText(DateUtil.formatHeaderDate(LocalDate.now()));
        clockLabel.setText(DateUtil.formatTime(LocalTime.now()));
    }

    private String ellipsize(String text, int maxCharacters) {
        return text.length() <= maxCharacters ? text : text.substring(0, maxCharacters - 1) + "…";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Glassmorphic styling: semi-transparent solid background
        g2.setColor(Theme.bgHeader());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Shadow bottom line
        g2.setColor(Theme.border());
        g2.setStroke(new java.awt.BasicStroke(1.2f));
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

        g2.dispose();
    }
}
