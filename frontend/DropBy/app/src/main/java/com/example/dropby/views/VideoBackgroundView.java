package com.example.dropby.views;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.Nullable;

public class VideoBackgroundView extends TextureView implements TextureView.SurfaceTextureListener {

    private MediaPlayer mediaPlayer;
    private int rawVideoResId;
    private boolean shouldPlay;

    public VideoBackgroundView(Context context) {
        super(context);
        init();
    }

    public VideoBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void play(int rawVideoResId) {
        this.rawVideoResId = rawVideoResId;
        shouldPlay = true;
        if (isAvailable()) {
            openPlayer(new Surface(getSurfaceTexture()));
        }
    }

    public void resumeVideo() {
        shouldPlay = true;
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        } else if (mediaPlayer == null && isAvailable() && rawVideoResId != 0) {
            openPlayer(new Surface(getSurfaceTexture()));
        }
    }

    public void pauseVideo() {
        shouldPlay = false;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void releaseVideo() {
        shouldPlay = false;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void openPlayer(Surface surface) {
        releaseCurrentPlayer();
        try {
            MediaPlayer player = new MediaPlayer();
            AssetFileDescriptor afd = getResources().openRawResourceFd(rawVideoResId);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.setSurface(surface);
            player.setLooping(true);
            player.setVolume(0f, 0f);
            player.setOnPreparedListener(preparedPlayer -> {
                applyCoverTransform(preparedPlayer.getVideoWidth(), preparedPlayer.getVideoHeight());
                if (shouldPlay) {
                    preparedPlayer.start();
                }
            });
            player.setOnVideoSizeChangedListener((preparedPlayer, width, height) ->
                    applyCoverTransform(width, height));
            player.prepareAsync();
            mediaPlayer = player;
        } catch (Exception e) {
            android.util.Log.e("VideoBackgroundView", "Unable to play login background", e);
            surface.release();
        }
    }

    private void releaseCurrentPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void applyCoverTransform(int videoWidth, int videoHeight) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0 || videoWidth == 0 || videoHeight == 0) {
            return;
        }

        float scale = Math.max(viewWidth / (float) videoWidth, viewHeight / (float) videoHeight);
        float scaledWidth = videoWidth * scale;
        float scaledHeight = videoHeight * scale;
        float dx = (viewWidth - scaledWidth) / 2f;
        float dy = (viewHeight - scaledHeight) / 2f;

        Matrix matrix = new Matrix();
        matrix.setScale(scaledWidth / viewWidth, scaledHeight / viewHeight);
        matrix.postTranslate(dx, dy);
        setTransform(matrix);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (shouldPlay && rawVideoResId != 0) {
            openPlayer(new Surface(surfaceTexture));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (mediaPlayer != null) {
            applyCoverTransform(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        releaseVideo();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}
