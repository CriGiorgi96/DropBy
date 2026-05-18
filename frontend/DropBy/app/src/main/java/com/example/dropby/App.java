package com.example.dropby;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        NotificationReceiver.createChannelIfNeeded(context);
    }

    public static Context getContext() {
        return context;
    }
}