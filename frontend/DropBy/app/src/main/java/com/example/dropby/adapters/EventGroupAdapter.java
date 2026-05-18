package com.example.dropby.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.models.EventGroup;
import com.example.dropby.network.GlideHelper;

import java.util.List;

/**
 * Adapter per liste di EventGroup.
 * Usato in TheaterDetailFragment (showVenue=false) e
 * in EventDetailFragment per le "altre repliche" (showVenue=true).
 */
public class EventGroupAdapter extends RecyclerView.Adapter<EventGroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(EventGroup group);
    }

    private final List<EventGroup> groups;
    private final OnGroupClickListener listener;
    private final boolean showVenue; // mostra il nome del teatro accanto a ogni data?

    public EventGroupAdapter(List<EventGroup> groups,
                             boolean showVenue,
                             OnGroupClickListener listener) {
        this.groups    = groups;
        this.showVenue = showVenue;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventGroup group = groups.get(position);
        Context ctx = holder.itemView.getContext();

        holder.title.setText(group.getTitle() != null ? group.getTitle() : "—");
        holder.category.setText(group.getCategory() != null ? group.getCategory() : "");

        // Immagine locandina
        if (group.getMainImage() != null && group.getMainImage().getUrl() != null) {
            Glide.with(ctx)
                    .load(GlideHelper.url(group.getMainImage().getUrl()))
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.image_placeholder);
        }

        // Date dinamiche: costruisci le righe nel datesContainer
        holder.datesContainer.removeAllViews();
        for (EventGroup.Occurrence occ : group.getOccurrences()) {
            TextView row = new TextView(ctx);
            row.setText(occ.toDisplayString(showVenue));
            row.setTextSize(13f);
            row.setPadding(0, 4, 0, 4);
            row.setTextColor(0xFF6A6274); // grigio brand
            holder.datesContainer.addView(row);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(group);
        });
    }

    @Override
    public int getItemCount() { return groups.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView     poster;
        TextView      title, category;
        LinearLayout  datesContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster         = itemView.findViewById(R.id.groupPosterImage);
            title          = itemView.findViewById(R.id.groupTitleText);
            category       = itemView.findViewById(R.id.groupCategoryText);
            datesContainer = itemView.findViewById(R.id.groupDatesContainer);
        }
    }
}