package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.network.GlideHelper;

import java.util.List;
import java.util.function.Consumer;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.VH> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int eventId);
    }

    private List<FavoriteItem> items;
    private final Consumer<FavoriteItem> onRemove;
    private final OnFavoriteClickListener clickListener;

    public FavoriteAdapter(List<FavoriteItem> items, Consumer<FavoriteItem> onRemove,
                           OnFavoriteClickListener clickListener) {
        this.items         = items;
        this.onRemove      = onRemove;
        this.clickListener = clickListener;
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

        String imgUrl = item.getMainImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(holder.image.getContext())
                    .load(GlideHelper.url(imgUrl))
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.image_placeholder);
        }

        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onFavoriteClick(item.getEventId()));
        } else {
            holder.itemView.setOnClickListener(null);
        }

        if (onRemove != null) {
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.removeBtn.setOnClickListener(v -> onRemove.accept(item));
        } else {
            holder.removeBtn.setVisibility(View.GONE);
        }
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
