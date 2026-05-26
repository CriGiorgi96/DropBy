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
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.EventActionsHelper;
import com.example.dropby.R;
import com.example.dropby.models.EventGroup;
import com.example.dropby.network.GlideHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter per liste di EventGroup.
 * Usato in TheaterDetailFragment (showVenue=false) e
 * in EventDetailFragment per le "altre repliche" (showVenue=true).
 */
public class EventGroupAdapter extends RecyclerView.Adapter<EventGroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(EventGroup group);
    }

    private final List<EventGroup> groups;
    private final OnGroupClickListener listener;
    private final boolean showVenue; // mostra il nome del teatro accanto a ogni data?

    public EventGroupAdapter(List<EventGroup> groups,
                             boolean showVenue,
                             OnGroupClickListener listener) {
        this.groups    = groups;
        this.showVenue = showVenue;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventGroup group = groups.get(position);
        Context ctx = holder.itemView.getContext();

        holder.title.setText(group.getTitle() != null ? group.getTitle() : "—");
        holder.category.setText(group.getCategory() != null ? group.getCategory() : "");

        // Immagine locandina
        if (group.getMainImage() != null && group.getMainImage().getUrl() != null) {
            Glide.with(ctx)
                    .load(GlideHelper.url(group.getMainImage().getUrl()))
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.image_placeholder);
        }

        // Date dinamiche: costruisci le righe nel datesContainer
        holder.datesContainer.removeAllViews();
        for (EventGroup.Occurrence occ : group.getOccurrences()) {
            TextView row = new TextView(ctx);
            row.setText(occ.toDisplayString(showVenue));
            row.setTextSize(13f);
            row.setPadding(0, 4, 0, 4);
            row.setTextColor(0xFF6A6274); // grigio brand
            holder.datesContainer.addView(row);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(group);
        });

        // Tre bottoni espliciti: visibili solo se loggato
        SharedPreferences prefs = ctx.getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean isLogged = prefs.getString("token", null) != null;
        int btnVis = isLogged ? View.VISIBLE : View.GONE;

        if (holder.btnFavorite != null) {
            holder.btnFavorite.setVisibility(btnVis);

            if (isLogged && group.getRepresentative() != null) {
                int eventId = group.getRepresentative().getId();

                holder.isFavorite = false;
                holder.btnFavorite.setImageResource(R.drawable.ic_heart_empty);

                checkFavoriteFromBackend(holder, eventId);

                holder.btnFavorite.setOnClickListener(v ->
                        showHeartMenu(ctx, holder, group)
                );
            }
        }
        if (holder.btnReminder != null) {
            holder.btnReminder.setVisibility(btnVis);
            holder.btnReminder.setOnClickListener(v -> {
                Activity activity = (Activity) ctx;
                new EventActionsHelper(activity, group.getRepresentative()).showReminderPicker();
            });
        }
        if (holder.btnPropose != null) {
            holder.btnPropose.setVisibility(btnVis);
            holder.btnPropose.setOnClickListener(v -> {
                Activity activity = (Activity) ctx;
                new EventActionsHelper(activity, group.getRepresentative()).showProposeFriendDialog();
            });
        }
    }


    @Override
    public int getItemCount() { return groups.size(); }

    private void checkFavoriteFromBackend(ViewHolder holder, int eventId) {
        ApiClient.getApiService().getFavorites()
                .enqueue(new Callback<ApiService.FavoritesWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.FavoritesWrapper> call,
                                           Response<ApiService.FavoritesWrapper> response) {
                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().favorites == null) {
                            updateFavoriteIcon(holder);
                            return;
                        }

                        holder.isFavorite = false;

                        for (FavoriteItem item : response.body().favorites) {
                            if (item.getEventId() == eventId) {
                                holder.isFavorite = true;
                                break;
                            }
                        }

                        updateFavoriteIcon(holder);
                    }

                    @Override
                    public void onFailure(Call<ApiService.FavoritesWrapper> call, Throwable t) {
                        updateFavoriteIcon(holder);
                    }
                });
    }

    private void updateFavoriteIcon(ViewHolder holder) {
        if (holder.btnFavorite == null) return;

        holder.btnFavorite.setImageResource(
                holder.isFavorite
                        ? R.drawable.ic_heart_full
                        : R.drawable.ic_heart_empty
        );
    }

    private void showHeartMenu(Context ctx, ViewHolder holder, EventGroup group) {
        PopupMenu popup = new PopupMenu(ctx, holder.btnFavorite);

        popup.getMenu().add(
                0,
                1,
                0,
                holder.isFavorite
                        ? ctx.getString(R.string.remove_from_favorites)
                        : ctx.getString(R.string.add_to_favorites)
        );

        popup.getMenu().add(0, 2, 1, ctx.getString(R.string.set_reminder));
        popup.getMenu().add(0, 3, 2, ctx.getString(R.string.propose_to_friend));

        popup.setOnMenuItemClickListener(item -> {
            Activity activity = (Activity) ctx;

            switch (item.getItemId()) {
                case 1:
                    toggleFavoriteOnBackend(ctx, holder, group.getRepresentative().getId());
                    break;

                case 2:
                    new EventActionsHelper(activity, group.getRepresentative()).showReminderPicker();
                    break;

                case 3:
                    new EventActionsHelper(activity, group.getRepresentative()).showProposeFriendDialog();
                    break;
            }

            return true;
        });

        popup.show();
    }

    private void toggleFavoriteOnBackend(Context ctx, ViewHolder holder, int eventId) {
        holder.btnFavorite.setEnabled(false);

        if (holder.isFavorite) {
            ApiClient.getApiService().removeFavorite(eventId)
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            holder.btnFavorite.setEnabled(true);

                            if (response.isSuccessful()) {
                                holder.isFavorite = false;
                                updateFavoriteIcon(holder);

                                Toast.makeText(
                                        ctx,
                                        ctx.getString(R.string.removed_from_favorites),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            holder.btnFavorite.setEnabled(true);
                        }
                    });

        } else {
            Map<String, Integer> body = new HashMap<>();
            body.put("event_id", eventId);

            ApiClient.getApiService().addFavorite(body)
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            holder.btnFavorite.setEnabled(true);

                            if (response.isSuccessful()) {
                                holder.isFavorite = true;
                                updateFavoriteIcon(holder);

                                Toast.makeText(
                                        ctx,
                                        ctx.getString(R.string.added_to_favorites),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            holder.btnFavorite.setEnabled(true);
                        }
                    });
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView     poster;
        TextView      title, category;
        LinearLayout  datesContainer;
        ImageButton   btnFavorite, btnReminder, btnPropose;

        boolean       isFavorite = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster         = itemView.findViewById(R.id.groupPosterImage);
            title          = itemView.findViewById(R.id.groupTitleText);
            category       = itemView.findViewById(R.id.groupCategoryText);
            datesContainer = itemView.findViewById(R.id.groupDatesContainer);
            btnFavorite    = itemView.findViewById(R.id.btnFavorite);
            btnReminder    = itemView.findViewById(R.id.btnReminder);
            btnPropose     = itemView.findViewById(R.id.btnPropose);
        }
    }
}