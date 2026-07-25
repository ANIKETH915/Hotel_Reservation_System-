package ui;

import components.AppEvents;
import components.CardPanel;
import components.IconActionButton;
import components.ModernTextField;
import components.NavIcons;
import components.PageHeader;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.ThemeManager;
import components.ThemeOptionCard;
import components.Toast;
import components.UiLayout;
import database.DatabaseConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import model.DashboardStats;
import service.BackupService;
import service.DashboardService;
import service.ImportExportService;
import service.ReportDataService;
import service.SettingsService;

/**
 * Enterprise settings dashboard — UI redesign only.
 * Persist/load uses existing SettingsService.setSetting / get APIs.
 */
public class SettingsPanel extends JPanel implements MainFrame.RefreshablePanel {

    private static final int CARD_PAD = 24;
    private static final int CARD_GAP = 16;
    private static final int SECTION_GAP = 20;
    private static final int FIELD_H = 40;
    private static final DateTimeFormatter BACKUP_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final SettingsService settingsService = new SettingsService();
    private final ImportExportService importExportService = new ImportExportService();
    private final BackupService backupService = new BackupService();
    private final DashboardService dashboardService = new DashboardService();
    private final ReportDataService reportDataService = new ReportDataService();
    private final MainFrame mainFrame;

    private final ModernTextField hotelNameField = new ModernTextField(24);
    private final ModernTextField addressField = new ModernTextField(24);
    private final ModernTextField phoneField = new ModernTextField(16);
    private final ModernTextField emailField = new ModernTextField(24);
    private final ModernTextField taxRateField = new ModernTextField(8);
    private final StyledComboBox<String> currencyCombo =
            new StyledComboBox<>(new String[]{"INR", "USD", "EUR"});
    private final StyledComboBox<String> timezoneCombo = new StyledComboBox<>(new String[]{
            "Asia/Kolkata", "UTC", "Asia/Dubai", "Europe/London", "America/New_York"
    });
    private final StyledComboBox<String> languageCombo =
            new StyledComboBox<>(new String[]{"English", "Hindi"});

    private ThemeOptionCard lightThemeCard;
    private ThemeOptionCard darkThemeCard;

    private PageHeader pageHeader;
    private JScrollPane pageScroll;
    private JPanel columnsRow;
    private final List<InfoRow> infoRows = new ArrayList<>();
    private final List<JLabel> sectionTitles = new ArrayList<>();

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.bgPrimary());
        setBorder(new EmptyBorder(4, 4, 4, 4));
        buildUi();
        ThemeManager.addListener(dark -> syncThemeCards());
    }

    private void buildUi() {
        pageHeader = new PageHeader("Settings",
                "Manage hotel configuration, appearance, localization, and system preferences.");

        styleField(hotelNameField);
        styleField(addressField);
        styleField(phoneField);
        styleField(emailField);
        styleField(taxRateField);
        styleField(currencyCombo);
        styleField(timezoneCombo);
        styleField(languageCombo);

        CardPanel configCard = buildConfigurationCard();
        CardPanel infoCard = buildSystemInfoCard();
        CardPanel dataCard = buildDataToolsCard();

        columnsRow = new JPanel(new GridLayout(1, 2, CARD_GAP, CARD_GAP));
        columnsRow.setOpaque(false);
        columnsRow.add(configCard);
        columnsRow.add(infoCard);

        UiLayout.ViewportWidthPanel content = new UiLayout.ViewportWidthPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, SECTION_GAP, 0));
        content.add(UiLayout.fullWidth(pageHeader));
        content.add(Box.createVerticalStrut(CARD_GAP));
        content.add(UiLayout.fullWidth(columnsRow));
        content.add(Box.createVerticalStrut(SECTION_GAP));
        content.add(UiLayout.fullWidth(dataCard));

        pageScroll = UiLayout.pageScroll(content);
        add(pageScroll, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cols = getWidth() < 960 ? 1 : 2;
                GridLayout layout = (GridLayout) columnsRow.getLayout();
                if (layout.getColumns() != cols) {
                    columnsRow.setLayout(new GridLayout(cols == 1 ? 2 : 1, cols, CARD_GAP, CARD_GAP));
                    columnsRow.revalidate();
                }
                UiLayout.refreshFullWidth(columnsRow);
            }
        });
    }

    private CardPanel buildConfigurationCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));
        card.setArc(14);

        card.add(cardTitle("Hotel Configuration", "rooms"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addSettingRow(form, gbc, row++, "Hotel Name", hotelNameField);
        addSettingRow(form, gbc, row++, "Hotel Address", addressField);
        addSettingRow(form, gbc, row++, "Contact Number", phoneField);
        addSettingRow(form, gbc, row++, "Email Address", emailField);
        addSettingRow(form, gbc, row++, "Tax Rate (%)", taxRateField);
        addSettingRow(form, gbc, row++, "Currency", currencyCombo);
        addSettingRow(form, gbc, row++, "Time Zone", timezoneCombo);
        addSettingRow(form, gbc, row++, "Language", languageCombo);

        gbc.gridy = row * 2;
        gbc.insets = new Insets(4, 0, 8, 0);
        JLabel themeLabel = fieldLabel("Theme Selection");
        form.add(themeLabel, gbc);

        JPanel themeRow = new JPanel(new GridLayout(1, 2, 12, 0));
        themeRow.setOpaque(false);
        lightThemeCard = new ThemeOptionCard(false, "Light Mode", "Bright workspace");
        darkThemeCard = new ThemeOptionCard(true, "Dark Mode", "Low-light friendly");
        lightThemeCard.setOnSelect(() -> ThemeManager.setDark(false));
        darkThemeCard.setOnSelect(() -> ThemeManager.setDark(true));
        themeRow.add(lightThemeCard);
        themeRow.add(darkThemeCard);
        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        form.add(themeRow, gbc);
        syncThemeCards();

        StyledButton saveBtn = new StyledButton("Save Settings", StyledButton.Style.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(0, 42));
        saveBtn.addActionListener(e -> saveSettings());
        gbc.gridy = row * 2 + 2;
        gbc.insets = new Insets(4, 0, 0, 0);
        form.add(saveBtn, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private CardPanel buildSystemInfoCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));
        card.setArc(14);

        card.add(cardTitle("System Information", "settings"), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        infoRows.clear();
        addInfo(list, "Hotel Name", "—", "rooms");
        addInfo(list, "Application Version", "1.0.0", "about");
        addInfo(list, "Database Status", "Checking…", "backup");
        addInfo(list, "Total Rooms", "0", "rooms");
        addInfo(list, "Total Customers", "0", "customers");
        addInfo(list, "Total Bookings", "0", "bookings");
        addInfo(list, "Total Staff", "0", "customers");
        addInfo(list, "Last Backup", "Never", "backup");
        addInfo(list, "Current Theme", Theme.isDark() ? "Dark" : "Light", "settings");
        addInfo(list, "Current Currency", "INR", "payments");

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(list, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private CardPanel buildDataToolsCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));
        card.setArc(14);

        card.add(cardTitle("Data Tools", "export"), BorderLayout.NORTH);

        JPanel groups = new JPanel(new GridLayout(1, 3, CARD_GAP, CARD_GAP));
        groups.setOpaque(false);
        groups.add(toolGroup("Import", "export",
                toolBtn("Import Rooms CSV", "rooms", IconActionButton.Tone.NEUTRAL, () -> importCsv("rooms")),
                toolBtn("Import Customers CSV", "customers", IconActionButton.Tone.NEUTRAL, () -> importCsv("customers"))));
        groups.add(toolGroup("Export", "export",
                toolBtn("Export Rooms CSV", "rooms", IconActionButton.Tone.NEUTRAL, () -> exportCsv("rooms")),
                toolBtn("Export Customers CSV", "customers", IconActionButton.Tone.NEUTRAL, () -> exportCsv("customers"))));
        groups.add(toolGroup("Backup", "backup",
                toolBtn("Backup Database", "backup", IconActionButton.Tone.GOLD, this::backupDb)));

        card.add(groups, BorderLayout.CENTER);

        card.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cols = card.getWidth() < 700 ? 1 : 3;
                GridLayout layout = (GridLayout) groups.getLayout();
                if (layout.getColumns() != cols) {
                    groups.setLayout(new GridLayout(cols == 1 ? 3 : 1, cols, CARD_GAP, CARD_GAP));
                    groups.revalidate();
                }
            }
        });
        return card;
    }

    private JPanel toolGroup(String title, String iconKey, IconActionButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        heading.setOpaque(false);
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.ROYAL_BLUE.getRed(), Theme.ROYAL_BLUE.getGreen(),
                        Theme.ROYAL_BLUE.getBlue(), 28));
                g2.fillRoundRect(0, 0, 26, 26, 8, 8);
                NavIcons.paint(g2, iconKey, 5, 5, 16, Theme.ROYAL_BLUE);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(26, 26);
            }
        };
        icon.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(Theme.fontMedium(13));
        label.setForeground(Theme.textPrimary());
        sectionTitles.add(label);
        heading.add(icon);
        heading.add(label);
        panel.add(heading, BorderLayout.NORTH);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            IconActionButton b = buttons[i];
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            stack.add(b);
            if (i < buttons.length - 1) {
                stack.add(Box.createVerticalStrut(12));
            }
        }
        panel.add(stack, BorderLayout.CENTER);
        return panel;
    }

    private IconActionButton toolBtn(String text, String icon, IconActionButton.Tone tone, Runnable action) {
        IconActionButton button = new IconActionButton(text, icon, tone);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel cardTitle(String text, String iconKey) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), 40));
                g2.fillRoundRect(0, 0, 28, 28, 8, 8);
                NavIcons.paint(g2, iconKey, 6, 6, 16, Theme.ROYAL_BLUE);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(28, 28);
            }
        };
        icon.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(Theme.fontBold(16));
        label.setForeground(Theme.textPrimary());
        sectionTitles.add(label);
        row.add(icon);
        row.add(label);
        return row;
    }

    private void addInfo(JPanel parent, String label, String value, String iconKey) {
        InfoRow row = new InfoRow(label, value, iconKey);
        infoRows.add(row);
        parent.add(row);
        parent.add(Box.createVerticalStrut(4));
    }

    private void setInfo(int index, String value) {
        if (index >= 0 && index < infoRows.size()) {
            infoRows.get(index).setValue(value);
        }
    }

    private void styleField(javax.swing.JComponent field) {
        field.setPreferredSize(new Dimension(10, FIELD_H));
        field.setMinimumSize(new Dimension(80, FIELD_H));
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        return lbl;
    }

    private void addSettingRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row * 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(fieldLabel(label), gbc);
        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(field, gbc);
    }

    private void syncThemeCards() {
        if (lightThemeCard != null) {
            lightThemeCard.setSelected(!Theme.isDark());
            darkThemeCard.setSelected(Theme.isDark());
        }
        setInfo(8, Theme.isDark() ? "Dark Mode" : "Light Mode");
    }

    private void saveSettings() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                settingsService.setSetting("hotel_name", hotelNameField.getText().trim());
                settingsService.setSetting("hotel_address", addressField.getText().trim());
                settingsService.setSetting("hotel_phone", phoneField.getText().trim());
                settingsService.setSetting("hotel_email", emailField.getText().trim());
                settingsService.setSetting("tax_rate", taxRateField.getText().trim());
                settingsService.setSetting("currency", String.valueOf(currencyCombo.getSelectedItem()));
                settingsService.setSetting("timezone", String.valueOf(timezoneCombo.getSelectedItem()));
                settingsService.setSetting("language", String.valueOf(languageCombo.getSelectedItem()));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Settings saved");
                    mainFrame.notifyDataChanged(AppEvents.Domain.SETTINGS);
                    refresh();
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
                settingsService.setSetting("last_backup_at", LocalDateTime.now().format(BACKUP_FMT));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Backup saved");
                    refresh();
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Backup failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        new SwingWorker<SettingsSnapshot, Void>() {
            @Override
            protected SettingsSnapshot doInBackground() throws Exception {
                DashboardStats stats = dashboardService.loadStats();
                int bookings = reportDataService.listAllBookings().size();
                int staff = countAdmins();
                boolean dbOk = false;
                try (Connection conn = DatabaseConnection.getConnection()) {
                    dbOk = conn != null && !conn.isClosed() && conn.isValid(2);
                } catch (Exception ignored) {
                    dbOk = false;
                }
                return new SettingsSnapshot(
                        nz(settingsService.getHotelName()),
                        nz(settingsService.get("hotel_address")),
                        nz(settingsService.get("hotel_phone")),
                        nz(settingsService.get("hotel_email")),
                        settingsService.getTaxRate(),
                        settingsService.getCurrency(),
                        nz(settingsService.get("timezone"), "Asia/Kolkata"),
                        nz(settingsService.get("language"), "English"),
                        nz(settingsService.get("last_backup_at"), "Never"),
                        stats,
                        bookings,
                        staff,
                        dbOk
                );
            }

            @Override
            protected void done() {
                try {
                    SettingsSnapshot data = get();
                    hotelNameField.setText(data.hotelName);
                    addressField.setText(data.address);
                    phoneField.setText(data.phone);
                    emailField.setText(data.email);
                    taxRateField.setText(data.taxRate.stripTrailingZeros().toPlainString());
                    currencyCombo.setSelectedItem(data.currency);
                    timezoneCombo.setSelectedItem(data.timezone);
                    languageCombo.setSelectedItem(data.language);
                    syncThemeCards();

                    setInfo(0, data.hotelName);
                    setInfo(1, "1.0.0");
                    setInfo(2, data.dbConnected ? "Connected" : "Disconnected");
                    setInfo(3, String.valueOf(data.stats.getTotalRooms()));
                    setInfo(4, String.valueOf(data.stats.getTotalCustomers()));
                    setInfo(5, String.valueOf(data.totalBookings));
                    setInfo(6, String.valueOf(data.totalStaff));
                    setInfo(7, data.lastBackup);
                    setInfo(8, Theme.isDark() ? "Dark Mode" : "Light Mode");
                    setInfo(9, data.currency);
                } catch (Exception ignored) {
                    // keep fields
                }
            }
        }.execute();
    }

    private int countAdmins() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM admins");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return 0;
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    private static String nz(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        hotelNameField.applyTheme();
        addressField.applyTheme();
        phoneField.applyTheme();
        emailField.applyTheme();
        taxRateField.applyTheme();
        currencyCombo.applyTheme();
        timezoneCombo.applyTheme();
        languageCombo.applyTheme();
        syncThemeCards();
        for (InfoRow row : infoRows) {
            row.applyTheme();
        }
        for (JLabel title : sectionTitles) {
            title.setForeground(Theme.textPrimary());
        }
        if (pageScroll != null) {
            pageScroll.getViewport().setBackground(Theme.bgPrimary());
        }
        repaint();
    }

    private record SettingsSnapshot(
            String hotelName,
            String address,
            String phone,
            String email,
            BigDecimal taxRate,
            String currency,
            String timezone,
            String language,
            String lastBackup,
            DashboardStats stats,
            int totalBookings,
            int totalStaff,
            boolean dbConnected
    ) {
    }

    /** Single labeled system-info row with icon. */
    private static final class InfoRow extends JPanel {
        private final JLabel label;
        private final JLabel value;
        private final String iconKey;

        InfoRow(String labelText, String valueText, String iconKey) {
            this.iconKey = iconKey;
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(8, 4, 8, 4));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            JPanel icon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(Theme.ROYAL_BLUE.getRed(), Theme.ROYAL_BLUE.getGreen(),
                            Theme.ROYAL_BLUE.getBlue(), 22));
                    g2.fillRoundRect(0, 0, 28, 28, 8, 8);
                    NavIcons.paint(g2, iconKey, 6, 6, 16, Theme.ROYAL_BLUE);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(28, 28);
                }
            };
            icon.setOpaque(false);

            JPanel text = new JPanel(new BorderLayout(0, 2));
            text.setOpaque(false);
            label = new JLabel(labelText);
            label.setFont(Theme.fontRegular(11));
            label.setForeground(Theme.textSecondary());
            value = new JLabel(valueText);
            value.setFont(Theme.fontMedium(13));
            value.setForeground(Theme.textPrimary());
            text.add(label, BorderLayout.NORTH);
            text.add(value, BorderLayout.SOUTH);

            add(icon, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        void setValue(String text) {
            value.setText(text == null || text.isBlank() ? "—" : text);
        }

        void applyTheme() {
            label.setForeground(Theme.textSecondary());
            value.setForeground(Theme.textPrimary());
            repaint();
        }
    }
}
