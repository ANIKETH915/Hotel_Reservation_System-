package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.HeaderBar;
import components.LoadingOverlay;
import components.Sidebar;
import components.Theme;
import components.ThemeManager;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import service.AuthService;
import service.SettingsService;
import utils.ShortcutManager;
import utils.UiExec;

public class MainFrame extends JFrame {

    private static final Map<String, String> CARD_NAMES = Map.of(
            "Dashboard", "dashboard",
            "Rooms", "rooms",
            "Customers", "customers",
            "Bookings", "bookings",
            "Payments", "payments",
            "Reports", "reports",
            "Settings", "settings",
            "About", "about"
    );

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Sidebar sidebar;
    private final HeaderBar headerBar;
    private final LoadingOverlay loadingOverlay;
    private final Map<String, RefreshablePanel> panels = new HashMap<>();
    private final UiExec.Coalescer dataCoalescer = new UiExec.Coalescer(120);
    private final EnumSet<AppEvents.Domain> pendingDomains = EnumSet.noneOf(AppEvents.Domain.class);
    private String currentSection = "Dashboard";
    private Timer dashboardTimer;
    private String cachedHotelName;

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        // Avoid expensive window double-buffering issues
        getRootPane().setDoubleBuffered(true);

        headerBar = new HeaderBar();
        headerBar.setMainFrame(this);
        sidebar = new Sidebar(this::navigate);
        loadingOverlay = new LoadingOverlay();

        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(
                UiLayout.PAGE_INSET, UiLayout.PAGE_INSET, UiLayout.PAGE_INSET, UiLayout.PAGE_INSET));

        registerPanel("dashboard", new DashboardPanel(this));
        registerPanel("rooms", new RoomPanel(this));
        registerPanel("customers", new CustomerPanel(this));
        registerPanel("bookings", new BookingPanel(this));
        registerPanel("payments", new PaymentPanel(this));
        registerPanel("reports", new ReportsPanel(this));
        registerPanel("settings", new SettingsPanel(this));
        registerPanel("about", new AboutPanel());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Theme.bgPrimary());
        centerWrapper.setDoubleBuffered(true);
        centerWrapper.add(contentPanel, BorderLayout.CENTER);

        JPanel overlayHost = new JPanel();
        overlayHost.setLayout(new OverlayLayout(overlayHost));
        overlayHost.setBackground(Theme.bgPrimary());
        centerWrapper.setAlignmentX(0.5f);
        centerWrapper.setAlignmentY(0.5f);
        loadingOverlay.setAlignmentX(0.5f);
        loadingOverlay.setAlignmentY(0.5f);
        // Keep content under overlay so scrolling stays interactive when hidden
        overlayHost.add(centerWrapper);
        overlayHost.add(loadingOverlay);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.bgPrimary());
        body.add(sidebar, BorderLayout.WEST);
        body.add(overlayHost, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerBar, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        headerBar.setOnSearch(this::applySearch);

        ThemeManager.addListener(dark -> refreshTheme());
        AppEvents.addListener(AppEvents.Domain.ALL, this::onDataChanged);

        ShortcutManager.register(getRootPane(), key -> {
            if ("Logout".equals(key)) {
                doLogout();
            } else {
                navigate(key);
            }
        }, this::doLogout);

        getRootPane().registerKeyboardAction(e -> {
            RefreshablePanel panel = panels.get(CARD_NAMES.get(currentSection));
            if (panel != null) {
                panel.refresh();
            }
        }, javax.swing.KeyStroke.getKeyStroke("F5"), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> headerBar.clearSearch(),
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        dashboardTimer = new Timer(60_000, e -> {
            RefreshablePanel panel = panels.get("dashboard");
            if (panel != null && "Dashboard".equals(currentSection)) {
                panel.refresh();
            }
        });
        dashboardTimer.setRepeats(true);
        dashboardTimer.start();

        // Instant first paint — load branding off EDT
        setTitle("Hotel PMS");
        navigate("Dashboard");
        refreshTheme();
        reloadBrandingAsync();
    }

    private void reloadBrandingAsync() {
        UiExec.run(() -> new SettingsService().getHotelName(), name -> {
            cachedHotelName = name;
            setTitle(name + " — PMS");
            headerBar.applyHotelName(name);
            sidebar.refreshBranding(name);
        }, ignored -> setTitle("Hotel PMS"));
    }

    private void registerPanel(String cardName, RefreshablePanel panel) {
        panels.put(cardName, panel);
        JPanel view = (JPanel) panel;
        view.setDoubleBuffered(true);
        contentPanel.add(view, cardName);
        // Smooth scroll defaults for any nested scroll panes created by panels
        installScrollSpeed(view);
    }

    private void installScrollSpeed(java.awt.Container root) {
        UiLayout.installScrollDefaults(root);
    }

    public void navigate(String section) {
        if ("Logout".equals(section)) {
            doLogout();
            return;
        }

        String card = CARD_NAMES.get(section);
        if (card == null) {
            return;
        }

        // Instant card switch — no overlay flicker
        currentSection = section;
        sidebar.setActive(section);
        headerBar.setSectionTitle(section);
        headerBar.clearSearch();
        cardLayout.show(contentPanel, card);

        RefreshablePanel panel = panels.get(card);
        if (panel != null) {
            // Always refresh data in background; first visit & revisit both use SwingWorker inside panel
            panel.refresh();
            // Re-install scroll speed in case panel built scroll panes lazily
            installScrollSpeed((java.awt.Container) panel);
        }
    }

    public void notifyDataChanged(AppEvents.Domain domain) {
        AppEvents.fire(domain);
    }

    private void onDataChanged(AppEvents.Domain domain) {
        // Coalesce rapid fire events without losing domains (for example booking + payment).
        synchronized (pendingDomains) {
            if (domain == AppEvents.Domain.ALL) {
                pendingDomains.clear();
                pendingDomains.add(AppEvents.Domain.ALL);
            } else if (!pendingDomains.contains(AppEvents.Domain.ALL)) {
                pendingDomains.add(domain);
            }
        }
        dataCoalescer.request(() -> {
            EnumSet<AppEvents.Domain> domains;
            synchronized (pendingDomains) {
                domains = pendingDomains.clone();
                pendingDomains.clear();
            }
            applyDataChange(domains);
        });
    }

    private void applyDataChange(EnumSet<AppEvents.Domain> domains) {
        boolean all = domains.contains(AppEvents.Domain.ALL);

        RefreshablePanel dashboard = panels.get("dashboard");
        if (dashboard != null && (domains.contains(AppEvents.Domain.ROOMS)
                || domains.contains(AppEvents.Domain.BOOKINGS)
                || domains.contains(AppEvents.Domain.PAYMENTS)
                || domains.contains(AppEvents.Domain.CUSTOMERS)
                || all)) {
            // Only refresh dashboard if visible or when not on a heavy screen
            dashboard.refresh();
        }

        String card = CARD_NAMES.get(currentSection);
        if (card != null && !"dashboard".equals(card) && matchesDomain(card, domains, all)) {
            RefreshablePanel panel = panels.get(card);
            if (panel != null) {
                panel.refresh();
            }
        }

        if (domains.contains(AppEvents.Domain.SETTINGS) || all) {
            reloadBrandingAsync();
        }
    }

    private boolean matchesDomain(String card, EnumSet<AppEvents.Domain> domains, boolean all) {
        if (all) {
            return true;
        }
        return (domains.contains(AppEvents.Domain.ROOMS)
                && ("rooms".equals(card) || "bookings".equals(card)))
                || (domains.contains(AppEvents.Domain.CUSTOMERS)
                && ("customers".equals(card) || "bookings".equals(card)))
                || (domains.contains(AppEvents.Domain.BOOKINGS)
                && ("bookings".equals(card) || "rooms".equals(card)))
                || (domains.contains(AppEvents.Domain.PAYMENTS)
                && ("payments".equals(card) || "bookings".equals(card)))
                || (domains.contains(AppEvents.Domain.SETTINGS) && "settings".equals(card));
    }

    private void applySearch(String query) {
        RefreshablePanel panel = panels.get(CARD_NAMES.get(currentSection));
        if (panel != null) {
            panel.applySearch(query);
        }
    }

    private void doLogout() {
        if (!ConfirmDialog.confirm(this, "Sign Out",
                "Are you sure you want to sign out of the property management system?",
                "Sign Out", false)) {
            return;
        }
        dashboardTimer.stop();
        headerBar.stopClock();
        UiExec.runVoid(() -> new AuthService().logout(), () -> {
            dispose();
            new LoginFrame().setVisible(true);
        }, e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void refreshTheme() {
        getContentPane().setBackground(Theme.bgPrimary());
        headerBar.applyTheme();
        // Theme switch: light paint only, no DB
        for (RefreshablePanel panel : panels.values()) {
            panel.applyTheme();
        }
    }

    public void showLoading(String message) {
        SwingUtilities.invokeLater(() -> {
            loadingOverlay.showOverlay(message);
            loadingOverlay.revalidate();
        });
    }

    public void hideLoading() {
        SwingUtilities.invokeLater(loadingOverlay::hideOverlay);
    }

    public interface RefreshablePanel {
        void refresh();

        default void applySearch(String query) {
        }

        default void applyTheme() {
            if (this instanceof JPanel panel) {
                panel.setBackground(Theme.bgPrimary());
                panel.repaint();
            }
        }
    }
}
