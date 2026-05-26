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

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(4)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status != 0) return;
            if (sampleId == menuOpenSoundId) menuOpenLoaded = true;
            if (sampleId == buttonClickSoundId) buttonClickLoaded = true;
        });

        Context appContext = context.getApplicationContext();
        menuOpenSoundId = soundPool.load(appContext, R.raw.apertura_menu, 1);
        buttonClickSoundId = soundPool.load(appContext, R.raw.button_click, 1);
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
