package utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ImageUtil {
    private ImageUtil() {
    }

    public static Path uploadDir() {
        String configured = System.getProperty("hotel.upload.dir");
        Path dir;
        if (configured == null || configured.isBlank()) {
            dir = Paths.get(System.getProperty("user.home"), ".hotel-reservation", "uploads");
        } else {
            dir = Paths.get(configured);
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create upload directory", e);
        }
        return dir;
    }

    public static String copyImage(Path source) throws IOException {
        String ext = "";
        String name = source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            ext = name.substring(dot);
        }
        Path target = uploadDir().resolve(UUID.randomUUID() + ext);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public static ImageIcon loadScaled(String path, int width, int height) {
        if (path == null || path.isBlank() || !Files.exists(Paths.get(path))) {
            return null;
        }
        try {
            BufferedImage original = ImageIO.read(Paths.get(path).toFile());
            if (original == null) {
                return null;
            }
            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = output.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(output);
        } catch (IOException e) {
            return null;
        }
    }
}
