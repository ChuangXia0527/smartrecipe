package com.example.smartrecipe.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final List<Recipe> recipes;
    private final OnItemClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnItemClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.meta.setText(recipe.getMinutes() + "分钟 · " + recipe.getCalorie() + "kcal");
        holder.tags.setText("标签：" + joinTags(recipe.getTags()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            sb.append(tags.get(i));
            if (i != tags.size() - 1) sb.append(" / ");
        }
        return sb.toString();
    }

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView meta;
        TextView tags;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvRecipeName);
            meta = itemView.findViewById(R.id.tvRecipeMeta);
            tags = itemView.findViewById(R.id.tvRecipeTags);
        }
    }
}
