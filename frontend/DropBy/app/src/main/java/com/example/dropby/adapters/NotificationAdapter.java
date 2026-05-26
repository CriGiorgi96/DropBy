package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<Notification> items;
    private final Consumer<Notification> onMarkRead;
<<<<<<< HEAD
    private final Consumer<Notification> onDelete;

    public NotificationAdapter(List<Notification> items,
                               Consumer<Notification> onMarkRead,
                               Consumer<Notification> onDelete) {
        this.items      = new ArrayList<>(items);
        this.onMarkRead = onMarkRead;
        this.onDelete   = onDelete;
=======

    public NotificationAdapter(List<Notification> items, Consumer<Notification> onMarkRead) {
        this.items      = new ArrayList<>(items);
        this.onMarkRead = onMarkRead;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    }

    public void update(List<Notification> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public List<Notification> getItems() { return items; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification n = items.get(position);

        holder.type.setText(n.getTypeLabel());
        holder.content.setText(n.getContent() != null ? n.getContent() : "");
        holder.date.setText(n.getCreatedAt() != null ? n.getCreatedAt() : "");
        holder.unreadDot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);

        holder.markReadBtn.setOnClickListener(v -> onMarkRead.accept(n));
        holder.markReadBtn.setEnabled(!n.isRead());
        holder.markReadBtn.setAlpha(n.isRead() ? 0.3f : 1f);
<<<<<<< HEAD

        holder.deleteBtn.setOnClickListener(v -> onDelete.accept(n));
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView type, content, date;
<<<<<<< HEAD
        View     unreadDot;
        Button   markReadBtn, deleteBtn;
=======
        View unreadDot;
        Button markReadBtn;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

        VH(View v) {
            super(v);
            type       = v.findViewById(R.id.notifType);
            content    = v.findViewById(R.id.notifContent);
            date       = v.findViewById(R.id.notifDate);
            unreadDot  = v.findViewById(R.id.unreadDot);
<<<<<<< HEAD
            markReadBtn = v.findViewById(R.id.btnMarkRead);
            deleteBtn   = v.findViewById(R.id.btnDeleteNotification);
=======
            markReadBtn= v.findViewById(R.id.btnMarkRead);
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        }
    }
}
