package com.example.dropby;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID        = "event_reminders";
    public static final String EXTRA_EVENT_ID    = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        int    eventId    = intent.getIntExtra(EXTRA_EVENT_ID, 0);
        String eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        if (eventTitle == null) eventTitle = "Evento";

        createChannelIfNeeded(context);

        Intent openIntent = new Intent(context, HomeActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, eventId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_24)
                .setContentTitle("Promemoria evento")
                .setContentText("Tra poco: " + eventTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Hai un evento in programma: " + eventTitle))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(eventId, builder.build());
    }

    public static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Promemoria eventi",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notifiche di promemoria per gli eventi");
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
}
