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
        adapter = new FavoriteAdapter(new ArrayList<>(), this::onRemoveFavorite);
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
