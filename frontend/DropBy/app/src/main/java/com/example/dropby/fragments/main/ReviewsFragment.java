package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.models.Review;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsFragment extends Fragment {

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

        RecyclerView rv = view.findViewById(R.id.reviewsMyRecyclerView);
        TextView statusText = view.findViewById(R.id.reviewsMyStatusText);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                                statusText.setText("Non hai ancora scritto recensioni.");
                                statusText.setVisibility(View.VISIBLE);
                            } else {
                                statusText.setVisibility(View.GONE);
                                rv.setAdapter(new MyReviewAdapter(reviews));
                            }
                        } else {
                            statusText.setText("Impossibile caricare le recensioni.");
                            statusText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.ReviewsWrapper> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        statusText.setText("Errore di rete. Riprova.");
                        statusText.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ── Adapter inline ────────────────────────────────────────────────────────

    private static class MyReviewAdapter
            extends RecyclerView.Adapter<MyReviewAdapter.VH> {

        private final List<Review> items;

        MyReviewAdapter(List<Review> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_review, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Review r = items.get(position);
            holder.eventTitle.setText(
                    r.getEventTitle() != null ? r.getEventTitle() : "Evento sconosciuto");
            holder.stars.setText(r.getStarsString());
            holder.comment.setText(r.getComment() != null ? r.getComment() : "");
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView eventTitle, stars, comment;
            VH(View v) {
                super(v);
                eventTitle = v.findViewById(R.id.myReviewEventTitle);
                stars      = v.findViewById(R.id.myReviewStars);
                comment    = v.findViewById(R.id.myReviewComment);
            }
        }
    }
}
