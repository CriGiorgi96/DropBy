package com.example.dropby.fragments.main;

import android.os.Bundle;
<<<<<<< HEAD
import android.text.InputType;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog;
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
import androidx.fragment.app.Fragment;

import com.example.dropby.R;
import com.example.dropby.fragments.main.set.ProfileSettingsFragment;

public class SettingsFragment extends Fragment {

<<<<<<< HEAD
    private static final String CONFIG_PASSWORD = "Admin1234";

    public SettingsFragment() {
        super(R.layout.fragment_settings);
=======
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
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button urlButton = view.findViewById(R.id.btnUrlSettings);
        urlButton.setOnClickListener(v -> showConfigPasswordDialog());
    }

    private void showConfigPasswordDialog() {
        EditText passwordInput = new EditText(requireContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint(getString(R.string.config_password_hint));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.config_access_title))
                .setMessage(getString(R.string.config_access_message))
                .setView(passwordInput)
                .setPositiveButton(getString(R.string.confirm_button), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) {
                    passwordInput.setError(getString(R.string.password_required));
                    return;
                }
                if (CONFIG_PASSWORD.equals(password)) {
                    dialog.dismiss();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new UrlConfigFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
