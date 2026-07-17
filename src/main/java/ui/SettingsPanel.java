package ui;

import components.AppEvents;
import components.CardPanel;
import components.ModernTextField;
import components.PageHeader;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.ThemeManager;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import service.BackupService;
import service.ImportExportService;
import service.SettingsService;

public class SettingsPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final SettingsService settingsService = new SettingsService();
    private final ImportExportService importExportService = new ImportExportService();
    private final BackupService backupService = new BackupService();
    private final MainFrame mainFrame;

    private final ModernTextField hotelNameField = new ModernTextField(24);
    private final ModernTextField taxRateField = new ModernTextField(8);
    private final StyledComboBox<String> currencyCombo = new StyledComboBox<>(new String[]{"INR", "USD", "EUR"});
    private PageHeader pageHeader;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Settings", "Hotel branding, tax, currency, and data tools");

        CardPanel generalCard = new CardPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);

        addSettingRow(generalCard, gbc, 0, "Hotel Name", hotelNameField);
        addSettingRow(generalCard, gbc, 1, "Tax Rate (%)", taxRateField);
        addSettingRow(generalCard, gbc, 2, "Currency", currencyCombo);

        StyledButton saveBtn = new StyledButton("Save Settings");
        gbc.gridy = 6;
        gbc.insets = new java.awt.Insets(4, 0, 8, 0);
        generalCard.add(saveBtn, gbc);
        saveBtn.addActionListener(e -> saveSettings());

        StyledButton themeBtn = new StyledButton("Toggle Theme", StyledButton.Style.SECONDARY);
        gbc.gridy = 7;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        generalCard.add(themeBtn, gbc);
        themeBtn.addActionListener(e -> ThemeManager.toggle());

        CardPanel dataCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        dataCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        StyledButton importRooms = new StyledButton("Import Rooms CSV", StyledButton.Style.SECONDARY);
        StyledButton importCustomers = new StyledButton("Import Customers CSV", StyledButton.Style.SECONDARY);
        StyledButton exportRooms = new StyledButton("Export Rooms CSV", StyledButton.Style.GHOST);
        StyledButton exportCustomers = new StyledButton("Export Customers CSV", StyledButton.Style.GHOST);
        StyledButton backupBtn = new StyledButton("Backup Database", StyledButton.Style.GOLD);

        dataCard.add(importRooms);
        dataCard.add(importCustomers);
        dataCard.add(exportRooms);
        dataCard.add(exportCustomers);
        dataCard.add(backupBtn);

        importRooms.addActionListener(e -> importCsv("rooms"));
        importCustomers.addActionListener(e -> importCsv("customers"));
        exportRooms.addActionListener(e -> exportCsv("rooms"));
        exportCustomers.addActionListener(e -> exportCsv("customers"));
        backupBtn.addActionListener(e -> backupDb());

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(pageHeader);
        content.add(generalCard);
        content.add(javax.swing.Box.createVerticalStrut(16));
        content.add(dataCard);

        JPanel contentHost = new JPanel(new BorderLayout());
        contentHost.setOpaque(false);
        contentHost.add(content, BorderLayout.NORTH);
        add(contentHost, BorderLayout.CENTER);
    }

    private void addSettingRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridy = row * 2;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        panel.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);
        panel.add(field, gbc);
    }

    private void saveSettings() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                settingsService.setSetting("hotel_name", hotelNameField.getText().trim());
                settingsService.setSetting("tax_rate", taxRateField.getText().trim());
                settingsService.setSetting("currency", (String) currencyCombo.getSelectedItem());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Settings saved");
                    mainFrame.notifyDataChanged(AppEvents.Domain.SETTINGS);
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Save failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void importCsv(String type) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return switch (type) {
                    case "rooms" -> importExportService.importRooms(path);
                    case "customers" -> importExportService.importCustomers(path);
                    default -> throw new IllegalArgumentException("Unknown type");
                };
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    Toast.success(mainFrame, "Imported " + count + " record(s)");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Import failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void exportCsv(String type) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(type + "_export.csv"));
        if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if ("rooms".equals(type)) {
                    importExportService.exportRooms(path);
                } else {
                    importExportService.exportCustomers(path);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Export complete");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Export failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void backupDb() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("hotel_backup.sql"));
        if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
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
                    Toast.success(mainFrame, "Backup saved");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Backup failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        new SwingWorker<SettingsData, Void>() {
            @Override
            protected SettingsData doInBackground() throws Exception {
                return new SettingsData(
                        settingsService.getHotelName(),
                        settingsService.getTaxRate(),
                        settingsService.getCurrency()
                );
            }

            @Override
            protected void done() {
                try {
                    SettingsData data = get();
                    hotelNameField.setText(data.hotelName);
                    taxRateField.setText(data.taxRate.stripTrailingZeros().toPlainString());
                    currencyCombo.setSelectedItem(data.currency);
                } catch (Exception ignored) {
                    // keep fields
                }
            }
        }.execute();
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        hotelNameField.applyTheme();
        taxRateField.applyTheme();
        repaint();
    }

    private record SettingsData(String hotelName, BigDecimal taxRate, String currency) {
    }
}
