package com.example.recipebook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends Activity {
    private RecipeDbHelper database;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private TextView recipeCount;
    private Button toggleViewButton;
    private SharedPreferences preferences;
    private boolean gridMode;
    private Parcelable savedRecyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new RecipeDbHelper(this);
        preferences = getSharedPreferences("recipe_book_settings", MODE_PRIVATE);
        gridMode = preferences.getBoolean("grid_mode", true);

        recyclerView = findViewById(R.id.rvRecipes);
        emptyState = findViewById(R.id.emptyState);
        recipeCount = findViewById(R.id.tvRecipeCount);
        toggleViewButton = findViewById(R.id.btnToggleView);
        Button addButton = findViewById(R.id.btnAddRecipe);

        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        applyListMode();
        if (savedInstanceState != null) {
            savedRecyclerState = savedInstanceState.getParcelable("recycler_state");
            recyclerView.post(() -> {
                if (savedRecyclerState != null && recyclerView.getLayoutManager() != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerState);
                    savedRecyclerState = null;
                }
            });
        }

        toggleViewButton.setOnClickListener(v -> {
            gridMode = !gridMode;
            preferences.edit().putBoolean("grid_mode", gridMode).apply();
            applyListMode();
        });

        addButton.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && adapter != null) refreshRecipes();
    }

    private void applyListMode() {
        if (gridMode) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            toggleViewButton.setText("Вид: сетка");
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            toggleViewButton.setText("Вид: список");
        }
    }

    private void refreshRecipes() {
        List<Recipe> recipes = database.getAllRecipes();
        adapter.submitList(recipes);
        recipeCount.setText(recipes.size() + " рецептов");
        boolean isEmpty = recipes.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            outState.putParcelable("recycler_state", recyclerView.getLayoutManager().onSaveInstanceState());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
