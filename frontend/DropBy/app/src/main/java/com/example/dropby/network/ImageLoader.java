package com.example.dropby.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.dropby.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ImageLoader() {
    }

    public static void loadInto(ImageView imageView, String imageUrl) {
        imageView.setImageResource(R.drawable.image_placeholder);
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setTag(null);
            return;
        }

        String resolvedUrl = ApiClient.resolveUrl(imageUrl);
        imageView.setTag(resolvedUrl);

        EXECUTOR.execute(() -> {
            Bitmap bitmap = downloadBitmap(resolvedUrl);
            MAIN_HANDLER.post(() -> {
                Object currentTag = imageView.getTag();
                if (!resolvedUrl.equals(currentTag)) {
                    return;
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.image_placeholder);
                }
            });
        });
    }

    private static Bitmap downloadBitmap(String resolvedUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolvedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(true);

            // ↓ AGGIUNGI QUESTI DUE HEADER
            connection.setRequestProperty("ngrok-skip-browser-warning", "true");
            connection.setRequestProperty("User-Agent", "AndroidApp/1.0");

            // ↓ AGGIUNGI IL LOGGING (rimuovilo dopo aver risolto)
            int responseCode = connection.getResponseCode();
            android.util.Log.d("ImageLoader", "URL: " + resolvedUrl);
            android.util.Log.d("ImageLoader", "HTTP: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try (InputStream stream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(stream);
            }
        } catch (Exception e) {
            android.util.Log.e("ImageLoader", "Errore su: " + resolvedUrl + " → " + e.getMessage());
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
