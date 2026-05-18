package com.example.dropby.fragments.main.set;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;

public class ProfileSettingsFragment extends Fragment {

    public ProfileSettingsFragment() {
        super(R.layout.fragment_profile_settings);
    }
    private ImageView imgProfilePreview;
    private String selectedImageType = "";
    private String selectedImageValue = "";
    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);

        imgProfilePreview = view.findViewById(R.id.imgProfilePreview);
        Button btnChangeProfileImg = view.findViewById(R.id.btnChangeProfileImg);
        EditText usernameEditText = view.findViewById(R.id.editUsername);
        Button saveButton = view.findViewById(R.id.btnSaveProfile);
        Button backButton = view.findViewById(R.id.btnBackToSettings);

        usernameEditText.setText(prefs.getString("username", "Spettatore"));

        if (selectedImageType.isEmpty()) {
            selectedImageType = prefs.getString("profileImageType", "");
        }

        if (selectedImageValue.isEmpty()) {
            selectedImageValue = prefs.getString("profileImageValue", "");
        }

        loadProfileImagePreview(prefs);

        btnChangeProfileImg.setOnClickListener(v -> showImageChoiceMenu());

        saveButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Inserisci un nome utente");
                return;
            }

            prefs.edit()
                    .putString("username", username)
                    .putString("profileImageType", selectedImageType)
                    .putString("profileImageValue", selectedImageValue)
                    .apply();

            if (requireActivity() instanceof HomeActivity) {
                ((HomeActivity) requireActivity()).refreshProfileUi();
            }

            Toast.makeText(requireContext(), "Profilo aggiornato", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    // Shows options to select profile image
    private void showImageChoiceMenu() {
        String[] options = {"Scegli tra immagini predefinite", "Carica dal dispositivo"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Immagine profilo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openDefaultImagePicker();
                    } else {
                        openImageFromDevice();
                    }
                })
                .show();
    }

    // Chose profile image from default images
    private void openDefaultImagePicker() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DefaultImagesFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", Context.MODE_PRIVATE);

        selectedImageType = prefs.getString("profileImageType", "");
        selectedImageValue = prefs.getString("profileImageValue", "");

        if (imgProfilePreview != null) {
            loadProfileImagePreview(prefs);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {

                        requireContext().getContentResolver()
                                .takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );

                        selectedImageType = "custom";
                        selectedImageValue = uri.toString();

                        SharedPreferences prefs = requireActivity()
                                .getSharedPreferences("app", Context.MODE_PRIVATE);

                        prefs.edit()
                                .putString("profileImageType", selectedImageType)
                                .putString("profileImageValue", selectedImageValue)
                                .apply();

                        imgProfilePreview.setImageURI(uri);

                        if (requireActivity() instanceof HomeActivity) {
                            ((HomeActivity) requireActivity()).refreshProfileUi();
                        }
                    }
                }
        );
    }

    private void loadProfileImagePreview(SharedPreferences prefs) {
        String gender = prefs.getString("gender", "guest");

        if ("custom".equals(selectedImageType) && !selectedImageValue.isEmpty()) {
            imgProfilePreview.setImageURI(Uri.parse(selectedImageValue));
            return;
        }

        if ("default".equals(selectedImageType) && !selectedImageValue.isEmpty()) {
            int resId = getResources().getIdentifier(
                    selectedImageValue,
                    "drawable",
                    requireContext().getPackageName()
            );

            if (resId != 0) {
                imgProfilePreview.setImageResource(resId);
                return;
            }
        }

        if ("male".equalsIgnoreCase(gender)) {
            imgProfilePreview.setImageResource(R.drawable.default_male);
        } else if ("female".equalsIgnoreCase(gender)) {
            imgProfilePreview.setImageResource(R.drawable.default_female);
        } else {
            imgProfilePreview.setImageResource(R.drawable.default_guest);
        }
    }

    // Load profile image from device
    private void openImageFromDevice() {
        imagePickerLauncher.launch(new String[]{"image/*"});
    }
}
