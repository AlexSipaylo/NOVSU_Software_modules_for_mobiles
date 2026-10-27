package com.example.recipebook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends Activity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final int REQUEST_CALENDAR_PERMISSION = 41;
    private static final int REQUEST_EDIT_RECIPE = 42;

    private RecipeDbHelper database;
    private Recipe recipe;
    private ImageView photoView;
    private TextView titleView;
    private TextView metaView;
    private TextView ingredientsView;
    private TextView stepsView;
    private long recipeId;
    private boolean waitingForCalendarPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        database = new RecipeDbHelper(this);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);

        photoView = findViewById(R.id.ivDetailPhoto);
        titleView = findViewById(R.id.tvDetailTitle);
        metaView = findViewById(R.id.tvDetailMeta);
        ingredientsView = findViewById(R.id.tvDetailIngredients);
        stepsView = findViewById(R.id.tvDetailSteps);
        Button viewPhotoButton = findViewById(R.id.btnViewPhoto);
        Button planButton = findViewById(R.id.btnPlanCooking);
        Button editButton = findViewById(R.id.btnEditRecipe);
        Button deleteButton = findViewById(R.id.btnDeleteRecipe);

        photoView.setOnClickListener(v -> openPhotoViewer());
        viewPhotoButton.setOnClickListener(v -> openPhotoViewer());
        planButton.setOnClickListener(v -> requestCalendarAndPlan());
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecipeFormActivity.class);
            intent.putExtra(RecipeFormActivity.EXTRA_RECIPE_ID, recipeId);
            startActivityForResult(intent, REQUEST_EDIT_RECIPE);
        });
        deleteButton.setOnClickListener(v -> confirmDelete());
        loadRecipe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null) loadRecipe();
    }

    private void loadRecipe() {
        recipe = database.getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }
        titleView.setText(recipe.getTitle());
        String category = recipe.getCategory() == null || recipe.getCategory().trim().isEmpty()
                ? "Без категории" : recipe.getCategory();
        String time = recipe.getPrepMinutes() > 0 ? recipe.getPrepMinutes() + " минут" : "Время не указано";
        metaView.setText(category + "  ·  " + time);
        ingredientsView.setText(recipe.getIngredients());
        stepsView.setText(recipe.getSteps());
        Glide.with(this).load(recipe.getImagePath())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food).centerCrop().into(photoView);
        findViewById(R.id.btnViewPhoto).setVisibility(
                recipe.getImagePath() == null ? View.GONE : View.VISIBLE);
    }

    private void openPhotoViewer() {
        if (recipe == null || recipe.getImagePath() == null) return;
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, recipe.getImagePath());
        startActivity(intent);
    }

    private void requestCalendarAndPlan() {
        if (CalendarHelper.hasCalendarPermissions(this)) {
            showDatePicker();
        } else {
            waitingForCalendarPermission = true;
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR},
                    REQUEST_CALENDAR_PERMISSION);
        }
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> showTimePicker(year, month, dayOfMonth),
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dateDialog.setTitle("Дата приготовления");
        dateDialog.show();
    }

    private void showTimePicker(int year, int month, int day) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timeDialog = new TimePickerDialog(this,
                (view, hour, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, day);
                    selected.set(Calendar.HOUR_OF_DAY, hour);
                    selected.set(Calendar.MINUTE, minute);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    if (selected.getTimeInMillis() < System.currentTimeMillis()) {
                        Toast.makeText(this, "Выберите время в будущем", Toast.LENGTH_LONG).show();
                        return;
                    }
                    showRepeatOptions(selected.getTimeInMillis());
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        timeDialog.setTitle("Время начала");
        timeDialog.show();
    }

    private void showRepeatOptions(long startMs) {
        new AlertDialog.Builder(this)
                .setTitle("Как повторять готовку?")
                .setItems(new String[]{"Один раз", "Каждую неделю"}, (dialog, which) ->
                        checkConflictsAndCreate(startMs, which == 1))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void checkConflictsAndCreate(long startMs, boolean weekly) {
        long endMs = startMs + 60L * 60L * 1000L;
        List<String> conflicts = CalendarHelper.findConflicts(this, startMs, endMs,
                recipe.getCalendarEventId());
        if (conflicts.isEmpty()) {
            createCalendarEvent(startMs, weekly);
            return;
        }
        String message = "На это время уже запланировано:\n\n" + String.join("\n", conflicts)
                + "\n\nВсё равно создать событие?";
        new AlertDialog.Builder(this)
                .setTitle("Возможное наложение")
                .setMessage(message)
                .setPositiveButton("Создать", (dialog, which) -> createCalendarEvent(startMs, weekly))
                .setNegativeButton("Выбрать другое время", null)
                .show();
    }

    private void createCalendarEvent(long startMs, boolean weekly) {
        try {
            long eventId = CalendarHelper.createCookingEvent(this, recipe, startMs, weekly);
            database.updateCalendarData(recipe.getId(), eventId, startMs, weekly);
            loadRecipe();
            String formatted = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(startMs));
            String repeatText = weekly ? "с еженедельным повторением" : "без повторения";
            Toast.makeText(this, "Готовка запланирована на " + formatted + ", " + repeatText,
                    Toast.LENGTH_LONG).show();
        } catch (SecurityException ex) {
            Toast.makeText(this, "Нет разрешения на календарь. Остальные функции доступны.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Не удалось создать событие: " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALENDAR_PERMISSION && waitingForCalendarPermission) {
            waitingForCalendarPermission = false;
            boolean granted = grantResults.length >= 2;
            for (int result : grantResults) granted = granted && result == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                showDatePicker();
            } else {
                Toast.makeText(this,
                        "Доступ к календарю не предоставлен. Книга рецептов продолжает работать.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить рецепт?")
                .setMessage("Рецепт и его сохранённая фотография будут удалены. Отменить это действие нельзя.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) -> deleteRecipe())
                .show();
    }

    private void deleteRecipe() {
        boolean eventRemoved = true;
        if (recipe.getCalendarEventId() != null) {
            eventRemoved = CalendarHelper.deleteEvent(this, recipe.getCalendarEventId());
        }
        ImageUtils.deleteStoredImage(recipe.getImagePath());
        database.deleteRecipe(recipe.getId());
        if (!eventRemoved) {
            Toast.makeText(this,
                    "Рецепт удалён. Событие календаря не удалось удалить без разрешения календаря.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Рецепт удалён", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_RECIPE && resultCode == RESULT_OK) loadRecipe();
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
