package com.example.smartrecipe.ui.recognize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IngredientCheckAdapter extends RecyclerView.Adapter<IngredientCheckAdapter.VH> {

    public static class IngredientItem {
        public final String name;
        public final Float accuracy;

        public IngredientItem(String name, Float accuracy) {
            this.name = name;
            this.accuracy = accuracy;
        }
    }

    private final List<IngredientItem> data;
    private final Set<String> selected = new LinkedHashSet<>();

    public IngredientCheckAdapter(List<IngredientItem> data) {
        this.data = data;
        // 默认全选
        if (data != null) {
            for (IngredientItem item : data) {
                selected.add(item.name);
            }
        }
    }

    public Set<String> getSelected() {
        return selected;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_check, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        IngredientItem item = data.get(position);
        holder.tvName.setText(item.name);
        if (item.accuracy != null) {
            holder.tvAccuracy.setText(String.format("准确率：%.1f%%", item.accuracy * 100f));
            holder.tvAccuracy.setVisibility(View.VISIBLE);
        } else {
            holder.tvAccuracy.setVisibility(View.GONE);
        }

        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(selected.contains(item.name));

        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selected.add(item.name);
            else selected.remove(item.name);
        });

        holder.itemView.setOnClickListener(v -> holder.cb.setChecked(!holder.cb.isChecked()));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;
        TextView tvName;
        TextView tvAccuracy;

        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cb);
            tvName = itemView.findViewById(R.id.tvName);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);
        }
    }
}
