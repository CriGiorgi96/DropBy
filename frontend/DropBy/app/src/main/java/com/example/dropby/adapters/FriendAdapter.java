package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Friend;
import com.example.dropby.models.UserSearchResult;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.VH> {

    public enum Mode   { FRIENDS, REQUESTS, SEARCH }
    public enum Action { ACCEPT, REJECT, SEND_REQUEST }

    public interface ActionListener {
        void onAction(Action action, Object item);
    }

    private List<Object> items = new ArrayList<>();
    private Mode mode = Mode.FRIENDS;
    private final ActionListener listener;

    public FriendAdapter(List<Friend> friends, ActionListener listener) {
        this.items.addAll(friends);
        this.listener = listener;
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
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView username, status;
        View actionButtons;
        Button acceptBtn, rejectBtn, sendRequestBtn;

        VH(View v) {
            super(v);
            username       = v.findViewById(R.id.friendUsername);
            status         = v.findViewById(R.id.friendStatus);
            actionButtons  = v.findViewById(R.id.friendActionButtons);
            acceptBtn      = v.findViewById(R.id.btnAcceptFriend);
            rejectBtn      = v.findViewById(R.id.btnRejectFriend);
            sendRequestBtn = v.findViewById(R.id.btnSendRequest);
        }
    }
}
