package com.example.dropby.fragments.main;

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
        noResultsImage = view.findViewById(R.id.noResultsImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(R.string.theatersStatusText);

        ApiClient.getApiService().getLocations(null, null)
                .enqueue(new Callback<LocationsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationsResponse> call,
                                           @NonNull Response<LocationsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            allLocations = response.body().getLocations();
                            statusText.setVisibility(View.GONE);
                            showLocations(allLocations);
                        } else {
                            statusText.setText(R.string.server_error + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<LocationsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        statusText.setText(R.string.no_connection_server);
                    }
                });
    }

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
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.no_theater_found + query + "\"");
            recyclerView.setAdapter(null);
        } else {
            statusText.setVisibility(View.GONE);
            showLocations(filtered);
        }
    }

    private void showLocations(List<Location> locations) {
        recyclerView.setAdapter(new LocationAdapter(locations, loc ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TheaterDetailFragment.newInstance(loc))
                        .addToBackStack(null).commit()));
    }
}