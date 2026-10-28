package com.example.recipebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder> {
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK = new DiffUtil.ItemCallback<Recipe>() {
        @Override
        public boolean areItemsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getCategory().equals(newItem.getCategory())
                    && oldItem.getPrepMinutes() == newItem.getPrepMinutes()
                    && safeEquals(oldItem.getImagePath(), newItem.getImagePath())
                    && oldItem.getIngredients().equals(newItem.getIngredients())
                    && oldItem.getSteps().equals(newItem.getSteps());
        }

        private boolean safeEquals(String first, String second) {
            return first == null ? second == null : first.equals(second);
        }
    };

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = getItem(position);
        holder.title.setText(recipe.getTitle());
        String category = recipe.getCategory() == null || recipe.getCategory().trim().isEmpty()
                ? "Без категории" : recipe.getCategory();
        String time = recipe.getPrepMinutes() > 0 ? recipe.getPrepMinutes() + " мин" : "Время не указано";
        holder.meta.setText(category + "  ·  " + time);
        Glide.with(holder.photo)
                .load(recipe.getImagePath())
                .override(600, 420)
                .centerCrop()
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(holder.photo);
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final ImageView photo;
        final TextView title;
        final TextView meta;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.ivRecipePhoto);
            title = itemView.findViewById(R.id.tvRecipeTitle);
            meta = itemView.findViewById(R.id.tvRecipeMeta);
        }
    }
}
