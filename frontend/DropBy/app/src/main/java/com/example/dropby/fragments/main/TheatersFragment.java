package com.example.dropby.fragments.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.LocationAdapter;
import com.example.dropby.models.Location;
import com.example.dropby.models.LocationsResponse;
import com.example.dropby.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheatersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView statusText;
    private EditText editSearch;
    private ImageView loadingImage;
    private ImageView noResultsImage;
    private List<Location> allLocations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theaters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.theatersRecyclerView);
        statusText   = view.findViewById(R.id.theatersStatusText);
        editSearch   = view.findViewById(R.id.editSearchTheater);
        loadingImage = view.findViewById(R.id.loadingImage);
        noResultsImage = view.findViewById(R.id.noResultsImage);

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(loadingImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterLocations(s.toString().trim());
            }
        });

        loadTheaters();
    }

    private void loadTheaters() {
        setStatus(getString(R.string.theatersStatusText));
        showLoadingState();

        ApiClient.getApiService().getLocations(null, null)
                .enqueue(new Callback<LocationsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationsResponse> call,
                                           @NonNull Response<LocationsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            allLocations = response.body().getLocations();
                            showLocations(allLocations);
                        } else {
                            statusText.setText(getString(R.string.server_error, response.code()));
                            showErrorState();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<LocationsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        statusText.setText(R.string.no_connection_server);
                        showErrorState();
                    }
                });
    }

    @SuppressLint("StringFormatInvalid")
    private void filterLocations(String query) {
        if (query.isEmpty()) { showLocations(allLocations); return; }
        String lower = query.toLowerCase();
        List<Location> filtered = new ArrayList<>();
        for (Location loc : allLocations) {
            boolean matchName = loc.getName() != null && loc.getName().toLowerCase().contains(lower);
            boolean matchCity = loc.getCity() != null && loc.getCity().toLowerCase().contains(lower);
            if (matchName || matchCity) filtered.add(loc);
        }
        if (filtered.isEmpty()) {
            setStatus(getString(R.string.no_results_for, query));
            recyclerView.setAdapter(null);
            showNoResultsState();
        } else {
            showLocations(filtered);
        }
    }

    private void showLocations(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            setStatus(getString(R.string.no_theaters_available));
            showNoResultsState();
            return;
        }

        showResultsState();

        recyclerView.setAdapter(new LocationAdapter(locations, loc ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TheaterDetailFragment.newInstance(loc))
                        .addToBackStack(null).commit()));
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }

    //  Uses the no result image or the loading gif based on status
    private void showLoadingState() {
        recyclerView.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);

        if (loadingImage != null) loadingImage.setVisibility(View.VISIBLE);
        if (noResultsImage != null) noResultsImage.setVisibility(View.GONE);
    }

    private void showNoResultsState() {
        recyclerView.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);

        if (loadingImage != null) loadingImage.setVisibility(View.GONE);
        if (noResultsImage != null) noResultsImage.setVisibility(View.VISIBLE);
    }

    private void showResultsState() {
        recyclerView.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);

        if (loadingImage != null) loadingImage.setVisibility(View.GONE);
        if (noResultsImage != null) noResultsImage.setVisibility(View.GONE);
    }

    private void showErrorState() {
        recyclerView.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);

        if (loadingImage != null) loadingImage.setVisibility(View.GONE);
        if (noResultsImage != null) noResultsImage.setVisibility(View.GONE);
    }
}