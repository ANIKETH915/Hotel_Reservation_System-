package dao;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingsDao {

    public String get(String key) throws SQLException {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        }
        return null;
    }

    public void set(String key, String value) throws SQLException {
        String sql = "INSERT OR REPLACE INTO app_settings (setting_key, setting_value) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public Map<String, String> getAll() throws SQLException {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM app_settings ORDER BY setting_key";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return settings;
    }
}
