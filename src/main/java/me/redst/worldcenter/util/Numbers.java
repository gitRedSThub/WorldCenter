package me.redst.worldcenter.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Numbers {
    private static final Pattern EXACT = Pattern.compile("[+-]?\\d+(\\.\\d{1,2})?");

    private static final Pattern ANY = Pattern.compile("[+-]?\\d+(\\.\\d+)?");

    public static final int MAX_DECIMALS = 2;

    private Numbers() {
    }

    public static Double parse(String input) {
        if (input == null || !EXACT.matcher(input).matches()) {
            return null;
        }
        try {
            double value = Double.parseDouble(input);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static boolean isTooPrecise(String input) {
        return input != null && ANY.matcher(input).matches() && !EXACT.matcher(input).matches();
    }

    public static boolean hasAllowedPrecision(double value) {
        if (!Double.isFinite(value)) {
            return false;
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().scale() <= MAX_DECIMALS;
    }

    public static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public static double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    public static int toTicks(double seconds) {
        return Math.max(1, (int) Math.round(seconds * 20.0D));
    }
}
