package com.example.dropby.fragments.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dropby.R;
import com.example.dropby.models.Location;
import com.example.dropby.models.LocationsResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.GlideHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    // ── COORDINATE CITTÀ ITALIANE ────────────────────────────────────────────
    private static final Map<String, LatLng> CITY_COORDS = new HashMap<>();
    static {
        CITY_COORDS.put("Tutte le città",      null);
        CITY_COORDS.put("Roma",                new LatLng(41.9028, 12.4964));
        CITY_COORDS.put("Milano",              new LatLng(45.4654, 9.1859));
        CITY_COORDS.put("Napoli",              new LatLng(40.8518, 14.2681));
        CITY_COORDS.put("Torino",              new LatLng(45.0703, 7.6869));
        CITY_COORDS.put("Bologna",             new LatLng(44.4949, 11.3426));
        CITY_COORDS.put("Firenze",             new LatLng(43.7696, 11.2558));
        CITY_COORDS.put("Venezia",             new LatLng(45.4408, 12.3155));
        CITY_COORDS.put("Palermo",             new LatLng(38.1157, 13.3615));
        CITY_COORDS.put("Verona",              new LatLng(45.4384, 10.9916));
        CITY_COORDS.put("Trieste",             new LatLng(45.6495, 13.7768));
        CITY_COORDS.put("Reggio Emilia",       new LatLng(44.6989, 10.6297));
        CITY_COORDS.put("Modena",              new LatLng(44.6471, 10.9252));
        CITY_COORDS.put("Parma",               new LatLng(44.8015, 10.3279));
        CITY_COORDS.put("Carpi",               new LatLng(44.7833, 10.8833));
        CITY_COORDS.put("Casalecchio di Reno", new LatLng(44.4742, 11.2706));
        CITY_COORDS.put("Piacenza",            new LatLng(45.0526, 9.6930));
        CITY_COORDS.put("Ravenna",             new LatLng(44.4184, 12.2035));
        CITY_COORDS.put("Ferrara",             new LatLng(44.8381, 11.6198));
        CITY_COORDS.put("Rimini",              new LatLng(44.0594, 12.5683));
        CITY_COORDS.put("Bari",                new LatLng(41.1171, 16.8719));
        CITY_COORDS.put("Catania",             new LatLng(37.5079, 15.0830));
        CITY_COORDS.put("Genova",              new LatLng(44.4056, 8.9463));
        CITY_COORDS.put("Pesaro",              new LatLng(43.9107, 12.9131));
    }

    private static final int[] RADIUS_KM = {10, 25, 50, 100, 200, 500};

    // Google Maps
    private GoogleMap googleMap;
    private Circle radiusCircle;
    private final List<Marker> currentMarkers = new ArrayList<>();
    private final Map<Integer, Bitmap> bitmapCache = new HashMap<>();

    // Filtri UI
    private Spinner citySpinner;
    private SeekBar radiusSeekBar;
    private TextView radiusLabel;
    private TextView statusText;

    // Dati
    private List<Location> allLocations = new ArrayList<>();
    private String selectedCity;
    private int selectedRadiusKm = 50;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

<<<<<<< HEAD
        selectedCity = getString(R.string.all_cities);

=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        citySpinner   = view.findViewById(R.id.citySpinner);
        radiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        radiusLabel   = view.findViewById(R.id.radiusLabel);
        statusText    = view.findViewById(R.id.theatersStatusText);

        setupCitySpinner();
        setupRadiusSeekBar();

        view.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> applyFilter());

        // Inizializza Google Maps tramite fragment annidato
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadAllTheaters();
    }

    // ── GOOGLE MAPS CALLBACK ──────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(42.5, 12.5), 5.5f));

        enableMyLocation();

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return buildInfoWindow(marker);
            }
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                return null;
            }
        });

        // Tap su marker: mostra InfoWindow (return false = default behavior) e avvia caricamento immagine
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Location) {
                Location loc = (Location) tag;
                if (!bitmapCache.containsKey(loc.getId())) {
                    loadMarkerImage(loc, marker);
                }
            }
            return false; // mostra InfoWindow e sposta la camera
        });

        // Tap sull'InfoWindow → apre la scheda teatro
        googleMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Location) {
                openTheaterDetail((Location) tag);
            }
        });

        if (!allLocations.isEmpty()) applyFilter();
    }

    private View buildInfoWindow(Marker marker) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.item_map_infowindow, null);

        Object tag = marker.getTag();
        if (!(tag instanceof Location)) return view;
        Location loc = (Location) tag;

        TextView nameView  = view.findViewById(R.id.infoWindowName);
        TextView cityView  = view.findViewById(R.id.infoWindowCity);
        ImageView imgView  = view.findViewById(R.id.infoWindowImage);

        nameView.setText(loc.getName());
        cityView.setText(loc.getCity());

        Bitmap cached = bitmapCache.get(loc.getId());
        if (cached != null) {
            imgView.setImageBitmap(cached);
        } else {
            imgView.setImageResource(R.drawable.image_placeholder);
        }
        return view;
    }

    private void loadMarkerImage(Location loc, Marker marker) {
        String url = loc.resolveImageEstUrl();
        if (url == null || !isAdded()) return;

        Glide.with(this)
                .asBitmap()
                .load(GlideHelper.url(url))
                .into(new CustomTarget<Bitmap>(400, 220) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        bitmapCache.put(loc.getId(), resource);
                        if (marker.isInfoWindowShown()) {
                            marker.showInfoWindow(); // re-render con immagine caricata
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) googleMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    // ── SPINNER CITTÀ ─────────────────────────────────────────────────────────

    private void setupCitySpinner() {
        List<String> cities = new ArrayList<>(CITY_COORDS.keySet());
        cities.remove("Tutte le città");
        cities.sort(String::compareTo);
        cities.add(0, "Tutte le città");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedCity = (String) p.getItemAtPosition(pos);
                boolean showRadius = !"Tutte le città".equals(selectedCity);
                radiusSeekBar.setVisibility(showRadius ? View.VISIBLE : View.GONE);
                radiusLabel.setVisibility(showRadius ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ── SEEKBAR RAGGIO ────────────────────────────────────────────────────────

    private void setupRadiusSeekBar() {
        radiusSeekBar.setMax(RADIUS_KM.length - 1);
        radiusSeekBar.setProgress(2);
        updateRadiusLabel(RADIUS_KM[2]);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                selectedRadiusKm = RADIUS_KM[p];
                updateRadiusLabel(selectedRadiusKm);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void updateRadiusLabel(int km) {
        radiusLabel.setText(getString(R.string.radius_km, km));
    }

    // ── CARICAMENTO TEATRI ────────────────────────────────────────────────────

    private void loadAllTheaters() {
        statusText.setVisibility(View.VISIBLE);
<<<<<<< HEAD
        statusText.setText(getString(R.string.theatersStatusText));
=======
        statusText.setText("Caricamento teatri…");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

        ApiClient.getApiService().getLocations(null, null)
                .enqueue(new Callback<LocationsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationsResponse> call,
                                           @NonNull Response<LocationsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            allLocations = response.body().getLocations();
                            statusText.setVisibility(View.GONE);
                            if (googleMap != null) applyFilter();
                        } else {
<<<<<<< HEAD
                            statusText.setText(getString(R.string.error_loading_theaters));
=======
                            statusText.setText("Errore caricamento teatri");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<LocationsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
<<<<<<< HEAD
                        statusText.setText(getString(R.string.network_absent));
=======
                        statusText.setText("Rete assente");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                    }
                });
    }

    // ── FILTRO E MARKER ───────────────────────────────────────────────────────

    private void applyFilter() {
        if (googleMap == null) return;

        // Rimuovi marker e cerchio precedenti
        for (Marker m : currentMarkers) m.remove();
        currentMarkers.clear();
        if (radiusCircle != null) { radiusCircle.remove(); radiusCircle = null; }

        LatLng center = CITY_COORDS.get(selectedCity);
        List<Location> filtered = new ArrayList<>();

        if (center == null) {
            filtered.addAll(allLocations);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(42.5, 12.5), 5.5f));
        } else {
            for (Location loc : allLocations) {
                if (loc.getLatitude() == 0 && loc.getLongitude() == 0) continue;
                double dist = haversineKm(center.latitude, center.longitude,
                        loc.getLatitude(), loc.getLongitude());
                if (dist <= selectedRadiusKm) filtered.add(loc);
            }

            // Disegna cerchio del raggio
            radiusCircle = googleMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(selectedRadiusKm * 1000.0)
                    .strokeColor(0xFF7B1FA2)
                    .fillColor(0x1A7B1FA2)
                    .strokeWidth(3f));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    center, radiusToZoom(selectedRadiusKm)));
        }

        for (Location loc : filtered) {
            if (loc.getLatitude() == 0 && loc.getLongitude() == 0) continue;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title(loc.getName())
                    .snippet(loc.getCity())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            if (marker != null) {
                marker.setTag(loc);
                currentMarkers.add(marker);
            }
        }
    }

    private void openTheaterDetail(Location location) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, TheaterDetailFragment.newInstance(location))
                .addToBackStack(null)
                .commit();
    }

    // ── MATH ─────────────────────────────────────────────────────────────────

    private static double haversineKm(double lat1, double lon1,
                                      double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static float radiusToZoom(int km) {
        if (km <= 10)  return 12f;
        if (km <= 25)  return 11f;
        if (km <= 50)  return 10f;
        if (km <= 100) return 9f;
        if (km <= 200) return 8f;
        return 7f;
    }
}
