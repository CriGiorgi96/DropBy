package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.network.ImageLoader;

import java.util.List;
import java.util.function.Consumer;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.VH> {

    private List<FavoriteItem> items;
    private final Consumer<FavoriteItem> onRemove;

    public FavoriteAdapter(List<FavoriteItem> items, Consumer<FavoriteItem> onRemove) {
        this.items    = items;
        this.onRemove = onRemove;
    }

    public void update(List<FavoriteItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FavoriteItem item = items.get(position);

        holder.title.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.category.setText(item.getCategory() != null ? item.getCategory() : "");

        StringBuilder meta = new StringBuilder();
        if (item.getCity() != null)      meta.append(item.getCity());
        if (item.getEventDate() != null) meta.append(" · ").append(item.getEventDate());
        holder.meta.setText(meta.toString());

        ImageLoader.loadInto(holder.image, item.getMainImageUrl());

        holder.removeBtn.setOnClickListener(v -> onRemove.accept(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, category, meta;
        ImageButton removeBtn;

        VH(View v) {
            super(v);
            image     = v.findViewById(R.id.favoriteImage);
            title     = v.findViewById(R.id.favoriteTitle);
            category  = v.findViewById(R.id.favoriteCategory);
            meta      = v.findViewById(R.id.favoriteMeta);
            removeBtn = v.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
