package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {
    private static String currency = "INR";

    private CurrencyUtil() {
    }

    public static void setCurrency(String code) {
        currency = code == null || code.isBlank() ? "INR" : code.trim().toUpperCase();
    }

    public static String getCurrency() {
        return currency;
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        NumberFormat format;
        if ("USD".equalsIgnoreCase(currency)) {
            format = NumberFormat.getCurrencyInstance(Locale.US);
        } else if ("EUR".equalsIgnoreCase(currency)) {
            format = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        } else {
            format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        }
        return format.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public static BigDecimal withTax(BigDecimal base, BigDecimal taxRatePercent) {
        if (base == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = taxRatePercent == null ? BigDecimal.ZERO : taxRatePercent;
        BigDecimal tax = base.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(tax);
    }
}
