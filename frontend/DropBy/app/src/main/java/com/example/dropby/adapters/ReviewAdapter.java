package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD
import android.widget.ImageView;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.models.Review;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.GlideHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    public interface OnAvatarClickListener {
        void onAvatarClick(int userId);
    }

    public interface OnReviewClickListener {
        void onReviewClick(int eventId);
    }

    private List<Review> items;
    private final int currentUserId;
    private final OnAvatarClickListener avatarClickListener;
    private final OnReviewClickListener reviewClickListener;

    public ReviewAdapter(List<Review> items, int currentUserId,
                         OnAvatarClickListener avatarClickListener,
                         OnReviewClickListener reviewClickListener) {
        this.items               = items;
        this.currentUserId       = currentUserId;
        this.avatarClickListener = avatarClickListener;
        this.reviewClickListener = reviewClickListener;
    }
=======
import com.example.dropby.R;
import com.example.dropby.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private List<Review> items;

    public ReviewAdapter(List<Review> items) { this.items = items; }
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

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
<<<<<<< HEAD

        holder.username.setText(r.getUsername() != null ? r.getUsername() : "Anonimo");
        holder.stars.setText(r.getStarsString());
        holder.comment.setText(r.getComment() != null ? r.getComment() : "");

        bindAvatar(holder, r);

        if (r.getEventTitle() != null && !r.getEventTitle().isEmpty()) {
            holder.eventTitle.setText(r.getEventTitle());
            holder.eventTitle.setVisibility(View.VISIBLE);
        } else {
            holder.eventTitle.setVisibility(View.GONE);
        }

        if (reviewClickListener != null && r.getEventId() > 0) {
            holder.itemView.setOnClickListener(v -> reviewClickListener.onReviewClick(r.getEventId()));
        } else {
            holder.itemView.setOnClickListener(null);
        }

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

    private static boolean isServerFilename(String s) {
        if (s == null || s.isEmpty()) return false;
        String lower = s.toLowerCase(Locale.US);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".webp")
            || lower.endsWith(".gif");
    }

    private void bindAvatar(VH holder, Review r) {
        String profileImage = r.getProfileImage();
        android.content.Context ctx = holder.avatar.getContext();

        Object loadSource;
        if (isServerFilename(profileImage)) {
            String url = ApiClient.getBaseUrl() + "assets/avatars/" + profileImage;
            loadSource = GlideHelper.url(url);
        } else if (profileImage != null && !profileImage.isEmpty()) {
            int resId = ctx.getResources().getIdentifier(
                    profileImage, "drawable", ctx.getPackageName());
            loadSource = resId != 0 ? resId : R.drawable.default_guest;
        } else {
            loadSource = R.drawable.default_guest;
        }

        Glide.with(ctx)
                .load(loadSource)
                .placeholder(R.drawable.default_guest)
                .circleCrop()
                .into(holder.avatar);

        boolean isOtherUser = avatarClickListener != null && r.getUserId() != currentUserId;
        if (isOtherUser) {
            holder.avatar.setOnClickListener(v -> avatarClickListener.onAvatarClick(r.getUserId()));
            holder.avatar.setAlpha(1.0f);
        } else {
            holder.avatar.setOnClickListener(null);
            holder.avatar.setAlpha(0.75f);
        }
=======
        holder.username.setText(r.getUsername() != null ? r.getUsername() : "Anonimo");
        holder.stars.setText(r.getStarsString());
        holder.comment.setText(r.getComment() != null ? r.getComment() : "");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    }

    @Override
    public int getItemCount() { return items.size(); }

<<<<<<< HEAD
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
        ImageView avatar;
        TextView  username, stars, comment, eventTitle, locationDate;

        VH(View v) {
            super(v);
            avatar       = v.findViewById(R.id.reviewAvatar);
            username     = v.findViewById(R.id.reviewUsername);
            stars        = v.findViewById(R.id.reviewStars);
            comment      = v.findViewById(R.id.reviewComment);
            eventTitle   = v.findViewById(R.id.reviewEventTitle);
            locationDate = v.findViewById(R.id.reviewLocationDate);
=======
    static class VH extends RecyclerView.ViewHolder {
        TextView username, stars, comment;
        VH(View v) {
            super(v);
            username = v.findViewById(R.id.reviewUsername);
            stars    = v.findViewById(R.id.reviewStars);
            comment  = v.findViewById(R.id.reviewComment);
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        }
    }
}
