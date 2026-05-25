package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.Review;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsFragment extends Fragment {

    private RecyclerView rv;
    private TextView     statusText;
    private MyReviewAdapter myAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv         = view.findViewById(R.id.reviewsMyRecyclerView);
        statusText = view.findViewById(R.id.reviewsMyStatusText);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadReviews();
    }

    private void loadReviews() {
        ApiClient.getApiService().getUserReviews()
                .enqueue(new Callback<ApiService.ReviewsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.ReviewsWrapper> call,
                                           @NonNull Response<ApiService.ReviewsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().reviews != null) {
                            List<Review> reviews = response.body().reviews;
                            if (reviews.isEmpty()) {
                                statusText.setText(getString(R.string.no_written_reviews));
                                statusText.setVisibility(View.VISIBLE);
                                rv.setVisibility(View.GONE);
                            } else {
                                statusText.setVisibility(View.GONE);
                                rv.setVisibility(View.VISIBLE);
                                myAdapter = new MyReviewAdapter(reviews, ReviewsFragment.this::confirmDelete);
                                rv.setAdapter(myAdapter);
                            }
                        } else {
                            statusText.setText(getString(R.string.error_loading_reviews));
                            statusText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.ReviewsWrapper> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        statusText.setText(getString(R.string.network_error_retry));
                        statusText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void confirmDelete(Review review) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.delete_review_confirm, review.getEventTitle()))
                .setPositiveButton(R.string.delete, (d, w) -> deleteReview(review))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteReview(Review review) {
        ApiClient.getApiService().deleteReview(review.getId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isAdded() && response.isSuccessful()) loadReviews();
                    }
                    @Override public void onFailure(@NonNull Call<GenericResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    // ── Adapter inline ────────────────────────────────────────────────────────

    private static class MyReviewAdapter
            extends RecyclerView.Adapter<MyReviewAdapter.VH> {

        interface OnDeleteClick { void onDelete(Review review); }

        private final List<Review>  items;
        private final OnDeleteClick onDelete;

        MyReviewAdapter(List<Review> items, OnDeleteClick onDelete) {
            this.items    = items;
            this.onDelete = onDelete;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_review, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Review r = items.get(pos);

            h.eventTitle.setText(r.getEventTitle() != null ? r.getEventTitle()
                    : h.eventTitle.getContext().getString(R.string.unknown_event));
            h.stars.setText(r.getStarsString());
            h.comment.setText(r.getComment() != null ? r.getComment() : "");
            h.eventDate.setText(formatDate(r.getEventDate()));
            h.eventCity.setText(r.getEventCity() != null ? r.getEventCity() : "");

            h.btnDelete.setOnClickListener(v -> onDelete.onDelete(r));
        }

        @Override
        public int getItemCount() { return items.size(); }

        private String formatDate(String raw) {
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

        static class VH extends RecyclerView.ViewHolder {
            TextView eventTitle, stars, comment, eventDate, eventCity;
            Button   btnDelete;
            VH(View v) {
                super(v);
                eventTitle = v.findViewById(R.id.myReviewEventTitle);
                stars      = v.findViewById(R.id.myReviewStars);
                comment    = v.findViewById(R.id.myReviewComment);
                eventDate  = v.findViewById(R.id.myReviewEventDate);
                eventCity  = v.findViewById(R.id.myReviewEventCity);
                btnDelete  = v.findViewById(R.id.btnDeleteReview);
            }
        }
    }
}
