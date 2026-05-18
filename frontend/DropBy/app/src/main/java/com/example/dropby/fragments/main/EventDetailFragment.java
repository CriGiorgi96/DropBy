package com.example.dropby.fragments.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.adapters.ReviewAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.ReminderScheduler;
import com.example.dropby.models.Review;
import com.example.dropby.models.ReviewRequest;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.example.dropby.network.GlideHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dettaglio di un singolo evento.
 *
 * Mostra:
 *  - Locandina, titolo, categoria, descrizione, prezzo
 *  - Pulsante biglietti (se disponibile)
 *  - Sezione "Altre repliche": tutte le date/teatri per la stessa produzione,
 *    raggruppate per location (showVenue=true in EventGroup.from)
 */
public class EventDetailFragment extends Fragment {

    private static final String ARG_EVENT = "event";
    private Event event;

    public static EventDetailFragment newInstance(Event event) {
        EventDetailFragment f = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null)
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        if (event == null) return;

        bindEventData(view);
        setupFavoriteButton(view);
        loadReviews(view);
        setupReviewForm(view);
        loadOtherReplicas(view);
    }

    // ── BIND DATA ────────────────────────────────────────────────────────────

    private void bindEventData(View view) {
        // Locandina
        ImageView poster = view.findViewById(R.id.eventDetailPoster);
        if (poster != null) {
            if (event.getMainImage() != null && event.getMainImage().getUrl() != null) {
                Glide.with(this)
                        .load(GlideHelper.url(event.getMainImage().getUrl()))
                        .placeholder(R.drawable.image_placeholder)
                        .into(poster);
            } else {
                poster.setImageResource(R.drawable.image_placeholder);
            }
        }

        setText(view, R.id.eventDetailTitle,       event.getTitle());
        setText(view, R.id.eventDetailCategory,    event.getCategory());
        setText(view, R.id.eventDetailDate,        safe(event.getDate()) + " · " + safe(event.getStartTime()));
        setText(view, R.id.eventDetailCity,        event.getCity());
        setText(view, R.id.eventDetailDescription, event.getDescription());
        setText(view, R.id.eventDetailPrice,
                event.getPrice() != null ? "€ " + event.getPrice() : "Prezzo non indicato");

        // Pulsante biglietti
        Button ticketBtn = view.findViewById(R.id.eventDetailTicketButton);
        if (ticketBtn != null) {
            if (event.getTicketUrl() != null && !event.getTicketUrl().isEmpty()) {
                ticketBtn.setVisibility(View.VISIBLE);
                ticketBtn.setOnClickListener(v -> {
                    android.net.Uri uri = android.net.Uri.parse(event.getTicketUrl());
                    startActivity(new android.content.Intent(
                            android.content.Intent.ACTION_VIEW, uri));
                });
            } else {
                ticketBtn.setVisibility(View.GONE);
            }
        }
    }

    // ── PREFERITI ────────────────────────────────────────────────────────────

    private ImageButton btnFavorite;
    private boolean isFavorite = false;

    // ── RECENSIONI ───────────────────────────────────────────────────────────

    private ReviewAdapter reviewAdapter;

    private void loadReviews(View view) {
        RecyclerView rv = view.findViewById(R.id.reviewsRecyclerView);
        TextView status = view.findViewById(R.id.reviewsStatusText);
        if (rv == null) return;

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        rv.setAdapter(reviewAdapter);

        ApiClient.getApiService().getEventReviews(event.getId())
                .enqueue(new Callback<ApiService.ReviewsWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.ReviewsWrapper> call,
                                           Response<ApiService.ReviewsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().reviews != null) {
                            List<Review> reviews = response.body().reviews;
                            if (reviews.isEmpty() && status != null) {
                                status.setText("Nessuna recensione per questo evento");
                                status.setVisibility(View.VISIBLE);
                            } else {
                                reviewAdapter.update(reviews);
                                if (status != null) status.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.ReviewsWrapper> call, Throwable t) {}
                });
    }

    private void setupReviewForm(View view) {
        View formLayout = view.findViewById(R.id.reviewFormLayout);
        if (formLayout == null) return;

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        boolean isLogged = prefs.getString("token", null) != null;

        if (isLogged) {
            formLayout.setVisibility(View.VISIBLE);
            Button submitBtn = view.findViewById(R.id.btnSubmitReview);
            RatingBar ratingBar = view.findViewById(R.id.reviewRatingBar);
            EditText commentInput = view.findViewById(R.id.reviewCommentInput);

            submitBtn.setOnClickListener(v -> {
                int rating = (int) ratingBar.getRating();
                String comment = commentInput.getText().toString().trim();
                if (comment.isEmpty()) {
                    commentInput.setError("Scrivi un commento");
                    return;
                }
                submitBtn.setEnabled(false);
                ApiClient.getApiService()
                        .addReview(new ReviewRequest(event.getId(), rating, comment))
                        .enqueue(new Callback<GenericResponse>() {
                            @Override
                            public void onResponse(Call<GenericResponse> call,
                                                   Response<GenericResponse> response) {
                                if (!isAdded()) return;
                                submitBtn.setEnabled(true);
                                if (response.isSuccessful()) {
                                    commentInput.setText("");
                                    ratingBar.setRating(3);
                                    Toast.makeText(requireContext(),
                                            "Recensione inviata!", Toast.LENGTH_SHORT).show();
                                    loadReviews(view);
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Errore invio recensione", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<GenericResponse> call, Throwable t) {
                                if (isAdded()) submitBtn.setEnabled(true);
                            }
                        });
            });
        }
    }

    // ── ALTRE REPLICHE (cross-referencing) ───────────────────────────────────

    /**
     * Cerca tutti gli eventi con lo stesso production_id e li mostra
     * raggruppati per location, con data e orario per ognuno.
     */
    private void loadOtherReplicas(View view) {
        TextView replicasLabel  = view.findViewById(R.id.eventDetailReplicasLabel);
        TextView replicasStatus = view.findViewById(R.id.eventDetailReplicasStatus);
        RecyclerView replicasRv = view.findViewById(R.id.eventDetailReplicasRecyclerView);

        if (replicasLabel == null || replicasRv == null) return;

        // Se non c'è un production_id valido non possiamo cercare le repliche
        if (event.getProductionId() == 0) {
            replicasLabel.setVisibility(View.GONE);
            if (replicasStatus != null) replicasStatus.setVisibility(View.GONE);
            return;
        }

        replicasLabel.setVisibility(View.VISIBLE);
        replicasRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        replicasRv.setNestedScrollingEnabled(false);

        if (replicasStatus != null) {
            replicasStatus.setVisibility(View.VISIBLE);
            replicasStatus.setText("Caricamento repliche...");
        }

        // Cerca tutti gli eventi della stessa produzione tramite query sul titolo
        // (se il backend supporta production_id come parametro, usarlo direttamente)
        ApiClient.getApiService()
                .searchEvents(null, null, null, event.getTitle(), null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            // Filtra per stesso production_id
                            List<Event> allSame = new java.util.ArrayList<>();
                            for (Event e : response.body().getEvents()) {
                                if (e.getProductionId() == event.getProductionId())
                                    allSame.add(e);
                            }

                            // Raggruppa per location_id (showVenue=true → mostra città/teatro)
                            List<EventGroup> groups = EventGroup.from(allSame, true);

                            if (replicasStatus != null) replicasStatus.setVisibility(View.GONE);

                            if (groups.isEmpty()) {
                                replicasLabel.setVisibility(View.GONE);
                            } else {
                                replicasRv.setAdapter(new EventGroupAdapter(
                                        groups, true, g -> {
                                    // Tap su un'altra replica → apri quel dettaglio
                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container,
                                                    EventDetailFragment.newInstance(
                                                            g.getRepresentative()))
                                            .addToBackStack(null).commit();
                                }));
                            }
                        } else {
                            replicasLabel.setVisibility(View.GONE);
                            if (replicasStatus != null) replicasStatus.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        replicasLabel.setVisibility(View.GONE);
                        if (replicasStatus != null) replicasStatus.setVisibility(View.GONE);
                    }
                });
    }

    // ── PREFERITI ────────────────────────────────────────────────────────────

    private void setupFavoriteButton(View view) {
        btnFavorite = view.findViewById(R.id.btnFavorite);
        if (btnFavorite == null) return;

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        if (prefs.getString("token", null) == null) {
            btnFavorite.setVisibility(View.GONE);
            return;
        }

        btnFavorite.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getFavorites()
                .enqueue(new Callback<ApiService.FavoritesWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.FavoritesWrapper> call,
                                           Response<ApiService.FavoritesWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().favorites != null) {
                            for (FavoriteItem item : response.body().favorites) {
                                if (item.getEventId() == event.getId()) {
                                    isFavorite = true;
                                    break;
                                }
                            }
                        }
                        updateFavoriteIcon();
                    }
                    @Override
                    public void onFailure(Call<ApiService.FavoritesWrapper> call, Throwable t) {
                        if (isAdded()) updateFavoriteIcon();
                    }
                });

        btnFavorite.setOnClickListener(this::showHeartMenu);
    }

    private void updateFavoriteIcon() {
        if (btnFavorite == null) return;
        btnFavorite.setImageResource(isFavorite
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite_border);
    }

    private void toggleFavorite() {
        btnFavorite.setEnabled(false);
        if (isFavorite) {
            ApiClient.getApiService().removeFavorite(event.getId())
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            if (!isAdded()) return;
                            btnFavorite.setEnabled(true);
                            if (response.isSuccessful()) {
                                isFavorite = false;
                                updateFavoriteIcon();
                                Toast.makeText(requireContext(),
                                        "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            if (isAdded()) btnFavorite.setEnabled(true);
                        }
                    });
        } else {
            Map<String, Integer> body = new HashMap<>();
            body.put("event_id", event.getId());
            ApiClient.getApiService().addFavorite(body)
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            if (!isAdded()) return;
                            btnFavorite.setEnabled(true);
                            if (response.isSuccessful()) {
                                isFavorite = true;
                                updateFavoriteIcon();
                                Toast.makeText(requireContext(),
                                        "Aggiunto ai preferiti", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            if (isAdded()) btnFavorite.setEnabled(true);
                        }
                    });
        }
    }

    // ── POPUP CUORE ──────────────────────────────────────────────────────────

    private void showHeartMenu(View anchor) {
        android.widget.PopupMenu popup =
                new android.widget.PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0,
                isFavorite ? "Rimuovi dai preferiti" : "Aggiungi ai preferiti");
        popup.getMenu().add(0, 2, 1, "Imposta promemoria");
        popup.getMenu().add(0, 3, 2, "Proponi ad un amico");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: toggleFavorite();         break;
                case 2: showReminderPicker();     break;
                case 3: showProposeFriendDialog(); break;
            }
            return true;
        });
        popup.show();
    }

    // ── PROPONI AD UN AMICO ──────────────────────────────────────────────────

    private void showProposeFriendDialog() {
        android.app.AlertDialog loading = new android.app.AlertDialog.Builder(requireContext())
                .setMessage("Caricamento amici...")
                .setCancelable(false)
                .show();

        ApiClient.getApiService().getFriends()
                .enqueue(new Callback<ApiService.FriendsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.FriendsWrapper> call,
                                           @NonNull Response<ApiService.FriendsWrapper> response) {
                        if (!isAdded()) return;
                        loading.dismiss();

                        if (!response.isSuccessful() || response.body() == null
                                || response.body().friends == null
                                || response.body().friends.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Nessun amico trovato", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Friend> friends = response.body().friends;
                        String[] names = new String[friends.size()];
                        for (int i = 0; i < friends.size(); i++) {
                            names[i] = friends.get(i).getUsername();
                        }

                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Proponi \"" + event.getTitle() + "\" a:")
                                .setItems(names, (dialog, which) ->
                                        submitProposal(friends.get(which)))
                                .setNegativeButton("Annulla", null)
                                .show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.FriendsWrapper> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loading.dismiss();
                        Toast.makeText(requireContext(),
                                "Errore di rete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitProposal(Friend friend) {
        Map<String, Integer> body = new HashMap<>();
        body.put("to_user_id", friend.getUserId());
        body.put("event_id",   event.getId());

        ApiClient.getApiService().proposeEvent(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Proposta inviata a " + friend.getUsername() + "!",
                                    Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(requireContext(),
                                    "Proposta già inviata o utente non amico",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Errore nell'invio della proposta",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(),
                                    "Rete assente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReminderPicker() {
        // Check POST_NOTIFICATIONS permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                requireActivity().checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
            Toast.makeText(requireContext(),
                    "Concedi il permesso per le notifiche nelle impostazioni",
                    Toast.LENGTH_LONG).show();
            return;
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (dp, year, month, day) ->
            new android.app.TimePickerDialog(requireContext(), (tp, hour, minute) -> {
                cal.set(year, month, day, hour, minute, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(requireContext(),
                            "Scegli una data futura", Toast.LENGTH_SHORT).show();
                    return;
                }
                scheduleReminder(cal);
            }, cal.get(java.util.Calendar.HOUR_OF_DAY),
               cal.get(java.util.Calendar.MINUTE), true).show(),
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleReminder(java.util.Calendar cal) {
        String remindAt = String.format(java.util.Locale.US, "%04d-%02d-%02dT%02d:%02d:00",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE));
        Map<String, Object> body = new HashMap<>();
        body.put("event_id", event.getId());
        body.put("remind_at", remindAt);
        ApiClient.getApiService().setReminder(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call,
                                           Response<GenericResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            ReminderScheduler.schedule(requireContext(), event.getId(),
                                    event.getTitle(), cal.getTimeInMillis());
                            Toast.makeText(requireContext(),
                                    "Promemoria impostato!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Errore impostazione promemoria", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {}
                });
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "—");
    }
    private String safe(String s) { return s != null ? s : ""; }
}