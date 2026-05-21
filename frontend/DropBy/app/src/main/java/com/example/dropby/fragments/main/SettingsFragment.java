package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.dropby.R;
import com.example.dropby.fragments.main.UrlConfigFragment;
import com.example.dropby.network.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
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
        passwordInput.setHint("Inserisci la password di configurazione");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Accesso configurazione")
                .setMessage("Inserisci la password per accedere alle impostazioni URL.")
                .setView(passwordInput)
                .setPositiveButton("Conferma", null)
                .setNegativeButton("Annulla", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) {
                    passwordInput.setError("Password obbligatoria");
                    return;
                }
                verifyPassword(password, dialog);
            });
        });

        dialog.show();
    }

    private void verifyPassword(String password, AlertDialog dialog) {
        Map<String, String> body = new HashMap<>();
        body.put("password", password);

        ApiClient.getApiService().verifyConfigPassword(body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Object valid = response.body().get("valid");
                            if (Boolean.TRUE.equals(valid)) {
                                dialog.dismiss();
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new UrlConfigFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                Toast.makeText(requireContext(), "Password errata", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Password errata", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Errore di rete", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
