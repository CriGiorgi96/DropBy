package com.example.dropby.fragments.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dropby.R;
import com.example.dropby.adapters.EventGroupAdapter;
import com.example.dropby.models.Event;
import com.example.dropby.models.EventGroup;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.models.Location;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.GlideHelper;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheaterDetailFragment extends Fragment {

    private static final String ARG_LOCATION  = "location";
    // Campi singoli usati quando si naviga dalle repliche (evita il Gson round-trip)
    private static final String ARG_LOC_ID       = "loc_id";
    private static final String ARG_LOC_NAME     = "loc_name";
    private static final String ARG_LOC_CITY     = "loc_city";
    private static final String ARG_LOC_IMG_EST  = "loc_img_est";
    private static final String ARG_LOC_IMG_INT  = "loc_img_int";

    private Location     location;
    private TextView     eventsStatusTextView;
    private RecyclerView eventsRecyclerView;
    private ImageView    galleryImageView;
    private TextView     previousImageButton, nextImageButton;

    // Galleria: costruita dalle due immagini disponibili (est + int)
    private String[] galleryUrls;
    private int selectedImageIndex = 0;
    private boolean eventsLoaded = false;

    /** Usato dalla lista teatri: Location completa già disponibile. */
    public static TheaterDetailFragment newInstance(Location location) {
        TheaterDetailFragment f = new TheaterDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LOCATION, location);
        f.setArguments(args);
        return f;
    }

    /** Usato dalla sezione "In scena anche a…": dati parziali da LocationGroup. */
    public static TheaterDetailFragment newInstance(int locationId, String name, String city,
                                                    String imageEst, String imageInt) {
        TheaterDetailFragment f = new TheaterDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOC_ID,      locationId);
        args.putString(ARG_LOC_NAME,    name);
        args.putString(ARG_LOC_CITY,    city);
        args.putString(ARG_LOC_IMG_EST, imageEst);
        args.putString(ARG_LOC_IMG_INT, imageInt);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theater_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;

        if (args.containsKey(ARG_LOCATION)) {
            // Percorso normale: Location completa dalla lista teatri
            location = (Location) args.getSerializable(ARG_LOCATION);
        } else {
            // Percorso repliche: dati parziali da Bundle primitivi
            partialId   = args.getInt(ARG_LOC_ID, 0);
            partialName = args.getString(ARG_LOC_NAME);
            partialCity = args.getString(ARG_LOC_CITY);
            partialImgEst = args.getString(ARG_LOC_IMG_EST);
            partialImgInt = args.getString(ARG_LOC_IMG_INT);
        }

        initViews(view);

        if (location != null) {
            // Location completa: mostra subito e poi aggiorna in background
            buildGalleryUrls();
            bindLocationData();
            if (!eventsLoaded) { eventsLoaded = true; loadEventsForTheater(); }
            loadFullLocation(location.getId());
        } else if (partialId > 0) {
            // Dati parziali: mostra nome/città/immagine subito, poi carica il resto dall'API
            bindPartialData(view);
            if (!eventsLoaded) { eventsLoaded = true; loadEventsForLocation(partialId); }
            loadFullLocation(partialId);
        }
    }

    // ── Campi temporanei per il percorso "repliche" ─────────────────────────
    private int    partialId;
    private String partialName, partialCity, partialImgEst, partialImgInt;

    /** Popola immediatamente nome, città e immagine dai dati parziali del Bundle. */
    private void bindPartialData(View v) {
        setText(v, R.id.theaterNameText,    partialName);
        setText(v, R.id.theaterAddressText, safe(partialCity));
        setText(v, R.id.theaterDetailsText, "");
        TextView srv = v.findViewById(R.id.theaterServicesText);
        if (srv != null) srv.setText("");

        // Galleria con i soli filename parziali
        String estUrl = com.example.dropby.network.ApiClient
                .resolveLocationImage(partialImgEst, Location.LocationImageType.EXTERIOR);
        String intUrl = com.example.dropby.network.ApiClient
                .resolveLocationImage(partialImgInt, Location.LocationImageType.INTERIOR);
        if (estUrl != null && intUrl != null)      galleryUrls = new String[]{ estUrl, intUrl };
        else if (estUrl != null)                   galleryUrls = new String[]{ estUrl };
        else if (intUrl != null)                   galleryUrls = new String[]{ intUrl };
        else                                       galleryUrls = new String[0];
        setupGallery();
    }

    private void loadFullLocation(int locationId) {
        if (locationId <= 0) return;
        ApiClient.getApiService().getLocationById(locationId)
                .enqueue(new Callback<Location>() {
                    @Override
                    public void onResponse(@NonNull Call<Location> call,
                                           @NonNull Response<Location> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            location = response.body();
                            buildGalleryUrls();
                            bindLocationData();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Location> call, @NonNull Throwable t) {}
                });
    }

    private void initViews(View view) {
        galleryImageView     = view.findViewById(R.id.theaterGalleryImage);
        previousImageButton  = view.findViewById(R.id.theaterPreviousImageButton);
        nextImageButton      = view.findViewById(R.id.theaterNextImageButton);
        eventsStatusTextView = view.findViewById(R.id.theaterEventsStatusText);
        eventsRecyclerView   = view.findViewById(R.id.theaterEventsRecyclerView);
        if (eventsRecyclerView != null) {
            eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            eventsRecyclerView.setNestedScrollingEnabled(false);
        }
    }

    /**
     * Costruisce l'array degli URL della galleria dalle due immagini disponibili.
     * Usa resolveImageEstUrl() / resolveImageIntUrl() che già conoscono la sottocartella.
     */
    private void buildGalleryUrls() {
        String est = location.resolveImageEstUrl();
        String int_ = location.resolveImageIntUrl();

        if (est != null && int_ != null) {
            galleryUrls = new String[]{ est, int_ };
        } else if (est != null) {
            galleryUrls = new String[]{ est };
        } else if (int_ != null) {
            galleryUrls = new String[]{ int_ };
        } else {
            galleryUrls = new String[0];
        }
    }

    // ── BIND DATA ────────────────────────────────────────────────────────────

    private void bindLocationData() {
        View v = getView();
        if (v == null) return;

        // Aggiorna solo se il valore dal server è disponibile (non sovrascrive con null)
        if (location.getName() != null)
            setText(v, R.id.theaterNameText, location.getName());

        String addr = safe(location.getAddress());
        String city = safe(location.getCity());
        if (!addr.isEmpty() || !city.isEmpty())
            setText(v, R.id.theaterAddressText,
                    addr.isEmpty() ? city : (city.isEmpty() ? addr : addr + " - " + city));

        if (location.getCapacity() > 0) {
            setText(
                    v,
                    R.id.theaterDetailsText,
                    getString(R.string.theatre_capacity, location.getCapacity())
            );
        }

        TextView srv = v.findViewById(R.id.theaterServicesText);
        if (srv != null && location.getServices() != null) {
            srv.setText(location.getServices().isEmpty()
                    ? getString(R.string.services_not_available)
                    : getString(R.string.services_label, TextUtils.join(", ", location.getServices())));
        }

        // Pulsante planimetria
        Button btnPlan = v.findViewById(R.id.theaterFloorPlanButton);
        if (btnPlan != null) {
            String planUrl = location.resolveFloorPlanUrl();
            btnPlan.setVisibility(planUrl != null ? View.VISIBLE : View.GONE);
            if (planUrl != null) btnPlan.setOnClickListener(x -> showFloorPlanDialog(planUrl));
        }

        // Pulsante indicazioni stradali
        Button btnDirections = v.findViewById(R.id.theaterDirectionsButton);
        if (btnDirections != null) {
            boolean hasCoords = location.getLatitude() != 0 || location.getLongitude() != 0;
            btnDirections.setVisibility(hasCoords ? View.VISIBLE : View.GONE);
            if (hasCoords) {
                btnDirections.setOnClickListener(x -> {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Uri navUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
                    navIntent.setPackage("com.google.android.apps.maps");
                    if (navIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivity(navIntent);
                    } else {
                        // Fallback: apre Google Maps nel browser
                        Uri fallback = Uri.parse(
                                "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lon);
                        startActivity(new Intent(Intent.ACTION_VIEW, fallback));
                    }
                });
            }
        }

        setupGallery();
    }

    // ── GALLERIA ─────────────────────────────────────────────────────────────

    private void setupGallery() {
        boolean multi = galleryUrls.length > 1;
        if (previousImageButton != null)
            previousImageButton.setVisibility(multi ? View.VISIBLE : View.GONE);
        if (nextImageButton != null)
            nextImageButton.setVisibility(multi ? View.VISIBLE : View.GONE);

        if (multi) {
            previousImageButton.setOnClickListener(x -> changeGalleryImage(-1));
            nextImageButton.setOnClickListener(x -> changeGalleryImage(1));
        }
        displayImage(0);
    }

    private void changeGalleryImage(int delta) {
        if (galleryUrls.length == 0) return;
        selectedImageIndex = (selectedImageIndex + delta + galleryUrls.length) % galleryUrls.length;
        displayImage(selectedImageIndex);
    }

    private void displayImage(int index) {
        if (galleryUrls.length == 0 || galleryImageView == null) return;
        // L'URL è già assoluto (costruito da resolveImageEstUrl/Int)
        Glide.with(this)
                .load(GlideHelper.url(galleryUrls[index]))
                .placeholder(R.drawable.image_placeholder)
                .into(galleryImageView);
    }

    private void showFloorPlanDialog(String resolvedUrl) {
        WebView wv = new WebView(requireContext());
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLoadWithOverviewMode(true);

        // Aggiunge gli header ngrok direttamente al WebView
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("ngrok-skip-browser-warning", "true");
        headers.put("User-Agent", "AndroidApp/1.0");
        wv.loadUrl(resolvedUrl, headers);   // ← overload con headers

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.floor_plan_title, safe(location.getName())))
                .setView(wv)
                .setPositiveButton(R.string.close_button, null)
                .show();
    }

    // ── EVENTI ───────────────────────────────────────────────────────────────

    private void loadEventsForTheater() {
        loadEventsForLocation(location != null ? location.getId() : 0);
    }

    private void loadEventsForLocation(int locationId) {
        if (locationId <= 0 || eventsStatusTextView == null) return;
        eventsStatusTextView.setVisibility(View.VISIBLE);
        eventsStatusTextView.setText(R.string.eventsStatusText);

        ApiClient.getApiService()
                .searchEvents(null, null, null, null, locationId, null, null, null)
                .enqueue(new Callback<EventsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EventsResponse> call,
                                           @NonNull Response<EventsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<EventGroup> groups = EventGroup.from(
                                    response.body().getEvents(), false);
                            if (groups.isEmpty()) {
                                eventsStatusTextView.setText(R.string.no_event_found);
                            } else {
                                eventsStatusTextView.setVisibility(View.GONE);
                                if (eventsRecyclerView != null)
                                    eventsRecyclerView.setAdapter(new EventGroupAdapter(
                                            groups, false,
                                            g -> openEventDetail(g.getRepresentative())));
                            }
                        } else {
                            eventsStatusTextView.setText(R.string.event_loading_error);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<EventsResponse> call, @NonNull Throwable t) {
                        if (isAdded() && eventsStatusTextView != null)
                            eventsStatusTextView.setText(R.string.connection_error);
                    }
                });
    }

    private void openEventDetail(Event event) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, EventDetailFragment.newInstance(event))
                .addToBackStack(null).commit();
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "—");
    }
    private String safe(String s) { return s != null ? s : ""; }
}