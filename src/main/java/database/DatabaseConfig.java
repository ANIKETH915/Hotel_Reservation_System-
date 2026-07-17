package database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
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

    public static String getUrl() {
        return envOrProperty("HOTEL_DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/hotel_reservation_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
    }

    public static String getUser() {
        return envOrProperty("HOTEL_DB_USER", "db.user", "root");
    }

    public static String getPassword() {
        return envOrProperty("HOTEL_DB_PASSWORD", "db.password", "");
    }

    public static String getHotelName() {
        return PROPS.getProperty("hotel.name", "Grand Azure Hotel & Suites");
    }

    public static String getUploadDir() {
        return PROPS.getProperty("hotel.upload.dir", "");
    }

    private static String envOrProperty(String environmentName, String propertyName, String fallback) {
        String environment = System.getenv(environmentName);
        if (environment != null && !environment.isBlank()) {
            return environment;
        }
        return PROPS.getProperty(propertyName, fallback);
    }
}
