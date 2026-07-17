package components;

import java.awt.Color;
import java.awt.Font;

/**
 * Brand theme tokens for Grand Azure PMS.
 * Light and dark palettes switch via ThemeManager.
 */
public final class Theme {
    public static final Color ROYAL_BLUE = new Color(0x1E, 0x3A, 0x8A);
    public static final Color DARK_NAVY = new Color(0x0F, 0x17, 0x2A);
    public static final Color GOLD = new Color(0xC9, 0xA2, 0x27);
    public static final Color EMERALD = new Color(0x05, 0x96, 0x69);
    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);
    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);

    private static boolean dark;

    private Theme() {
    }

    public static void setDark(boolean value) {
        dark = value;
    }

    public static boolean isDark() {
        return dark;
    }

    public static Color bgPrimary() {
        return dark ? new Color(0x0B, 0x12, 0x20) : new Color(0xF3, 0xF4, 0xF6);
    }

    public static Color bgCard() {
        return dark ? new Color(0x15, 0x1F, 0x30) : Color.WHITE;
    }

    public static Color bgSidebar() {
        return DARK_NAVY;
    }

    public static Color bgHeader() {
        return dark ? new Color(0x11, 0x1A, 0x2B) : Color.WHITE;
    }

    public static Color textPrimary() {
        return dark ? new Color(0xF1, 0xF5, 0xF9) : new Color(0x0F, 0x17, 0x2A);
    }

    public static Color textSecondary() {
        return dark ? new Color(0x94, 0xA3, 0xB8) : new Color(0x64, 0x74, 0x8B);
    }

    public static Color textMuted() {
        return dark ? new Color(0x64, 0x74, 0x8B) : new Color(0x94, 0xA3, 0xB8);
    }

    public static Color border() {
        return dark ? new Color(0x1E, 0x29, 0x3B) : new Color(0xE5, 0xE7, 0xEB);
    }

    public static Color tableAlt() {
        return dark ? new Color(0x12, 0x1A, 0x2A) : new Color(0xF8, 0xFA, 0xFC);
    }

    public static Color tableHover() {
        return dark ? new Color(0x1E, 0x3A, 0x5F) : new Color(0xDB, 0xEA, 0xFE);
    }

    public static Color inputBg() {
        return dark ? new Color(0x0F, 0x17, 0x2A) : Color.WHITE;
    }

    public static Font fontRegular(float size) {
        return new Font("Segoe UI", Font.PLAIN, Math.round(size));
    }

    public static Font fontMedium(float size) {
        return new Font("Segoe UI Semibold", Font.PLAIN, Math.round(size));
    }

    public static Font fontBold(float size) {
        return new Font("Segoe UI", Font.BOLD, Math.round(size));
    }

    public static Font fontDisplay(float size) {
        return new Font("Georgia", Font.BOLD, Math.round(size));
    }

    public static Color statusColor(String status) {
        if (status == null) {
            return textMuted();
        }
        return switch (status) {
            case "Available", "Paid", "Checked Out" -> EMERALD;
            case "Booked", "Checked In", "Confirmed" -> ROYAL_BLUE;
            case "Reserved", "Partial", "Pending" -> GOLD;
            case "Maintenance", "Cancelled", "Refunded" -> DANGER;
            case "Cleaning" -> WARNING;
            default -> textMuted();
        };
    }
}
