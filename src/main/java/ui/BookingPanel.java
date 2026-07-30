package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StatusBadge;
import components.StyledButton;
import components.TableCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Booking;
import model.BookingStatus;
import reports.InvoicePrinter;
import reports.ReceiptPrinter;
import service.BookingService;
import ui.dialog.BookingFormDialog;
import ui.dialog.CheckoutDialog;
import ui.dialog.PaymentDialog;
import utils.CurrencyUtil;
import utils.DateUtil;

public class BookingPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final BookingService bookingService = new BookingService();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Guest", "Room", "Check-in", "Check-out", "Nights", "Amount", "Booking", "Payment"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay overlay;
    private TableCard tableCard;
    private List<Booking> bookings = List.of();
    private PageHeader pageHeader;

    // Search and hovers
    private SearchField searchField;
    private int hoveredRow = -1;

    public BookingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Front Desk Bookings", "Reservations, arrivals, departures, and billing");

        PremiumAddButton newBtn = new PremiumAddButton("New Booking");
        pageHeader.addAction(newBtn);

        // Toolbar: Split layout
        final JPanel toolbar = new JPanel(new BorderLayout(UiLayout.SPACE_MD, 0));
        toolbar.setOpaque(false);

        // Left Actions: CRUD and workflows
        final JPanel leftActions = new JPanel(new GridBagLayout());
        leftActions.setOpaque(false);
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridy = 0;
        lGbc.fill = GridBagConstraints.VERTICAL;
        lGbc.insets = new java.awt.Insets(0, 0, 0, UiLayout.SPACE_SM);

        PremiumActionButton checkInBtn = new PremiumActionButton("Check-in", PremiumActionButton.Style.SECONDARY, "checkin");
        PremiumActionButton checkOutBtn = new PremiumActionButton("Check-out", PremiumActionButton.Style.SECONDARY, "checkout");
        PremiumActionButton cancelBtn = new PremiumActionButton("Cancel", PremiumActionButton.Style.DANGER, "cancel");
        PremiumActionButton payBtn = new PremiumActionButton("Record Payment", PremiumActionButton.Style.SECONDARY, "pay");
        PremiumActionButton receiptBtn = new PremiumActionButton("Receipt", PremiumActionButton.Style.GHOST, "receipt");
        PremiumActionButton invoiceBtn = new PremiumActionButton("Invoice", PremiumActionButton.Style.GHOST, "invoice");

        leftActions.add(checkInBtn, lGbc);
        leftActions.add(checkOutBtn, lGbc);
        leftActions.add(cancelBtn, lGbc);
        leftActions.add(payBtn, lGbc);
        leftActions.add(receiptBtn, lGbc);
        lGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        leftActions.add(invoiceBtn, lGbc);

        // Right Actions: Search field
        final JPanel rightActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        searchField = new SearchField();
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                table.filter(searchField.getText());
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });
        rightActions.add(searchField);

        toolbar.add(leftActions, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);

        table = new ModernTable(tableModel);
        table.setRowHeight(44);

        // Hover listeners
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        // Table Header rounded renderer
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(Theme.fontBold(12));
                label.setForeground(Theme.GOLD);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

                JPanel cell = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(Theme.DARK_NAVY);
                        int w = getWidth();
                        int h = getHeight();

                        if (column == 0) {
                            g2.fillRoundRect(0, 2, w + 12, h - 4, 12, 12);
                            g2.fillRect(w - 12, 2, 12, h - 4);
                        } else if (column == t.getColumnCount() - 1) {
                            g2.fillRoundRect(-12, 2, w + 12, h - 4, 12, 12);
                            g2.fillRect(0, 2, 12, h - 4);
                        } else {
                            g2.fillRect(0, 2, w, h - 4);
                        }

                        g2.setColor(Theme.border());
                        g2.drawLine(0, h - 1, w, h - 1);

                        g2.dispose();
                    }
                };
                cell.setOpaque(false);
                cell.add(label, BorderLayout.CENTER);
                return cell;
            }
        });

        // Table Cell Renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            private final StatusBadge badge = new StatusBadge("");
            private final JLabel label = new JLabel();
            {
                label.setOpaque(true);
                label.setFont(Theme.fontRegular(13));
                label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                String valStr = value == null ? "" : value.toString();

                // Columns 6 and 7 are status badges
                if (column == 6 || column == 7) {
                    badge.setText(valStr);
                    JPanel p = new JPanel(new GridBagLayout());
                    p.setOpaque(true);

                    if (isSelected) {
                        p.setBackground(Theme.ROYAL_BLUE);
                    } else if (row == hoveredRow) {
                        p.setBackground(Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9));
                    } else {
                        p.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                    }

                    p.add(badge);
                    return p;
                }

                label.setText(valStr);

                if (isSelected) {
                    label.setBackground(Theme.ROYAL_BLUE);
                    label.setForeground(Color.WHITE);
                } else if (row == hoveredRow) {
                    label.setBackground(Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9));
                    label.setForeground(Theme.textPrimary());
                } else {
                    label.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                    label.setForeground(Theme.textPrimary());
                }

                return label;
            }
        });

        EmptyStatePanel empty = new EmptyStatePanel("No bookings yet",
                "Create a booking once rooms and guests are in the system.");
        empty.setIconKey("bookings");
        empty.setAction("New Booking", () -> new BookingFormDialog(mainFrame, this::afterMutation).setVisible(true));
        overlay = new TableEmptyOverlay(UiLayout.tableScroll(table), empty);
        tableCard = new TableCard(overlay);

        JPanel north = new JPanel(new BorderLayout(0, UiLayout.SPACE_SM));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        newBtn.addActionListener(e ->
                new BookingFormDialog(mainFrame, this::afterMutation).setVisible(true));
        checkInBtn.addActionListener(e -> checkInSelected());
        checkOutBtn.addActionListener(e -> checkOutSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        payBtn.addActionListener(e -> paySelected());
        receiptBtn.addActionListener(e -> {
            Booking booking = selectedBooking();
            if (booking == null) {
                Toast.error(mainFrame, "Select a booking");
                return;
            }
            ReceiptPrinter.printBookingReceipt(mainFrame, booking);
        });
        invoiceBtn.addActionListener(e -> {
            Booking booking = selectedBooking();
            if (booking == null) {
                Toast.error(mainFrame, "Select a booking");
                return;
            }
            InvoicePrinter.printInvoice(mainFrame, booking);
        });

        // Add responsiveness handler
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                int w = getWidth();
                if (w < 1040) {
                    if (toolbar.getLayout() instanceof BorderLayout) {
                        toolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, UiLayout.SPACE_SM));
                        toolbar.add(leftActions);
                        toolbar.add(rightActions);
                        toolbar.revalidate();
                    }
                } else {
                    if (!(toolbar.getLayout() instanceof BorderLayout)) {
                        toolbar.setLayout(new BorderLayout(UiLayout.SPACE_MD, 0));
                        toolbar.add(leftActions, BorderLayout.WEST);
                        toolbar.add(rightActions, BorderLayout.EAST);
                        toolbar.revalidate();
                    }
                }
            }
        });
    }

    private void afterMutation() {
        mainFrame.notifyDataChanged(AppEvents.Domain.BOOKINGS);
    }

    private Booking selectedBooking() {
        int row = table.getSelectedModelRow();
        if (row < 0 || row >= bookings.size()) {
            return null;
        }
        return bookings.get(row);
    }

    private void checkInSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            Toast.error(mainFrame, "Only confirmed bookings can be checked in");
            return;
        }
        if (!ConfirmDialog.confirm(mainFrame, "Confirm Check-in",
                "Check in " + booking.getCustomerName() + " to room " + booking.getRoomNumber() + "?",
                "Check-in", false)) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingService.checkIn(booking.getBookingId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Guest checked in");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void checkOutSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            Toast.error(mainFrame, "Only checked-in bookings can be checked out");
            return;
        }
        new CheckoutDialog(mainFrame, booking, this::afterMutation).setVisible(true);
    }

    private void cancelSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (!ConfirmDialog.confirm(mainFrame, "Cancel Reservation",
                "Cancel reservation for " + booking.getCustomerName() + "? The room will become available.",
                "Cancel Booking", true)) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingService.cancel(booking.getBookingId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Booking cancelled — room freed");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void paySelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        new PaymentDialog(mainFrame, booking, () -> {
            mainFrame.notifyDataChanged(AppEvents.Domain.PAYMENTS);
        }).setVisible(true);
    }

    @Override
    public void refresh() {
        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.list();
            }

            @Override
            protected void done() {
                try {
                    bookings = get();
                    tableModel.setRowCount(0);
                    for (Booking b : bookings) {
                        tableModel.addRow(new Object[]{
                                b.getCustomerName(),
                                b.getRoomNumber(),
                                DateUtil.format(b.getCheckIn()),
                                DateUtil.format(b.getCheckOut()),
                                b.getDays(),
                                CurrencyUtil.format(b.getTotalAmount()),
                                b.getBookingStatus().getLabel(),
                                b.getPaymentStatus().getLabel()
                        });
                    }
                    overlay.updateVisibility();
                    pageHeader.setSubtitle(bookings.size() + " bookings in the system");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load bookings");
                }
            }
        }.execute();
    }

    @Override
    public void applySearch(String query) {
        if (!searchField.getText().equals(query)) {
            searchField.setText(query);
        }
        table.filter(query);
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        table.applyTheme();
        overlay.applyTheme();
        tableCard.applyTheme();
        searchField.applyTheme();
        repaint();
    }

    // Custom Component: PremiumAddButton (subclassing StyledButton for type compatibility)
    private static class PremiumAddButton extends StyledButton {
        private boolean hover = false;

        public PremiumAddButton(String text) {
            super(text, Style.PRIMARY);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.fontBold(13));
            setForeground(Color.WHITE);

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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(140, 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = hover ? new Color(0x25, 0x63, 0xEB) : Theme.ROYAL_BLUE;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawLine(18, 21, 26, 21);
            g2.drawLine(22, 17, 22, 25);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), 36, y);

            g2.dispose();
        }
    }

    // Custom Component: PremiumActionButton
    private static class PremiumActionButton extends JButton {
        public enum Style { SECONDARY, DANGER, GHOST }
        private final Style style;
        private final String iconType;
        private boolean hover = false;

        public PremiumActionButton(String text, Style style, String iconType) {
            super(text);
            this.style = style;
            this.iconType = iconType;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.fontMedium(12));

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

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());
            int width = metrics.stringWidth(getText() == null ? "" : getText()) + 40;
            return new Dimension(width, 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            Color text;
            Color border = null;

            switch (style) {
                case DANGER -> {
                    fill = hover ? new Color(0xDC, 0x26, 0x26) : new Color(0xEF, 0x44, 0x44);
                    text = Color.WHITE;
                }
                case GHOST -> {
                    fill = hover ? (Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9)) : new Color(0, 0, 0, 0);
                    text = Theme.ROYAL_BLUE;
                }
                default -> { // SECONDARY
                    fill = hover ? (Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xE5, 0xE7, 0xEB)) : Theme.bgCard();
                    text = Theme.textPrimary();
                    border = Theme.border();
                }
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }

            g2.setColor(text);
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            drawIcon(g2, 12, (getHeight() - 12) / 2);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), 32, y);

            g2.dispose();
        }

        private void drawIcon(Graphics2D g2, int x, int y) {
            switch (iconType) {
                case "checkin" -> {
                    g2.drawOval(x, y, 11, 11);
                    g2.drawLine(x + 5, y + 2, x + 5, y + 8);
                    g2.drawLine(x + 2, y + 5, x + 8, y + 5);
                }
                case "checkout" -> {
                    g2.drawRect(x, y + 2, 8, 8);
                    g2.drawLine(x + 4, y, x + 12, y + 5);
                    g2.drawLine(x + 12, y + 5, x + 4, y + 10);
                }
                case "cancel" -> {
                    g2.drawLine(x + 2, y + 2, x + 10, y + 10);
                    g2.drawLine(x + 10, y + 2, x + 2, y + 10);
                }
                case "pay" -> {
                    g2.drawRoundRect(x, y + 2, 12, 8, 2, 2);
                    g2.drawLine(x, y + 5, x + 12, y + 5);
                }
                case "receipt" -> {
                    g2.drawRect(x + 2, y, 8, 11);
                    g2.drawLine(x + 4, y + 3, x + 8, y + 3);
                    g2.drawLine(x + 4, y + 6, x + 8, y + 6);
                }
                case "invoice" -> {
                    g2.drawRect(x + 2, y, 8, 11);
                    g2.drawLine(x + 4, y + 3, x + 8, y + 3);
                    g2.drawLine(x + 4, y + 6, x + 8, y + 6);
                    g2.drawLine(x + 4, y + 9, x + 7, y + 9);
                }
            }
        }
    }

    // Custom Component: SearchField
    private static class SearchField extends JTextField {
        private boolean hover = false;
        private final String placeholder = "Search bookings...";

        public SearchField() {
            setOpaque(false);
            setFont(Theme.fontRegular(13));
            setForeground(Theme.textPrimary());
            setCaretColor(Theme.ROYAL_BLUE);
            setBorder(BorderFactory.createEmptyBorder(8, 36, 8, 12));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(280, 36);
        }

        public void applyTheme() {
            setForeground(Theme.textPrimary());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.inputBg());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            if (isFocusOwner()) {
                g2.setColor(Theme.ROYAL_BLUE);
                g2.setStroke(new java.awt.BasicStroke(1.8f));
            } else if (hover) {
                g2.setColor(Theme.textSecondary());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
            } else {
                g2.setColor(Theme.border());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(isFocusOwner() ? Theme.ROYAL_BLUE : Theme.textSecondary());
            g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawOval(12, 11, 7, 7);
            g2.drawLine(18, 17, 22, 21);

            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D gPlaceholder = (Graphics2D) g.create();
                gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gPlaceholder.setColor(Theme.textMuted());
                gPlaceholder.setFont(getFont());
                FontMetrics fm = gPlaceholder.getFontMetrics();
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                gPlaceholder.drawString(placeholder, 36, y);
                gPlaceholder.dispose();
            }
        }
    }
}
