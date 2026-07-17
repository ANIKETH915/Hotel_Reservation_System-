package ui;

import components.AppEvents;
import components.CardPanel;
import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StatCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Booking;
import model.DashboardStats;
import service.BookingService;
import service.DashboardService;
import utils.CurrencyUtil;
import utils.DateUtil;

public class DashboardPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final MainFrame mainFrame;
    private final DashboardService dashboardService = new DashboardService();
    private final BookingService bookingService = new BookingService();

    private StatCard totalRoomsCard;
    private StatCard availableCard;
    private StatCard occupiedCard;
    private StatCard reservedCard;
    private StatCard todayBookingsCard;
    private StatCard todayRevenueCard;
    private StatCard customersCard;
    private StatCard occupancyCard;
    private PageHeader pageHeader;
    private JPanel statsGrid;
    private JPanel arrivalDepartureGrid;

    private final DefaultTableModel activeModel = nonEditable("Guest", "Room", "Status", "Check-out");
    private final DefaultTableModel checkInModel = nonEditable("Guest", "Room", "Check-in");
    private final DefaultTableModel checkOutModel = nonEditable("Guest", "Room", "Check-out");
    private ModernTable activeTable;
    private ModernTable checkInTable;
    private ModernTable checkOutTable;
    private TableEmptyOverlay activeOverlay;
    private TableEmptyOverlay checkInOverlay;
    private TableEmptyOverlay checkOutOverlay;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUi();
    }

    private static DefaultTableModel nonEditable(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void buildUi() {
        pageHeader = new PageHeader("Operations Overview",
                "Live property status · " + DateUtil.format(LocalDate.now()));

        statsGrid = new JPanel(new GridLayout(2, 4, 16, 16));
        statsGrid.setOpaque(false);

        totalRoomsCard = new StatCard("Total Rooms", "0", Theme.ROYAL_BLUE);
        availableCard = new StatCard("Available", "0", Theme.EMERALD);
        occupiedCard = new StatCard("In Use", "0", Theme.GOLD);
        reservedCard = new StatCard("Reserved", "0", Theme.ROYAL_BLUE);
        todayBookingsCard = new StatCard("Today's Bookings", "0", Theme.ROYAL_BLUE);
        todayRevenueCard = new StatCard("Today's Revenue", CurrencyUtil.format(java.math.BigDecimal.ZERO), Theme.EMERALD);
        customersCard = new StatCard("Total Customers", "0", Theme.ROYAL_BLUE);
        occupancyCard = new StatCard("Occupancy Rate", "0.0%", Theme.GOLD);

        statsGrid.add(totalRoomsCard);
        statsGrid.add(availableCard);
        statsGrid.add(occupiedCard);
        statsGrid.add(reservedCard);
        statsGrid.add(todayBookingsCard);
        statsGrid.add(todayRevenueCard);
        statsGrid.add(customersCard);
        statsGrid.add(occupancyCard);

        activeTable = new ModernTable(activeModel);
        checkInTable = new ModernTable(checkInModel);
        checkOutTable = new ModernTable(checkOutModel);

        EmptyStatePanel activeEmpty = new EmptyStatePanel("No active stays",
                "Active reservations and checked-in guests will appear here.");
        activeEmpty.setIconKey("bookings");
        EmptyStatePanel inEmpty = new EmptyStatePanel("No check-ins today",
                "Create a booking with today's check-in date to see arrivals.");
        inEmpty.setIconKey("bookings");
        EmptyStatePanel outEmpty = new EmptyStatePanel("No check-outs today",
                "Departures scheduled for today will show here.");
        outEmpty.setIconKey("bookings");

        activeOverlay = new TableEmptyOverlay(wrap(activeTable), activeEmpty);
        checkInOverlay = new TableEmptyOverlay(wrap(checkInTable), inEmpty);
        checkOutOverlay = new TableEmptyOverlay(wrap(checkOutTable), outEmpty);

        CardPanel activeCard = sectionCard("Active Stays Today", activeOverlay);
        CardPanel inCard = sectionCard("Today's Check-ins", checkInOverlay);
        CardPanel outCard = sectionCard("Today's Check-outs", checkOutOverlay);

        arrivalDepartureGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        arrivalDepartureGrid.setOpaque(false);
        arrivalDepartureGrid.add(inCard);
        arrivalDepartureGrid.add(outCard);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(pageHeader);
        content.add(statsGrid);
        content.add(Box.createVerticalStrut(16));
        content.add(activeCard);
        content.add(Box.createVerticalStrut(16));
        content.add(arrivalDepartureGrid);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.bgPrimary());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                int availableWidth = Math.max(0, getWidth() - 40);
                int columns = availableWidth < 700 ? 1 : availableWidth < 980 ? 2 : 4;
                GridLayout layout = (GridLayout) statsGrid.getLayout();
                if (layout.getColumns() != columns) {
                    statsGrid.setLayout(new GridLayout(0, columns, 16, 16));
                    statsGrid.revalidate();
                }
                int lowerRows = availableWidth < 700 ? 2 : 1;
                GridLayout lowerLayout = (GridLayout) arrivalDepartureGrid.getLayout();
                if (lowerLayout.getRows() != lowerRows) {
                    arrivalDepartureGrid.setLayout(new GridLayout(lowerRows, 1, 16, 16));
                    arrivalDepartureGrid.revalidate();
                }
            }
        });
    }

    private JScrollPane wrap(ModernTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(100, 260));
        return sp;
    }

    private CardPanel sectionCard(String title, JPanel body) {
        CardPanel card = new CardPanel(new BorderLayout(0, 8));
        javax.swing.JLabel label = new javax.swing.JLabel(title);
        label.setFont(Theme.fontMedium(14));
        label.setForeground(Theme.textPrimary());
        card.add(label, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
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
                return new DashboardData(stats, active, checkIns, checkOuts);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateStats(data.stats);
                    activeModel.setRowCount(0);
                    for (Booking b : data.active) {
                        activeModel.addRow(new Object[]{
                                b.getCustomerName(),
                                b.getRoomNumber(),
                                b.getBookingStatus().getLabel(),
                                DateUtil.format(b.getCheckOut())
                        });
                    }
                    fillSimple(checkInModel, data.checkIns, true);
                    fillSimple(checkOutModel, data.checkOuts, false);
                    activeOverlay.updateVisibility();
                    checkInOverlay.updateVisibility();
                    checkOutOverlay.updateVisibility();
                    pageHeader.setSubtitle("Live property status · " + DateUtil.format(LocalDate.now())
                            + " · Auto-refreshes every 60s");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Dashboard failed to load: "
                            + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    updateStats(new DashboardStats());
                }
            }
        }.execute();
    }

    private void updateStats(DashboardStats stats) {
        int inUse = stats.getOccupiedRooms() + stats.getReservedRooms();
        totalRoomsCard.setValue(String.valueOf(stats.getTotalRooms()));
        availableCard.setValue(String.valueOf(stats.getAvailableRooms()));
        occupiedCard.setValue(String.valueOf(inUse));
        occupiedCard.setHint("Booked + Reserved");
        occupiedCard.setProgress(stats.getTotalRooms() > 0 ? (double) inUse / stats.getTotalRooms() : 0);
        reservedCard.setValue(String.valueOf(stats.getReservedRooms()));
        todayBookingsCard.setValue(String.valueOf(stats.getTodayBookings()));
        todayRevenueCard.setValue(CurrencyUtil.format(
                stats.getTodayRevenue() == null ? java.math.BigDecimal.ZERO : stats.getTodayRevenue()));
        customersCard.setValue(String.valueOf(stats.getTotalCustomers()));
        customersCard.setHint(stats.getVipCustomers() + " VIP");
        occupancyCard.setValue(String.format("%.1f%%", stats.getOccupancyRate()));
        occupancyCard.setProgress(stats.getOccupancyRate() / 100.0);
        occupancyCard.setHint("Based on rooms currently in use");
    }

    private void fillActive(List<Booking> bookings) {
        activeModel.setRowCount(0);
        for (Booking b : bookings) {
            activeModel.addRow(new Object[]{
                    b.getCustomerName(),
                    b.getRoomNumber(),
                    b.getBookingStatus().getLabel(),
                    DateUtil.format(b.getCheckOut())
            });
        }
    }

    private void fillSimple(DefaultTableModel model, List<Booking> bookings, boolean checkIn) {
        model.setRowCount(0);
        for (Booking b : bookings) {
            model.addRow(new Object[]{
                    b.getCustomerName(),
                    b.getRoomNumber(),
                    DateUtil.format(checkIn ? b.getCheckIn() : b.getCheckOut())
            });
        }
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        totalRoomsCard.applyTheme();
        availableCard.applyTheme();
        occupiedCard.applyTheme();
        reservedCard.applyTheme();
        todayBookingsCard.applyTheme();
        todayRevenueCard.applyTheme();
        customersCard.applyTheme();
        occupancyCard.applyTheme();
        activeTable.applyTheme();
        checkInTable.applyTheme();
        checkOutTable.applyTheme();
        repaint();
    }

    private record DashboardData(DashboardStats stats, List<Booking> active,
                                 List<Booking> checkIns, List<Booking> checkOuts) {
    }
}
