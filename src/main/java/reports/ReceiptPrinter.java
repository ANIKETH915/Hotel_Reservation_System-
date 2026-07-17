package reports;

import model.Booking;
import service.SettingsService;
import utils.CurrencyUtil;
import utils.DateUtil;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ReceiptPrinter {

    private static final DateTimeFormatter PRINT_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private ReceiptPrinter() {
    }

    public static void printBookingReceipt(Component parent, Booking booking) {
        if (booking == null) {
            return;
        }
        String content = buildReceiptText(booking);
        PrintUtil.showPreview(parent, "Booking Receipt", content);
    }

    public static String buildReceiptText(Booking booking) {
        String hotelName = "Grand Azure Hotel & Suites";
        try {
            hotelName = new SettingsService().getHotelName();
        } catch (Exception ignored) {
            // use default
        }

        StringBuilder sb = new StringBuilder();
        sb.append(center(hotelName, 42)).append('\n');
        sb.append(center("BOOKING RECEIPT", 42)).append('\n');
        sb.append(repeat('-', 42)).append('\n');
        sb.append(String.format("Receipt #: RCP-%06d%n", booking.getBookingId()));
        sb.append(String.format("Date: %s%n", PRINT_TIME.format(LocalDateTime.now())));
        sb.append(repeat('-', 42)).append('\n');
        sb.append(String.format("Guest: %s%n", nullSafe(booking.getCustomerName())));
        sb.append(String.format("Room: %s (%s)%n", nullSafe(booking.getRoomNumber()), nullSafe(booking.getRoomType())));
        sb.append(String.format("Check-in:  %s%n", DateUtil.format(booking.getCheckIn())));
        sb.append(String.format("Check-out: %s%n", DateUtil.format(booking.getCheckOut())));
        sb.append(String.format("Nights:    %d%n", booking.getDays()));
        sb.append(repeat('-', 42)).append('\n');
        sb.append(String.format("Total Amount: %s%n", CurrencyUtil.format(booking.getTotalAmount())));
        sb.append(String.format("Booking Status: %s%n",
                booking.getBookingStatus() != null ? booking.getBookingStatus().getLabel() : "-"));
        sb.append(String.format("Payment Status: %s%n",
                booking.getPaymentStatus() != null ? booking.getPaymentStatus().getLabel() : "-"));
        sb.append(repeat('-', 42)).append('\n');
        sb.append(center("Thank you for staying with us!", 42)).append('\n');
        sb.append(center("Grand Azure — Where luxury meets comfort", 42)).append('\n');
        return sb.toString();
    }

    private static String center(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private static String repeat(char c, int count) {
        return String.valueOf(c).repeat(count);
    }

    private static String nullSafe(String value) {
        return value != null ? value : "-";
    }
}
