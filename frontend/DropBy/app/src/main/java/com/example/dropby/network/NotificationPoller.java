package com.example.dropby.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.dropby.network.ApiService.NotificationsWrapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Esegue il polling delle notifiche non lette ogni 60 secondi mentre l'app è in foreground.
 * Chiama il callback con il numero di notifiche non lette.
 */
public class NotificationPoller {

    public interface BadgeCallback {
        void onUnreadCount(int count);
    }

    private static final String TAG = "NotificationPoller";
    private static final long INTERVAL_MS = 60_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BadgeCallback callback;
    private boolean running = false;

    public NotificationPoller(BadgeCallback callback) {
        this.callback = callback;
    }

    public void start() {
        running = true;
        poll();
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void poll() {
        if (!running) return;
        ApiClient.getApiService()
                .getNotifications(true)
                .enqueue(new Callback<NotificationsWrapper>() {
                    @Override
                    public void onResponse(Call<NotificationsWrapper> call,
                                           Response<NotificationsWrapper> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<?> list = response.body().notifications;
                            callback.onUnreadCount(list != null ? list.size() : 0);
                        }
                        scheduleNext();
                    }

                    @Override
                    public void onFailure(Call<NotificationsWrapper> call, Throwable t) {
                        Log.w(TAG, "Polling fallito: " + t.getMessage());
                        scheduleNext();
                    }
                });
    }

    private void scheduleNext() {
        if (running) handler.postDelayed(this::poll, INTERVAL_MS);
    }
}
