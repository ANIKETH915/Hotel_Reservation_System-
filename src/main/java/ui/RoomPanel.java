package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.EmptyStatePanel;
import components.ModernTable;
import components.NavIcons;
import components.PageHeader;
import components.StatusBadge;
import components.StyledButton;
import components.StyledComboBox;
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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Room;
import model.RoomStatus;
import service.RoomService;
import ui.dialog.RoomDetailDialog;
import ui.dialog.RoomFormDialog;
import utils.CurrencyUtil;

public class RoomPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final RoomService roomService = new RoomService();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Number", "Type", "Floor", "Price", "Capacity", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay overlay;
    private TableCard tableCard;
    private java.util.List<Room> rooms = java.util.List.of();
    private PageHeader pageHeader;

    // Redesigned Stats Cards
    private StatCard totalCard;
    private StatCard availableCard;
    private StatCard reservedCard;
    private StatCard occupiedCard;
    private StatCard cleaningCard;
    private StatCard maintenanceCard;

    // Redesigned Search Field
    private SearchField searchField;

    // Row Hover State
    private int hoveredRow = -1;

    public RoomPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Room Inventory", "Manage rooms, rates, and housekeeping status");

        PremiumAddButton addBtn = new PremiumAddButton("Add Room");
        addBtn.setToolTipText("Add a new room to inventory");
        pageHeader.addAction(addBtn);

        // Stats Cards Grid Panel
        final JPanel statsPanel = new JPanel(new java.awt.GridLayout(0, 6, UiLayout.SPACE_SM, UiLayout.SPACE_SM));
        statsPanel.setOpaque(false);

        totalCard = new StatCard("Total Rooms", "total", Theme.textSecondary());
        availableCard = new StatCard("Available", "available", Theme.EMERALD);
        reservedCard = new StatCard("Reserved", "reserved", Theme.GOLD);
        occupiedCard = new StatCard("Occupied", "occupied", Theme.ROYAL_BLUE);
        cleaningCard = new StatCard("Cleaning", "cleaning", Theme.WARNING);
        maintenanceCard = new StatCard("Maintenance", "maintenance", Theme.DANGER);

        statsPanel.add(totalCard);
        statsPanel.add(availableCard);
        statsPanel.add(reservedCard);
        statsPanel.add(occupiedCard);
        statsPanel.add(cleaningCard);
        statsPanel.add(maintenanceCard);

        // Toolbar: Split layout
        final JPanel toolbar = new JPanel(new BorderLayout(UiLayout.SPACE_MD, 0));
        toolbar.setOpaque(false);

        // Left Side: Action Buttons
        final JPanel leftActions = new JPanel(new GridBagLayout());
        leftActions.setOpaque(false);
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridy = 0;
        lGbc.fill = GridBagConstraints.VERTICAL;
        lGbc.insets = new java.awt.Insets(0, 0, 0, UiLayout.SPACE_SM);

        PremiumActionButton editBtn = new PremiumActionButton("Edit", PremiumActionButton.Style.SECONDARY, "edit");
        PremiumActionButton viewBtn = new PremiumActionButton("View", PremiumActionButton.Style.GHOST, "view");
        PremiumActionButton deleteBtn = new PremiumActionButton("Delete", PremiumActionButton.Style.DANGER, "delete");

        StyledComboBox<RoomStatus> statusCombo = new StyledComboBox<>(new RoomStatus[]{
                RoomStatus.AVAILABLE, RoomStatus.CLEANING, RoomStatus.MAINTENANCE
        });
        statusCombo.setToolTipText("Housekeeping status only — Booked/Reserved are set by bookings");
        PremiumActionButton statusBtn = new PremiumActionButton("Set Status", PremiumActionButton.Style.SECONDARY, "status");

        leftActions.add(editBtn, lGbc);
        leftActions.add(viewBtn, lGbc);
        leftActions.add(deleteBtn, lGbc);
        leftActions.add(statusCombo, lGbc);
        lGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        leftActions.add(statusBtn, lGbc);

        // Right Side: Rounded Search Box
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

        // Table initialization and styling overrides
        table = new ModernTable(tableModel);
        table.setRowHeight(44);

        // Hover effect listeners
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

        // Rounded table header renderer
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

        // Alternate colors, row hovers, status badges renderer
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

                if (column == 5) {
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

        EmptyStatePanel empty = new EmptyStatePanel("No rooms configured",
                "Add your first room to start taking reservations.");
        empty.setIconKey("rooms");
        empty.setAction("Add Room", () -> new RoomFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        overlay = new TableEmptyOverlay(UiLayout.tableScroll(table), empty);
        tableCard = new TableCard(overlay);

        // Assembly
        JPanel north = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);

        JPanel centerNorth = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        centerNorth.setOpaque(false);
        centerNorth.add(statsPanel, BorderLayout.NORTH);
        centerNorth.add(toolbar, BorderLayout.SOUTH);

        north.add(centerNorth, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        addBtn.addActionListener(e -> new RoomFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.addActionListener(e -> viewSelected());
        statusBtn.addActionListener(e -> updateStatus((RoomStatus) statusCombo.getSelectedItem()));

        // Add responsiveness handler
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                int w = getWidth();
                int availableWidth = Math.max(0, w - 40);
                
                // Responsive stats columns: 6, 3, or 2 depending on width
                int cols = availableWidth < 640 ? 2 : availableWidth < 960 ? 3 : 6;
                GridLayout statsLayout = (GridLayout) statsPanel.getLayout();
                if (statsLayout.getColumns() != cols) {
                    statsPanel.setLayout(new GridLayout(0, cols, UiLayout.SPACE_SM, UiLayout.SPACE_SM));
                    statsPanel.revalidate();
                }

                // Responsive toolbar layout
                if (w < 920) {
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
        mainFrame.notifyDataChanged(AppEvents.Domain.ROOMS);
    }

    private Room selectedRoom() {
        int row = table.getSelectedModelRow();
        if (row < 0 || row >= rooms.size()) {
            return null;
        }
        return rooms.get(row);
    }

    private void editSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new RoomFormDialog(mainFrame, room, this::afterMutation).setVisible(true);
    }

    private void viewSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new RoomDetailDialog(mainFrame, room).setVisible(true);
    }

    private void deleteSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        if (!ConfirmDialog.confirmDelete(mainFrame,
                "Permanently delete room " + room.getRoomNumber() + "? This cannot be undone.")) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                roomService.delete(room.getRoomId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Room deleted");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateStatus(RoomStatus status) {
        Room room = selectedRoom();
        if (room == null || status == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                roomService.updateStatus(room.getRoomId(), status);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Status updated");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        new SwingWorker<java.util.List<Room>, Void>() {
            @Override
            protected java.util.List<Room> doInBackground() throws Exception {
                return roomService.list();
            }

            @Override
            protected void done() {
                try {
                    rooms = get();
                    tableModel.setRowCount(0);
                    
                    int total = rooms.size();
                    int available = 0;
                    int reserved = 0;
                    int occupied = 0;
                    int cleaning = 0;
                    int maintenance = 0;

                    for (Room r : rooms) {
                        tableModel.addRow(new Object[]{
                                r.getRoomNumber(),
                                r.getRoomType().getLabel(),
                                r.getFloor(),
                                CurrencyUtil.format(r.getPrice()),
                                r.getCapacity(),
                                r.getStatus().getLabel()
                        });

                        if (r.getStatus() != null) {
                            switch (r.getStatus()) {
                                case AVAILABLE -> available++;
                                case RESERVED -> reserved++;
                                case BOOKED -> occupied++;
                                case CLEANING -> cleaning++;
                                case MAINTENANCE -> maintenance++;
                            }
                        }
                    }

                    totalCard.setValue(total);
                    availableCard.setValue(available);
                    reservedCard.setValue(reserved);
                    occupiedCard.setValue(occupied);
                    cleaningCard.setValue(cleaning);
                    maintenanceCard.setValue(maintenance);

                    overlay.updateVisibility();
                    pageHeader.setSubtitle(rooms.size() + " rooms in inventory");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load rooms from database");
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
        totalCard.updateTheme();
        availableCard.updateTheme();
        reservedCard.updateTheme();
        occupiedCard.updateTheme();
        cleaningCard.updateTheme();
        maintenanceCard.updateTheme();
        searchField.applyTheme();
        repaint();
    }

    // Custom Component: StatCard
    private static class StatCard extends JPanel {
        private final JLabel valueLabel;
        private final JLabel titleLabel;
        private final String iconKey;
        private final Color accentColor;

        public StatCard(String title, String iconKey, Color accentColor) {
            this.iconKey = iconKey;
            this.accentColor = accentColor;

            setLayout(new BorderLayout(UiLayout.SPACE_MD, 0));
            setBackground(Theme.bgCard());
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
                    g2.fillOval(0, 0, 36, 36);

                    g2.setColor(accentColor);
                    g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    drawCustomIcon(g2, iconKey, accentColor);

                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(36, 36);
                }
            };
            iconPanel.setOpaque(false);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            titleLabel = new JLabel(title);
            titleLabel.setFont(Theme.fontMedium(11));
            titleLabel.setForeground(Theme.textSecondary());

            valueLabel = new JLabel("0");
            valueLabel.setFont(Theme.fontBold(20));
            valueLabel.setForeground(Theme.textPrimary());

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(valueLabel);

            add(iconPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        private void drawCustomIcon(Graphics2D g2, String iconKey, Color accentColor) {
            switch (iconKey) {
                case "total" -> {
                    g2.drawRect(8, 8, 8, 8);
                    g2.drawRect(18, 8, 8, 8);
                    g2.drawRect(8, 18, 8, 8);
                    g2.drawRect(18, 18, 8, 8);
                }
                case "available" -> {
                    g2.drawOval(8, 8, 20, 20);
                    g2.drawLine(13, 18, 16, 21);
                    g2.drawLine(16, 21, 22, 13);
                }
                case "reserved" -> {
                    g2.drawRoundRect(8, 10, 20, 18, 4, 4);
                    g2.drawLine(8, 16, 28, 16);
                    g2.drawLine(13, 7, 13, 11);
                    g2.drawLine(23, 7, 23, 11);
                }
                case "occupied" -> {
                    g2.drawLine(8, 12, 8, 24);
                    g2.drawLine(8, 20, 28, 20);
                    g2.drawLine(28, 16, 28, 24);
                    g2.drawRoundRect(10, 14, 6, 4, 2, 2);
                    g2.drawRoundRect(16, 17, 12, 4, 1, 1);
                }
                case "cleaning" -> {
                    g2.drawLine(10, 24, 22, 12);
                    g2.drawLine(22, 12, 26, 16);
                    g2.drawLine(22, 12, 24, 10);
                    g2.drawLine(26, 16, 28, 14);
                    g2.drawLine(24, 10, 28, 14);
                    g2.fillOval(10, 10, 3, 3);
                    g2.fillOval(25, 23, 2, 2);
                }
                case "maintenance" -> {
                    g2.drawLine(10, 26, 20, 16);
                    g2.drawOval(18, 10, 8, 8);
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
                    g2.fillOval(21, 11, 4, 4);
                }
            }
        }

        public void setValue(int value) {
            valueLabel.setText(String.valueOf(value));
        }

        public void updateTheme() {
            setBackground(Theme.bgCard());
            titleLabel.setForeground(Theme.textSecondary());
            valueLabel.setForeground(Theme.textPrimary());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(Theme.border());
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            g2.dispose();
        }
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
                case "edit" -> {
                    g2.drawRect(x, y + 5, 6, 3);
                    g2.drawLine(x + 1, y + 5, x + 4, y + 2);
                    g2.drawLine(x + 3, y + 7, x + 6, y + 4);
                }
                case "delete" -> {
                    g2.drawRect(x + 2, y + 2, 8, 8);
                    g2.drawLine(x, y + 2, x + 12, y + 2);
                    g2.drawLine(x + 4, y, x + 8, y);
                    g2.drawLine(x + 4, y + 4, x + 4, y + 8);
                    g2.drawLine(x + 8, y + 4, x + 8, y + 8);
                }
                case "view" -> {
                    g2.drawOval(x, y + 2, 12, 6);
                    g2.fillOval(x + 4, y + 3, 4, 4);
                }
                case "status" -> {
                    g2.drawOval(x, y, 10, 10);
                    g2.drawLine(x + 5, y + 2, x + 5, y + 8);
                    g2.drawLine(x + 2, y + 5, x + 8, y + 5);
                }
            }
        }
    }

    // Custom Component: SearchField
    private static class SearchField extends JTextField {
        private boolean hover = false;
        private final String placeholder = "Search rooms, types, floors...";

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
