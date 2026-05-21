package com.example.dropby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.dropby.fragments.main.EventsFragment;
import com.example.dropby.fragments.main.FaqFragment;
import com.example.dropby.fragments.main.FavoritesFragment;
import com.example.dropby.fragments.main.FriendsFragment;
import com.example.dropby.fragments.main.HomeFragment;
import com.example.dropby.fragments.main.MapFragment;
import com.example.dropby.fragments.main.NotificationManagementFragment; // Mantenuto per i tuoi Promemoria
import com.example.dropby.fragments.main.NotificationsFragment;
import com.example.dropby.fragments.main.set.ProfileSettingsFragment;
import com.example.dropby.fragments.main.ProposalsFragment;
import com.example.dropby.fragments.main.ReviewsFragment;
import com.example.dropby.fragments.main.SettingsFragment;
import com.example.dropby.fragments.main.TheatersFragment;
import com.example.dropby.network.NotificationPoller;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {

    private View leftDrawerPanel;
    private View rightDrawerPanel;
    private boolean leftOpen = false;
    private boolean rightOpen = false;
    private NavigationView navLeft;
    private NavigationView navRight;
    private TextView notificationBadge;
    private NotificationPoller notificationPoller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FIX BUG: Collegamento del badge delle notifiche (mancava nel codice della collega)
        notificationBadge = findViewById(R.id.notificationBadge);

        // Gestione dei margini di sistema (Status Bar e Navigation Bar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int navLeftInset = navBars.left;
            int navRightInset = navBars.right;
            int navBottomInset = navBars.bottom;

            view.setPadding(
                    navLeftInset,
                    statusBarHeight,
                    navRightInset,
                    view.getPaddingBottom()
            );

            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = getResources().getDimensionPixelSize(R.dimen.toolbar_height) + statusBarHeight;
            view.setLayoutParams(lp);

            navLeft = findViewById(R.id.navigation_view);
            navRight = findViewById(R.id.navigation_view_right);

            navLeft.setPadding(navLeftInset, lp.height, 0, navBottomInset);
            navRight.setPadding(0, lp.height, navRightInset, navBottomInset);

            FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
            fragmentContainer.setPadding(
                    navLeftInset,
                    lp.height,
                    navRightInset,
                    0
            );

            return insets;
        });

        // Il click sul logo della toolbar riporta alla HomeFragment
        findViewById(R.id.toolbarLogo).setOnClickListener(v -> {
            openFragment(new HomeFragment(), false);
            closeLeftDrawer();
            closeRightDrawer();
        });

        ImageView toolbarProfileButton = findViewById(R.id.toolbarUserButton);
        loadToolbarProfileImage(toolbarProfileButton);

        leftDrawerPanel = findViewById(R.id.leftDrawerPanel);
        rightDrawerPanel = findViewById(R.id.rightDrawerPanel);

        // Imposta le proporzioni dei pannelli laterali
        setDrawerWidth(leftDrawerPanel);
        setDrawerWidth(rightDrawerPanel);

        leftDrawerPanel.post(() -> {
            leftDrawerPanel.setTranslationX(-leftDrawerPanel.getWidth());
            rightDrawerPanel.setTranslationX(rightDrawerPanel.getWidth());
        });

        // Gestione click pulsanti Toolbar per aprire/chiudere i nuovi pannelli
        findViewById(R.id.toolbarLeftMenuButton).setOnClickListener(v -> toggleLeftDrawer());
        findViewById(R.id.toolbarUserButton).setOnClickListener(v -> toggleRightDrawer());

        navLeft = findViewById(R.id.navigation_view);
        navRight = findViewById(R.id.navigation_view_right);

        setupLeftDrawer();
        setupRightDrawer();
        updateRightDrawerHeader();

        // Intercettazione del pulsante "Indietro" fisico/gestuale dello smartphone
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (rightOpen) {
                    closeRightDrawer();
                    return;
                }
                if (leftOpen) {
                    closeLeftDrawer();
                    return;
                }
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    finish();
                }
            }
        });

        if (savedInstanceState == null) {
            openFragment(new HomeFragment(), false);
            navLeft.setCheckedItem(R.id.nav_home);
        }

        startNotificationPoller();
    }

    // ── DRAWER SINISTRO ──────────────────────────────────────────────────────

    private void setupLeftDrawer() {
        navLeft.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target = null;

            if (id == R.id.nav_home)           target = new HomeFragment();
            else if (id == R.id.nav_events)    target = new EventsFragment();
            else if (id == R.id.nav_theaters)  target = new TheatersFragment();
            else if (id == R.id.nav_map)       target = new MapFragment();
            else if (id == R.id.nav_settings)  target = new SettingsFragment();
            else if (id == R.id.nav_faq)       target = new FaqFragment();
            else if (id == R.id.nav_logout)    { logout(); return true; }

            if (target != null) {
                openFragment(target, false);
            }
            closeLeftDrawer();
            closeRightDrawer();
            return true;
        });
    }

    private void toggleLeftDrawer() {
        leftOpen = !leftOpen;
        leftDrawerPanel.animate()
                .translationX(leftOpen ? 0 : -leftDrawerPanel.getWidth())
                .setDuration(200)
                .start();
    }

    private void closeLeftDrawer() {
        leftOpen = false;
        leftDrawerPanel.animate()
                .translationX(-leftDrawerPanel.getWidth())
                .setDuration(200)
                .start();
    }

    // ── DRAWER DESTRO ────────────────────────────────────────────────────────

    private void setupRightDrawer() {
        boolean isLogged = isLoggedIn();
        Menu menu = navRight.getMenu();

        // Voci visibili solo agli utenti autenticati (REINSERITO i tuoi Promemoria)
        setMenuItemVisible(menu, R.id.nav_right_profile,       isLogged);
        setMenuItemVisible(menu, R.id.nav_right_favorites,     isLogged);
        setMenuItemVisible(menu, R.id.nav_right_reviews,       isLogged);
        setMenuItemVisible(menu, R.id.nav_right_friends,       isLogged);
        setMenuItemVisible(menu, R.id.nav_right_proposals,     isLogged);
        setMenuItemVisible(menu, R.id.nav_right_notifications, isLogged);
        setMenuItemVisible(menu, R.id.nav_right_reminders,     isLogged); // <--- Salvato!
        setMenuItemVisible(menu, R.id.nav_right_settings,      isLogged);
        setMenuItemVisible(menu, R.id.nav_right_logout,        isLogged);

        // Submenu headers
        setMenuItemVisible(menu, R.id.nav_right_group_social,  isLogged);
        setMenuItemVisible(menu, R.id.nav_right_group_account, isLogged);

        // Voci visibili solo agli ospiti
        setMenuItemVisible(menu, R.id.nav_right_login,    !isLogged);
        setMenuItemVisible(menu, R.id.nav_right_register, !isLogged);
        setMenuItemVisible(menu, R.id.nav_right_exit,     !isLogged);

        navRight.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target = null;

            if (isLogged) {
                if      (id == R.id.nav_right_profile)       target = new ProfileSettingsFragment();
                else if (id == R.id.nav_right_favorites)     target = new FavoritesFragment();
                else if (id == R.id.nav_right_reviews)       target = new ReviewsFragment();
                else if (id == R.id.nav_right_friends)       target = new FriendsFragment();
                else if (id == R.id.nav_right_proposals)     target = new ProposalsFragment();
                else if (id == R.id.nav_right_notifications) target = new NotificationsFragment();
                else if (id == R.id.nav_right_reminders)     target = new NotificationManagementFragment(); // <--- Salvato!
                else if (id == R.id.nav_right_settings)      target = new SettingsFragment();
                else if (id == R.id.nav_right_logout)        { logout(); return true; }
            } else {
                if      (id == R.id.nav_right_login)    { launchAuth("login");    return true; }
                else if (id == R.id.nav_right_register) { launchAuth("register"); return true; }
                else if (id == R.id.nav_right_exit)     { goToStart();            return true; }
            }

            if (target != null) openFragment(target, true);
            closeRightDrawer();
            closeLeftDrawer();
            return true;
        });
    }

    private void setMenuItemVisible(Menu menu, int itemId, boolean visible) {
        android.view.MenuItem item = menu.findItem(itemId);
        if (item != null) item.setVisible(visible).setEnabled(visible);
    }

    private void launchAuth(String mode) {
        closeRightDrawer();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }

    private void goToStart() {
        closeRightDrawer();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateRightDrawerHeader() {
        View header = navRight.getHeaderView(0);
        if (header == null) return;

        ImageView avatar     = header.findViewById(R.id.headerAvatar);
        TextView usernameView = header.findViewById(R.id.headerUsername);
        TextView emailView    = header.findViewById(R.id.headerEmail);

        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        boolean logged = isLoggedIn();

        if (logged) {
            String username = prefs.getString("username", "Utente");
            String email    = prefs.getString("email", "");
            usernameView.setText(username);
            emailView.setText(email);

            loadToolbarProfileImage(avatar);
        } else {
            usernameView.setText("Ospite");
            emailView.setText("");
            avatar.setImageResource(R.drawable.default_guest);
        }
    }

    private void toggleRightDrawer() {
        rightOpen = !rightOpen;
        rightDrawerPanel.animate()
                .translationX(rightOpen ? 0 : rightDrawerPanel.getWidth())
                .setDuration(200)
                .start();
    }

    private void closeRightDrawer() {
        rightOpen = false;
        rightDrawerPanel.animate()
                .translationX(rightDrawerPanel.getWidth())
                .setDuration(200)
                .start();
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    public void openFragment(Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction tx =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }

    public void logout() {
        if (notificationPoller != null) notificationPoller.stop();

        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);

        // ── DEBUG LOG: stato prefs PRIMA del logout ──────────────────────────
        Log.d("DEBUG_PREFS", "[LOGOUT] userId="      + prefs.getInt("userId", -1)
                + " | imageType="  + prefs.getString("profileImageType", "")
                + " | imageValue=" + prefs.getString("profileImageValue", "")
                + " | username="   + prefs.getString("username", ""));
        // ─────────────────────────────────────────────────────────────────────

        prefs.edit()
                .remove("token")
                .remove("userType")
                .remove("email")
                .remove("username")
                .remove("userId")
                .remove("profileImageType")
                .remove("profileImageValue")
                .remove("gender")
                .remove("privateFavorites")
                .remove("privateReviews")
                .apply();

        Log.d("DEBUG_PREFS", "[LOGOUT] SharedPreferences azzerate completamente");

        com.example.dropby.network.ApiClient.setCustomBaseUrl(com.example.dropby.network.ApiClient.getBaseUrl());

        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        return token != null && !token.isEmpty();
    }

    public String getUserType() {
        return getSharedPreferences("app", MODE_PRIVATE).getString("userType", "GUEST");
    }

    public String getUsername() {
        return getSharedPreferences("app", MODE_PRIVATE).getString("username", "Spettatore");
    }

    public String getGender() {
        return getSharedPreferences("app", MODE_PRIVATE).getString("gender", "guest");
    }

    public void updateNotificationBadge(int count) {
        if (notificationBadge == null) return;
        if (count > 0) {
            notificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            notificationBadge.setVisibility(View.VISIBLE);
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    // ── NOTIFICHE POLLING ────────────────────────────────────────────────────

    private void startNotificationPoller() {
        if (!isLoggedIn()) return;
        notificationPoller = new NotificationPoller(count ->
                runOnUiThread(() -> updateNotificationBadge(count)));
        notificationPoller.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRightDrawerHeader();
        setupRightDrawer();
        if (notificationPoller == null && isLoggedIn()) startNotificationPoller();
        loadToolbarProfileImage(findViewById(R.id.toolbarUserButton));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationPoller != null) notificationPoller.stop();
        notificationPoller = null;
    }

    // ── LAYOUT ───────────────────────────────────────────────────────────────

    private void setDrawerWidth(View drawer) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        boolean isLandscape = dm.widthPixels > dm.heightPixels;
        int drawerWidth = isLandscape ? dm.widthPixels / 3 : dm.widthPixels / 2;

        ViewGroup.LayoutParams lp = drawer.getLayoutParams();
        lp.width = drawerWidth;
        drawer.setLayoutParams(lp);
    }

    private void showLoginPrompt() {
        Toast.makeText(this, "Accedi per usare questa funzione", Toast.LENGTH_SHORT).show();
    }

    // ── GESTIONE IMMAGINE AVATAR REALE ────────────────────────────────────────
    private void loadToolbarProfileImage(ImageView avatar) {
        if (avatar == null) return;
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);

        String imageType  = prefs.getString("profileImageType", "");
        String imageValue = prefs.getString("profileImageValue", "");
        String gender     = prefs.getString("gender", "guest");

        // ── DEBUG LOG: avatar letto ───────────────────────────────────────────
        Log.d("DEBUG_PREFS", "[TOOLBAR] userId=" + prefs.getInt("userId", -1)
                + " | imageType="  + imageType
                + " | imageValue=" + imageValue);

        if ("server".equals(imageType) && isServerFilename(imageValue)) {
            String url = com.example.dropby.network.ApiClient.getBaseUrl()
                    + "assets/avatars/" + imageValue;
            loadAvatarFromUrl(avatar, url);
            return;
        }

        if ("custom".equals(imageType) && !imageValue.isEmpty()) {
            avatar.setImageURI(Uri.parse(imageValue));
            return;
        }

        if ("default".equals(imageType) && !imageValue.isEmpty()) {
            int resId = getResources().getIdentifier(imageValue, "drawable", getPackageName());
            if (resId != 0) {
                avatar.setImageResource(resId);
                return;
            }
        }

        if ("male".equalsIgnoreCase(gender))        avatar.setImageResource(R.drawable.default_male);
        else if ("female".equalsIgnoreCase(gender)) avatar.setImageResource(R.drawable.default_female);
        else                                        avatar.setImageResource(R.drawable.default_guest);
    }

    private static boolean isServerFilename(String s) {
        if (s == null || s.isEmpty()) return false;
        String lower = s.toLowerCase(java.util.Locale.US);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".webp")
            || lower.endsWith(".gif");
    }

    private void loadAvatarFromUrl(ImageView avatar, String url) {
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) imageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeStream(conn.getInputStream());
                if (bitmap != null) {
                    runOnUiThread(() -> avatar.setImageBitmap(bitmap));
                }
            } catch (java.io.IOException ignored) { }
        }).start();
    }

    // Chiamato da ProfileSettingsFragment per aggiornare all'istante
    public void refreshProfileUi() {
        ImageView toolbarProfileButton = findViewById(R.id.toolbarUserButton);
        loadToolbarProfileImage(toolbarProfileButton);
        updateRightDrawerHeader();
    }
}
