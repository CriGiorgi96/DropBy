package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.models.Location;
import com.example.dropby.network.GlideHelper;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    public interface OnLocationClickListener {
        void onLocationClick(Location location);
    }

    private final List<Location> locations;
    private final OnLocationClickListener listener;

    public LocationAdapter(List<Location> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location loc = locations.get(position);

        holder.name.setText(loc.getName() != null ? loc.getName() : "—");
        holder.city.setText(loc.getCity() != null ? loc.getCity() : "");

        // Usa l'immagine esterna; fallback sull'interna se mancante
        String imageUrl = loc.resolveImageEstUrl();
        if (imageUrl == null) imageUrl = loc.resolveImageIntUrl();

        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(GlideHelper.url(imageUrl))   // GlideHelper aggiunge header ngrok
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onLocationClick(loc));
    }

    @Override
    public int getItemCount() { return locations.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  name, city;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name  = itemView.findViewById(R.id.locationNameText);
            city  = itemView.findViewById(R.id.locationMetaText);
            image = itemView.findViewById(R.id.locationImage);
        }
    }
}