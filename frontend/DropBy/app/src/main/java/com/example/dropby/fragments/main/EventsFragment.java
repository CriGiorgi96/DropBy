package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView     statusText;
    private EditText     editSearch;

    private List<Event> allEvents = new ArrayList<>();

    public EventsFragment() { super(R.layout.fragment_events); }

    /** Mantenuto per compatibilità con HomeFragment — il parametro category non è usato. */
    public static EventsFragment newInstance(String category) {
        return new EventsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        statusText   = view.findViewById(R.id.eventsStatusText);
        editSearch   = view.findViewById(R.id.editSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ImageButton btnSearch = view.findViewById(R.id.btnSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(v -> filterAndShow());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterAndShow(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    private void loadData() {
        setStatus("Caricamento spettacoli…");

        ApiClient.getApiService().searchEvents(null, null, null, null, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getEvents() != null) {
                            allEvents = response.body().getEvents();
                        }
                        filterAndShow();
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                        if (isAdded()) setStatus("Errore di rete");
                    }
                });
    }

    private void filterAndShow() {
        if (!isAdded()) return;
        String query = editSearch != null
                ? editSearch.getText().toString().trim().toLowerCase()
                : "";

        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            boolean titleMatch = e.getTitle() != null && e.getTitle().toLowerCase().contains(query);
            boolean cityMatch  = e.getCity()  != null && e.getCity().toLowerCase().contains(query);
            boolean catMatch   = e.getCategory() != null && e.getCategory().toLowerCase().contains(query);
            if (query.isEmpty() || titleMatch || cityMatch || catMatch) filtered.add(e);
        }

        List<EventGroup> groups = EventGroup.from(filtered, true);

        if (groups.isEmpty()) {
            setStatus(query.isEmpty() ? "Nessuno spettacolo disponibile" : "Nessun risultato per: " + query);
        } else {
            statusText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new EventGroupAdapter(groups, true, group -> openEvent(group.getRepresentative())));
        }
    }

    private void openEvent(Event event) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventDetailFragment.newInstance(event))
                .addToBackStack(null)
                .commit();
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
