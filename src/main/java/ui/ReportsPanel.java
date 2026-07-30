package ui;

import components.CardPanel;
import components.EmptyStatePanel;
import components.IconActionButton;
import components.ModernTable;
import components.PageHeader;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import model.DashboardStats;
import service.BackupService;
import service.DashboardService;
import service.ImportExportService;
import service.ReportDataService;
import utils.CurrencyUtil;

public class ReportsPanel extends JPanel implements MainFrame.RefreshablePanel {

    private static final int OUTER = 4;
    private static final int CARD_GAP = 16;
    private static final int BTN_GAP = 12;
    private static final int SECTION_GAP = 20;

    private final ReportDataService reportService = new ReportDataService();
    private final DashboardService dashboardService = new DashboardService();
    private final ImportExportService importExportService = new ImportExportService();
    private final BackupService backupService = new BackupService();
    private final java.awt.Window owner;

    private PageHeader pageHeader;
    private JPanel statsGrid;
    private JPanel actionsGrid;
    private MetricCard todayRevenueCard;
    private MetricCard monthlyRevenueCard;
    private MetricCard bookingsCard;
    private MetricCard customersCard;
    private MetricCard occupancyCard;

    private final CardLayout viewerCards = new CardLayout();
    private final JPanel viewerBody = new JPanel(viewerCards);
    private JLabel viewerTitleLabel;
    private JLabel viewerSubtitleLabel;
    private JPanel summaryPanel;
    private JScrollPane summaryScroll;
    private JScrollPane tableScroll;
    private ModernTable reportTable;
    private DefaultTableModel tableModel;
    private EmptyStatePanel emptyState;

    private final List<IconActionButton> reportActionButtons = new ArrayList<>();
    private IconActionButton activeReportButton;

    // Hover row tracking
    private int hoveredRow = -1;

    public ReportsPanel(java.awt.Window owner) {
        this.owner = owner;
        setLayout(new BorderLayout(0, SECTION_GAP));
        setBackground(Theme.bgPrimary());
        setBorder(new EmptyBorder(OUTER, OUTER, OUTER, OUTER));
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Reports & Analytics",
                "Generate business insights and export data");

        statsGrid = new JPanel(new GridLayout(1, 5, CARD_GAP, CARD_GAP));
        statsGrid.setOpaque(false);
        todayRevenueCard = new MetricCard("Today's Revenue", CurrencyUtil.format(BigDecimal.ZERO),
                "revenue", Theme.EMERALD);
        monthlyRevenueCard = new MetricCard("Monthly Revenue", CurrencyUtil.format(BigDecimal.ZERO),
                "monthly", Theme.GOLD);
        bookingsCard = new MetricCard("Total Bookings", "0", "bookings", Theme.ROYAL_BLUE);
        customersCard = new MetricCard("Total Customers", "0", "customers", Theme.ROYAL_BLUE);
        occupancyCard = new MetricCard("Occupancy", "0.0%", "utilization", Theme.GOLD);
        statsGrid.add(todayRevenueCard);
        statsGrid.add(monthlyRevenueCard);
        statsGrid.add(bookingsCard);
        statsGrid.add(customersCard);
        statsGrid.add(occupancyCard);

        actionsGrid = new JPanel(new GridLayout(1, 4, CARD_GAP, CARD_GAP));
        actionsGrid.setOpaque(false);
        actionsGrid.add(actionSection("Revenue Reports",
                reportBtn("Daily Revenue", "daily", this::showDailyReport),
                reportBtn("Monthly Revenue", "monthly", this::showMonthlyReport),
                reportBtn("Revenue Summary", "summary", this::showRevenueSummary)));
        actionsGrid.add(actionSection("Hotel Reports",
                reportBtn("Room Utilization", "utilization", this::showUtilizationReport),
                reportBtn("Customer Report", "customers", this::showCustomerReport)));
        actionsGrid.add(actionSection("Export",
                actionBtn("Export Rooms CSV", "export", IconActionButton.Tone.NEUTRAL, () -> exportCsv("rooms")),
                actionBtn("Export Guests CSV", "export", IconActionButton.Tone.NEUTRAL, () -> exportCsv("customers")),
                actionBtn("Export Bookings CSV", "export", IconActionButton.Tone.NEUTRAL, () -> exportCsv("bookings"))));
        actionsGrid.add(actionSection("Database",
                actionBtn("Backup Database", "backup", IconActionButton.Tone.GOLD, this::backupDatabase)));

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(UiLayout.fullWidth(pageHeader));
        north.add(Box.createVerticalStrut(CARD_GAP));
        north.add(UiLayout.fullWidth(statsGrid));
        north.add(Box.createVerticalStrut(SECTION_GAP));
        north.add(UiLayout.fullWidth(actionsGrid));

        add(north, BorderLayout.NORTH);
        add(buildViewerCard(), BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyResponsiveLayout();
            }
        });
    }

    private CardPanel buildViewerCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, CARD_GAP));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setArc(14);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        viewerTitleLabel = new JLabel("Report Viewer");
        viewerTitleLabel.setFont(Theme.fontMedium(15));
        viewerTitleLabel.setForeground(Theme.textPrimary());
        viewerSubtitleLabel = new JLabel("Select a report to generate analytics");
        viewerSubtitleLabel.setFont(Theme.fontRegular(12));
        viewerSubtitleLabel.setForeground(Theme.textSecondary());
        header.add(viewerTitleLabel, BorderLayout.NORTH);
        header.add(viewerSubtitleLabel, BorderLayout.SOUTH);

        emptyState = new EmptyStatePanel("No report selected",
                "Click any report above to generate analytics.");
        emptyState.setIconKey("reports");

        summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        summaryScroll = new JScrollPane(summaryPanel);
        UiLayout.configureScrollPane(summaryScroll);
        summaryScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        summaryScroll.getViewport().setBackground(Theme.bgCard());

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new ModernTable(tableModel);
        reportTable.setRowHeight(44);

        // Hover row listeners
        reportTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = reportTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    reportTable.repaint();
                }
            }
        });
        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                reportTable.repaint();
            }
        });

        // Table Header rounded renderer
        reportTable.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
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

        // Default cell renderer
        reportTable.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            private final JLabel label = new JLabel();
            {
                label.setOpaque(true);
                label.setFont(Theme.fontRegular(13));
                label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());
                label.setHorizontalAlignment(SwingConstants.LEFT);

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

        tableScroll = UiLayout.tableScroll(reportTable);
        tableScroll.getViewport().setBackground(Theme.bgCard());

        viewerBody.setOpaque(false);
        viewerBody.add(emptyState, "empty");
        viewerBody.add(summaryScroll, "summary");
        viewerBody.add(tableScroll, "table");
        viewerCards.show(viewerBody, "empty");

        card.add(header, BorderLayout.NORTH);
        card.add(viewerBody, BorderLayout.CENTER);
        return card;
    }

    private CardPanel actionSection(String title, IconActionButton... buttons) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, BTN_GAP));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setArc(14);

        JLabel label = new JLabel(title);
        label.setFont(Theme.fontMedium(13));
        label.setForeground(Theme.textPrimary());
        card.add(label, BorderLayout.NORTH);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            IconActionButton button = buttons[i];
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            stack.add(button);
            if (i < buttons.length - 1) {
                stack.add(Box.createVerticalStrut(BTN_GAP));
            }
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(stack, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private IconActionButton reportBtn(String text, String icon, Runnable action) {
        IconActionButton button = new IconActionButton(text, icon, IconActionButton.Tone.NEUTRAL);
        reportActionButtons.add(button);
        button.addActionListener(e -> {
            setActiveReportButton(button);
            action.run();
        });
        return button;
    }

    private IconActionButton actionBtn(String text, String icon, IconActionButton.Tone tone, Runnable action) {
        IconActionButton button = new IconActionButton(text, icon, tone);
        button.addActionListener(e -> {
            clearActiveReportButton();
            action.run();
        });
        return button;
    }

    private void setActiveReportButton(IconActionButton button) {
        for (IconActionButton b : reportActionButtons) {
            b.setActive(b == button);
        }
        activeReportButton = button;
    }

    private void clearActiveReportButton() {
        for (IconActionButton b : reportActionButtons) {
            b.setActive(false);
        }
        activeReportButton = null;
    }

    private void applyResponsiveLayout() {
        int w = Math.max(0, getWidth());
        int statCols = w < 720 ? 1 : w < 980 ? 2 : w < 1200 ? 3 : 5;
        GridLayout statsLayout = (GridLayout) statsGrid.getLayout();
        if (statsLayout.getColumns() != statCols) {
            statsGrid.setLayout(new GridLayout(0, statCols, CARD_GAP, CARD_GAP));
            statsGrid.revalidate();
        }

        int actionCols = w < 780 ? 1 : w < 1100 ? 2 : 4;
        GridLayout actionsLayout = (GridLayout) actionsGrid.getLayout();
        if (actionsLayout.getColumns() != actionCols) {
            actionsGrid.setLayout(new GridLayout(0, actionCols, CARD_GAP, CARD_GAP));
            actionsGrid.revalidate();
        }
        UiLayout.refreshFullWidth(statsGrid);
        UiLayout.refreshFullWidth(actionsGrid);
    }

    private void showEmptyViewer() {
        viewerTitleLabel.setText("Report Viewer");
        viewerSubtitleLabel.setText("Select a report to generate analytics");
        viewerCards.show(viewerBody, "empty");
    }

    private void showSummary(String title, String subtitle, List<SummaryRow> rows) {
        viewerTitleLabel.setText(title);
        viewerSubtitleLabel.setText(subtitle == null ? " " : subtitle);
        summaryPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;
        for (SummaryRow item : rows) {
            gbc.gridy = row++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel key = new JLabel(item.label());
            key.setFont(Theme.fontMedium(12));
            key.setForeground(Theme.textSecondary());
            summaryPanel.add(key, gbc);

            gbc.gridy = row++;
            gbc.insets = new Insets(0, 0, 16, 0);
            JLabel value = new JLabel(item.value());
            value.setFont(item.emphasize() ? Theme.fontBold(22) : Theme.fontMedium(15));
            value.setForeground(Theme.textPrimary());
            summaryPanel.add(value, gbc);
        }

        gbc.gridy = row;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        summaryPanel.add(spacer, gbc);

        summaryPanel.revalidate();
        summaryPanel.repaint();
        summaryScroll.getViewport().setBackground(Theme.bgCard());
        viewerCards.show(viewerBody, "summary");
        summaryScroll.getVerticalScrollBar().setValue(0);
    }

    private void showTable(String title, String subtitle, String[] columns, List<Object[]> rows,
                           int... centerColumns) {
        viewerTitleLabel.setText(title);
        viewerSubtitleLabel.setText(subtitle == null ? " " : subtitle);
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        for (Object[] r : rows) {
            tableModel.addRow(r);
        }

        TableColumnModel columnsModel = reportTable.getColumnModel();
        for (int col : centerColumns) {
            if (col >= 0 && col < columnsModel.getColumnCount()) {
                columnsModel.getColumn(col).setCellRenderer(new javax.swing.table.TableCellRenderer() {
                    private final JLabel cellLabel = new JLabel();
                    {
                        cellLabel.setOpaque(true);
                        cellLabel.setFont(Theme.fontRegular(13));
                        cellLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
                        cellLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object value,
                                                                   boolean isSelected, boolean hasFocus,
                                                                   int row, int column) {
                        cellLabel.setText(value == null ? "" : value.toString());
                        if (isSelected) {
                            cellLabel.setBackground(Theme.ROYAL_BLUE);
                            cellLabel.setForeground(Color.WHITE);
                        } else if (row == hoveredRow) {
                            cellLabel.setBackground(Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9));
                            cellLabel.setForeground(Theme.textPrimary());
                        } else {
                            cellLabel.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                            cellLabel.setForeground(Theme.textPrimary());
                        }
                        return cellLabel;
                    }
                });
            }
        }

        if (columnsModel.getColumnCount() >= 2) {
            columnsModel.getColumn(0).setPreferredWidth(160);
            if (columnsModel.getColumnCount() == 2) {
                columnsModel.getColumn(1).setPreferredWidth(120);
            }
        }
        reportTable.applyTheme();
        viewerCards.show(viewerBody, "table");
        tableScroll.getVerticalScrollBar().setValue(0);
    }

    private void showDailyReport() {
        new SwingWorker<BigDecimal, Void>() {
            private final LocalDate today = LocalDate.now();

            @Override
            protected BigDecimal doInBackground() throws Exception {
                return reportService.dailyRevenue(today);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal revenue = get();
                    showSummary("Daily Revenue", "Date: " + today, List.of(
                            new SummaryRow("Report", "Daily Revenue Report", false),
                            new SummaryRow("Date", today.toString(), false),
                            new SummaryRow("Revenue", CurrencyUtil.format(revenue), true)
                    ));
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showMonthlyReport() {
        new SwingWorker<BigDecimal, Void>() {
            private final YearMonth month = YearMonth.now();

            @Override
            protected BigDecimal doInBackground() throws Exception {
                return reportService.monthlyRevenue(month);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal revenue = get();
                    showSummary("Monthly Revenue", "Month: " + month, List.of(
                            new SummaryRow("Report", "Monthly Revenue Report", false),
                            new SummaryRow("Month", month.toString(), false),
                            new SummaryRow("Revenue", CurrencyUtil.format(revenue), true)
                    ));
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showRevenueSummary() {
        new SwingWorker<RevenueSummaryData, Void>() {
            @Override
            protected RevenueSummaryData doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                YearMonth month = YearMonth.now();
                return new RevenueSummaryData(
                        today,
                        month,
                        reportService.dailyRevenue(today),
                        reportService.monthlyRevenue(month),
                        reportService.totalRoomCount()
                );
            }

            @Override
            protected void done() {
                try {
                    RevenueSummaryData data = get();
                    showSummary("Revenue Summary", "Snapshot for today and this month", List.of(
                            new SummaryRow("Today (" + data.today + ")", CurrencyUtil.format(data.daily), true),
                            new SummaryRow("This Month (" + data.month + ")", CurrencyUtil.format(data.monthly), true),
                            new SummaryRow("Total Rooms", String.valueOf(data.totalRooms), false)
                    ));
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showUtilizationReport() {
        new SwingWorker<Map<String, Double>, Void>() {
            private final LocalDate end = LocalDate.now();
            private final LocalDate start = end.minusDays(29);

            @Override
            protected Map<String, Double> doInBackground() throws Exception {
                return reportService.roomUtilization(start, end);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Double> util = get();
                    List<Object[]> rows = new ArrayList<>();
                    for (Map.Entry<String, Double> e : util.entrySet()) {
                        rows.add(new Object[]{e.getKey(), String.format("%.1f%%", e.getValue())});
                    }
                    showTable("Room Utilization",
                            "Last 30 days · " + start + " to " + end,
                            new String[]{"Room", "Utilization"},
                            rows,
                            1);
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showCustomerReport() {
        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return reportService.customerReport();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> data = get();
                    List<Object[]> rows = new ArrayList<>();
                    for (Map.Entry<String, Object> e : data.get(0).entrySet()) {
                    }
                    for (Map<String, Object> row : data) {
                        rows.add(new Object[]{
                                row.get("fullName"),
                                row.get("bookingCount"),
                                CurrencyUtil.format((BigDecimal) row.get("totalSpent")),
                                Boolean.TRUE.equals(row.get("vip")) ? "Yes" : "No"
                        });
                    }
                    showTable("Customer Report",
                            data.size() + " guests · sorted by booking activity",
                            new String[]{"Name", "Bookings", "Total Spent", "VIP"},
                            rows,
                            1, 3);
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void exportCsv(String type) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File(type + "_export.csv"));
        if (chooser.showSaveDialog(owner) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                switch (type) {
                    case "rooms" -> importExportService.exportRooms(path);
                    case "customers" -> importExportService.exportCustomers(path);
                    case "bookings" -> importExportService.exportBookings(path);
                    default -> throw new IllegalArgumentException("Unknown export type");
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(owner, "Exported to " + path.getFileName());
                    showSummary("Export Complete", path.getFileName().toString(), List.of(
                            new SummaryRow("Export Type", type.substring(0, 1).toUpperCase() + type.substring(1), false),
                            new SummaryRow("Saved To", path.toAbsolutePath().toString(), false)
                    ));
                } catch (Exception ex) {
                    Toast.error(owner, "Export failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void backupDatabase() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File("hotel_backup.sql"));
        if (chooser.showSaveDialog(owner) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                backupService.backupToFile(path);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(owner, "Backup saved successfully");
                    showSummary("Database Backup", "Backup completed successfully", List.of(
                            new SummaryRow("Status", "Success", false),
                            new SummaryRow("Saved To", path.toAbsolutePath().toString(), false)
                    ));
                } catch (Exception ex) {
                    Toast.error(owner, "Backup failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        showEmptyViewer();
        clearActiveReportButton();
        new SwingWorker<AnalyticsSnapshot, Void>() {
            @Override
            protected AnalyticsSnapshot doInBackground() throws Exception {
                DashboardStats stats = dashboardService.loadStats();
                int totalBookings = reportService.listAllBookings().size();
                return new AnalyticsSnapshot(stats, totalBookings);
            }

            @Override
            protected void done() {
                try {
                    AnalyticsSnapshot snap = get();
                    DashboardStats s = snap.stats();
                    todayRevenueCard.setValue(CurrencyUtil.format(
                            s.getTodayRevenue() == null ? BigDecimal.ZERO : s.getTodayRevenue()));
                    monthlyRevenueCard.setValue(CurrencyUtil.format(
                            s.getMonthRevenue() == null ? BigDecimal.ZERO : s.getMonthRevenue()));
                    bookingsCard.setValue(String.valueOf(snap.totalBookings()));
                    customersCard.setValue(String.valueOf(s.getTotalCustomers()));
                    occupancyCard.setValue(String.format("%.1f%%", s.getOccupancyRate()));
                } catch (Exception ex) {
                }
            }
        }.execute();
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        todayRevenueCard.applyTheme();
        monthlyRevenueCard.applyTheme();
        bookingsCard.applyTheme();
        customersCard.applyTheme();
        occupancyCard.applyTheme();
        viewerTitleLabel.setForeground(Theme.textPrimary());
        viewerSubtitleLabel.setForeground(Theme.textSecondary());
        emptyState.applyTheme();
        reportTable.applyTheme();
        if (summaryScroll != null) {
            summaryScroll.getViewport().setBackground(Theme.bgCard());
        }
        if (tableScroll != null) {
            tableScroll.getViewport().setBackground(Theme.bgCard());
        }
        for (IconActionButton button : reportActionButtons) {
            button.repaint();
        }
        repaint();
    }

    private record SummaryRow(String label, String value, boolean emphasize) {
    }

    private record RevenueSummaryData(LocalDate today, YearMonth month,
                                       BigDecimal daily, BigDecimal monthly, int totalRooms) {
    }

    private record AnalyticsSnapshot(DashboardStats stats, int totalBookings) {
    }

    // Custom Component: MetricCard (Local premium StatCard implementation)
    private static class MetricCard extends JPanel {
        private final JLabel valueLabel;
        private final JLabel titleLabel;
        private final String iconKey;
        private final Color accentColor;

        public MetricCard(String title, String value, String iconKey, Color accentColor) {
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

            valueLabel = new JLabel(value);
            valueLabel.setFont(Theme.fontBold(18));
            valueLabel.setForeground(Theme.textPrimary());

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(valueLabel);

            add(iconPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        private void drawCustomIcon(Graphics2D g2, String iconKey, Color accentColor) {
            switch (iconKey) {
                case "revenue", "daily" -> {
                    g2.drawRoundRect(8, 10, 20, 15, 3, 3);
                    g2.drawLine(8, 14, 28, 14);
                    g2.drawLine(12, 19, 16, 19);
                }
                case "monthly" -> {
                    g2.drawRoundRect(8, 10, 20, 18, 4, 4);
                    g2.drawLine(8, 16, 28, 16);
                    g2.drawLine(13, 7, 13, 11);
                    g2.drawLine(23, 7, 23, 11);
                }
                case "bookings" -> {
                    g2.drawRoundRect(8, 10, 20, 18, 4, 4);
                    g2.drawLine(8, 16, 28, 16);
                    g2.drawLine(13, 7, 13, 11);
                    g2.drawLine(23, 7, 23, 11);
                }
                case "customers" -> {
                    g2.drawOval(14, 10, 8, 8);
                    g2.drawArc(10, 19, 16, 10, 0, 180);
                }
                case "utilization" -> {
                    g2.drawOval(8, 8, 20, 20);
                    g2.fillArc(10, 10, 16, 16, 90, -200);
                }
                default -> {
                    g2.drawRect(8, 8, 20, 20);
                }
            }
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }

        public void applyTheme() {
            updateTheme();
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
}
