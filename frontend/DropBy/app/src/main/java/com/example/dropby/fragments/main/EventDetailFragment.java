package com.example.dropby.fragments.main;

<<<<<<< HEAD
import android.content.Context;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
<<<<<<< HEAD
import android.widget.CheckBox;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
<<<<<<< HEAD
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
=======
import android.widget.TextView;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.EventActionsHelper;
import com.example.dropby.LangUtil;
import com.example.dropby.R;
<<<<<<< HEAD
import com.example.dropby.ReminderScheduler;
import com.example.dropby.UiSoundPlayer;
import com.example.dropby.adapters.ReviewAdapter;
import com.example.dropby.adapters.TheaterReplicaAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.Location;
=======
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.adapters.ReviewAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.ReminderScheduler;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import com.example.dropby.models.Review;
import com.example.dropby.models.ReviewRequest;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.example.dropby.network.GlideHelper;

<<<<<<< HEAD
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
=======
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailFragment extends Fragment {

    private static final String ARG_EVENT = "event";

    private Event     event;
    private CheckBox  checkShowPast;

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
<<<<<<< HEAD
        setupShowPastCheckbox(view);
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        loadOtherReplicas(view);
    }

    // ── BIND DATA ────────────────────────────────────────────────────────────

    private void bindEventData(View view) {
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

        setText(view, R.id.eventDetailTitle,
                LangUtil.pick(requireContext(), event.getTitle(), event.getTitleEng()));
        setText(view, R.id.eventDetailCategory,    event.getCategory());

        // Prossima replica: data + teatro + città
        Event.EventReplica next = event.getNextReplica();
        if (next != null) {
            String d = formatEventDate(next.getDatePart());
            String t = next.getTimePart();
            setText(view, R.id.eventDetailDate, t.isEmpty() ? d : d + " · " + t);
            String loc = safe(next.getLocationName());
            if (!safe(next.getLocationCity()).isEmpty()) loc += " · " + next.getLocationCity();
            setText(view, R.id.eventDetailCity, loc);
        } else {
            setText(view, R.id.eventDetailDate, "—");
            setText(view, R.id.eventDetailCity, "—");
        }

        setText(view, R.id.eventDetailDescription,
                LangUtil.pick(requireContext(), event.getDescription(), event.getDescriptionEng()));

        // Link approfondimento (Wikipedia / info_url)
        bindInfoLink(view);

        // Pulsante biglietti → dialogo selezione teatro
        // Mostra il bottone solo se esiste almeno una replica FUTURA con URL biglietti.
        Button ticketBtn = view.findViewById(R.id.eventDetailTicketButton);
        if (ticketBtn != null) {
            List<Event.EventReplica> replicas = event.getReplicas();
            boolean hasTicketUrl = replicas != null && !replicas.isEmpty()
                    && replicas.stream().anyMatch(r ->
                            !r.isPast()
                            && r.getTicketUrl() != null
                            && !r.getTicketUrl().isEmpty());
            if (hasTicketUrl) {
                ticketBtn.setVisibility(View.VISIBLE);
                ticketBtn.setOnClickListener(v -> {
                    UiSoundPlayer.playButtonClick(requireContext());
                    showTicketDialog();
                });
            } else {
                ticketBtn.setVisibility(View.GONE);
            }
        }
    }

<<<<<<< HEAD
    // ── LINK APPROFONDIMENTO ─────────────────────────────────────────────────
=======
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
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

    private void bindInfoLink(View view) {
        android.widget.TextView tv = view.findViewById(R.id.eventDetailInfoLink);
        if (tv == null) return;

        boolean en = LangUtil.isEnglish(requireContext());
        String infoUrl = en ? event.getInfoUrlEng() : event.getInfoUrlIt();
        if (infoUrl == null || infoUrl.trim().isEmpty()) {
            tv.setVisibility(View.GONE);
            return;
        }

        String label = getString(R.string.more_details);
        android.text.SpannableString ss = new android.text.SpannableString(label);
        ss.setSpan(new android.text.style.UnderlineSpan(), 0, label.length(), 0);
        tv.setText(ss);
        tv.setVisibility(View.VISIBLE);
        tv.setOnClickListener(v -> openTicketUrl(infoUrl));
    }

    // ── DIALOGO ACQUISTO BIGLIETTI ───────────────────────────────────────────

    private void showTicketDialog() {
        List<Event.EventReplica> replicas = event.getReplicas();
        if (replicas == null || replicas.isEmpty()) return;

        String[] labels = new String[replicas.size()];
        for (int i = 0; i < replicas.size(); i++) {
            Event.EventReplica r = replicas.get(i);
            String venue = r.getLocationName() != null ? r.getLocationName() : "—";
            String city  = r.getLocationCity() != null ? r.getLocationCity()  : "";
            String date  = formatEventDate(r.getDatePart());
            String time  = r.getTimePart();
            labels[i] = venue + (city.isEmpty() ? "" : " · " + city)
                    + "\n" + date + (time.isEmpty() ? "" : " · " + time);
            if (r.isPast()) labels[i] += getString(R.string.already_happened_warning);
        }

        final int[] selected = {0};

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_theater))
                .setSingleChoiceItems(labels, 0, (dialog, which) -> {
                    UiSoundPlayer.playButtonClick(requireContext());
                    selected[0] = which;
                })
                .setPositiveButton(getString(R.string.buy_tickets), (dialog, which) -> {
                    UiSoundPlayer.playButtonClick(requireContext());
                    Event.EventReplica replica = replicas.get(selected[0]);
                    if (replica.isPast()) {
                        showTicketWarningDialog(replica.getTicketUrl());
                    } else {
                        openTicketUrl(replica.getTicketUrl());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showTicketWarningDialog(String ticketUrl) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.show_already_happened_title))
                .setMessage(getString(R.string.show_already_happened_message))
                .setPositiveButton(getString(R.string.proceed_anyway), (d, w) -> {
                    UiSoundPlayer.playButtonClick(requireContext());
                    openTicketUrl(ticketUrl);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openTicketUrl(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.ticket_url_not_available), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new android.content.Intent(
                android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)));
    }

    // ── CHECKBOX MOSTRA REPLICHE PASSATE ────────────────────────────────────

    private void setupShowPastCheckbox(View view) {
        checkShowPast = view.findViewById(R.id.checkShowPastReplicas);
        if (checkShowPast == null) return;
        checkShowPast.setOnCheckedChangeListener((btn, checked) ->
                reloadEventWithPast(view, checked));
    }

    private void reloadEventWithPast(View view, boolean includePast) {
        ApiClient.getApiService()
                .getEventDetail(event.getId(), includePast)
                .enqueue(new Callback<ApiService.EventWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.EventWrapper> call,
                                           @NonNull Response<ApiService.EventWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().event != null) {
                            event = response.body().event;
                            // Aggiorna il bottone biglietti e la sezione repliche
                            bindEventData(view);
                            loadOtherReplicas(view);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiService.EventWrapper> call, @NonNull Throwable t) {}
                });
    }

    // ── SEZIONE "IN SCENA ANCHE A…" ─────────────────────────────────────────

    private void loadOtherReplicas(View view) {
        TextView     label     = view.findViewById(R.id.eventDetailReplicasLabel);
        TextView     status    = view.findViewById(R.id.eventDetailReplicasStatus);
        LinearLayout container = view.findViewById(R.id.eventDetailReplicasContainer);
        if (label == null || container == null) return;

        List<Event.EventReplica> replicas = event.getReplicas();

        if (replicas == null || replicas.isEmpty()) {
            label.setVisibility(View.GONE);
            if (status != null) status.setVisibility(View.GONE);
            container.removeAllViews();
            return;
        }

        // Raggruppa le repliche per location_id
        LinkedHashMap<Integer, TheaterReplicaAdapter.LocationGroup> map = new LinkedHashMap<>();
        for (Event.EventReplica r : replicas) {
            if (!map.containsKey(r.getLocationId())) {
                TheaterReplicaAdapter.LocationGroup grp =
                        new TheaterReplicaAdapter.LocationGroup(
                                r.getLocationId(),
                                r.getLocationName(),
                                r.getLocationCity(),
                                r.getLocationImageEst(),
                                r.getLocationImageInt());
                grp.representative = event;
                map.put(r.getLocationId(), grp);
            }
            map.get(r.getLocationId()).slots.add(
                    new String[]{r.getDatePart(), r.getTimePart()});
        }

        List<TheaterReplicaAdapter.LocationGroup> groups = new ArrayList<>(map.values());
        if (status != null) status.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);

        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        boolean isLogged = prefs.getString("token", null) != null;

        for (TheaterReplicaAdapter.LocationGroup g : groups) {
            View card = inflater.inflate(R.layout.item_theater_replica, container, false);

            ImageView image    = card.findViewById(R.id.theaterReplicaImage);
            TextView  nameTv   = card.findViewById(R.id.theaterReplicaName);
            TextView  cityTv   = card.findViewById(R.id.theaterReplicaCity);
            LinearLayout dates = card.findViewById(R.id.theaterReplicaDates);
            ImageButton btnAct = card.findViewById(R.id.btnTheaterReplicaActions);

            if (nameTv != null) nameTv.setText(g.locationName != null ? g.locationName : "—");
            if (cityTv != null) cityTv.setText(g.city != null ? g.city : "—");

            if (image != null) {
                String imgUrl = ApiClient.resolveLocationImage(
                        g.imageEst, Location.LocationImageType.EXTERIOR);
                if (imgUrl == null) imgUrl = ApiClient.resolveLocationImage(
                        g.imageInt, Location.LocationImageType.INTERIOR);
                if (imgUrl != null) {
                    Glide.with(this).load(GlideHelper.url(imgUrl))
                            .placeholder(R.drawable.image_placeholder)
                            .centerCrop().into(image);
                } else {
                    image.setImageResource(R.drawable.image_placeholder);
                }
            }

            if (dates != null) {
                for (String[] slot : g.slots) {
                    String rawDate = slot.length > 0 ? slot[0] : null;
                    String rawTime = slot.length > 1 ? slot[1] : null;
                    String dateStr = formatEventDate(rawDate);
                    String timeStr = rawTime != null
                            ? rawTime.substring(0, Math.min(5, rawTime.length())) : "—";
                    TextView row = new TextView(requireContext());
                    row.setText(dateStr + "  ·  " + timeStr);
                    row.setTextSize(12f);
                    row.setPadding(0, 3, 0, 3);
                    row.setTextColor(0xFF6A6274);
                    dates.addView(row);
                }
            }

            if (btnAct != null) {
                btnAct.setVisibility(isLogged && g.representative != null ? View.VISIBLE : View.GONE);
                if (isLogged && g.representative != null) {
                    Event rep = g.representative;
                    btnAct.setOnClickListener(v ->
                            new EventActionsHelper(requireActivity(), rep).showMenu(v));
                }
            }

            card.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container,
                                    TheaterDetailFragment.newInstance(
                                            g.locationId, g.locationName,
                                            g.city, g.imageEst, g.imageInt))
                            .addToBackStack(null).commit());

            container.addView(card);
        }
    }

    // ── PREFERITI ────────────────────────────────────────────────────────────

    private ImageButton btnFavorite;
    private boolean     isFavorite = false;

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
                        Log.d("FAV_ICON", "checkFav HTTP=" + response.code()
                                + " eventId=" + event.getId());
                        if (response.isSuccessful() && response.body() != null
                                && response.body().favorites != null) {
                            for (FavoriteItem item : response.body().favorites) {
                                if (item.getEventId() == event.getId()) {
                                    isFavorite = true;
                                    Log.d("FAV_ICON", "found in favorites → star=FULL");
                                    break;
                                }
                            }
                        }
                        updateFavoriteIcon();
                    }
                    @Override
                    public void onFailure(Call<ApiService.FavoritesWrapper> call, Throwable t) {
                        Log.e("FAV_ICON", "checkFav failed: " + t.getMessage());
                        if (isAdded()) updateFavoriteIcon();
                    }
                });
        btnFavorite.setOnClickListener(this::showHeartMenu);
    }

    private void updateFavoriteIcon() {
        if (btnFavorite == null) return;
        btnFavorite.setImageResource(isFavorite
                ? R.drawable.ic_heart_full
                : R.drawable.ic_heart_empty);
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
                                        getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
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
                            // isSuccessful() copre 200-299 incluso 201 Created
                            if (response.isSuccessful()) {
                                isFavorite = true;
                                updateFavoriteIcon();   // aggiorna stella immediatamente
                                Log.d("FAV_ICON", "addFavorite OK (" + response.code() + ") → star=FULL");
                                Toast.makeText(requireContext(),
                                        getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w("FAV_ICON", "addFavorite failed HTTP=" + response.code());
                            }
                        }
                        @Override
                        public void onFailure(Call<GenericResponse> call, Throwable t) {
                            Log.e("FAV_ICON", "addFavorite network error: " + t.getMessage());
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
                isFavorite ? getString(R.string.remove_from_favorites) : getString(R.string.add_to_favorites));
        popup.getMenu().add(0, 2, 1, getString(R.string.set_reminder));
        popup.getMenu().add(0, 3, 2, getString(R.string.propose_to_friend));
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: toggleFavorite();          break;
                case 2: showReminderPicker();      break;
                case 3: showProposeFriendDialog(); break;
            }
            return true;
        });
        popup.show();
    }

    // ── PROPONI AD UN AMICO ──────────────────────────────────────────────────

    private void showProposeFriendDialog() {
        android.app.AlertDialog loading = new android.app.AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.loading_friends))
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
                                    R.string.no_friend_found, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<Friend> friends = response.body().friends;
                        String[] names = new String[friends.size()];
                        for (int i = 0; i < friends.size(); i++) names[i] = friends.get(i).getUsername();
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.propose_event_to, event.getTitle()))
                                .setItems(names, (dialog, which) -> submitProposal(friends.get(which)))
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiService.FriendsWrapper> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loading.dismiss();
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
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
                                    R.string.proposal_send_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(), R.string.network_absent, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── RECENSIONI ───────────────────────────────────────────────────────────

    private ReviewAdapter reviewAdapter;

    private void loadReviews(View view) {
        RecyclerView rv     = view.findViewById(R.id.reviewsRecyclerView);
        TextView     status = view.findViewById(R.id.reviewsStatusText);
        if (rv == null) return;

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        int currentUserId = prefs.getInt("userId", -1);

        reviewAdapter = new ReviewAdapter(new ArrayList<>(), currentUserId, authorUserId -> {
            if (!isAdded()) return;
            Bundle bundle = new Bundle();
            bundle.putInt("user_id", authorUserId);
            com.example.dropby.fragments.main.UserDetailsFragment udf =
                    new com.example.dropby.fragments.main.UserDetailsFragment();
            udf.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, udf)
                    .addToBackStack(null).commit();
        }, null);
        rv.setAdapter(reviewAdapter);
        rv.setNestedScrollingEnabled(false);

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
                                status.setText(getString(R.string.no_reviews_for_event));
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
        if (prefs.getString("token", null) == null) return;

        // Se ci sono spettacoli futuri: mostra box informativo, nascondi form
        if (isEventInFuture()) {
            formLayout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.reviewSpinnerSection).setVisibility(View.GONE);
            view.findViewById(R.id.reviewRatingBar).setVisibility(View.GONE);
            view.findViewById(R.id.reviewCommentInput).setVisibility(View.GONE);
            view.findViewById(R.id.btnSubmitReview).setVisibility(View.GONE);
            view.findViewById(R.id.reviewFutureInfoBox).setVisibility(View.VISIBLE);

            Event.EventReplica next = event.getNextReplica();
            TextView futureBoxDate     = view.findViewById(R.id.futureBoxDate);
            TextView futureBoxLocation = view.findViewById(R.id.futureBoxLocation);
            TextView futureBoxCity     = view.findViewById(R.id.futureBoxCity);
            if (futureBoxDate     != null && next != null)
                futureBoxDate.setText(formatEventDate(next.getDatePart()));
            if (futureBoxLocation != null)
                futureBoxLocation.setText(safe(event.getNextLocationName()));
            if (futureBoxCity     != null && next != null)
                futureBoxCity.setText(safe(next.getLocationCity()));
            return;
        }

        formLayout.setVisibility(View.VISIBLE);

        Spinner   locationSpinner = view.findViewById(R.id.reviewLocationSpinner);
        Spinner   dateSpinner     = view.findViewById(R.id.reviewDateSpinner);
        View      dateLabel       = view.findViewById(R.id.reviewDateSpinnerLabel);
        RatingBar ratingBar       = view.findViewById(R.id.reviewRatingBar);
        EditText  commentInput    = view.findViewById(R.id.reviewCommentInput);
        Button    submitBtn       = view.findViewById(R.id.btnSubmitReview);

        submitBtn.setEnabled(false);

        submitBtn.setOnClickListener(v -> {
            int    rating  = (int) ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();
            if (rating < 1) {
                Toast.makeText(requireContext(), R.string.select_at_least_one_star, Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                commentInput.setError(getString(R.string.write_comment));
                return;
            }
            submitBtn.setEnabled(false);
            ApiClient.getApiService()
                    .addReview(new ReviewRequest(event.getId(), rating, comment))
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GenericResponse> call,
                                               @NonNull Response<GenericResponse> response) {
                            if (!isAdded()) return;
                            submitBtn.setEnabled(true);
                            if (response.isSuccessful()) {
                                commentInput.setText("");
                                ratingBar.setRating(3);
                                locationSpinner.setSelection(0);
                                submitBtn.setEnabled(false);
                                Toast.makeText(requireContext(),
                                        R.string.review_sent, Toast.LENGTH_SHORT).show();
                                loadReviews(view);
                            } else {
                                String msg = getString(R.string.review_send_error);
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

        loadReplicasForSpinners(locationSpinner, dateSpinner, dateLabel, submitBtn);
    }

    /**
     * Carica le repliche passate (include_past=true) e popola gli spinner
     * del form di recensione.  Il submit usa sempre event.getId() come target.
     */
    private void loadReplicasForSpinners(Spinner locationSpinner, Spinner dateSpinner,
                                         View dateLabel, Button submitBtn) {
        ApiClient.getApiService()
                .getEventDetail(event.getId(), true)
                .enqueue(new Callback<ApiService.EventWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.EventWrapper> call,
                                           @NonNull Response<ApiService.EventWrapper> response) {
                        if (!isAdded()) return;

                        // Estrai solo le repliche già avvenute
                        List<Event.EventReplica> pastReplicas = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().event != null
                                && response.body().event.getReplicas() != null) {
                            for (Event.EventReplica r : response.body().event.getReplicas()) {
                                if (r.isPast()) pastReplicas.add(r);
                            }
                        }

                        // Se non ci sono repliche passate, abilita subito il submit
                        if (pastReplicas.isEmpty()) {
                            submitBtn.setEnabled(true);
                            return;
                        }

                        // Raggruppa per location_id
                        LinkedHashMap<Integer, List<Event.EventReplica>> byLoc = new LinkedHashMap<>();
                        List<Integer> locIds   = new ArrayList<>();
                        List<String>  locNames = new ArrayList<>();

                        for (Event.EventReplica r : pastReplicas) {
                            if (!byLoc.containsKey(r.getLocationId())) {
                                byLoc.put(r.getLocationId(), new ArrayList<>());
                                locIds.add(r.getLocationId());
                                String label = safe(r.getLocationName());
                                if (!safe(r.getLocationCity()).isEmpty())
                                    label += "  –  " + r.getLocationCity();
                                locNames.add(label.isEmpty() ? "—" : label);
                            }
                            byLoc.get(r.getLocationId()).add(r);
                        }

                        List<String> locLabels = new ArrayList<>();
                        locLabels.add(getString(R.string.select_place));
                        locLabels.addAll(locNames);

                        ArrayAdapter<String> locAdap = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, locLabels);
                        locAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        locationSpinner.setAdapter(locAdap);

                        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                                if (pos == 0) {
                                    submitBtn.setEnabled(false);
                                    dateSpinner.setVisibility(View.GONE);
                                    dateLabel.setVisibility(View.GONE);
                                    return;
                                }
                                List<Event.EventReplica> dates = byLoc.get(locIds.get(pos - 1));
                                if (dates == null || dates.isEmpty()) return;

                                if (dates.size() == 1) {
                                    submitBtn.setEnabled(true);
                                    dateSpinner.setVisibility(View.GONE);
                                    dateLabel.setVisibility(View.GONE);
                                } else {
                                    submitBtn.setEnabled(false);
                                    List<String> dLabels = new ArrayList<>();
                                    dLabels.add(getString(R.string.select_date));
                                    for (Event.EventReplica r : dates) {
                                        String ds = formatEventDate(r.getDatePart());
                                        String ts = r.getTimePart();
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
                                                    submitBtn.setEnabled(pos2 > 0);
                                                }
                                                @Override
                                                public void onNothingSelected(AdapterView<?> p2) {}
                                            });
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> p) {}
                        });

                        if (locIds.size() == 1) locationSpinner.setSelection(1);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.EventWrapper> call, @NonNull Throwable t) {
                        if (isAdded()) submitBtn.setEnabled(true);
                    }
                });
    }

<<<<<<< HEAD
    // ── PROMEMORIA ───────────────────────────────────────────────────────────

    private void showReminderPicker() {
=======
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
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                requireActivity().checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
            Toast.makeText(requireContext(),
<<<<<<< HEAD
                    R.string.notification_permission_needed,
=======
                    "Concedi il permesso per le notifiche nelle impostazioni",
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
<<<<<<< HEAD
                            R.string.choose_future_date, Toast.LENGTH_SHORT).show();
=======
                            "Scegli una data futura", Toast.LENGTH_SHORT).show();
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
<<<<<<< HEAD
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
        body.put("event_id",  event.getId());
=======
        String remindAt = String.format(java.util.Locale.US, "%04d-%02d-%02dT%02d:%02d:00",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE));
        Map<String, Object> body = new HashMap<>();
        body.put("event_id", event.getId());
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
<<<<<<< HEAD
                                    R.string.reminder_set, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    R.string.reminder_set_error, Toast.LENGTH_SHORT).show();
=======
                                    "Promemoria impostato!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Errore impostazione promemoria", Toast.LENGTH_SHORT).show();
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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

    /** True se l'evento ha almeno uno spettacolo futuro. */
    private boolean isEventInFuture() {
        return event.getNextShowDate() != null && !event.getNextShowDate().isEmpty();
    }

    /** Formatta "YYYY-MM-DD" in "17 mag 2026". */
    private String formatEventDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
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
