package components;

import service.SettingsService;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ThemeManager {
    private static final List<Consumer<Boolean>> LISTENERS = new ArrayList<>();
    private static final SettingsService SETTINGS = new SettingsService();

    private ThemeManager() {
    }

    public static void init() {
        try {
            boolean dark = "dark".equalsIgnoreCase(SETTINGS.getTheme());
            Theme.setDark(dark);
        } catch (Exception e) {
            Theme.setDark(false);
            System.err.println("Theme init fallback: " + e.getMessage());
        }
    }

    public static void toggle() {
        setDark(!Theme.isDark());
    }

    public static void setDark(boolean dark) {
        Theme.setDark(dark);
        try {
            SETTINGS.setTheme(dark ? "dark" : "light");
        } catch (Exception e) {
            System.err.println("Unable to persist theme: " + e.getMessage());
        }
        for (Consumer<Boolean> listener : new ArrayList<>(LISTENERS)) {
            listener.accept(dark);
        }
    }

    public static void addListener(Consumer<Boolean> listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(Consumer<Boolean> listener) {
        LISTENERS.remove(listener);
    }

    public static void refreshTree(Component root) {
        if (root != null) {
            root.repaint();
            root.revalidate();
        }
    }
}
