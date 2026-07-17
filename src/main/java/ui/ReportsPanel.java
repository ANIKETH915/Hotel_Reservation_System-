package ui;

import components.CardPanel;
import components.StyledButton;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import service.BackupService;
import service.ImportExportService;
import service.ReportDataService;
import utils.CurrencyUtil;

public class ReportsPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final ReportDataService reportService = new ReportDataService();
    private final ImportExportService importExportService = new ImportExportService();
    private final BackupService backupService = new BackupService();
    private final java.awt.Window owner;

    private final JTextArea reportArea = new JTextArea();

    public ReportsPanel(java.awt.Window owner) {
        this.owner = owner;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(Theme.fontBold(22));
        title.setForeground(Theme.textPrimary());

        JPanel reportButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        reportButtons.setOpaque(false);
        reportButtons.add(btn("Daily Revenue", this::showDailyReport));
        reportButtons.add(btn("Monthly Revenue", this::showMonthlyReport));
        reportButtons.add(btn("Revenue Summary", this::showRevenueSummary));
        reportButtons.add(btn("Room Utilization", this::showUtilizationReport));
        reportButtons.add(btn("Customer Report", this::showCustomerReport));

        JPanel exportButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        exportButtons.setOpaque(false);
        exportButtons.add(btn("Export Rooms CSV", () -> exportCsv("rooms")));
        exportButtons.add(btn("Export Customers CSV", () -> exportCsv("customers")));
        exportButtons.add(btn("Export Bookings CSV", () -> exportCsv("bookings")));
        exportButtons.add(btn("Backup Database", this::backupDatabase, StyledButton.Style.GOLD));

        reportArea.setEditable(false);
        reportArea.setFont(Theme.fontRegular(13));
        reportArea.setForeground(Theme.textPrimary());
        reportArea.setBackground(Theme.bgCard());
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        CardPanel reportCard = new CardPanel(new BorderLayout());
        reportCard.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 8));
        controls.setOpaque(false);
        controls.add(reportButtons);
        controls.add(exportButtons);

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(controls, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(reportCard, BorderLayout.CENTER);
    }

    private StyledButton btn(String text, Runnable action) {
        return btn(text, action, StyledButton.Style.SECONDARY);
    }

    private StyledButton btn(String text, Runnable action, StyledButton.Style style) {
        StyledButton button = new StyledButton(text, style);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void showDailyReport() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                var revenue = reportService.dailyRevenue(today);
                return "DAILY REVENUE REPORT\n"
                        + "Date: " + today + "\n"
                        + "Revenue: " + CurrencyUtil.format(revenue) + "\n";
            }

            @Override
            protected void done() {
                try {
                    reportArea.setText(get());
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showMonthlyReport() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                YearMonth month = YearMonth.now();
                var revenue = reportService.monthlyRevenue(month);
                return "MONTHLY REVENUE REPORT\n"
                        + "Month: " + month + "\n"
                        + "Revenue: " + CurrencyUtil.format(revenue) + "\n";
            }

            @Override
            protected void done() {
                try {
                    reportArea.setText(get());
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showRevenueSummary() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                YearMonth month = YearMonth.now();
                var daily = reportService.dailyRevenue(today);
                var monthly = reportService.monthlyRevenue(month);
                return "REVENUE SUMMARY\n\n"
                        + "Today (" + today + "): " + CurrencyUtil.format(daily) + "\n"
                        + "This Month (" + month + "): " + CurrencyUtil.format(monthly) + "\n"
                        + "Total Rooms: " + reportService.totalRoomCount() + "\n";
            }

            @Override
            protected void done() {
                try {
                    reportArea.setText(get());
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showUtilizationReport() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                LocalDate end = LocalDate.now();
                LocalDate start = end.minusDays(29);
                Map<String, Double> util = reportService.roomUtilization(start, end);
                StringBuilder sb = new StringBuilder();
                sb.append("ROOM UTILIZATION (Last 30 Days)\n");
                sb.append("Period: ").append(start).append(" to ").append(end).append("\n\n");
                for (Map.Entry<String, Double> e : util.entrySet()) {
                    sb.append(String.format("Room %-8s %5.1f%%%n", e.getKey(), e.getValue()));
                }
                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    reportArea.setText(get());
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showCustomerReport() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Map<String, Object>> rows = reportService.customerReport();
                StringBuilder sb = new StringBuilder();
                sb.append("CUSTOMER REPORT\n\n");
                sb.append(String.format("%-24s %-10s %-12s %s%n", "Name", "Bookings", "Total Spent", "VIP"));
                sb.append("-".repeat(70)).append('\n');
                for (Map<String, Object> row : rows) {
                    sb.append(String.format("%-24s %-10d %-12s %s%n",
                            row.get("fullName"),
                            row.get("bookingCount"),
                            CurrencyUtil.format((java.math.BigDecimal) row.get("totalSpent")),
                            Boolean.TRUE.equals(row.get("vip")) ? "Yes" : "No"));
                }
                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    reportArea.setText(get());
                } catch (Exception ex) {
                    Toast.error(owner, "Report failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void exportCsv(String type) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(type + "_export.csv"));
        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
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
                } catch (Exception ex) {
                    Toast.error(owner, "Export failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void backupDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("hotel_backup.sql"));
        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
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
                    reportArea.setText("Database backup saved to:\n" + path.toAbsolutePath());
                } catch (Exception ex) {
                    Toast.error(owner, "Backup failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        reportArea.setText("Select a report type above to view analytics.");
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        reportArea.setForeground(Theme.textPrimary());
        reportArea.setBackground(Theme.bgCard());
        repaint();
    }
}
