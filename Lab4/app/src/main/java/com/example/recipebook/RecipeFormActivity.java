package com.example.recipebook;

import androidx.activity.ComponentActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeFormActivity extends ComponentActivity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private RecipeDbHelper database;
    private EditText titleInput;
    private EditText categoryInput;
    private EditText minutesInput;
    private EditText ingredientsInput;
    private EditText stepsInput;
    private ImageView photoView;
    private ProgressBar photoProgress;
    private Button saveButton;
    private Button choosePhotoButton;
    private Button takePhotoButton;
    private ExecutorService imageExecutor;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPicker;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraUri;
    private File cameraTempFile;

    private long recipeId = -1L;
    private Recipe originalRecipe;
    private String currentImagePath;
    private String newlyProcessedImagePath;
    private boolean processing;
    private boolean saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        database = new RecipeDbHelper(this);
        imageExecutor = Executors.newSingleThreadExecutor();
        bindViews();
        registerImageLaunchers();

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);
        if (recipeId >= 0) {
            originalRecipe = database.getRecipe(recipeId);
            if (originalRecipe != null) fillForm(originalRecipe);
        }

        choosePhotoButton.setOnClickListener(v -> photoPicker.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        takePhotoButton.setOnClickListener(v -> openCamera());
        saveButton.setOnClickListener(v -> saveRecipe());
    }

    private void bindViews() {
        TextView formTitle = findViewById(R.id.tvFormTitle);
        titleInput = findViewById(R.id.etTitle);
        categoryInput = findViewById(R.id.etCategory);
        minutesInput = findViewById(R.id.etMinutes);
        ingredientsInput = findViewById(R.id.etIngredients);
        stepsInput = findViewById(R.id.etSteps);
        photoView = findViewById(R.id.ivFormPhoto);
        photoProgress = findViewById(R.id.progressPhoto);
        saveButton = findViewById(R.id.btnSaveRecipe);
        choosePhotoButton = findViewById(R.id.btnChoosePhoto);
        takePhotoButton = findViewById(R.id.btnTakePhoto);
        formTitle.setText("Новый рецепт");
    }

    private void registerImageLaunchers() {
        photoPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) processImage(uri);
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (Boolean.TRUE.equals(success) && cameraUri != null) {
                processImage(cameraUri);
            } else {
                deleteCameraTempFile();
                Toast.makeText(this, "Фотография не сделана", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillForm(Recipe recipe) {
        ((TextView) findViewById(R.id.tvFormTitle)).setText("Редактирование рецепта");
        titleInput.setText(recipe.getTitle());
        categoryInput.setText(recipe.getCategory());
        minutesInput.setText(recipe.getPrepMinutes() > 0 ? String.valueOf(recipe.getPrepMinutes()) : "");
        ingredientsInput.setText(recipe.getIngredients());
        stepsInput.setText(recipe.getSteps());
        currentImagePath = recipe.getImagePath();
        Glide.with(this).load(currentImagePath).placeholder(R.drawable.placeholder_food).into(photoView);
        saveButton.setText("Сохранить изменения");
    }

    private void openCamera() {
        try {
            File cacheDirectory = new File(getCacheDir(), "images");
            if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
                throw new IOException("Не удалось создать временную папку");
            }
            File photo = File.createTempFile("recipe_camera_", ".jpg", cacheDirectory);
            cameraTempFile = photo;
            cameraUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photo);
            cameraLauncher.launch(cameraUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Не удалось открыть камеру: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processImage(Uri sourceUri) {
        if (processing) return;
        setProcessing(true);
        imageExecutor.execute(() -> {
            String outputPath = null;
            String error = null;
            try {
                outputPath = ImageUtils.compressAndStore(getApplicationContext(), sourceUri);
            } catch (Exception ex) {
                error = ex.getMessage() == null ? "ошибка обработки изображения" : ex.getMessage();
            }
            if (cameraUri != null && cameraUri.equals(sourceUri)) deleteCameraTempFile();
            final String processedPath = outputPath;
            final String processingError = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    if (processedPath != null) ImageUtils.deleteStoredImage(processedPath);
                    return;
                }
                setProcessing(false);
                if (processedPath != null) {
                    String originalPath = originalRecipe == null ? null : originalRecipe.getImagePath();
                    if (newlyProcessedImagePath != null
                            && !newlyProcessedImagePath.equals(originalPath)) {
                        ImageUtils.deleteStoredImage(newlyProcessedImagePath);
                    }
                    newlyProcessedImagePath = processedPath;
                    currentImagePath = processedPath;
                    Glide.with(this).load(currentImagePath).placeholder(R.drawable.placeholder_food).into(photoView);
                    Toast.makeText(this, "Фото сжато и сохранено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Не удалось обработать фото: " + processingError,
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setProcessing(boolean value) {
        processing = value;
        photoProgress.setVisibility(value ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!value);
        choosePhotoButton.setEnabled(!value);
        takePhotoButton.setEnabled(!value);
    }

    private void deleteCameraTempFile() {
        if (cameraTempFile != null && cameraTempFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cameraTempFile.delete();
        }
        cameraTempFile = null;
        cameraUri = null;
    }

    private void saveRecipe() {
        String title = titleInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String ingredients = ingredientsInput.getText().toString().trim();
        String steps = stepsInput.getText().toString().trim();
        int minutes = 0;
        String rawMinutes = minutesInput.getText().toString().trim();
        if (!rawMinutes.isEmpty()) {
            try {
                minutes = Integer.parseInt(rawMinutes);
            } catch (NumberFormatException ex) {
                minutes = 0;
            }
        }

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Заполните название, ингредиенты и описание", Toast.LENGTH_LONG).show();
            return;
        }
        if (minutes < 0) {
            Toast.makeText(this, "Время не может быть отрицательным", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentImagePath == null || currentImagePath.trim().isEmpty()) {
            Toast.makeText(this, "Добавьте фотографию блюда", Toast.LENGTH_LONG).show();
            return;
        }

        Recipe recipe = new Recipe(
                recipeId,
                title,
                category,
                minutes,
                ingredients,
                steps,
                currentImagePath,
                originalRecipe == null ? null : originalRecipe.getCalendarEventId(),
                originalRecipe == null ? null : originalRecipe.getPlannedAt(),
                originalRecipe != null && originalRecipe.isWeeklyRepeat());

        try {
            if (recipeId >= 0) {
                database.updateRecipe(recipe);
                if (originalRecipe != null && originalRecipe.getImagePath() != null
                        && !originalRecipe.getImagePath().equals(currentImagePath)) {
                    ImageUtils.deleteStoredImage(originalRecipe.getImagePath());
                }
            } else {
                long newId = database.insertRecipe(recipe);
                recipe.setId(newId);
            }
            saved = true;
            Toast.makeText(this, "Рецепт сохранён", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent());
            finish();
        } catch (Exception ex) {
            Toast.makeText(this, "Ошибка сохранения: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (!saved && newlyProcessedImagePath != null) {
            ImageUtils.deleteStoredImage(newlyProcessedImagePath);
        }
        deleteCameraTempFile();
        if (imageExecutor != null) imageExecutor.shutdown();
        if (database != null) database.close();
        super.onDestroy();
    }
}
