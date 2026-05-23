package com.example.dropby.fragments.main.set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;

public class DefaultImagesFragment extends Fragment {

    public DefaultImagesFragment() {
        super(R.layout.fragment_default_images);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupImage(view, R.id.imgDefaultMale, "default_male");
        setupImage(view, R.id.imgDefaultFemale, "default_female");
        setupImage(view, R.id.imgDefaultGuest, "default_guest");
        setupImage(view, R.id.imgDefaultM1, "male_01");
        setupImage(view, R.id.imgDefaultM2, "male_02");
        setupImage(view, R.id.imgDefaultM3, "male_03");
        setupImage(view, R.id.imgDefaultF1, "female_01");
        setupImage(view, R.id.imgDefaultF2, "female_02");
        setupImage(view, R.id.imgDefaultF3, "female_03");

        ImageButton btnBack = view.findViewById(R.id.btnBackToSettings);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    // Saves selected image to SharedPreferences and pops backstack
    private void setupImage(View view, int imageViewId, String drawableName) {
        ImageView imageView = view.findViewById(imageViewId);

        imageView.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("app", Context.MODE_PRIVATE);

            prefs.edit()
                    .putString("profileImageType", "default")
                    .putString("profileImageValue", drawableName)
                    .apply();

            if (requireActivity() instanceof HomeActivity) {
                ((HomeActivity) requireActivity()).refreshProfileUi();
            }

            Toast.makeText(requireContext(), "Immagine selezionata", Toast.LENGTH_SHORT).show();

            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}