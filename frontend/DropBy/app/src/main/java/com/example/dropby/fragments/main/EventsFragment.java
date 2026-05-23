package com.example.dropby.fragments.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.network.ApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {

    private static final String[] FILTER_TYPES = {
        "Filtra per...", "Cerca per genere", "Cerca per data"
    };

    private static final String[] GENRES = {
        "Tutti i generi", "Opera", "Musical", "Balletto",
        "Danza teatrale", "Teatro contemporaneo"
    };

    private RecyclerView recyclerView;
    private TextView     statusText;
    private EditText     editSearch;
    private CheckBox     checkOnlyUpcoming;
    private Spinner      spinnerFilterType;
    private Spinner      spinnerGenre;
    private LinearLayout dateContainer;
    private Button       btnDateFrom;
    private Button       btnDateTo;

    private List<Event> allEvents = new ArrayList<>();

    private String selectedCategory  = null;
    private String selectedStartDate = null;
    private String selectedEndDate   = null;

    private ImageView loadingImage;
    private ImageView noResultsImage;

    // Blocca i listener durante l'inizializzazione degli spinner
    private boolean filtersReady = false;

    public EventsFragment() { super(R.layout.fragment_events); }

    /** Mantenuto per compatibilità con HomeFragment. */
    public static EventsFragment newInstance(String category) {
        return new EventsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView      = view.findViewById(R.id.recyclerView);
        statusText        = view.findViewById(R.id.eventsStatusText);
        editSearch        = view.findViewById(R.id.editSearch);
        checkOnlyUpcoming = view.findViewById(R.id.checkOnlyUpcoming);
        spinnerFilterType = view.findViewById(R.id.spinnerFilterType);
        spinnerGenre      = view.findViewById(R.id.spinnerGenre);
        dateContainer     = view.findViewById(R.id.dateContainer);
        btnDateFrom       = view.findViewById(R.id.btnDateFrom);
        btnDateTo         = view.findViewById(R.id.btnDateTo);

        loadingImage = view.findViewById(R.id.loadingImage);
        noResultsImage = view.findViewById(R.id.noResultsImage);

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(loadingImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ── Spinner tipo filtro ───────────────────────────────────────────────
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, FILTER_TYPES);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterType.setAdapter(filterAdapter);

        // ── Spinner generi ────────────────────────────────────────────────────
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, GENRES);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // ── Listeners ─────────────────────────────────────────────────────────
        ImageButton btnSearch = view.findViewById(R.id.btnSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(v -> loadData());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                showEvents(); // filtro locale, nessuna nuova chiamata API
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        checkOnlyUpcoming.setOnCheckedChangeListener((btn, checked) -> {
            if (filtersReady) loadData();
        });

        spinnerFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                updateFilterVisibility(pos);
                if (filtersReady) loadData();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                selectedCategory = (pos == 0) ? null : GENRES[pos];
                if (filtersReady) loadData();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnDateTo.setOnClickListener(v -> showDatePicker(false));

        filtersReady = true;
        loadData();
    }

    // ── CARICAMENTO DATI (chiamata API con filtri server-side) ────────────────

    private void loadData() {
        if (!isAdded()) return;
        setStatus("Caricamento spettacoli…");

        boolean onlyUpcoming = checkOnlyUpcoming != null && checkOnlyUpcoming.isChecked();

        ApiClient.getApiService().searchEvents(
                null,
                selectedCategory,
                null,
                null,
                null,
                onlyUpcoming,
                selectedStartDate,
                selectedEndDate
        ).enqueue(new Callback<EventsResponse>() {
            @Override
            public void onResponse(@NonNull Call<EventsResponse> call,
                                   @NonNull Response<EventsResponse> response) {
                if (!isAdded()) return;
                allEvents = (response.isSuccessful()
                        && response.body() != null
                        && response.body().getEvents() != null)
                        ? response.body().getEvents()
                        : new ArrayList<>();
                showEvents();
            }

            @Override
            public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                if (isAdded()) setStatus("Errore di rete");
            }
        });
    }

    // ── VISUALIZZAZIONE (filtro testo locale su allEvents già filtrati) ────────

    private void showEvents() {
        if (!isAdded()) return;
        String query = editSearch != null
                ? editSearch.getText().toString().trim().toLowerCase(Locale.getDefault())
                : "";

        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            boolean titleMatch = e.getTitle()    != null && e.getTitle().toLowerCase(Locale.getDefault()).contains(query);
            boolean cityMatch  = e.getCity()     != null && e.getCity().toLowerCase(Locale.getDefault()).contains(query);
            boolean catMatch   = e.getCategory() != null && e.getCategory().toLowerCase(Locale.getDefault()).contains(query);
            if (query.isEmpty() || titleMatch || cityMatch || catMatch) filtered.add(e);
        }

        List<EventGroup> groups = EventGroup.from(filtered, true);
        if (groups.isEmpty()) {
            setStatus(query.isEmpty() ? "Nessuno spettacolo disponibile" : "Nessun risultato per: " + query);
        } else {
            statusText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (loadingImage != null) loadingImage.setVisibility(View.GONE);
            if (noResultsImage != null) noResultsImage.setVisibility(View.GONE);

            recyclerView.setAdapter(new EventGroupAdapter(
                    groups, true, group -> openEvent(group.getRepresentative())));
        }
    }

    // ── VISIBILITÀ PANNELLO FILTRI ────────────────────────────────────────────

    private void updateFilterVisibility(int pos) {
        switch (pos) {
            case 1: // Cerca per genere
                spinnerGenre.setVisibility(View.VISIBLE);
                dateContainer.setVisibility(View.GONE);
                selectedStartDate = null;
                selectedEndDate   = null;
                break;
            case 2: // Cerca per data
                spinnerGenre.setVisibility(View.GONE);
                dateContainer.setVisibility(View.VISIBLE);
                selectedCategory = null;
                spinnerGenre.setSelection(0);
                break;
            default: // Filtra per... (reset)
                spinnerGenre.setVisibility(View.GONE);
                dateContainer.setVisibility(View.GONE);
                selectedCategory  = null;
                selectedStartDate = null;
                selectedEndDate   = null;
                spinnerGenre.setSelection(0);
                btnDateFrom.setText("Da: --");
                btnDateTo.setText("A: --");
                break;
        }
    }

    // ── DATE PICKER ───────────────────────────────────────────────────────────

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (pickerView, year, month, day) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                    if (isStartDate) {
                        selectedStartDate = date;
                        btnDateFrom.setText("Da: " + date);
                    } else {
                        selectedEndDate = date;
                        btnDateTo.setText("A: " + date);
                    }
                    loadData();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

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

        if (loadingImage != null) {
            boolean isLoading = msg.toLowerCase(Locale.getDefault()).contains("caricamento");
            loadingImage.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (noResultsImage != null) {
            boolean noResults = msg.toLowerCase(Locale.getDefault()).contains("nessun");
            noResultsImage.setVisibility(noResults ? View.VISIBLE : View.GONE);
        }
    }
}
