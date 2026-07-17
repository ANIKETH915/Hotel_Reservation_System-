package components;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lightweight in-app event bus so mutations refresh dashboard and related panels.
 */
public final class AppEvents {
    public enum Domain {
        ROOMS, CUSTOMERS, BOOKINGS, PAYMENTS, SETTINGS, ALL
    }

    private static final Map<Domain, List<Consumer<Domain>>> LISTENERS = new EnumMap<>(Domain.class);

    static {
        for (Domain d : Domain.values()) {
            LISTENERS.put(d, new ArrayList<>());
        }
    }

    private AppEvents() {
    }

    public static synchronized void addListener(Domain domain, Consumer<Domain> listener) {
        LISTENERS.get(domain).add(listener);
        if (domain != Domain.ALL) {
            LISTENERS.get(Domain.ALL).add(listener);
        }
    }

    public static synchronized void removeListener(Domain domain, Consumer<Domain> listener) {
        LISTENERS.get(domain).remove(listener);
        LISTENERS.get(Domain.ALL).remove(listener);
    }

    public static synchronized void fire(Domain domain) {
        List<Consumer<Domain>> copy = new ArrayList<>(LISTENERS.get(domain));
        if (domain != Domain.ALL) {
            copy.addAll(LISTENERS.get(Domain.ALL));
        }
        for (Consumer<Domain> listener : copy) {
            try {
                listener.accept(domain);
            } catch (Exception e) {
                System.err.println("AppEvents listener error: " + e.getMessage());
            }
        }
    }
}
