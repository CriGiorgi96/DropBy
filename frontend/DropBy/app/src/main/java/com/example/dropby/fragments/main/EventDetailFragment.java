package com.example.dropby.fragments.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.ReviewAdapter;
import com.example.dropby.adapters.TheaterReplicaAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.models.Location;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.ReminderScheduler;
import com.example.dropby.models.Review;
import com.example.dropby.models.ReviewRequest;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.example.dropby.network.GlideHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        SharedPreferences prefs = requireActivity().getSharedPreferences("app", requireContext().MODE_PRIVATE);
        int currentUserId = prefs.getInt("userId", -1);

        reviewAdapter = new ReviewAdapter(new ArrayList<>(), currentUserId, authorUserId -> {
            if (!isAdded()) return;
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putInt("user_id", authorUserId);
            com.example.dropby.fragments.main.UserDetailsFragment udf =
                    new com.example.dropby.fragments.main.UserDetailsFragment();
            udf.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, udf)
                    .addToBackStack(null)
                    .commit();
        }, null);
        rv.setAdapter(reviewAdapter);

        ApiClient.getApiService().getEventReviews(event.getId())
                .enqueue(new Callback<ApiService.ReviewsWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.ReviewsWrapper> call,
                                           Response<ApiService.ReviewsWrapper> response) {
                        if (!isAdded()) return;

                        // ── DEBUG LOG: JSON grezzo delle recensioni ───────────────────────
                        android.util.Log.d("DEBUG_REVIEW_JSON",
                                "HTTP " + response.code()
                                + " | body=" + new com.google.gson.Gson().toJson(response.body()));
                        // ─────────────────────────────────────────────────────────────────

                        if (response.isSuccessful() && response.body() != null
                                && response.body().reviews != null) {
                            List<Review> reviews = response.body().reviews;

                            // ── DEBUG LOG: prima recensione ricevuta ───────────────────────
                            if (!reviews.isEmpty()) {
                                Review first = reviews.get(0);
                                android.util.Log.d("DEBUG_REVIEW_JSON",
                                        "Review[0] userId="       + first.getUserId()
                                        + " | username="          + first.getUsername()
                                        + " | profileImage="      + first.getProfileImage()
                                        + " | rating="            + first.getRating());
                            }
                            // ─────────────────────────────────────────────────────────────

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
                    public void onFailure(Call<ApiService.ReviewsWrapper> call, Throwable t) {
                        android.util.Log.e("DEBUG_REVIEW_JSON", "Chiamata fallita: " + t.getMessage());
                    }
                });
    }

    private void setupReviewForm(View view) {
        View formLayout = view.findViewById(R.id.reviewFormLayout);
        if (formLayout == null) return;

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        if (prefs.getString("token", null) == null) return;

        // Evento futuro → box informativo TicketOne, form nascosto
        if (isEventInFuture()) {
            formLayout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.reviewSpinnerSection).setVisibility(View.GONE);
            view.findViewById(R.id.reviewRatingBar).setVisibility(View.GONE);
            view.findViewById(R.id.reviewCommentInput).setVisibility(View.GONE);
            view.findViewById(R.id.btnSubmitReview).setVisibility(View.GONE);
            view.findViewById(R.id.reviewFutureInfoBox).setVisibility(View.VISIBLE);
            TextView futureBoxDate     = view.findViewById(R.id.futureBoxDate);
            TextView futureBoxLocation = view.findViewById(R.id.futureBoxLocation);
            TextView futureBoxCity     = view.findViewById(R.id.futureBoxCity);
            if (futureBoxDate     != null) futureBoxDate.setText(formatEventDate(event.getDate()));
            if (futureBoxLocation != null) futureBoxLocation.setText(safe(event.getLocationName()));
            if (futureBoxCity     != null) futureBoxCity.setText(safe(event.getCity()));
            return;
        }

        formLayout.setVisibility(View.VISIBLE);

        Spinner  locationSpinner = view.findViewById(R.id.reviewLocationSpinner);
        Spinner  dateSpinner     = view.findViewById(R.id.reviewDateSpinner);
        View     dateLabel       = view.findViewById(R.id.reviewDateSpinnerLabel);
        RatingBar ratingBar      = view.findViewById(R.id.reviewRatingBar);
        EditText commentInput    = view.findViewById(R.id.reviewCommentInput);
        Button   submitBtn       = view.findViewById(R.id.btnSubmitReview);

        submitBtn.setEnabled(false);

        // selectedEventId[0] == -1 → nessuna replica selezionata
        final int[] selectedEventId = {-1};

        submitBtn.setOnClickListener(v -> {
            int    rating  = (int) ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();

            if (rating < 1) {
                Toast.makeText(requireContext(), "Seleziona almeno 1 stella", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                commentInput.setError("Scrivi un commento");
                return;
            }
            if (selectedEventId[0] <= 0) {
                Toast.makeText(requireContext(),
                        "Seleziona luogo e data dell'evento", Toast.LENGTH_SHORT).show();
                return;
            }

            submitBtn.setEnabled(false);
            ApiClient.getApiService()
                    .addReview(new ReviewRequest(selectedEventId[0], rating, comment))
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GenericResponse> call,
                                               @NonNull Response<GenericResponse> response) {
                            if (!isAdded()) return;
                            submitBtn.setEnabled(true);
                            if (response.isSuccessful()) {
                                commentInput.setText("");
                                ratingBar.setRating(3);
                                selectedEventId[0] = -1;
                                locationSpinner.setSelection(0);
                                submitBtn.setEnabled(false);
                                Toast.makeText(requireContext(),
                                        "Recensione inviata!", Toast.LENGTH_SHORT).show();
                                loadReviews(view);
                            } else {
                                String msg = "Errore invio recensione";
                                try {
                                    if (response.errorBody() != null) {
                                        org.json.JSONObject err =
                                                new org.json.JSONObject(response.errorBody().string());
                                        msg = err.optString("error", msg);
                                    }
                                } catch (Exception ignored) {}
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                            if (isAdded()) submitBtn.setEnabled(true);
                        }
                    });
        });

        loadReplicasForSpinners(locationSpinner, dateSpinner, dateLabel, selectedEventId, submitBtn);
    }

    private void loadReplicasForSpinners(Spinner locationSpinner, Spinner dateSpinner,
                                         View dateLabel, int[] selectedEventId, Button submitBtn) {
        ApiClient.getApiService()
                .searchEvents(null, null, null, event.getTitle(), null, null, null, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;

                        // Raggruppa repliche passate per location
                        final java.util.LinkedHashMap<String, List<Event>> byLoc =
                                new java.util.LinkedHashMap<>();
                        final List<String> locKeys = new ArrayList<>();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date today = new Date();

                        List<Event> candidates = (response.isSuccessful() && response.body() != null)
                                ? response.body().getEvents() : new ArrayList<>();

                        for (Event e : candidates) {
                            // Filtra per production_id (o solo l'evento corrente)
                            if (event.getProductionId() > 0
                                    && e.getProductionId() != event.getProductionId()) continue;
                            if (event.getProductionId() == 0
                                    && e.getId() != event.getId()) continue;

                            // Solo repliche già avvenute
                            if (e.getDate() != null) {
                                try {
                                    Date d = sdf.parse(e.getDate().substring(0, 10));
                                    if (d != null && d.after(today)) continue;
                                } catch (Exception ignored) {}
                            }

                            String name = e.getLocationName() != null ? e.getLocationName() : "—";
                            String city = e.getCity()         != null ? e.getCity()         : "";
                            String key  = name + "§" + city;

                            if (!byLoc.containsKey(key)) {
                                byLoc.put(key, new ArrayList<>());
                                locKeys.add(key);
                            }
                            byLoc.get(key).add(e);
                        }

                        // Ordina le date per ognuna delle location (crescente)
                        for (List<Event> list : byLoc.values())
                            java.util.Collections.sort(list, (a, b) -> {
                                if (a.getDate() == null) return 1;
                                if (b.getDate() == null) return -1;
                                return a.getDate().compareTo(b.getDate());
                            });

                        // Fallback: se non trovato nulla, usa l'evento corrente
                        if (byLoc.isEmpty()) {
                            String name = event.getLocationName() != null ? event.getLocationName() : "—";
                            String city = event.getCity()         != null ? event.getCity()         : "";
                            String key  = name + "§" + city;
                            byLoc.put(key, new ArrayList<>(java.util.Collections.singletonList(event)));
                            locKeys.add(key);
                        }

                        // Popola spinner location
                        List<String> locLabels = new ArrayList<>();
                        locLabels.add("Seleziona luogo…");
                        for (String key : locKeys) {
                            String[] p = key.split("§", 2);
                            String label = p[0];
                            if (p.length > 1 && !p[1].isEmpty()) label += "  –  " + p[1];
                            locLabels.add(label);
                        }
                        ArrayAdapter<String> locAdap = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, locLabels);
                        locAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        locationSpinner.setAdapter(locAdap);

                        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                                if (pos == 0) {
                                    selectedEventId[0] = -1;
                                    submitBtn.setEnabled(false);
                                    dateSpinner.setVisibility(View.GONE);
                                    dateLabel.setVisibility(View.GONE);
                                    return;
                                }
                                List<Event> dates = byLoc.get(locKeys.get(pos - 1));
                                if (dates == null || dates.isEmpty()) return;

                                if (dates.size() == 1) {
                                    // Data unica: auto-seleziona, nascondi spinner date
                                    selectedEventId[0] = dates.get(0).getId();
                                    submitBtn.setEnabled(true);
                                    dateSpinner.setVisibility(View.GONE);
                                    dateLabel.setVisibility(View.GONE);
                                } else {
                                    // Più date: mostra spinner date
                                    selectedEventId[0] = -1;
                                    submitBtn.setEnabled(false);

                                    List<String> dLabels = new ArrayList<>();
                                    dLabels.add("Seleziona data…");
                                    for (Event e : dates) {
                                        String ds = formatEventDate(e.getDate());
                                        String ts = e.getStartTime() != null
                                                ? e.getStartTime().substring(
                                                        0, Math.min(5, e.getStartTime().length()))
                                                : "";
                                        dLabels.add(ds + (ts.isEmpty() ? "" : "  ·  " + ts));
                                    }
                                    ArrayAdapter<String> dAdap = new ArrayAdapter<>(requireContext(),
                                            android.R.layout.simple_spinner_item, dLabels);
                                    dAdap.setDropDownViewResource(
                                            android.R.layout.simple_spinner_dropdown_item);
                                    dateSpinner.setAdapter(dAdap);
                                    dateLabel.setVisibility(View.VISIBLE);
                                    dateSpinner.setVisibility(View.VISIBLE);

                                    dateSpinner.setOnItemSelectedListener(
                                            new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> p2,
                                                                           View v2, int pos2, long id2) {
                                                    if (pos2 == 0) {
                                                        selectedEventId[0] = -1;
                                                        submitBtn.setEnabled(false);
                                                        return;
                                                    }
                                                    selectedEventId[0] = dates.get(pos2 - 1).getId();
                                                    submitBtn.setEnabled(true);
                                                }
                                                @Override
                                                public void onNothingSelected(AdapterView<?> p2) {}
                                            });
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> p) {}
                        });

                        // Se c'è una sola location, auto-selezionala
                        if (locKeys.size() == 1) locationSpinner.setSelection(1);
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                        // Fallback silenzioso: il submit resterà disabilitato
                        if (!isAdded()) return;
                        // Pre-seleziona l'evento corrente per non bloccare l'utente
                        selectedEventId[0] = event.getId();
                        submitBtn.setEnabled(true);
                    }
                });
    }

    // ── IN SCENA ANCHE A… (repliche raggruppate per teatro) ──────────────────

    private void loadOtherReplicas(View view) {
        TextView     replicasLabel  = view.findViewById(R.id.eventDetailReplicasLabel);
        TextView     replicasStatus = view.findViewById(R.id.eventDetailReplicasStatus);
        RecyclerView replicasRv    = view.findViewById(R.id.eventDetailReplicasRecyclerView);

        if (replicasLabel == null || replicasRv == null) return;

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

        ApiClient.getApiService()
                .searchEvents(null, null, null, event.getTitle(), null, null, null, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            // Filtra per stesso production_id e ordina per data crescente
                            List<Event> allSame = new ArrayList<>();
                            for (Event e : response.body().getEvents()) {
                                if (e.getProductionId() == event.getProductionId())
                                    allSame.add(e);
                            }
                            java.util.Collections.sort(allSame, (a, b) -> {
                                if (a.getDate() == null) return 1;
                                if (b.getDate() == null) return -1;
                                return a.getDate().compareTo(b.getDate());
                            });

                            // Raggruppa per location_id — il primo evento inserito è il rappresentativo
                            java.util.LinkedHashMap<Integer, TheaterReplicaAdapter.LocationGroup> map =
                                    new java.util.LinkedHashMap<>();
                            for (Event e : allSame) {
                                int locId = e.getLocationId();
                                if (!map.containsKey(locId)) {
                                    TheaterReplicaAdapter.LocationGroup grp =
                                            new TheaterReplicaAdapter.LocationGroup(
                                                    locId,
                                                    e.getLocationName(),
                                                    e.getCity(),
                                                    e.getLocationImageEst(),
                                                    e.getLocationImageInt());
                                    grp.representative = e;
                                    map.put(locId, grp);
                                }
                                map.get(locId).slots.add(
                                        new String[]{ e.getDate(), e.getStartTime() });
                            }

                            List<TheaterReplicaAdapter.LocationGroup> groups =
                                    new ArrayList<>(map.values());

                            if (replicasStatus != null) replicasStatus.setVisibility(View.GONE);

                            if (groups.isEmpty()) {
                                replicasLabel.setVisibility(View.GONE);
                            } else {
                                replicasRv.setAdapter(new TheaterReplicaAdapter(groups, g ->
                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container,
                                                    TheaterDetailFragment.newInstance(
                                                            g.locationId, g.locationName,
                                                            g.city, g.imageEst, g.imageInt))
                                            .addToBackStack(null).commit()
                                ));
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
        // Converte l'ora locale in UTC prima di inviarla al server
        java.util.Calendar utcCal = java.util.Calendar.getInstance(
                java.util.TimeZone.getTimeZone("UTC"));
        utcCal.setTimeInMillis(cal.getTimeInMillis());
        String remindAt = String.format(java.util.Locale.US, "%04d-%02d-%02dT%02d:%02d:00Z",
                utcCal.get(java.util.Calendar.YEAR),
                utcCal.get(java.util.Calendar.MONTH) + 1,
                utcCal.get(java.util.Calendar.DAY_OF_MONTH),
                utcCal.get(java.util.Calendar.HOUR_OF_DAY),
                utcCal.get(java.util.Calendar.MINUTE));
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

    /** Restituisce true se la data dell'evento è successiva a oggi. */
    private boolean isEventInFuture() {
        String dateStr = event.getDate();
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            String normalized = dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date eventDate = sdf.parse(normalized);
            return eventDate != null && eventDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** Formatta "YYYY-MM-DD" in "17 mag 2026". */
    private String formatEventDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            String normalized = raw.length() > 10 ? raw.substring(0, 10) : raw;
            SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy", new Locale("it", "IT"));
            Date d = sdfIn.parse(normalized);
            return d != null ? sdfOut.format(d) : raw;
        } catch (Exception e) {
            return raw;
        }
    }
}