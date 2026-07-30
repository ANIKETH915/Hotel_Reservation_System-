package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class DatabaseConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                PROPS.load(in);
            } else {
                System.err.println("application.properties not found — using defaults");
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseConfig() {
    }

    public static Path getDatabasePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".HotelReservationSystem", "hotel.db");
    }

    public static String getUrl() {
        Path dbPath = getDatabasePath();
        String absPath = dbPath.toAbsolutePath().toString().replace('\\', '/');
        return "jdbc:sqlite:" + absPath + "?journal_mode=WAL&busy_timeout=5000";
    }

    public static String getUser() {
        return "";
    }

    public static String getPassword() {
        return "";
    }

    public static String getHotelName() {
        return PROPS.getProperty("hotel.name", "Grand Azure Hotel & Suites");
    }

    public static String getUploadDir() {
        return PROPS.getProperty("hotel.upload.dir", "");
    }
}
