package components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import service.SettingsService;
import utils.DateUtil;
import utils.SessionManager;
import utils.UiExec;

public class HeaderBar extends JPanel {

    private final JLabel hotelLabel;
    private final JLabel sectionLabel;
    private final JLabel dateLabel;
    private final JLabel clockLabel;
    private final JLabel profileLabel;
    private final ModernTextField searchField;
    private final StyledButton themeButton;
    private final Timer clockTimer;
    private final UiExec.Coalescer searchCoalescer = new UiExec.Coalescer(120);
    private Consumer<String> onSearch;

    public HeaderBar() {
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.bgHeader());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                new EmptyBorder(UiLayout.SPACE_SM + 2, UiLayout.PAGE_INSET + 4, UiLayout.SPACE_SM + 2, UiLayout.PAGE_INSET + 4)
        ));
        setPreferredSize(new Dimension(0, 80));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        hotelLabel = new JLabel("Hotel PMS");
        hotelLabel.setFont(Theme.fontBold(16));
        hotelLabel.setForeground(Theme.textPrimary());

        sectionLabel = new JLabel("Dashboard");
        sectionLabel.setFont(Theme.fontRegular(12));
        sectionLabel.setForeground(Theme.textSecondary());

        dateLabel = new JLabel();
        dateLabel.setFont(Theme.fontRegular(11));
        dateLabel.setForeground(Theme.textMuted());

        left.add(hotelLabel);
        left.add(sectionLabel);
        left.add(dateLabel);

        searchField = new ModernTextField(22);
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.setToolTipText("Filter the current section table");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                if (onSearch != null) {
                    String text = searchField.getText();
                    searchCoalescer.request(() -> onSearch.accept(text));
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, UiLayout.SPACE_SM));
        center.setOpaque(false);
        center.add(searchField);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiLayout.SPACE_MD - 4, UiLayout.SPACE_SM));
        right.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(Theme.fontMedium(13));
        clockLabel.setForeground(Theme.ROYAL_BLUE);

        profileLabel = new JLabel();
        profileLabel.setFont(Theme.fontMedium(12));
        profileLabel.setForeground(Theme.textPrimary());
        profileLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        profileLabel.setOpaque(true);
        profileLabel.setBackground(Theme.bgCard());

        themeButton = new StyledButton(Theme.isDark() ? "Light" : "Dark", StyledButton.Style.GHOST);
        themeButton.setPreferredSize(new Dimension(72, 34));
        themeButton.setToolTipText("Toggle light / dark theme");
        themeButton.addActionListener(e -> ThemeManager.toggle());

        right.add(clockLabel);
        right.add(profileLabel);
        right.add(themeButton);

        add(left, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        refreshProfile();
        loadHotelName();
        updateClock();
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    public void setSectionTitle(String section) {
        sectionLabel.setText(section == null ? " " : section);
        searchField.setToolTipText("Search " + (section == null ? "this section" : section.toLowerCase()));
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public void refreshProfile() {
        var admin = SessionManager.getCurrentAdmin();
        if (admin != null) {
            profileLabel.setText(admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
        } else {
            profileLabel.setText("Admin");
        }
    }

    public void loadHotelName() {
        try {
            applyHotelName(new SettingsService().getHotelName());
        } catch (Exception e) {
            applyHotelName("Hotel PMS");
        }
    }

    public void applyHotelName(String name) {
        String displayName = name == null || name.isBlank() ? "Hotel PMS" : name.trim();
        hotelLabel.setToolTipText(displayName);
        hotelLabel.setText(ellipsize(displayName, 28));
    }

    public void applyTheme() {
        setBackground(Theme.bgHeader());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                new EmptyBorder(UiLayout.SPACE_SM + 2, UiLayout.PAGE_INSET + 4, UiLayout.SPACE_SM + 2, UiLayout.PAGE_INSET + 4)
        ));
        hotelLabel.setForeground(Theme.textPrimary());
        sectionLabel.setForeground(Theme.textSecondary());
        dateLabel.setForeground(Theme.textMuted());
        clockLabel.setForeground(Theme.ROYAL_BLUE);
        profileLabel.setForeground(Theme.textPrimary());
        profileLabel.setBackground(Theme.bgCard());
        searchField.applyTheme();
        themeButton.setText(Theme.isDark() ? "Light" : "Dark");
        repaint();
    }

    public void stopClock() {
        clockTimer.stop();
    }

    private void updateClock() {
        dateLabel.setText(DateUtil.formatHeaderDate(LocalDate.now()));
        clockLabel.setText(DateUtil.formatTime(LocalTime.now()));
    }

    private String ellipsize(String text, int maxCharacters) {
        return text.length() <= maxCharacters ? text : text.substring(0, maxCharacters - 1) + "…";
    }
}
