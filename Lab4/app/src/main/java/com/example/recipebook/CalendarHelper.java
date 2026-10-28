package com.example.recipebook;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class CalendarHelper {
    private static final long EVENT_DURATION_MS = 60L * 60L * 1000L;

    private CalendarHelper() { }

    public static boolean hasCalendarPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static List<String> findConflicts(Context context, long startMs, long endMs,
                                              Long excludedEventId) {
        List<String> conflicts = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) return conflicts;

        long queryStart = startMs - 24L * 60L * 60L * 1000L;
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, queryStart);
        ContentUris.appendId(builder, endMs);
        String[] projection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END
        };

        try (Cursor cursor = context.getContentResolver().query(builder.build(), projection,
                null, null, CalendarContract.Instances.BEGIN + " ASC")) {
            if (cursor == null) return conflicts;
            int idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID);
            int titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE);
            int beginIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN);
            int endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(idIndex);
                if (excludedEventId != null && excludedEventId == eventId) continue;
                long eventBegin = cursor.getLong(beginIndex);
                long eventEnd = cursor.getLong(endIndex);
                if (eventBegin < endMs && eventEnd > startMs) {
                    String title = cursor.getString(titleIndex);
                    if (title == null || title.trim().isEmpty()) title = "Событие календаря";
                    conflicts.add(title + " (" + timeFormat.format(new Date(eventBegin))
                            + "–" + timeFormat.format(new Date(eventEnd)) + ")");
                }
            }
        } catch (SecurityException ignored) {
        } catch (RuntimeException ignored) {
        }
        return conflicts;
    }

    public static long createCookingEvent(Context context, Recipe recipe, long startMs,
                                          boolean weeklyRepeat) throws Exception {
        if (!hasCalendarPermissions(context)) {
            throw new SecurityException("Нет разрешения на работу с календарём");
        }

        long calendarId = findWritableCalendarId(context);
        if (calendarId < 0) {
            throw new IllegalStateException("Не найден календарь, доступный для записи");
        }

        if (recipe.getCalendarEventId() != null) {
            deleteEvent(context, recipe.getCalendarEventId());
        }

        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        event.put(CalendarContract.Events.TITLE, "Готовить: " + recipe.getTitle());
        event.put(CalendarContract.Events.DTSTART, startMs);
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        event.put(CalendarContract.Events.DESCRIPTION,
                "План приготовления блюда из приложения «Книга рецептов».");
        if (weeklyRepeat) {
            event.put(CalendarContract.Events.DURATION, "PT1H");
            event.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;INTERVAL=1");
        } else {
            event.put(CalendarContract.Events.DTEND, startMs + EVENT_DURATION_MS);
        }

        Uri inserted = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, event);
        if (inserted == null) throw new IllegalStateException("Календарь не создал событие");
        long eventId = ContentUris.parseId(inserted);

        ContentValues reminder = new ContentValues();
        reminder.put(CalendarContract.Reminders.EVENT_ID, eventId);
        reminder.put(CalendarContract.Reminders.MINUTES, 30);
        reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri reminderUri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, reminder);
        if (reminderUri == null) {
            deleteEvent(context, eventId);
            throw new IllegalStateException("Не удалось создать напоминание календаря");
        }
        return eventId;
    }

    private static long findWritableCalendarId(Context context) {
        String[] projection = {CalendarContract.Calendars._ID};
        String selection = CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ?";
        String[] selectionArgs = {
                String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
        };
        try (Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                CalendarContract.Calendars.IS_PRIMARY + " DESC, " + CalendarContract.Calendars._ID + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
            }
        }
        return -1L;
    }

    public static boolean deleteEvent(Context context, Long eventId) {
        if (eventId == null || !hasCalendarPermissions(context)) return false;
        try {
            Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            return context.getContentResolver().delete(eventUri, null, null) > 0;
        } catch (SecurityException | IllegalArgumentException ex) {
            return false;
        }
    }
}
