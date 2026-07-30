package database;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() throws Exception {
        Path dbPath = DatabaseConfig.getDatabasePath();
        
        // If the database file does not exist, provision it from the template
        if (!Files.exists(dbPath)) {
            if (dbPath.getParent() != null) {
                Files.createDirectories(dbPath.getParent());
            }
            try (InputStream in = DatabaseInitializer.class.getResourceAsStream("/template.db")) {
                if (in != null) {
                    Files.copy(in, dbPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    System.err.println("template.db not found in resources - database file will be created empty by JDBC");
                }
            }
        }

        // Warm up connection pool
        DatabaseConnection.warmup();

        String upload = DatabaseConfig.getUploadDir();
        if (upload != null && !upload.isBlank()) {
            System.setProperty("hotel.upload.dir", upload);
        }
    }
}
