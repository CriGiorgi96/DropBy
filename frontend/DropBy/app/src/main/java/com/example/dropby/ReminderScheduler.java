package com.example.dropby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderScheduler {

    public static void schedule(Context ctx, int eventId, String eventTitle, long triggerAtMs) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = buildPi(ctx, eventId, eventTitle);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Exact alarm permission not granted: fall back to inexact
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
            }
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
        }
    }

    public static void cancel(Context ctx, int eventId) {
        PendingIntent pi = PendingIntent.getBroadcast(ctx, eventId,
                new Intent(ctx, NotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi == null) return;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pi);
        pi.cancel();
    }

    public static boolean isScheduled(Context ctx, int eventId) {
        return PendingIntent.getBroadcast(ctx, eventId,
                new Intent(ctx, NotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
    }

    private static PendingIntent buildPi(Context ctx, int eventId, String eventTitle) {
        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_ID, eventId);
        intent.putExtra(NotificationReceiver.EXTRA_EVENT_TITLE, eventTitle);
        return PendingIntent.getBroadcast(ctx, eventId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
