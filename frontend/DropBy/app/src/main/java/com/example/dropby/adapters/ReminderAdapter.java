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

<<<<<<< HEAD
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
=======
import java.util.List;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

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
<<<<<<< HEAD
        h.date.setText("Data evento: " + formatDate(r.getEventDate()));
        h.remindAt.setText("Promemoria: " + formatDateTime(r.getRemindAt()));
=======
        h.date.setText("Data evento: " + safe(r.getEventDate()));
        h.remindAt.setText("Promemoria: " + safe(r.getRemindAt()));
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override public int getItemCount() { return items.size(); }

<<<<<<< HEAD
    private String formatDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            String s   = raw.trim();
            String base = s.length() >= 19 ? s.substring(0, 19) : s;

            SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy – HH:mm", new Locale("it", "IT"));
            sdfOut.setTimeZone(TimeZone.getDefault());

            Date date;
            if (s.length() > 19) {
                // Normalizza "+02:00" / "Z" → "+0200" (RFC 822) per il pattern 'Z'
                String off = s.substring(19).replace(":", "").trim();
                if (off.startsWith("Z") || off.isEmpty()) off = "+0000";
                if (off.length() > 5) off = off.substring(0, 5);
                SimpleDateFormat sdfTz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                date = sdfTz.parse(base + off);
            } else {
                // Stringa naive → trattata come UTC
                SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = sdfUtc.parse(base);
            }

            return sdfOut.format(date);
        } catch (Exception e) {
            return raw;
        }
    }

    /**
     * Converte una stringa data "YYYY-MM-DD" nel formato leggibile "17 mag 2026".
     */
    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            String normalized = raw.length() > 10 ? raw.substring(0, 10) : raw;
            SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy", new Locale("it", "IT"));
            return sdfOut.format(sdfIn.parse(normalized));
        } catch (Exception e) {
            return raw;
        }
    }

=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
