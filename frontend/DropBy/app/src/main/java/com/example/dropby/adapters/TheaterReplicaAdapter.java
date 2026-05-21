package com.example.dropby.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.EventActionsHelper;
import com.example.dropby.R;
import com.example.dropby.models.Event;
import com.example.dropby.models.Location;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.GlideHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter per la sezione "In scena anche a…" di EventDetailFragment.
 * Mostra una card per ogni teatro/location con foto, nome, città e le date
 * in cui lo spettacolo è in programma in quella sede.
 */
public class TheaterReplicaAdapter
        extends RecyclerView.Adapter<TheaterReplicaAdapter.VH> {

    // ── Modello dati ─────────────────────────────────────────────────────────

    public static class LocationGroup {
        public final int    locationId;
        public final String locationName;
        public final String city;
        public final String imageEst;   // filename, es. "teatro_x_est.jpg"
        public final String imageInt;   // filename, es. "teatro_x_int.jpg"
        public final List<String[]> slots = new ArrayList<>(); // [date, startTime]
        public Event representative;    // earliest occurrence — used by EventActionsHelper

        public LocationGroup(int locationId, String locationName, String city,
                             String imageEst, String imageInt) {
            this.locationId   = locationId;
            this.locationName = locationName;
            this.city         = city;
            this.imageEst     = imageEst;
            this.imageInt     = imageInt;
        }
    }

    // ── Interfaccia click ────────────────────────────────────────────────────

    public interface OnClickListener {
        void onClick(LocationGroup group);
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private final List<LocationGroup> items;
    private final OnClickListener     listener;

    public TheaterReplicaAdapter(List<LocationGroup> items, OnClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theater_replica, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        LocationGroup g = items.get(pos);

        h.name.setText(g.locationName != null ? g.locationName : "—");
        h.city.setText(g.city        != null ? g.city         : "—");

        // Foto teatro: prova esterna, poi interna, poi placeholder
        String imageUrl = ApiClient.resolveLocationImage(g.imageEst, Location.LocationImageType.EXTERIOR);
        if (imageUrl == null)
            imageUrl = ApiClient.resolveLocationImage(g.imageInt, Location.LocationImageType.INTERIOR);

        if (imageUrl != null) {
            Glide.with(h.itemView.getContext())
                    .load(GlideHelper.url(imageUrl))
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .into(h.image);
        } else {
            h.image.setImageResource(R.drawable.image_placeholder);
        }

        // Righe data/orario
        h.datesContainer.removeAllViews();
        for (String[] slot : g.slots) {
            TextView row = new TextView(h.itemView.getContext());
            row.setText(formatSlot(slot));
            row.setTextSize(12f);
            row.setPadding(0, 3, 0, 3);
            row.setTextColor(0xFF6A6274);
            h.datesContainer.addView(row);
        }

        // Bottone azioni rapide: visibile solo se loggato e c'è un evento rappresentativo
        SharedPreferences prefs = h.itemView.getContext()
                .getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean isLogged = prefs.getString("token", null) != null;
        h.btnActions.setVisibility(isLogged && g.representative != null ? View.VISIBLE : View.GONE);
        h.btnActions.setOnClickListener(v -> {
            Activity activity = (Activity) h.itemView.getContext();
            new EventActionsHelper(activity, g.representative).showMenu(v);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(g);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── Formattazione ────────────────────────────────────────────────────────

    private String formatSlot(String[] slot) {
        String rawDate = slot != null && slot.length > 0 ? slot[0] : null;
        String rawTime = slot != null && slot.length > 1 ? slot[1] : null;
        String date = formatDate(rawDate);
        String time = rawTime != null ? rawTime.substring(0, Math.min(5, rawTime.length())) : "—";
        return date + "  ·  " + time;
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            String normalized = raw.length() > 10 ? raw.substring(0, 10) : raw;
            SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat sdfOut = new SimpleDateFormat("d MMM yyyy", new Locale("it", "IT"));
            Date d = sdfIn.parse(normalized);
            return d != null ? sdfOut.format(d) : raw;
        } catch (Exception e) {
            return raw;
        }
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        ImageView    image;
        TextView     name, city;
        LinearLayout datesContainer;
        ImageButton  btnActions;

        VH(View v) {
            super(v);
            image          = v.findViewById(R.id.theaterReplicaImage);
            name           = v.findViewById(R.id.theaterReplicaName);
            city           = v.findViewById(R.id.theaterReplicaCity);
            datesContainer = v.findViewById(R.id.theaterReplicaDates);
            btnActions     = v.findViewById(R.id.btnTheaterReplicaActions);
        }
    }
}
