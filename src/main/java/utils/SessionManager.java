package utils;

import model.Admin;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SessionManager {
    private static Admin currentAdmin;
    private static final Path REMEMBER_FILE = Paths.get(System.getProperty("user.home"),
            ".hotel-reservation", "remember.token");

    private SessionManager() {
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public static void clear() {
        currentAdmin = null;
    }

    public static void saveRememberToken(String token) {
        try {
            Files.createDirectories(REMEMBER_FILE.getParent());
            Files.writeString(REMEMBER_FILE, token, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Unable to save remember token: " + e.getMessage());
        }
    }

    public static String loadRememberToken() {
        try {
            if (Files.exists(REMEMBER_FILE)) {
                return Files.readString(REMEMBER_FILE, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            System.err.println("Unable to load remember token: " + e.getMessage());
        }
        return null;
    }

    public static void clearRememberToken() {
        try {
            Files.deleteIfExists(REMEMBER_FILE);
        } catch (Exception ignored) {
            // ignore
        }
    }
}
