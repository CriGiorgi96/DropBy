package com.example.dropby.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.views.VideoBackgroundView;

public class StartFragment extends Fragment {

    private VideoBackgroundView backgroundVideo;

    public StartFragment() {
        super(R.layout.fragment_start);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView btnGuest = view.findViewById(R.id.btnGuest);
        backgroundVideo = view.findViewById(R.id.loginBackgroundVideo);
        ImageButton btnLanguageIt = view.findViewById(R.id.btnLanguageIt);
        ImageButton btnLanguageEn = view.findViewById(R.id.btnLanguageEn);

        updateLanguageButtons(btnLanguageIt, btnLanguageEn);

        btnLanguageIt.setOnClickListener(v -> setLanguage("it"));

        btnLanguageEn.setOnClickListener(v -> setLanguage("en-GB"));

        setupBackgroundVideo();

        btnLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnRegister.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new RegisterFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnGuest.setOnClickListener(v -> launchHome("GUEST"));
    }

    private void setupBackgroundVideo() {
        backgroundVideo.play(R.raw.dropby_login);
    }

    void launchHome(String userType) {
        requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE)
                .edit()
                .putString("userType", userType)
                .putString("username", "Spettatore")
                .putString("gender", "guest")
                // BUG FIX: un ospite non deve avere un userId (era hardcoded a 1).
                // Rimuoviamo qualsiasi userId precedente per evitare che un ospite
                // acceda a endpoint autenticati come /favorites/user/{userId}.
                .remove("userId")
                .apply();

        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (backgroundVideo != null) {
            backgroundVideo.resumeVideo();
        }
    }

    @Override
    public void onPause() {
        if (backgroundVideo != null) {
            backgroundVideo.pauseVideo();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (backgroundVideo != null) {
            backgroundVideo.releaseVideo();
            backgroundVideo = null;
        }
        super.onDestroyView();
    }

    // Change language
    private void setLanguage(String languageTag) {
        requireActivity()
                .getSharedPreferences("app_settings", requireContext().MODE_PRIVATE)
                .edit()
                .putString("language", languageTag)
                .apply();

        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageTag)
        );

        requireActivity().recreate();
    }

    private void updateLanguageButtons(ImageButton btnIt, ImageButton btnEn) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_settings", requireContext().MODE_PRIVATE);

        String language = prefs.getString("language", "it");

        if ("en-GB".equals(language)) {
            btnEn.setBackgroundResource(R.drawable.bg_language_selected);
            btnIt.setBackgroundResource(R.drawable.bg_language_normal);
        } else {
            btnIt.setBackgroundResource(R.drawable.bg_language_selected);
            btnEn.setBackgroundResource(R.drawable.bg_language_normal);
        }
    }
}
