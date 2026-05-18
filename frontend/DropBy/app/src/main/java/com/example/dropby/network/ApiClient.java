package com.example.dropby.network;

import android.content.SharedPreferences;
import android.os.Build;

import com.example.dropby.App;
import com.example.dropby.models.Location;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String EMULATOR_URL = "http://10.0.2.2:5000/";
    private static final String DEVICE_URL   = "http://127.0.0.1:5000/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        SharedPreferences prefs = App.getContext()
                                .getSharedPreferences("app", App.getContext().MODE_PRIVATE);
                        String token = prefs.getString("token", null);

                        okhttp3.Request.Builder builder = chain.request().newBuilder()
                                .header("ngrok-skip-browser-warning", "true")
                                .header("User-Agent", "AndroidApp/1.0");

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    // ── URL RESOLUTION ───────────────────────────────────────────────────────

    /**
     * Converte un URL relativo (es. /assets/events/...) in assoluto.
     * Se già assoluto, lo restituisce invariato.
     */
    public static String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String base = getBaseUrl();
        String path = url.startsWith("/") ? url.substring(1) : url;
        return base + path;
    }

    /**
     * Risolve il nome file di un'immagine di una location nel suo URL assoluto,
     * usando la sottocartella corretta in base al tipo.
     *
     * Struttura fisica:
     *   static/images/foto_luoghi/foto_teatri/   ← immagini esterne e interne
     *   static/images/foto_luoghi/planimetrie/   ← planimetrie
     *
     * @param filename  solo il nome file, es. "verona_est.jpg" (può essere null)
     * @param type      tipo di immagine (EXTERIOR, INTERIOR, FLOOR_PLAN)
     * @return URL assoluto o null se filename è null/vuoto
     */
    public static String resolveLocationImage(String filename,
                                              Location.LocationImageType type) {
        if (filename == null || filename.trim().isEmpty()) return null;

        String subfolder;
        switch (type) {
            case FLOOR_PLAN:
                subfolder = "static/images/foto_luoghi/planimetrie/";
                break;
            case EXTERIOR:
            case INTERIOR:
            default:
                subfolder = "static/images/foto_luoghi/foto_teatri/";
                break;
        }
        return getBaseUrl() + subfolder + filename;
    }

    // ── BASE URL ─────────────────────────────────────────────────────────────

    public static void setCustomBaseUrl(String url) {
        SharedPreferences prefs = App.getContext()
                .getSharedPreferences("app_settings", App.getContext().MODE_PRIVATE);
        prefs.edit().putString("base_url", url).apply();
        retrofit = null; // forza la ricreazione del client
    }

    public static String getBaseUrl() {
        SharedPreferences prefs = App.getContext()
                .getSharedPreferences("app_settings", App.getContext().MODE_PRIVATE);
        String saved = prefs.getString("base_url", null);
        if (saved != null && !saved.isEmpty()) return saved;
        return isProbablyEmulator() ? EMULATOR_URL : DEVICE_URL;
    }

    private static boolean isProbablyEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
