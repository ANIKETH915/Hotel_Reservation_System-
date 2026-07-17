package reports;

import model.Booking;
import service.SettingsService;
import utils.CurrencyUtil;
import utils.DateUtil;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class InvoicePrinter {

    private static final DateTimeFormatter PRINT_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private InvoicePrinter() {
    }

    public static void printInvoice(Component parent, Booking booking) {
        if (booking == null) {
            return;
        }
        String content = buildInvoiceText(booking);
        PrintUtil.showPreview(parent, "Tax Invoice", content);
    }

    public static String buildInvoiceText(Booking booking) {
        String hotelName = "Grand Azure Hotel & Suites";
        BigDecimal taxRate = BigDecimal.ZERO;
        try {
            SettingsService settings = new SettingsService();
            hotelName = settings.getHotelName();
            taxRate = settings.getTaxRate();
        } catch (Exception ignored) {
            // use defaults
        }

        BigDecimal subtotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(taxAmount);

        StringBuilder sb = new StringBuilder();
        sb.append(center(hotelName, 48)).append('\n');
        sb.append(center("TAX INVOICE", 48)).append('\n');
        sb.append(repeat('-', 48)).append('\n');
        sb.append(String.format("Invoice #: INV-%06d%n", booking.getBookingId()));
        sb.append(String.format("Issue Date: %s%n", PRINT_TIME.format(LocalDateTime.now())));
        sb.append(repeat('-', 48)).append('\n');
        sb.append("BILL TO:\n");
        sb.append(String.format("  %s%n", nullSafe(booking.getCustomerName())));
        sb.append(repeat('-', 48)).append('\n');
        sb.append(String.format("Room Number : %s%n", nullSafe(booking.getRoomNumber())));
        sb.append(String.format("Room Type   : %s%n", nullSafe(booking.getRoomType())));
        sb.append(String.format("Check-in    : %s%n", DateUtil.format(booking.getCheckIn())));
        sb.append(String.format("Check-out   : %s%n", DateUtil.format(booking.getCheckOut())));
        sb.append(String.format("Duration    : %d night(s)%n", booking.getDays()));
        sb.append(repeat('-', 48)).append('\n');
        sb.append(String.format("%-28s %18s%n", "Description", "Amount"));
        sb.append(String.format("%-28s %18s%n",
                "Room charges (" + booking.getDays() + " nights)", CurrencyUtil.format(subtotal)));
        sb.append(repeat('-', 48)).append('\n');
        sb.append(String.format("%-28s %18s%n", "Subtotal", CurrencyUtil.format(subtotal)));
        sb.append(String.format("%-28s %18s%n", "Tax (" + taxRate.stripTrailingZeros().toPlainString() + "%)",
                CurrencyUtil.format(taxAmount)));
        sb.append(String.format("%-28s %18s%n", "GRAND TOTAL", CurrencyUtil.format(grandTotal)));
        sb.append(repeat('-', 48)).append('\n');
        sb.append(String.format("Payment Status: %s%n",
                booking.getPaymentStatus() != null ? booking.getPaymentStatus().getLabel() : "-"));
        sb.append(String.format("Booking Status: %s%n",
                booking.getBookingStatus() != null ? booking.getBookingStatus().getLabel() : "-"));
        sb.append(repeat('-', 48)).append('\n');
        sb.append(center("This is a computer-generated invoice.", 48)).append('\n');
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
