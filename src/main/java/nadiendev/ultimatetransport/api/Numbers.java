package nadiendev.ultimatetransport.api;

public final class Numbers {
    private static final String[] UNITS = {"", "k", "M", "G", "T", "P", "E"};

    private Numbers() {
    }

    public static String compact(long value) {
        if (value < 0) {
            return "-" + compact(-value);
        }
        if (value < 1000) {
            return Long.toString(value);
        }
        int unit = 0;
        double scaled = value;
        while (scaled >= 1000.0 && unit < UNITS.length - 1) {
            scaled /= 1000.0;
            unit++;
        }
        String number = scaled >= 100.0
                ? String.format("%.0f", scaled)
                : scaled >= 10.0 ? trim(String.format("%.1f", scaled)) : trim(String.format("%.2f", scaled));
        return number + UNITS[unit];
    }

    public static String compact(double value) {
        return compact(Math.round(value));
    }

    private static String trim(String number) {
        String cleaned = number.replace(',', '.');
        if (!cleaned.contains(".")) {
            return cleaned;
        }
        cleaned = cleaned.replaceAll("0+$", "");
        return cleaned.endsWith(".") ? cleaned.substring(0, cleaned.length() - 1) : cleaned;
    }
}
