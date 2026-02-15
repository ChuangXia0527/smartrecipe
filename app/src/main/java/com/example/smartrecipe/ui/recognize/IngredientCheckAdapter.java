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

    private final List<String> data;
    private final Set<String> selected = new LinkedHashSet<>();

    public IngredientCheckAdapter(List<String> data) {
        this.data = data;
        // 默认全选
        if (data != null) selected.addAll(data);
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
        String ing = data.get(position);
        holder.tvName.setText(ing);

        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(selected.contains(ing));

        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selected.add(ing);
            else selected.remove(ing);
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

        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cb);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
