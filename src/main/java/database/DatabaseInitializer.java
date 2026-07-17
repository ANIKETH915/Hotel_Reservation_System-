package database;

import utils.PasswordUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() throws Exception {
        String fullUrl = DatabaseConfig.getUrl();
        // Strip DB name for bootstrap: jdbc:mysql://host:port/dbname?params → jdbc:mysql://host:port/?params
        String baseUrl = toBootstrapUrl(fullUrl);

        try (Connection bootstrap = DriverManager.getConnection(
                baseUrl, DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             Statement st = bootstrap.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS hotel_reservation_system "
                    + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        DatabaseConnection.warmup();

        if (!schemaReady()) {
            runSqlScript();
        }
        ensureAdminPassword();

        String upload = DatabaseConfig.getUploadDir();
        if (upload != null && !upload.isBlank()) {
            System.setProperty("hotel.upload.dir", upload);
        }
    }

    private static boolean schemaReady() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM information_schema.tables "
                             + "WHERE table_schema = 'hotel_reservation_system' AND table_name = 'rooms'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Build a JDBC URL without the database name (timezone paths like Asia/Kolkata must stay intact). */
    static String toBootstrapUrl(String fullUrl) {
        int schemeEnd = fullUrl.indexOf("://");
        if (schemeEnd < 0) {
            return fullUrl;
        }
        int pathStart = fullUrl.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) {
            return fullUrl;
        }
        int queryStart = fullUrl.indexOf('?', pathStart);
        if (queryStart < 0) {
            return fullUrl.substring(0, pathStart + 1);
        }
        return fullUrl.substring(0, pathStart + 1) + fullUrl.substring(queryStart);
    }

    private static void runSqlScript() throws Exception {
        Path sqlPath = Paths.get("database", "database.sql");
        String sql;
        if (Files.exists(sqlPath)) {
            sql = Files.readString(sqlPath, StandardCharsets.UTF_8);
        } else {
            try (InputStream in = DatabaseInitializer.class.getClassLoader()
                    .getResourceAsStream("database.sql")) {
                if (in == null) {
                    // Schema may already exist from a previous run
                    System.err.println("database.sql not found — skipping script (expecting existing schema)");
                    return;
                }
                sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            for (String statement : splitStatements(sql)) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String upper = trimmed.toUpperCase();
                if (upper.startsWith("CREATE DATABASE") || upper.startsWith("USE ")) {
                    continue;
                }
                try {
                    st.execute(trimmed);
                } catch (SQLException e) {
                    // Allow re-runs: duplicate data / non-fatal seed issues
                    String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                    if (msg.contains("duplicate") || msg.contains("already exists")) {
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    private static String[] splitStatements(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\n")) {
            String t = line.trim();
            if (t.startsWith("--")) {
                continue;
            }
            cleaned.append(line).append('\n');
        }
        return cleaned.toString().split(";");
    }

    private static void ensureAdminPassword() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT admin_id, password_hash, salt FROM admins WHERE username = 'admin'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    createAdmin(conn);
                    return;
                }
                String hash = rs.getString("password_hash");
                if ("PENDING".equals(hash) || hash == null || hash.isBlank()) {
                    String salt = PasswordUtil.generateSalt();
                    String passwordHash = PasswordUtil.hashPassword("admin123", salt);
                    String answerHash = PasswordUtil.hashPassword("azure", salt);
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE admins SET password_hash=?, salt=?, security_answer_hash=? WHERE username='admin'")) {
                        upd.setString(1, passwordHash);
                        upd.setString(2, salt);
                        upd.setString(3, answerHash);
                        upd.executeUpdate();
                    }
                }
            }
        }
    }

    private static void createAdmin(Connection conn) throws Exception {
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword("admin123", salt);
        String answerHash = PasswordUtil.hashPassword("azure", salt);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO admins (username, password_hash, salt, full_name, security_answer_hash) VALUES (?,?,?,?,?)")) {
            ps.setString(1, "admin");
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setString(4, "System Administrator");
            ps.setString(5, answerHash);
            ps.executeUpdate();
        }
    }
}
