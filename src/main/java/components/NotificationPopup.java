package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import model.Booking;
import model.Room;
import model.RoomStatus;
import service.BookingService;
import service.RoomService;
import ui.MainFrame;

public class NotificationPopup extends JPopupMenu {

    private final MainFrame mainFrame;
    private final BookingService bookingService = new BookingService();
    private final RoomService roomService = new RoomService();

    public NotificationPopup(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        
        setBackground(Theme.bgCard());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        buildAlertsList();
    }

    private void buildAlertsList() {
        removeAll();

        // Title Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 8, 8, 8));
        
        JLabel title = new JLabel("OPERATIONS ALERTS");
        title.setFont(Theme.fontBold(11));
        title.setForeground(Theme.GOLD);
        header.add(title, BorderLayout.WEST);
        
        add(header);
        add(Box.createVerticalStrut(4));
        
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.border());
        add(sep);
        add(Box.createVerticalStrut(6));

        // Dynamically fetch operational alerts
        List<AlertItem> alerts = new ArrayList<>();
        try {
            // 1. Check-ins pending today
            List<Booking> checkIns = bookingService.listTodayCheckIns();
            if (!checkIns.isEmpty()) {
                alerts.add(new AlertItem("arrivals", checkIns.size() + " Arrivals scheduled today", "Bookings", Theme.EMERALD));
            }

            // 2. Check-outs pending today
            List<Booking> checkOuts = bookingService.listTodayCheckOuts();
            if (!checkOuts.isEmpty()) {
                alerts.add(new AlertItem("logout", checkOuts.size() + " Departures scheduled today", "Bookings", Theme.ROYAL_BLUE));
            }

            // 3. Rooms cleaning / maintenance
            List<Room> rooms = roomService.list();
            int cleaningCount = 0;
            int maintCount = 0;
            for (Room r : rooms) {
                if (r.getStatus() == RoomStatus.CLEANING) cleaningCount++;
                if (r.getStatus() == RoomStatus.MAINTENANCE) maintCount++;
            }
            if (cleaningCount > 0) {
                alerts.add(new AlertItem("rooms", cleaningCount + " Rooms require cleaning", "Rooms", Theme.GOLD));
            }
            if (maintCount > 0) {
                alerts.add(new AlertItem("settings", maintCount + " Rooms in maintenance", "Rooms", Theme.DANGER));
            }

            // 4. Pending unpaid invoices
            List<Booking> active = bookingService.listActiveToday();
            int unpaidCount = 0;
            for (Booking b : active) {
                if (b.getPaymentStatus() == model.PaymentStatus.PENDING) {
                    unpaidCount++;
                }
            }
            if (unpaidCount > 0) {
                alerts.add(new AlertItem("payments", unpaidCount + " Unpaid bills for active stays", "Payments", Theme.DANGER));
            }

        } catch (SQLException e) {
            alerts.add(new AlertItem("about", "Failed to load active system alerts", "Dashboard", Theme.textMuted()));
        }

        if (alerts.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
            emptyPanel.setOpaque(false);
            JLabel msg = new JLabel("All operations clear. No pending alerts.");
            msg.setFont(Theme.fontRegular(12));
            msg.setForeground(Theme.textSecondary());
            emptyPanel.add(msg);
            add(emptyPanel);
        } else {
            for (AlertItem alert : alerts) {
                add(alert);
                add(Box.createVerticalStrut(2));
            }
        }

        setPreferredSize(new Dimension(280, getPreferredSize().height));
        revalidate();
    }

    private class AlertItem extends JPanel {
        private final String section;
        private boolean hover = false;

        public AlertItem(String iconKey, String text, String targetSection, Color color) {
            this.section = targetSection;
            setOpaque(false);
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    NavIcons.paint(g2, iconKey, 4, 4, 12, color);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(20, 20);
                }
            };
            iconPanel.setOpaque(false);

            JLabel label = new JLabel("<html><body style='width: 190px;'>" + text + "</body></html>");
            label.setFont(Theme.fontRegular(12));
            label.setForeground(Theme.textPrimary());

            add(iconPanel, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    NotificationPopup.this.setVisible(false);
                    mainFrame.navigate(section);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hover) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), 25));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    public void showPopup(java.awt.Component invoker, int x, int y) {
        buildAlertsList();
        show(invoker, x, y);
    }
}
