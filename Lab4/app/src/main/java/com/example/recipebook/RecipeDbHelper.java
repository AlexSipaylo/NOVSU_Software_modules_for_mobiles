package com.example.recipebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "recipe_book.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "recipes";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CATEGORY = "category";
    public static final String COL_MINUTES = "prep_minutes";
    public static final String COL_INGREDIENTS = "ingredients";
    public static final String COL_STEPS = "steps";
    public static final String COL_IMAGE_PATH = "image_path";
    public static final String COL_EVENT_ID = "calendar_event_id";
    public static final String COL_PLANNED_AT = "planned_at";
    public static final String COL_WEEKLY = "weekly_repeat";

    public RecipeDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL DEFAULT '', " +
                COL_MINUTES + " INTEGER NOT NULL DEFAULT 0, " +
                COL_INGREDIENTS + " TEXT NOT NULL, " +
                COL_STEPS + " TEXT NOT NULL, " +
                COL_IMAGE_PATH + " TEXT, " +
                COL_EVENT_ID + " INTEGER, " +
                COL_PLANNED_AT + " INTEGER, " +
                COL_WEEKLY + " INTEGER NOT NULL DEFAULT 0" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = baseValues(recipe);
        values.put(COL_EVENT_ID, recipe.getCalendarEventId());
        values.put(COL_PLANNED_AT, recipe.getPlannedAt());
        values.put(COL_WEEKLY, recipe.isWeeklyRepeat() ? 1 : 0);
        return db.insertOrThrow(TABLE, null, values);
    }

    public int updateRecipe(Recipe recipe) {
        return getWritableDatabase().update(TABLE, baseValues(recipe),
                COL_ID + " = ?", new String[]{String.valueOf(recipe.getId())});
    }

    public void updateCalendarData(long recipeId, Long eventId, Long plannedAt, boolean weekly) {
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_ID, eventId);
        values.put(COL_PLANNED_AT, plannedAt);
        values.put(COL_WEEKLY, weekly ? 1 : 0);
        getWritableDatabase().update(TABLE, values, COL_ID + " = ?",
                new String[]{String.valueOf(recipeId)});
    }

    public int deleteRecipe(long id) {
        return getWritableDatabase().delete(TABLE, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Recipe getRecipe(long id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE, null,
                null, null, null, null, COL_ID + " DESC")) {
            while (cursor.moveToNext()) result.add(fromCursor(cursor));
        }
        return result;
    }

    private ContentValues baseValues(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, recipe.getTitle());
        values.put(COL_CATEGORY, recipe.getCategory() == null ? "" : recipe.getCategory());
        values.put(COL_MINUTES, recipe.getPrepMinutes());
        values.put(COL_INGREDIENTS, recipe.getIngredients());
        values.put(COL_STEPS, recipe.getSteps());
        values.put(COL_IMAGE_PATH, recipe.getImagePath());
        return values;
    }

    private Recipe fromCursor(Cursor cursor) {
        int eventIndex = cursor.getColumnIndexOrThrow(COL_EVENT_ID);
        int plannedIndex = cursor.getColumnIndexOrThrow(COL_PLANNED_AT);
        Long eventId = cursor.isNull(eventIndex) ? null : cursor.getLong(eventIndex);
        Long plannedAt = cursor.isNull(plannedIndex) ? null : cursor.getLong(plannedIndex);
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_MINUTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_INGREDIENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)),
                eventId,
                plannedAt,
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_WEEKLY)) == 1
        );
    }
}
