package com.example.dropby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dropby.fragments.auth.LoginFragment;
import com.example.dropby.fragments.auth.RegisterFragment;
import com.example.dropby.fragments.auth.StartFragment;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Auto-login: se il token è già salvato salta la schermata di autenticazione
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token != null && !token.isEmpty()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

<<<<<<< HEAD
        SharedPreferences settings = getSharedPreferences("app_settings", MODE_PRIVATE);
        String language = settings.getString("language", "it");

        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language)
        );

=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        setContentView(R.layout.activity_auth);

        View authContainer = findViewById(R.id.auth_fragment_container);

        ViewCompat.setOnApplyWindowInsetsListener(authContainer, (view, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),
                    statusBarHeight,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            return insets;
        });

        // Carica StartFragment solo al primo avvio, non al cambio di configurazione
        if (savedInstanceState == null) {

            String mode = getIntent().getStringExtra("mode");

            Fragment selectedFragment;

            if ("register".equals(mode)) {
                selectedFragment = new RegisterFragment();
            } else if ("login".equals(mode)) {
                selectedFragment = new LoginFragment();
            } else {
                // Parte tua: schermata intro con video, titolo e bottoni
                selectedFragment = new StartFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, selectedFragment)
                    .commit();
        }
    }

    // Helper methods to avoid duplicated code with "new register/login"
    public void openLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new LoginFragment())
                .commit();
    }

    public void openRegister() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }
}
