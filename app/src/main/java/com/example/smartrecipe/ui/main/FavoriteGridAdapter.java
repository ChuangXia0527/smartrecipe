package com.example.smartrecipe.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;

import java.util.List;

public class FavoriteGridAdapter extends RecyclerView.Adapter<FavoriteGridAdapter.FavViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnItemClickListener listener;

    public FavoriteGridAdapter(List<Recipe> recipes, OnItemClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @Override
    public FavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_grid, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.cover.setImageResource(R.mipmap.ic_launcher);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView name;

        FavViewHolder(View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.ivFavCover);
            name = itemView.findViewById(R.id.tvFavName);
        }
    }
}
