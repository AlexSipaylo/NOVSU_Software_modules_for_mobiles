package ru.example.fintrack.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {
    private MoneyUtils() { }

    // 125,50 превращается в 12550 копеек.
    public static long parse(String value) {
        String normalized = value.trim().replace(" ", "").replace(',', '.');
        long result = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2).longValueExact();
        if (result <= 0) throw new IllegalArgumentException("Сумма должна быть больше нуля");
        return result;
    }

    public static String format(long kopecks) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(kopecks / 100.0) + " ₽";
    }
}
