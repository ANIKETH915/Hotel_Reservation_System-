package ui;

import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.PmsKpiCard;
import components.PmsLineChart;
import components.PmsPieChart;
import components.PmsBarChart;
import components.Theme;
import components.Toast;
import components.UiLayout;
import components.CardPanel;
import components.StyledButton;
import components.ConfirmDialog;
import components.TableCard;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import model.Booking;
import model.BookingStatus;
import model.PaymentStatus;
import model.DashboardStats;
import model.Room;
import model.RoomStatus;
import service.BookingService;
import service.DashboardService;
import service.RoomService;
import dao.PaymentDao;
import utils.CurrencyUtil;
import utils.DateUtil;

public class DashboardPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final MainFrame mainFrame;
    private final DashboardService dashboardService = new DashboardService();
    private final BookingService bookingService = new BookingService();
    private final RoomService roomService = new RoomService();

    // 12 Custom KPI Cards
    private PmsKpiCard totalRoomsCard;
    private PmsKpiCard availableCard;
    private PmsKpiCard occupiedCard;
    private PmsKpiCard reservedCard;
    private PmsKpiCard todayRevenueCard;
    private PmsKpiCard monthRevenueCard;
    private PmsKpiCard totalGuestsCard;
    private PmsKpiCard vipGuestsCard;
    private PmsKpiCard checkInsCard;
    private PmsKpiCard checkOutsCard;
    private PmsKpiCard pendingBookingsCard;
    private PmsKpiCard roomsToCleanCard;

    // Charts
    private PmsLineChart revenueLineChart;
    private PmsPieChart occupancyPieChart;
    private PmsBarChart bookingBarChart;

    // Quick Actions & Live Status
    private QuickActionsPanel quickActions;
    private LiveStatusPanel liveStatus;

    // Layout panels
    private PageHeader pageHeader;
    private JPanel statsGrid;
    private JScrollPane pageScroll;
    private UiLayout.ViewportWidthPanel content;

    // Bottom redone table with Tabs
    private JTabbedPane staysTabbedPane;
    private final DefaultTableModel activeModel = staysTableModel();
    private final DefaultTableModel checkInModel = staysTableModel();
    private final DefaultTableModel checkOutModel = staysTableModel();
    private ModernTable activeTable;
    private ModernTable checkInTable;
    private ModernTable checkOutTable;
    private JScrollPane activeScroll;
    private JScrollPane checkInScroll;
    private JScrollPane checkOutScroll;
    private TableCard bottomTableCard;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private static DefaultTableModel staysTableModel() {
        return new DefaultTableModel(new String[]{"Photo", "Guest Name", "Room", "Check-in", "Check-out", "Status", "Payment", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action buttons column is editable to receive clicks
            }
        };
    }

    private void buildUi() {
        pageHeader = new PageHeader("PMS Enterprise Dashboard",
                "Luxury Hotel Operations Console · " + DateUtil.format(LocalDate.now()));

        // 12 KPI cards grid
        statsGrid = new JPanel(new GridLayout(0, 6, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
        statsGrid.setOpaque(false);

        totalRoomsCard = new PmsKpiCard("Total Rooms", "0", "rooms", Theme.ROYAL_BLUE, "100% capacity", true);
        availableCard = new PmsKpiCard("Available Rooms", "0", "rooms", Theme.EMERALD, "Ready to sell", true);
        occupiedCard = new PmsKpiCard("Occupied Rooms", "0", "rooms", Theme.DANGER, "In house today", false);
        reservedCard = new PmsKpiCard("Reserved Rooms", "0", "rooms", Theme.GOLD, "Guaranteed arrivals", true);
        todayRevenueCard = new PmsKpiCard("Today's Revenue", "$0", "revenue", Theme.EMERALD, "Daily postings", true);
        monthRevenueCard = new PmsKpiCard("Monthly Revenue", "$0", "monthly", Theme.ROYAL_BLUE, "Mtd performance", true);
        totalGuestsCard = new PmsKpiCard("Total Guests", "0", "customers", Theme.ROYAL_BLUE, "Registered guest profiles", true);
        vipGuestsCard = new PmsKpiCard("VIP Guests", "0", "customers", Theme.GOLD, "Elite statuses", true);
        checkInsCard = new PmsKpiCard("Today's Arrivals", "0", "bookings", Theme.EMERALD, "Check-ins scheduled", true);
        checkOutsCard = new PmsKpiCard("Today's Departures", "0", "logout", Theme.ROYAL_BLUE, "Check-outs scheduled", false);
        pendingBookingsCard = new PmsKpiCard("Pending Bookings", "0", "summary", Theme.GOLD, "Awaiting arrivals", true);
        roomsToCleanCard = new PmsKpiCard("Rooms to Clean", "0", "settings", Theme.WARNING, "Housekeeping list", false);

        statsGrid.add(totalRoomsCard);
        statsGrid.add(availableCard);
        statsGrid.add(occupiedCard);
        statsGrid.add(reservedCard);
        statsGrid.add(todayRevenueCard);
        statsGrid.add(monthRevenueCard);
        statsGrid.add(totalGuestsCard);
        statsGrid.add(vipGuestsCard);
        statsGrid.add(checkInsCard);
        statsGrid.add(checkOutsCard);
        statsGrid.add(pendingBookingsCard);
        statsGrid.add(roomsToCleanCard);

        // Middle Section: Left (Actions/Status) & Right (Line Chart)
        JPanel midPanel = new JPanel(new GridLayout(1, 2, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
        midPanel.setOpaque(false);

        // Left of mid panel: Quick Actions & Live Status stacked
        JPanel midLeftWrapper = new JPanel();
        midLeftWrapper.setLayout(new BoxLayout(midLeftWrapper, BoxLayout.Y_AXIS));
        midLeftWrapper.setOpaque(false);

        quickActions = new QuickActionsPanel(mainFrame);
        liveStatus = new LiveStatusPanel();
        
        midLeftWrapper.add(quickActions);
        midLeftWrapper.add(Box.createVerticalStrut(UiLayout.SPACE_MD));
        midLeftWrapper.add(liveStatus);

        // Right of mid panel: Line Chart
        revenueLineChart = new PmsLineChart("Revenue Trend (Last 7 Days)", Theme.EMERALD);
        revenueLineChart.setPreferredSize(new Dimension(100, 280));
        
        midPanel.add(midLeftWrapper);
        midPanel.add(revenueLineChart);

        // Lower Section: Left (Pie Occupancy) & Right (Booking Bar Chart)
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
        lowerPanel.setOpaque(false);

        occupancyPieChart = new PmsPieChart("Room Occupancy Status");
        occupancyPieChart.setPreferredSize(new Dimension(100, 240));

        bookingBarChart = new PmsBarChart("Booking Statistics Analytics");
        bookingBarChart.setPreferredSize(new Dimension(100, 240));

        lowerPanel.add(occupancyPieChart);
        lowerPanel.add(bookingBarChart);

        // Bottom Section: redone tables inside tabs
        activeTable = new ModernTable(activeModel);
        checkInTable = new ModernTable(checkInModel);
        checkOutTable = new ModernTable(checkOutModel);

        configureTable(activeTable);
        configureTable(checkInTable);
        configureTable(checkOutTable);

        activeScroll = UiLayout.tableScroll(activeTable);
        checkInScroll = UiLayout.tableScroll(checkInTable);
        checkOutScroll = UiLayout.tableScroll(checkOutTable);

        // Setup custom tabbed pane
        staysTabbedPane = new JTabbedPane();
        staysTabbedPane.setOpaque(false);
        staysTabbedPane.setFont(Theme.fontMedium(12));
        
        staysTabbedPane.addTab("In-House Stays", activeScroll);
        staysTabbedPane.addTab("Scheduled Arrivals", checkInScroll);
        staysTabbedPane.addTab("Scheduled Departures", checkOutScroll);

        bottomTableCard = new TableCard(staysTabbedPane);
        bottomTableCard.setPreferredSize(new Dimension(100, 280));

        // Assemble viewport page content
        content = new UiLayout.ViewportWidthPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, UiLayout.SPACE_LG, 0));

        content.add(UiLayout.fullWidth(pageHeader));
        content.add(UiLayout.fullWidth(statsGrid));
        content.add(Box.createVerticalStrut(UiLayout.SPACE_MD));
        content.add(UiLayout.fullWidth(midPanel));
        content.add(Box.createVerticalStrut(UiLayout.SPACE_MD));
        content.add(UiLayout.fullWidth(lowerPanel));
        content.add(Box.createVerticalStrut(UiLayout.SPACE_MD));
        content.add(UiLayout.fullWidth(bottomTableCard));

        pageScroll = UiLayout.pageScroll(content);
        add(pageScroll, BorderLayout.CENTER);

        // Handle Responsive Card Columns
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                int availableWidth = Math.max(0, getWidth() - 40);
                // Grid responds to width changes: 6 columns, 4 columns, or 2 columns
                int columns = availableWidth < 680 ? 2 : availableWidth < 1080 ? 4 : 6;
                GridLayout layout = (GridLayout) statsGrid.getLayout();
                if (layout.getColumns() != columns) {
                    statsGrid.setLayout(new GridLayout(0, columns, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
                    statsGrid.revalidate();
                }
                
                int midCols = availableWidth < 900 ? 1 : 2;
                GridLayout midLayout = (GridLayout) midPanel.getLayout();
                if (midLayout.getColumns() != midCols) {
                    midPanel.setLayout(new GridLayout(0, midCols, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
                    midPanel.revalidate();
                }

                int lowerCols = availableWidth < 900 ? 1 : 2;
                GridLayout lowerLayout = (GridLayout) lowerPanel.getLayout();
                if (lowerLayout.getColumns() != lowerCols) {
                    lowerPanel.setLayout(new GridLayout(0, lowerCols, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
                    lowerPanel.revalidate();
                }

                UiLayout.refreshFullWidth(pageHeader);
                UiLayout.refreshFullWidth(statsGrid);
                UiLayout.refreshFullWidth(midPanel);
                UiLayout.refreshFullWidth(lowerPanel);
                UiLayout.refreshFullWidth(bottomTableCard);
            }
        });
    }

    private void configureTable(ModernTable table) {
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.ROYAL_BLUE);
        table.setSelectionForeground(Color.WHITE);

        // Hover row mouse listeners
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                Object oldHover = table.getClientProperty("hoveredRow");
                int oldHoverRow = oldHover instanceof Integer ? (Integer) oldHover : -1;
                if (row != oldHoverRow) {
                    table.putClientProperty("hoveredRow", row);
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                table.putClientProperty("hoveredRow", -1);
                table.repaint();
            }
        });

        // JTableHeader rounded renderer
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(Theme.fontBold(11));
                label.setForeground(Theme.GOLD);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                JPanel cell = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(Theme.DARK_NAVY);
                        int w = getWidth();
                        int h = getHeight();

                        if (column == 0) {
                            g2.fillRoundRect(0, 2, w + 10, h - 4, 10, 10);
                            g2.fillRect(w - 10, 2, 10, h - 4);
                        } else if (column == t.getColumnCount() - 1) {
                            g2.fillRoundRect(-10, 2, w + 10, h - 4, 10, 10);
                            g2.fillRect(0, 2, 10, h - 4);
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

        // Assign Custom Renderers
        table.getColumnModel().getColumn(0).setMinWidth(48);
        table.getColumnModel().getColumn(0).setMaxWidth(48);
        table.getColumnModel().getColumn(0).setCellRenderer(new AvatarRenderer());

        table.getColumnModel().getColumn(1).setCellRenderer(new TextRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new TextRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new TextRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new TextRenderer());

        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        // Assign Custom Actions Editor & Renderer
        table.getColumnModel().getColumn(7).setMinWidth(170);
        table.getColumnModel().getColumn(7).setMaxWidth(170);
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionsCellEditor(mainFrame, this::refresh));
    }

    @Override
    public void applySearch(String query) {
        if (activeTable != null) activeTable.filter(query);
        if (checkInTable != null) checkInTable.filter(query);
        if (checkOutTable != null) checkOutTable.filter(query);
    }

    @Override
    public void refresh() {
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardStats stats = dashboardService.loadStats();
                List<Booking> active = bookingService.listActiveToday();
                List<Booking> checkIns = bookingService.listTodayCheckIns();
                List<Booking> checkOuts = bookingService.listTodayCheckOuts();

                // Compute weekly and monthly bookings counts in memory
                List<Booking> all = bookingService.list();
                int todayCount = stats.getTodayBookings();
                int weeklyCount = 0;
                int monthlyCount = 0;
                int pendingPayments = 0;
                int lateCheckouts = 0;

                LocalDate today = LocalDate.now();
                LocalDate startOfWeek = today.minusDays(7);
                LocalDate startOfMonth = today.minusDays(30);

                for (Booking b : all) {
                    if (b.getCreatedAt() != null) {
                        LocalDate createdDate = b.getCreatedAt().toLocalDate();
                        if (!createdDate.isBefore(startOfWeek)) weeklyCount++;
                        if (!createdDate.isBefore(startOfMonth)) monthlyCount++;
                    }
                    if (b.getPaymentStatus() == PaymentStatus.PENDING && b.getBookingStatus() != BookingStatus.CANCELLED) {
                        pendingPayments++;
                    }
                    // Late departures: checked in, checkout date is yesterday or earlier
                    if (b.getBookingStatus() == BookingStatus.CHECKED_IN && b.getCheckOut().isBefore(today)) {
                        lateCheckouts++;
                    }
                }

                // Gather 7-day revenue trend
                List<LocalDate> revenueDates = new ArrayList<>();
                List<Double> revenues = new ArrayList<>();
                PaymentDao paymentDao = new PaymentDao();
                for (int i = 6; i >= 0; i--) {
                    LocalDate day = today.minusDays(i);
                    revenueDates.add(day);
                    BigDecimal sum = paymentDao.sumBetween(day, day);
                    revenues.add(sum == null ? 0.0 : sum.doubleValue());
                }

                return new DashboardData(stats, active, checkIns, checkOuts, weeklyCount, monthlyCount, pendingPayments, lateCheckouts, revenueDates, revenues);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateStats(data.stats, data.weeklyCount, data.monthlyCount);
                    
                    // Populate Stay Tables
                    populateTable(activeModel, data.active);
                    populateTable(checkInModel, data.checkIns);
                    populateTable(checkOutModel, data.checkOuts);

                    // Update Charts
                    revenueLineChart.setData(data.revenueDates, data.revenues);

                    List<PmsPieChart.Segment> segments = new ArrayList<>();
                    segments.add(new PmsPieChart.Segment("Available", data.stats.getAvailableRooms(), Theme.EMERALD));
                    segments.add(new PmsPieChart.Segment("Occupied", data.stats.getOccupiedRooms(), Theme.ROYAL_BLUE));
                    segments.add(new PmsPieChart.Segment("Reserved", data.stats.getReservedRooms(), Theme.GOLD));
                    segments.add(new PmsPieChart.Segment("Out of Order", data.stats.getMaintenanceRooms() + data.stats.getCleaningRooms(), Theme.DANGER));
                    occupancyPieChart.setOccupancyRate(data.stats.getOccupancyRate());
                    occupancyPieChart.setSegmentsData(segments);

                    List<PmsBarChart.BarItem> barItems = new ArrayList<>();
                    barItems.add(new PmsBarChart.BarItem("Today", data.stats.getTodayBookings(), Theme.EMERALD, new Color(0x34, 0xD3, 0x99)));
                    barItems.add(new PmsBarChart.BarItem("Weekly", data.weeklyCount, Theme.ROYAL_BLUE, new Color(0x60, 0xA5, 0xFA)));
                    barItems.add(new PmsBarChart.BarItem("Monthly", data.monthlyCount, Theme.GOLD, new Color(0xFB, 0xBF, 0x24)));
                    bookingBarChart.setData(barItems);

                    // Update Live Status
                    liveStatus.update(data.stats, data.pendingPayments, data.lateCheckouts);

                    pageHeader.setSubtitle("Luxury Hotel Operations Console · " + DateUtil.format(LocalDate.now())
                            + " · Auto-refreshes every 60s");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load property dashboard data: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateStats(DashboardStats stats, int weeklyCount, int monthlyCount) {
        totalRoomsCard.setValue(String.valueOf(stats.getTotalRooms()));
        availableCard.setValue(String.valueOf(stats.getAvailableRooms()));
        occupiedCard.setValue(String.valueOf(stats.getOccupiedRooms()));
        reservedCard.setValue(String.valueOf(stats.getReservedRooms()));
        
        todayRevenueCard.setValue(CurrencyUtil.format(stats.getTodayRevenue() == null ? BigDecimal.ZERO : stats.getTodayRevenue()));
        monthRevenueCard.setValue(CurrencyUtil.format(stats.getMonthRevenue() == null ? BigDecimal.ZERO : stats.getMonthRevenue()));
        
        totalGuestsCard.setValue(String.valueOf(stats.getTotalCustomers()));
        vipGuestsCard.setValue(String.valueOf(stats.getVipCustomers()));
        
        checkInsCard.setValue(String.valueOf(stats.getTodayCheckIns()));
        checkOutsCard.setValue(String.valueOf(stats.getTodayCheckOuts()));
        
        pendingBookingsCard.setValue(String.valueOf(stats.getTodayBookings())); // or pending reservations
        roomsToCleanCard.setValue(String.valueOf(stats.getCleaningRooms()));
    }

    private void populateTable(DefaultTableModel model, List<Booking> bookings) {
        model.setRowCount(0);
        for (Booking b : bookings) {
            model.addRow(new Object[]{
                    b.getCustomerName(), // Col 0: Photo (read initials from name)
                    b.getCustomerName(), // Col 1: Guest Name
                    b.getRoomNumber() + " (" + b.getRoomType() + ")", // Col 2: Room
                    DateUtil.format(b.getCheckIn()), // Col 3: Check-in
                    DateUtil.format(b.getCheckOut()), // Col 4: Check-out
                    b.getBookingStatus().getLabel(), // Col 5: Status
                    b.getPaymentStatus().getLabel(), // Col 6: Payment
                    b // Col 7: Actions (references the Booking object)
            });
        }
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        if (pageScroll != null) {
            pageScroll.getViewport().setBackground(Theme.bgPrimary());
        }
        pageHeader.applyTheme();
        
        totalRoomsCard.applyTheme();
        availableCard.applyTheme();
        occupiedCard.applyTheme();
        reservedCard.applyTheme();
        todayRevenueCard.applyTheme();
        monthRevenueCard.applyTheme();
        totalGuestsCard.applyTheme();
        vipGuestsCard.applyTheme();
        checkInsCard.applyTheme();
        checkOutsCard.applyTheme();
        pendingBookingsCard.applyTheme();
        roomsToCleanCard.applyTheme();
        
        activeTable.applyTheme();
        checkInTable.applyTheme();
        checkOutTable.applyTheme();
        bottomTableCard.applyTheme();
        
        revenueLineChart.applyTheme();
        occupancyPieChart.applyTheme();
        bookingBarChart.applyTheme();

        quickActions.applyTheme();
        liveStatus.applyTheme();
        repaint();
    }

    private record DashboardData(DashboardStats stats, List<Booking> active, List<Booking> checkIns, List<Booking> checkOuts,
                                 int weeklyCount, int monthlyCount, int pendingPayments, int lateCheckouts,
                                 List<LocalDate> revenueDates, List<Double> revenues) {
    }

    // ==========================================
    // Renderers & Editors for Stay Table redesign
    // ==========================================

    private static class AvatarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String name = value.toString();
            
            Object hoverProp = table.getClientProperty("hoveredRow");
            int hoverRow = hoverProp instanceof Integer ? (Integer) hoverProp : -1;
            boolean isHovered = (row == hoverRow);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Select background color based on guest name hash code
                    int hash = name.hashCode();
                    Color bg = switch (Math.abs(hash) % 4) {
                        case 0 -> Theme.ROYAL_BLUE;
                        case 1 -> Theme.GOLD;
                        case 2 -> Theme.EMERALD;
                        default -> new Color(0x7C, 0x3A, 0xED); // Indigo/Purple
                    };
                    
                    g2.setColor(bg);
                    g2.fillOval(8, 7, 32, 32);
                    
                    // Guest Initials
                    g2.setColor(Color.WHITE);
                    g2.setFont(Theme.fontBold(12));
                    String initials = "";
                    String[] parts = name.split("\\s+");
                    if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
                    initials = initials.toUpperCase();
                    
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initials, 8 + (32 - fm.stringWidth(initials)) / 2, 7 + (32 + fm.getAscent() - fm.getLeading()) / 2 - 2);
                    g2.dispose();
                }
            };
            panel.setOpaque(true);

            Color bgCol;
            if (isSelected) {
                bgCol = Theme.ROYAL_BLUE;
            } else if (isHovered) {
                bgCol = Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9);
            } else {
                bgCol = row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt();
            }
            panel.setBackground(bgCol);
            return panel;
        }
    }

    private static class TextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(Theme.fontRegular(13));
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            Object hoverProp = table.getClientProperty("hoveredRow");
            int hoverRow = hoverProp instanceof Integer ? (Integer) hoverProp : -1;
            boolean isHovered = (row == hoverRow);

            Color bgCol;
            Color fgCol;
            if (isSelected) {
                bgCol = Theme.ROYAL_BLUE;
                fgCol = Color.WHITE;
            } else if (isHovered) {
                bgCol = Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9);
                fgCol = Theme.textPrimary();
            } else {
                bgCol = row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt();
                fgCol = Theme.textPrimary();
            }
            label.setBackground(bgCol);
            label.setForeground(fgCol);
            return label;
        }
    }

    private static class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value.toString();
            Color statusColor = Theme.statusColor(text);
            
            Object hoverProp = table.getClientProperty("hoveredRow");
            int hoverRow = hoverProp instanceof Integer ? (Integer) hoverProp : -1;
            boolean isHovered = (row == hoverRow);

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 11)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw nice capsule wash
                    Color fill = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 28);
                    g2.setColor(fill);
                    g2.fillRoundRect(10, 8, getWidth() - 20, getHeight() - 16, 12, 12);
                    g2.setColor(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 70));
                    g2.drawRoundRect(10, 8, getWidth() - 20, getHeight() - 16, 12, 12);
                    
                    g2.dispose();
                }
            };
            panel.setOpaque(true);

            Color bgCol;
            if (isSelected) {
                bgCol = Theme.ROYAL_BLUE;
            } else if (isHovered) {
                bgCol = Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9);
            } else {
                bgCol = row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt();
            }
            panel.setBackground(bgCol);
            
            JLabel label = new JLabel(text);
            label.setFont(Theme.fontBold(10));
            label.setForeground(isSelected ? Color.WHITE : statusColor);
            panel.add(label);
            return panel;
        }
    }

    private static class ActionsPanel extends JPanel {
        final StyledButton btn1;
        final StyledButton btn2;
        Booking booking;

        ActionsPanel() {
            setOpaque(true);
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 8));
            btn1 = new StyledButton("", StyledButton.Style.PRIMARY);
            btn2 = new StyledButton("", StyledButton.Style.SECONDARY);
            btn1.setPreferredSize(new Dimension(74, 28));
            btn2.setPreferredSize(new Dimension(74, 28));
            btn1.setFont(Theme.fontBold(10));
            btn2.setFont(Theme.fontBold(10));
            add(btn1);
            add(btn2);
        }

        void setBooking(Booking b, Color bg) {
            this.booking = b;
            setBackground(bg);
            btn1.setVisible(false);
            btn2.setVisible(false);
            if (b == null) return;

            switch (b.getBookingStatus()) {
                case CONFIRMED -> {
                    btn1.setText("Check-in");
                    btn1.setBackground(Theme.EMERALD);
                    btn1.setVisible(true);
                    
                    btn2.setText("Cancel");
                    btn2.setBackground(Theme.DANGER);
                    btn2.setVisible(true);
                }
                case CHECKED_IN -> {
                    btn1.setText("Check-out");
                    btn1.setBackground(Theme.DANGER);
                    btn1.setVisible(true);
                    
                    btn2.setText("Billing");
                    btn2.setBackground(Theme.GOLD);
                    btn2.setVisible(true);
                }
                default -> {
                    btn1.setText("Invoice");
                    btn1.setBackground(Theme.ROYAL_BLUE);
                    btn1.setVisible(true);
                }
            }
        }
    }

    private static class ActionsCellRenderer extends JPanel implements TableCellRenderer {
        private final ActionsPanel panel = new ActionsPanel();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Booking b = (Booking) value;

            Object hoverProp = table.getClientProperty("hoveredRow");
            int hoverRow = hoverProp instanceof Integer ? (Integer) hoverProp : -1;
            boolean isHovered = (row == hoverRow);

            Color bgCol;
            if (isSelected) {
                bgCol = Theme.ROYAL_BLUE;
            } else if (isHovered) {
                bgCol = Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9);
            } else {
                bgCol = row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt();
            }
            panel.setBooking(b, bgCol);
            return panel;
        }
    }

    private static class ActionsCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final ActionsPanel panel;
        
        public ActionsCellEditor(MainFrame mainFrame, Runnable onMutation) {
            panel = new ActionsPanel();
            
            panel.btn1.addActionListener(e -> {
                stopCellEditing();
                handleAction(panel.booking, panel.btn1.getText(), mainFrame, onMutation);
            });
            
            panel.btn2.addActionListener(e -> {
                stopCellEditing();
                handleAction(panel.booking, panel.btn2.getText(), mainFrame, onMutation);
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Booking b = (Booking) value;
            panel.setBooking(b, table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return panel.booking;
        }

        private void handleAction(Booking booking, String action, MainFrame mainFrame, Runnable onMutation) {
            if (booking == null) return;
            BookingService bookingService = new BookingService();
            
            switch (action) {
                case "Check-in" -> {
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
                                Toast.success(mainFrame, "Guest checked in successfully");
                                onMutation.run();
                            } catch (Exception ex) {
                                Toast.error(mainFrame, "Check-in failed: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
                case "Check-out" -> {
                    new ui.dialog.CheckoutDialog(mainFrame, booking, onMutation).setVisible(true);
                }
                case "Cancel" -> {
                    if (!ConfirmDialog.confirm(mainFrame, "Cancel Booking",
                            "Are you sure you want to cancel the booking for " + booking.getCustomerName() + "?",
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
                                Toast.success(mainFrame, "Booking cancelled successfully");
                                onMutation.run();
                            } catch (Exception ex) {
                                Toast.error(mainFrame, "Cancellation failed: " + ex.getMessage());
                            }
                        }
                    }.execute();
                }
                case "Billing" -> {
                    new ui.dialog.PaymentDialog(mainFrame, booking, onMutation).setVisible(true);
                }
                case "Invoice" -> {
                    reports.InvoicePrinter.printInvoice(mainFrame, booking);
                }
            }
        }
    }

    // ==========================================
    // Quick Actions & Live Property Status Panels
    // ==========================================

    private static class QuickActionsPanel extends CardPanel {
        public QuickActionsPanel(MainFrame mainFrame) {
            super(new BorderLayout());
            setArc(16);
            setBorder(new EmptyBorder(14, 18, 14, 18));
            setPreferredSize(new Dimension(100, 115));
            
            JLabel title = new JLabel("FRONT DESK QUICK ACTIONS");
            title.setFont(Theme.fontBold(10));
            title.setForeground(Theme.textSecondary());
            add(title, BorderLayout.NORTH);
            
            JPanel grid = new JPanel(new java.awt.GridLayout(1, 0, 8, 0));
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(10, 0, 0, 0));
            
            grid.add(createActionButton("+ Booking", Theme.EMERALD, e -> {
                new ui.dialog.BookingFormDialog(mainFrame, () -> mainFrame.notifyDataChanged(components.AppEvents.Domain.BOOKINGS)).setVisible(true);
            }));
            
            grid.add(createActionButton("+ Guest", Theme.ROYAL_BLUE, e -> {
                new ui.dialog.CustomerFormDialog(mainFrame, null, () -> mainFrame.notifyDataChanged(components.AppEvents.Domain.CUSTOMERS)).setVisible(true);
            }));
            
            grid.add(createActionButton("+ Room", Theme.GOLD, e -> {
                new ui.dialog.RoomFormDialog(mainFrame, null, () -> mainFrame.notifyDataChanged(components.AppEvents.Domain.ROOMS)).setVisible(true);
            }));
            
            grid.add(createActionButton("Generate Inv", Theme.GOLD, e -> {
                mainFrame.navigate("Payments");
            }));
            
            grid.add(createActionButton("Reports", Theme.ROYAL_BLUE, e -> {
                mainFrame.navigate("Reports");
            }));
            
            add(grid, BorderLayout.CENTER);
        }
        
        private StyledButton createActionButton(String label, Color color, java.awt.event.ActionListener al) {
            StyledButton btn = new StyledButton(label, StyledButton.Style.PRIMARY);
            btn.setFont(Theme.fontBold(10));
            btn.addActionListener(al);
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            return btn;
        }

        public void applyTheme() {
            repaint();
        }
    }

    private static class LiveStatusPanel extends CardPanel {
        private final JLabel occupancyVal = new JLabel("0.0%");
        private final JLabel readyVal = new JLabel("0");
        private final JLabel houseVal = new JLabel("0");
        private final JLabel maintVal = new JLabel("0");
        private final JLabel paymentVal = new JLabel("0");
        private final JLabel vipVal = new JLabel("0");
        private final JLabel lateVal = new JLabel("0");

        public LiveStatusPanel() {
            super(new BorderLayout());
            setArc(16);
            setBorder(new EmptyBorder(14, 18, 14, 18));
            
            JLabel title = new JLabel("LIVE SYSTEM MONITOR");
            title.setFont(Theme.fontBold(10));
            title.setForeground(Theme.textSecondary());
            add(title, BorderLayout.NORTH);
            
            JPanel grid = new JPanel(new java.awt.GridLayout(2, 4, 8, 8));
            grid.setOpaque(false);
            grid.setBorder(new EmptyBorder(10, 0, 0, 0));
            
            grid.add(createStatusWidget("Occupancy", occupancyVal, Theme.ROYAL_BLUE));
            grid.add(createStatusWidget("Rooms Ready", readyVal, Theme.EMERALD));
            grid.add(createStatusWidget("Housekeeping", houseVal, Theme.GOLD));
            grid.add(createStatusWidget("Maintenance", maintVal, Theme.DANGER));
            grid.add(createStatusWidget("Unpaid Stays", paymentVal, Theme.DANGER));
            grid.add(createStatusWidget("VIP Stays", vipVal, Theme.GOLD));
            grid.add(createStatusWidget("Late Depart", lateVal, Theme.WARNING));
            
            // Blank cell for grid completion
            JPanel cell = new JPanel();
            cell.setOpaque(false);
            grid.add(cell);
            
            add(grid, BorderLayout.CENTER);
        }
        
        private JPanel createStatusWidget(String name, JLabel valLabel, Color color) {
            JPanel widget = new JPanel(new BorderLayout(6, 0));
            widget.setOpaque(false);
            widget.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border(), 1, true),
                new EmptyBorder(6, 10, 6, 10)
            ));
            
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(Theme.fontMedium(10));
            nameLabel.setForeground(Theme.textSecondary());
            
            valLabel.setFont(Theme.fontBold(13));
            valLabel.setForeground(color);
            
            widget.add(nameLabel, BorderLayout.CENTER);
            widget.add(valLabel, BorderLayout.EAST);
            
            return widget;
        }
        
        public void update(DashboardStats stats, int pendingPayments, int lateCheckouts) {
            occupancyVal.setText(String.format("%.1f%%", stats.getOccupancyRate()));
            readyVal.setText(String.valueOf(stats.getAvailableRooms()));
            houseVal.setText(String.valueOf(stats.getCleaningRooms()));
            maintVal.setText(String.valueOf(stats.getMaintenanceRooms()));
            paymentVal.setText(String.valueOf(pendingPayments));
            vipVal.setText(String.valueOf(stats.getVipCustomers()));
            lateVal.setText(String.valueOf(lateCheckouts));
        }

        public void applyTheme() {
            repaint();
        }
    }
}
