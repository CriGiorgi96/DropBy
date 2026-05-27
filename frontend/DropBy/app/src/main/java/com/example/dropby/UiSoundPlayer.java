package com.example.dropby;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public final class UiSoundPlayer {

    private static final float VOLUME = 0.8f;

    private static SoundPool soundPool;
    private static int menuOpenSoundId;
    private static int buttonClickSoundId;
    private static boolean menuOpenLoaded;
    private static boolean buttonClickLoaded;

    private UiSoundPlayer() {}

    public static synchronized void init(Context context) {
        if (soundPool != null) return;

        // Log per debug
        android.util.Log.d("SoundPlayer", "Inizializzazione in corso...");

        // ... (resto del codice)

        menuOpenSoundId = soundPool.load(appContext, R.raw.apertura_menu, 1);
        buttonClickSoundId = soundPool.load(appContext, R.raw.button_click, 1);

        // Log per verificare se l'ID è stato assegnato
        android.util.Log.d("SoundPlayer", "Menu Sound ID: " + menuOpenSoundId);
    }

    public static void playMenuOpen(Context context) {
        init(context);
        play(menuOpenSoundId, menuOpenLoaded);
    }

    public static void playButtonClick(Context context) {
        init(context);
        play(buttonClickSoundId, buttonClickLoaded);
    }

    private static synchronized void play(int soundId, boolean loaded) {
        if (soundPool == null || soundId == 0 || !loaded) return;
        soundPool.play(soundId, VOLUME, VOLUME, 1, 0, 1.0f);
    }

    public static synchronized void release() {
        if (soundPool == null) return;
        soundPool.release();
        soundPool = null;
        menuOpenSoundId = 0;
        buttonClickSoundId = 0;
        menuOpenLoaded = false;
        buttonClickLoaded = false;
    }
}
