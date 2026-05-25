package com.example.dropby.fragments.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dropby.R;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.example.dropby.network.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView statusTextView;
    private ImageView featuredImageView;
    private TextView featuredTitleTextView;
    private TextView featuredMetaTextView;
    private View featuredEventCard;
    private View featuredIndicatorOne;
    private View featuredIndicatorTwo;
    private View featuredIndicatorThree;
    private Event featuredEvent;
    private final List<Event> featuredEvents = new ArrayList<>();
    private final Handler featuredHandler = new Handler(Looper.getMainLooper());
    private int selectedFeaturedIndex = 0;
    private float featuredTouchStartX = 0f;
    private final Runnable rotateFeaturedEventRunnable = new Runnable() {
        @Override
        public void run() {
            if (featuredEvents.size() < 2 || !isAdded()) {
                return;
            }
            int nextIndex = (selectedFeaturedIndex + 1) % featuredEvents.size();
            showFeaturedEvent(nextIndex);
            featuredHandler.postDelayed(this, 5000);
        }
    };

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusTextView = view.findViewById(R.id.homeStatusText);
        featuredImageView = view.findViewById(R.id.featuredEventImage);
        featuredTitleTextView = view.findViewById(R.id.featuredTitleText);
        featuredMetaTextView = view.findViewById(R.id.featuredMetaText);
        loadCategoryImages(view);
        featuredEventCard = view.findViewById(R.id.featuredEventCard);
        featuredIndicatorOne = view.findViewById(R.id.featuredIndicatorOne);
        featuredIndicatorTwo = view.findViewById(R.id.featuredIndicatorTwo);
        featuredIndicatorThree = view.findViewById(R.id.featuredIndicatorThree);
        View featuredActionButton = view.findViewById(R.id.featuredActionButton);

        bindCategoryClick(view, R.id.categoryOperaCard, "Opera");
        bindCategoryClick(view, R.id.categoryMusicalCard, "Musical");
        bindCategoryClick(view, R.id.categoryBalletCard, "Balletto");
        bindCategoryClick(view, R.id.categoryDanceTheaterCard, "Danza teatrale");
        bindCategoryClick(view, R.id.categoryContemporaryTheaterCard, "Teatro contemporaneo");

        featuredEventCard.setOnClickListener(card -> {
            if (featuredEvent != null) {
                openEvent(featuredEvent);
            }
        });
        featuredEventCard.setOnTouchListener((card, event) -> handleFeaturedSwipe(event));

        featuredActionButton.setOnClickListener(button -> {
            if (featuredEvent != null) {
                openTicketPage(featuredEvent);
            }
        });

        bindFeaturedIndicator(featuredIndicatorOne, 0);
        bindFeaturedIndicator(featuredIndicatorTwo, 1);
        bindFeaturedIndicator(featuredIndicatorThree, 2);

        loadHomeEvents();
    }

    private void bindFeaturedIndicator(View indicator, int eventIndex) {
        indicator.setOnClickListener(view -> {
            if (eventIndex < featuredEvents.size()) {
                showFeaturedEvent(eventIndex);
                restartFeaturedRotation();
            }
        });
    }

    private boolean handleFeaturedSwipe(MotionEvent event) {
        if (featuredEvents.size() < 2) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            featuredTouchStartX = event.getX();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float distance = event.getX() - featuredTouchStartX;
            if (Math.abs(distance) < 80) {
                if (featuredEvent != null) {
                    openEvent(featuredEvent);
                }
                return true;
            }
            if (distance < 0) {
                showFeaturedEvent((selectedFeaturedIndex + 1) % featuredEvents.size());
            } else {
                showFeaturedEvent((selectedFeaturedIndex - 1 + featuredEvents.size()) % featuredEvents.size());
            }
            restartFeaturedRotation();
            return true;
        }
        return true;
    }

    private void loadCategoryImages(View rootView) {
        loadCategoryImage(rootView, R.id.categoryOperaImage, "Opera.png");
        loadCategoryImage(rootView, R.id.categoryMusicalImage, "Musical.png");
        loadCategoryImage(rootView, R.id.categoryBalletImage, "Balletto.png");
        loadCategoryImage(rootView, R.id.categoryDanceTheaterImage, "DanzaTeatrale.png");
        loadCategoryImage(rootView, R.id.categoryContemporaryTheaterImage, "TeatroContemporaneo.png");
    }

    private void loadCategoryImage(View rootView, int imageViewId, String filename) {
        ImageView imageView = rootView.findViewById(imageViewId);
        ImageLoader.loadInto(imageView, "/category-assets/" + filename);
    }

    private void bindCategoryClick(View rootView, int viewId, String category) {
        rootView.findViewById(viewId).setOnClickListener(view -> requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventsFragment.newInstance(category))
                .addToBackStack(null)
                .commit());
    }

    private void loadHomeEvents() {
        statusTextView.setVisibility(View.VISIBLE);
        statusTextView.setText(R.string.eventsStatusText);

        ApiService apiService = ApiClient.getApiService();
        // AGGIUNTO IL QUINTO PARAMETRO (null) PER location_id
        apiService.searchEvents(null, null, null, null, null, null, null, null, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(Call<EventsResponse> call, Response<EventsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<Event> events = response.body().getEvents();
                            if (events.isEmpty()) {
                                statusTextView.setText(R.string.no_event_found);
                            } else {
                                statusTextView.setVisibility(View.GONE);
                                bindFeaturedEvents(events);
                            }
                        } else {
                            statusTextView.setText(R.string.backend_not_available);
                        }
                    }

                    @Override
                    public void onFailure(Call<EventsResponse> call, Throwable t) {
                        if (isAdded()) {
                            statusTextView.setText(R.string.backend_not_reachable);
                        }
                    }
                });
    }

    private void bindFeaturedEvents(List<Event> events) {
        featuredEvents.clear();
        for (Event event : events) {
            if (featuredEvents.size() == 3) {
                break;
            }
            featuredEvents.add(event);
        }
        showFeaturedEvent(0);
        restartFeaturedRotation();
    }

    private void showFeaturedEvent(int index) {
        selectedFeaturedIndex = index;
        bindFeaturedEvent(featuredEvents.get(index));
        updateFeaturedIndicators(index);
    }

    private void restartFeaturedRotation() {
        featuredHandler.removeCallbacks(rotateFeaturedEventRunnable);
        if (featuredEvents.size() > 1) {
            featuredHandler.postDelayed(rotateFeaturedEventRunnable, 5000);
        }
    }

    private void bindFeaturedEvent(Event event) {
        featuredEvent = event;
        featuredTitleTextView.setText(textOrFallback(event.getTitle(), getString(R.string.highlighted_event)));
        featuredMetaTextView.setText(buildMeta(event));

        String imageUrl = event.getMainImage() != null ? event.getMainImage().getUrl() : null;
        ImageLoader.loadInto(featuredImageView, imageUrl);
    }

    private void updateFeaturedIndicators(int selectedIndex) {
        updateFeaturedIndicator(featuredIndicatorOne, selectedIndex == 0, featuredEvents.size() > 0);
        updateFeaturedIndicator(featuredIndicatorTwo, selectedIndex == 1, featuredEvents.size() > 1);
        updateFeaturedIndicator(featuredIndicatorThree, selectedIndex == 2, featuredEvents.size() > 2);
    }

    private void updateFeaturedIndicator(View indicator, boolean selected, boolean available) {
        indicator.setVisibility(available ? View.VISIBLE : View.GONE);
        indicator.setAlpha(selected ? 1.0f : 0.45f);
    }

    private void openEvent(Event event) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventDetailFragment.newInstance(event))
                .addToBackStack(null)
                .commit();
    }

    private void openTicketPage(Event event) {
        Event.EventReplica next = event.getNextReplica();
        String ticketUrl = next != null ? next.getTicketUrl() : null;
        if (ticketUrl == null || ticketUrl.trim().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.ticket_not_available_for_show), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ticketUrl.trim()));
        startActivity(intent);
    }

    private String buildMeta(Event event) {
        StringBuilder builder = new StringBuilder();
        Event.EventReplica next = event.getNextReplica();
        appendPart(builder, next != null ? next.getLocationName() : event.getNextLocationName());
        appendPart(builder, next != null ? next.getDatePart()     : null);
        appendPart(builder, event.getCategory());
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" - ");
        }
        builder.append(value.trim());
    }

    private String textOrFallback(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    @Override
    public void onDestroyView() {
        featuredHandler.removeCallbacks(rotateFeaturedEventRunnable);
        super.onDestroyView();
    }
}