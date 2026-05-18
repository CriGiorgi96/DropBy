package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.R;
import com.example.dropby.fragments.main.set.ProfileSettingsFragment;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button urlButton = view.findViewById(R.id.btnUrlSettings);
        Button profileButton = view.findViewById(R.id.btnProfileSettings);

        urlButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new UrlConfigFragment())
                        .addToBackStack(null)
                        .commit()
        );

        profileButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileSettingsFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }
}