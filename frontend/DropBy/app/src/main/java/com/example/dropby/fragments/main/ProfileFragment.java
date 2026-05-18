package com.example.dropby.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dropby.AuthActivity;
import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.fragments.main.set.ProfileSettingsFragment;

public class ProfileFragment extends Fragment {
    private ImageView imgProfile;
    private TextView tvGreeting;
    private Button btnProfileSettings;
    private Button btnLogout;
    private Button btnLogin;
    private Button btnRegister;
    private SharedPreferences prefs;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);

        imgProfile = view.findViewById(R.id.imgProfile);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        btnProfileSettings = view.findViewById(R.id.btnProfileSettings);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        setupProfile();

        return view;
    }

    private void setupProfile() {
        String userType = "GUEST";
        if (getActivity() instanceof HomeActivity) {
            userType = ((HomeActivity) getActivity()).getUserType();
        }
        if ("LOGGED".equals(userType)) {
            showLoggedUserProfile();
        } else {
            showGuestProfile();
        }
    }

    private void showGuestProfile() {
        tvGreeting.setText(R.string.greeting);
        imgProfile.setImageResource(R.drawable.default_guest);

        btnProfileSettings.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.VISIBLE);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.putExtra("mode", "login");
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.putExtra("mode", "register");
            startActivity(intent);
        });
    }

    private void loadProfileImage(String gender) {
        String imageType = prefs.getString("profileImageType", "");
        String imageValue = prefs.getString("profileImageValue", "");

        if ("custom".equals(imageType) && !imageValue.isEmpty()) {
            imgProfile.setImageURI(Uri.parse(imageValue));
            return;
        }

        if ("default".equals(imageType) && !imageValue.isEmpty()) {
            int resId = getResources().getIdentifier(
                    imageValue, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                imgProfile.setImageResource(resId);
                return;
            }
        }

        if ("male".equalsIgnoreCase(gender)) {
            imgProfile.setImageResource(R.drawable.default_male);
        } else if ("female".equalsIgnoreCase(gender)) {
            imgProfile.setImageResource(R.drawable.default_female);
        } else {
            imgProfile.setImageResource(R.drawable.default_guest);
        }
    }

    private void showLoggedUserProfile() {
        String username = "Spettatore";
        String gender = "guest";

        if (getActivity() instanceof HomeActivity) {
            username = ((HomeActivity) getActivity()).getUsername();
            gender = ((HomeActivity) getActivity()).getGender();
        }

        tvGreeting.setText("Ciao, " + username + "!");
        loadProfileImage(gender);

        btnProfileSettings.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
        btnRegister.setVisibility(View.GONE);

        btnProfileSettings.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileSettingsFragment())
                .addToBackStack(null)
                .commit());

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).logout();
        }
    }
}
