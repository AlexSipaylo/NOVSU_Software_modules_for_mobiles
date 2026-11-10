package ru.example.fintrack.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private DateUtils() { }

    public static String monthKey(long time) {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date(time));
    }

    public static long monthStart(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long nextMonthStart(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(monthStart(time));
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTimeInMillis();
    }

    // Переносим дату на тот же день следующего месяца; например, 31 января -> 28 февраля.
    public static long nextPaymentDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
        return calendar.getTimeInMillis();
    }

    public static String format(long time) {
        return new SimpleDateFormat("dd.MM.yyyy", new Locale("ru", "RU"))
                .format(new Date(time));
    }
}
