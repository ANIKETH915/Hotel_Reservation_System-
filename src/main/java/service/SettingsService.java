package service;

import dao.SettingsDao;
import utils.CurrencyUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsService {

    private final SettingsDao settingsDao = new SettingsDao();
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public String getHotelName() throws SQLException {
        String name = getCached("hotel_name");
        return name != null ? name : "Grand Azure Hotel & Suites";
    }

    public String getTheme() throws SQLException {
        String theme = getCached("theme");
        return theme != null ? theme : "light";
    }

    public void setTheme(String theme) throws SQLException {
        if (theme == null || theme.isBlank()) {
            throw new IllegalArgumentException("Theme is required");
        }
        setSetting("theme", theme.trim());
    }

    public BigDecimal getTaxRate() throws SQLException {
        String value = getCached("tax_rate");
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    public int getVipThreshold() throws SQLException {
        String value = getCached("vip_booking_threshold");
        if (value == null || value.isBlank()) {
            return 3;
        }
        return Integer.parseInt(value.trim());
    }

    public String getCurrency() throws SQLException {
        String value = getCached("currency");
        return value != null && !value.isBlank() ? value.trim() : "INR";
    }

    public void setSetting(String key, String value) throws SQLException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Setting key is required");
        }
        if (value == null) {
            throw new IllegalArgumentException("Setting value is required");
        }
        settingsDao.set(key.trim(), value.trim());
        CACHE.put(key.trim(), value.trim());
        if ("currency".equalsIgnoreCase(key.trim())) {
            CurrencyUtil.setCurrency(value.trim());
        }
    }

    public String get(String key) throws SQLException {
        return getCached(key);
    }

    private String getCached(String key) throws SQLException {
        String cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        String value = settingsDao.get(key);
        if (value != null) {
            CACHE.put(key, value);
        }
        return value;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
