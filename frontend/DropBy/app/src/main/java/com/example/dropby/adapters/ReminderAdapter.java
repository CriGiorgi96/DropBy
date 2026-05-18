package com.example.dropby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Reminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.VH> {

    public interface OnDeleteListener { void onDelete(Reminder r); }

    private List<Reminder> items;
    private final OnDeleteListener listener;

    public ReminderAdapter(List<Reminder> items, OnDeleteListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void update(List<Reminder> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Reminder r = items.get(pos);
        h.title.setText(r.getEventTitle() != null ? r.getEventTitle() : "Evento");
        h.city.setText(safe(r.getEventCity()));
        h.date.setText("Data evento: " + safe(r.getEventDate()));
        h.remindAt.setText("Promemoria: " + safe(r.getRemindAt()));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override public int getItemCount() { return items.size(); }

    private String safe(String s) { return s != null ? s : "—"; }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, city, date, remindAt;
        Button   btnDelete;
        VH(View v) {
            super(v);
            title     = v.findViewById(R.id.reminderEventTitle);
            city      = v.findViewById(R.id.reminderEventCity);
            date      = v.findViewById(R.id.reminderEventDate);
            remindAt  = v.findViewById(R.id.reminderTime);
            btnDelete = v.findViewById(R.id.btnDeleteReminder);
        }
    }
}
