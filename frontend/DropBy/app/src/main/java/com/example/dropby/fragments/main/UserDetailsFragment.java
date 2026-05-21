package com.example.dropby.fragments.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.FavoriteAdapter;
import com.example.dropby.adapters.ReviewAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Review;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.example.dropby.network.GlideHelper;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailsFragment extends Fragment {

    private int targetUserId = -1;

    public UserDetailsFragment() {
        super(R.layout.fragment_user_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            targetUserId = getArguments().getInt("user_id", -1);
        }

        Button btnBack = view.findViewById(R.id.btnUserDetailsBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (targetUserId == -1) {
            Toast.makeText(requireContext(), "Utente non trovato", Toast.LENGTH_SHORT).show();
            return;
        }

        loadUserProfile(view);
    }

    // ── PROFILO ───────────────────────────────────────────────────────────────

    private void loadUserProfile(View view) {
        ApiClient.getApiService().getUserPublicProfile(targetUserId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (!isAdded() || response.body() == null) return;
                        Map<String, Object> data = response.body();

                        bindProfileHeader(view, data);

                        boolean privateFav = Boolean.TRUE.equals(data.get("private_favorites"));
                        boolean privateRev = Boolean.TRUE.equals(data.get("private_reviews"));

                        handleFavoritesSection(view, privateFav);
                        handleReviewsSection(view, privateRev);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(),
                                "Errore caricamento profilo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindProfileHeader(View view, Map<String, Object> data) {
        ImageView avatar     = view.findViewById(R.id.userDetailsAvatar);
        TextView  tvUsername = view.findViewById(R.id.userDetailsUsername);

        String username   = (String) data.get("username");
        String profileImg = (String) data.get("profile_image");

        tvUsername.setText(username != null ? username : "Utente");

        Object loadSource;
        if (isServerFilename(profileImg)) {
            String url = ApiClient.getBaseUrl() + "assets/avatars/" + profileImg;
            loadSource = GlideHelper.url(url);
        } else if (profileImg != null && !profileImg.isEmpty()) {
            int resId = getResources().getIdentifier(
                    profileImg, "drawable", requireContext().getPackageName());
            loadSource = resId != 0 ? resId : R.drawable.default_guest;
        } else {
            loadSource = R.drawable.default_guest;
        }

        Glide.with(this)
                .load(loadSource)
                .placeholder(R.drawable.default_guest)
                .circleCrop()
                .into(avatar);
    }

    private void openEventDetail(int eventId) {
        ApiClient.getApiService().getEventDetail(eventId)
                .enqueue(new retrofit2.Callback<ApiService.EventWrapper>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<ApiService.EventWrapper> call,
                                           @NonNull retrofit2.Response<ApiService.EventWrapper> response) {
                        if (!isAdded()) return;
                        Event event = response.isSuccessful() && response.body() != null
                                ? response.body().event : null;
                        if (event != null) {
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container,
                                            EventDetailFragment.newInstance(event))
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Evento non trovato", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<ApiService.EventWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(),
                                "Errore di rete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static boolean isServerFilename(String s) {
        if (s == null || s.isEmpty()) return false;
        String lower = s.toLowerCase(java.util.Locale.US);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".webp")
            || lower.endsWith(".gif");
    }

    // ── PREFERITI ────────────────────────────────────────────────────────────

    private void handleFavoritesSection(View view, boolean isPrivate) {
        TextView  privacyMsg = view.findViewById(R.id.userDetailsFavPrivacy);
        TextView  emptyMsg   = view.findViewById(R.id.userDetailsFavEmpty);
        RecyclerView rv      = view.findViewById(R.id.userDetailsFavRecycler);

        if (isPrivate) {
            privacyMsg.setVisibility(View.VISIBLE);
            return;
        }

        ApiClient.getApiService().getUserFavorites(targetUserId)
                .enqueue(new Callback<ApiService.FavoritesWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.FavoritesWrapper> call,
                                           @NonNull Response<ApiService.FavoritesWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().favorites != null) {
                            List<FavoriteItem> list = response.body().favorites;
                            if (list.isEmpty()) {
                                emptyMsg.setVisibility(View.VISIBLE);
                            } else {
                                rv.setLayoutManager(new LinearLayoutManager(requireContext()));
                                rv.setAdapter(new FavoriteAdapter(list, null,
                                        eventId -> openEventDetail(eventId)));
                                rv.setVisibility(View.VISIBLE);
                            }
                        } else if (response.code() == 403) {
                            privacyMsg.setVisibility(View.VISIBLE);
                        } else {
                            emptyMsg.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.FavoritesWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) emptyMsg.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ── RECENSIONI ───────────────────────────────────────────────────────────

    private void handleReviewsSection(View view, boolean isPrivate) {
        TextView  privacyMsg = view.findViewById(R.id.userDetailsRevPrivacy);
        TextView  emptyMsg   = view.findViewById(R.id.userDetailsRevEmpty);
        RecyclerView rv      = view.findViewById(R.id.userDetailsRevRecycler);

        if (isPrivate) {
            privacyMsg.setVisibility(View.VISIBLE);
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("app", requireContext().MODE_PRIVATE);
        int currentUserId = prefs.getInt("userId", -1);

        ApiClient.getApiService().getUserReviews(targetUserId)
                .enqueue(new Callback<ApiService.ReviewsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.ReviewsWrapper> call,
                                           @NonNull Response<ApiService.ReviewsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().reviews != null) {
                            List<Review> list = response.body().reviews;
                            if (list.isEmpty()) {
                                emptyMsg.setVisibility(View.VISIBLE);
                            } else {
                                rv.setLayoutManager(new LinearLayoutManager(requireContext()));
                                rv.setAdapter(new ReviewAdapter(list, currentUserId, null,
                                        eventId -> openEventDetail(eventId)));
                                rv.setVisibility(View.VISIBLE);
                            }
                        } else if (response.code() == 403) {
                            privacyMsg.setVisibility(View.VISIBLE);
                        } else {
                            emptyMsg.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.ReviewsWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) emptyMsg.setVisibility(View.VISIBLE);
                    }
                });
    }
}
