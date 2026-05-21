package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.FavoriteAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView statusText;
    private FavoriteAdapter adapter;

    public FavoritesFragment() { super(R.layout.fragment_favorites); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        statusText   = view.findViewById(R.id.favoritesStatusText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FavoriteAdapter(new ArrayList<>(), this::onRemoveFavorite, this::openEventDetail);
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        setStatus("Caricamento preferiti…");
        ApiClient.getApiService().getFavorites()
                .enqueue(new Callback<ApiService.FavoritesWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.FavoritesWrapper> call,
                                           Response<ApiService.FavoritesWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().favorites != null) {
                            List<FavoriteItem> list = response.body().favorites;
                            if (list.isEmpty()) {
                                setStatus(getString(R.string.no_favorites));
                            } else {
                                statusText.setVisibility(View.GONE);
                                adapter.update(list);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            setStatus("Errore caricamento preferiti");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.FavoritesWrapper> call, Throwable t) {
                        if (isAdded()) setStatus("Errore di rete");
                    }
                });
    }

    private void openEventDetail(int eventId) {
        ApiClient.getApiService().getEventDetail(eventId)
                .enqueue(new Callback<ApiService.EventWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.EventWrapper> call,
                                           Response<ApiService.EventWrapper> response) {
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
                    public void onFailure(Call<ApiService.EventWrapper> call, Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(),
                                "Errore di rete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onRemoveFavorite(FavoriteItem item) {
        ApiClient.getApiService().removeFavorite(item.getEventId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call,
                                           Response<GenericResponse> response) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
                        loadFavorites();
                    }
                    @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
                });
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
