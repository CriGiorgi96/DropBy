package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Review;

import java.util.List;

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
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView username, stars, comment;
        VH(View v) {
            super(v);
            username = v.findViewById(R.id.reviewUsername);
            stars    = v.findViewById(R.id.reviewStars);
            comment  = v.findViewById(R.id.reviewComment);
        }
    }
}
