package com.example.dropby.fragments.main;

import android.os.Bundle;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.FavoriteAdapter;
<<<<<<< HEAD
import com.example.dropby.models.Event;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
<<<<<<< HEAD
        adapter = new FavoriteAdapter(new ArrayList<>(), this::onRemoveFavorite, this::openEventDetail);
=======
        adapter = new FavoriteAdapter(new ArrayList<>(), this::onRemoveFavorite);
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
<<<<<<< HEAD
        setStatus(getString(R.string.loading_favorites));
=======
        setStatus("Caricamento preferiti…");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        ApiClient.getApiService().getFavorites()
                .enqueue(new Callback<ApiService.FavoritesWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.FavoritesWrapper> call,
                                           Response<ApiService.FavoritesWrapper> response) {
                        if (!isAdded()) return;
<<<<<<< HEAD
                        Log.d("FAV_DEBUG", "HTTP " + response.code()
                                + " body_null=" + (response.body() == null)
                                + " favs=" + (response.body() != null && response.body().favorites != null
                                        ? response.body().favorites.size() : "null"));
                        if (response.isSuccessful() && response.body() != null
                                && response.body().favorites != null) {
                            List<FavoriteItem> list = response.body().favorites;
                            requireActivity().runOnUiThread(() -> {
                                if (!isAdded()) return;
                                if (list.isEmpty()) {
                                    setStatus(getString(R.string.no_favorites));
                                } else {
                                    statusText.setVisibility(View.GONE);
                                    adapter.update(list);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                if (isAdded()) setStatus(getString(R.string.error_loading_favorites));;
                            });
=======
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
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.FavoritesWrapper> call, Throwable t) {
<<<<<<< HEAD
                        Log.e("FAV_DEBUG", "onFailure: " + t.getMessage(), t);
                        if (isAdded()) requireActivity().runOnUiThread(
                                () -> { if (isAdded()) setStatus(getString(R.string.network_error)); });
                    }
                });
    }

    private void openEventDetail(int eventId) {
        ApiClient.getApiService().getEventDetail(eventId,false)
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
                                    getString(R.string.event_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.EventWrapper> call, Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(),
                                "Errore di rete", Toast.LENGTH_SHORT).show();
=======
                        if (isAdded()) setStatus("Errore di rete");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
<<<<<<< HEAD
                        Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
=======
                        Toast.makeText(requireContext(), "Rimosso dai preferiti", Toast.LENGTH_SHORT).show();
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
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
