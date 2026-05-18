package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";

    private RecyclerView recyclerView;
    private TextView     statusText;
    private EditText     editSearch;
    private ImageButton  btnSearch;
    private String       selectedCategory;
    private String       currentSearchQuery = null;

    public static EventsFragment newInstance(String category) {
        EventsFragment f = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        statusText = view.findViewById(R.id.eventsStatusText);
        editSearch = view.findViewById(R.id.editSearch);
        btnSearch  = view.findViewById(R.id.btnSearch);

        if (currentSearchQuery != null) editSearch.setText(currentSearchQuery);

        btnSearch.setOnClickListener(v -> performSearch());
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        if (getArguments() != null)
            selectedCategory = getArguments().getString(ARG_CATEGORY);

        TextView title = view.findViewById(R.id.text_events);
        if (title != null && selectedCategory != null) title.setText(selectedCategory);

        loadEvents(currentSearchQuery);
        return view;
    }

    private void performSearch() {
        currentSearchQuery = editSearch.getText().toString().trim();
        if (currentSearchQuery.isEmpty()) currentSearchQuery = null;
        loadEvents(currentSearchQuery);
    }

    private void loadEvents(String query) {
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(query == null ? "Caricamento eventi..." : "Ricerca in corso...");

        ApiService api = ApiClient.getApiService();
        api.searchEvents(null, selectedCategory, null, query, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            // Filtro locale per query (titolo o città)
                            var raw = response.body().getEvents();
                            List<com.example.dropby.models.Event> filtered;
                            if (query == null || query.isEmpty()) {
                                filtered = raw;
                            } else {
                                filtered = new ArrayList<>();
                                String lower = query.toLowerCase();
                                for (var e : raw) {
                                    boolean t = e.getTitle() != null && e.getTitle().toLowerCase().contains(lower);
                                    boolean c = e.getCity()  != null && e.getCity().toLowerCase().contains(lower);
                                    if (t || c) filtered.add(e);
                                }
                            }

                            // EventGroup.from deduplica per production_id e ordina per data
                            // showVenue=true: lista generale mostra anche la città
                            List<EventGroup> groups = EventGroup.from(filtered, true);

                            if (groups.isEmpty()) {
                                statusText.setVisibility(View.VISIBLE);
                                statusText.setText("Nessun risultato trovato" +
                                        (query != null ? " per: " + query : "."));
                                recyclerView.setAdapter(null);
                            } else {
                                statusText.setVisibility(View.GONE);
                                recyclerView.setAdapter(new EventGroupAdapter(
                                        groups, true,
                                        g -> requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_container,
                                                        EventDetailFragment.newInstance(
                                                                g.getRepresentative()))
                                                .addToBackStack(null).commit()));
                            }
                        } else {
                            statusText.setText("Errore server: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        statusText.setText("Rete assente o server spento");
                    }
                });
    }
}