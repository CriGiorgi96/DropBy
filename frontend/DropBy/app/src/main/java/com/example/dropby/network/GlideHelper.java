package com.example.dropby.network;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

/**
 * Aggiunge gli header necessari per ngrok a ogni richiesta Glide.
 * Glide usa il proprio stack HTTP separato da OkHttp/Retrofit,
 * quindi l'interceptor in ApiClient non lo copre.
 *
 * Uso:
 *   Glide.with(context)
 *        .load(GlideHelper.url(rawUrl))
 *        .placeholder(R.drawable....)
 *        .into(imageView);
 */
public final class GlideHelper {

    private GlideHelper() {}

    public static GlideUrl url(String rawUrl) {
        String resolved = ApiClient.resolveUrl(rawUrl);
        if (resolved == null || resolved.isEmpty()) return null;
        return new GlideUrl(resolved, new LazyHeaders.Builder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("User-Agent", "AndroidApp/1.0")
                .build());
    }
}