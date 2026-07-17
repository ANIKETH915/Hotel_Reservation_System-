package dao;

import database.DatabaseConnection;
import model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AdminDao {

    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, salt, full_name, security_answer_hash, "
                + "remember_token_hash, remember_token_expires, is_active, last_login "
                + "FROM admins WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Admin findByRememberTokenHash(String tokenHash) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, salt, full_name, security_answer_hash, "
                + "remember_token_hash, remember_token_expires, is_active, last_login "
                + "FROM admins WHERE remember_token_hash = ? "
                + "AND remember_token_expires IS NOT NULL AND remember_token_expires > NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void updateLastLogin(int adminId) throws SQLException {
        String sql = "UPDATE admins SET last_login = NOW() WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

    public void updatePassword(int adminId, String passwordHash, String salt) throws SQLException {
        String sql = "UPDATE admins SET password_hash = ?, salt = ? WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, adminId);
            ps.executeUpdate();
        }
    }

    public void updateRememberToken(int adminId, String tokenHash, LocalDateTime expires) throws SQLException {
        String sql = "UPDATE admins SET remember_token_hash = ?, remember_token_expires = ? WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.setTimestamp(2, Timestamp.valueOf(expires));
            ps.setInt(3, adminId);
            ps.executeUpdate();
        }
    }

    public void clearRememberToken(int adminId) throws SQLException {
        String sql = "UPDATE admins SET remember_token_hash = NULL, remember_token_expires = NULL WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        }
    }

    public void updateSecurityAnswer(int adminId, String securityAnswerHash) throws SQLException {
        String sql = "UPDATE admins SET security_answer_hash = ? WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, securityAnswerHash);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setSalt(rs.getString("salt"));
        admin.setFullName(rs.getString("full_name"));
        admin.setSecurityAnswerHash(rs.getString("security_answer_hash"));
        admin.setRememberTokenHash(rs.getString("remember_token_hash"));
        Timestamp expires = rs.getTimestamp("remember_token_expires");
        if (expires != null) {
            admin.setRememberTokenExpires(expires.toLocalDateTime());
        }
        admin.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            admin.setLastLogin(lastLogin.toLocalDateTime());
        }
        return admin;
    }
}
