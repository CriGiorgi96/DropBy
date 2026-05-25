package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Event;
import com.example.dropby.network.ImageLoader;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> events;

    private final OnEventClickListener listener;

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        private final ImageView posterImageView;

        private final TextView titleTextView;

        private final TextView dateBadgeTextView;

        private final TextView metaTextView;

        private final TextView descriptionTextView;

        EventViewHolder(View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.eventPosterImage);
            dateBadgeTextView = itemView.findViewById(R.id.eventDateBadge);
            titleTextView = itemView.findViewById(R.id.eventTitleText);
            metaTextView = itemView.findViewById(R.id.eventMetaText);
            descriptionTextView = itemView.findViewById(R.id.eventDescriptionText);
        }

        void bind(Event event, OnEventClickListener listener) {
            Event.EventReplica next = event.getNextReplica();
            String datePart = next != null ? next.getDatePart() : null;
            dateBadgeTextView.setText(textOrFallback(datePart, "Data da definire"));
            titleTextView.setText(textOrFallback(event.getTitle(), "Senza titolo"));
            metaTextView.setText(buildMeta(event, next));
            descriptionTextView.setText(textOrFallback(event.getDescription(), ""));

            String imageUrl = event.getMainImage() != null ? event.getMainImage().getUrl() : null;
            ImageLoader.loadInto(posterImageView, imageUrl);

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }

        private String buildMeta(Event event, Event.EventReplica next) {
            StringBuilder builder = new StringBuilder();
            appendPart(builder, next != null ? next.getLocationName() : null);
            appendPart(builder, next != null ? next.getTimePart()     : null);
            appendPart(builder, event.getCategory());
            return builder.toString();
        }

        private void appendPart(StringBuilder builder, String value) {
            if (value == null || value.trim().isEmpty()) {
                return;
            }
            if (builder.length() > 0) {
                builder.append(" - ");
            }
            builder.append(value.trim());
        }

        private String textOrFallback(String value, String fallback) {
            if (value == null || value.trim().isEmpty()) {
                return fallback;
            }
            return value;
        }
    }
}
