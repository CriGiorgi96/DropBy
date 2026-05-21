package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private List<Review> items;

    public ReviewAdapter(List<Review> items) { this.items = items; }

    public void update(List<Review> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = items.get(position);
        holder.username.setText(r.getUsername() != null ? r.getUsername() : "Anonimo");
        holder.stars.setText(r.getStarsString());
        holder.comment.setText(r.getComment() != null ? r.getComment() : "");

        String loc  = r.getLocationName();
        String date = formatDate(r.getEventDate());
        if (loc != null || date != null) {
            StringBuilder sb = new StringBuilder();
            if (loc  != null) sb.append(loc);
            if (loc  != null && date != null) sb.append("  ·  ");
            if (date != null) sb.append(date);
            holder.locationDate.setText(sb.toString());
            holder.locationDate.setVisibility(View.VISIBLE);
        } else {
            holder.locationDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            String normalized = raw.length() > 10 ? raw.substring(0, 10) : raw;
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", new Locale("it", "IT"));
            Date d = in.parse(normalized);
            return d != null ? out.format(d) : null;
        } catch (Exception e) {
            return null;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView username, stars, comment, locationDate;
        VH(View v) {
            super(v);
            username     = v.findViewById(R.id.reviewUsername);
            stars        = v.findViewById(R.id.reviewStars);
            comment      = v.findViewById(R.id.reviewComment);
            locationDate = v.findViewById(R.id.reviewLocationDate);
        }
    }
}
