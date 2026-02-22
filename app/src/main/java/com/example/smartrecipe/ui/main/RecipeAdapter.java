package com.example.smartrecipe.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;

import java.util.ArrayList;
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
        holder.meta.setText("⏱ " + recipe.getMinutes() + " 分钟 · 🔥 " + recipe.getCalorie() + " kcal");
        holder.tags.setText("标签：" + joinList(recipe.getTags()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void replaceData(List<Recipe> list) {
        recipes.clear();
        if (list != null) recipes.addAll(list);
        notifyDataSetChanged();
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "暂无";
        List<String> out = new ArrayList<>();
        for (String item : list) {
            if (item != null && !item.trim().isEmpty()) out.add(item.trim());
        }
        return out.isEmpty() ? "暂无" : String.join("、", out);
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
