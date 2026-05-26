package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.dropby.models.Friend;
import com.example.dropby.models.UserSearchResult;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.GlideHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
=======
import com.example.dropby.R;
import com.example.dropby.models.Friend;
import com.example.dropby.models.UserSearchResult;

import java.util.ArrayList;
import java.util.List;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.VH> {

    public enum Mode   { FRIENDS, REQUESTS, SEARCH }
    public enum Action { ACCEPT, REJECT, SEND_REQUEST }

    public interface ActionListener {
        void onAction(Action action, Object item);
    }

<<<<<<< HEAD
    public interface OnAvatarClickListener {
        void onAvatarClick(int userId);
    }

    private List<Object> items = new ArrayList<>();
    private Mode mode = Mode.FRIENDS;
    private final ActionListener listener;
    private final OnAvatarClickListener avatarClickListener;

    public FriendAdapter(List<Friend> friends, ActionListener listener,
                         OnAvatarClickListener avatarClickListener) {
        this.items.addAll(friends);
        this.listener            = listener;
        this.avatarClickListener = avatarClickListener;
=======
    private List<Object> items = new ArrayList<>();
    private Mode mode = Mode.FRIENDS;
    private final ActionListener listener;

    public FriendAdapter(List<Friend> friends, ActionListener listener) {
        this.items.addAll(friends);
        this.listener = listener;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    }

    public void update(List<Friend> friends, Mode newMode) {
        this.mode = newMode;
        this.items.clear();
        this.items.addAll(friends);
        notifyDataSetChanged();
    }

    public void updateFromSearch(List<UserSearchResult> results) {
        this.mode = Mode.SEARCH;
        this.items.clear();
        this.items.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Object item = items.get(position);

        if (mode == Mode.SEARCH && item instanceof UserSearchResult) {
            UserSearchResult u = (UserSearchResult) item;
            holder.username.setText(u.getUsername());
            holder.status.setText(u.getEmail());
            holder.actionButtons.setVisibility(View.GONE);
            holder.sendRequestBtn.setVisibility(View.VISIBLE);
            holder.sendRequestBtn.setOnClickListener(v -> listener.onAction(Action.SEND_REQUEST, u));
<<<<<<< HEAD
            holder.avatar.setImageResource(R.drawable.default_guest);
            holder.avatar.setOnClickListener(null);
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        } else if (item instanceof Friend) {
            Friend f = (Friend) item;
            holder.username.setText(f.getUsername());
            holder.sendRequestBtn.setVisibility(View.GONE);

            if (mode == Mode.REQUESTS) {
                holder.status.setText("Richiesta in arrivo");
                holder.actionButtons.setVisibility(View.VISIBLE);
                holder.acceptBtn.setOnClickListener(v -> listener.onAction(Action.ACCEPT, f));
                holder.rejectBtn.setOnClickListener(v -> listener.onAction(Action.REJECT, f));
            } else {
                holder.status.setText("Amico");
                holder.actionButtons.setVisibility(View.GONE);
            }
<<<<<<< HEAD

            loadAvatar(holder, f.getProfileImage());

            int uid = f.getUserId();
            if (avatarClickListener != null && uid > 0) {
                holder.avatar.setOnClickListener(v -> avatarClickListener.onAvatarClick(uid));
            } else {
                holder.avatar.setOnClickListener(null);
            }
        }
    }

    private void loadAvatar(VH holder, String profileImage) {
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
    }

    private static boolean isServerFilename(String s) {
        if (s == null || s.isEmpty()) return false;
        String lower = s.toLowerCase(Locale.US);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".webp")
            || lower.endsWith(".gif");
    }

=======
        }
    }

>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
<<<<<<< HEAD
        ImageView avatar;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        TextView username, status;
        View actionButtons;
        Button acceptBtn, rejectBtn, sendRequestBtn;

        VH(View v) {
            super(v);
<<<<<<< HEAD
            avatar         = v.findViewById(R.id.friendAvatar);
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
            username       = v.findViewById(R.id.friendUsername);
            status         = v.findViewById(R.id.friendStatus);
            actionButtons  = v.findViewById(R.id.friendActionButtons);
            acceptBtn      = v.findViewById(R.id.btnAcceptFriend);
            rejectBtn      = v.findViewById(R.id.btnRejectFriend);
            sendRequestBtn = v.findViewById(R.id.btnSendRequest);
        }
    }
}
