package com.example.dropby.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;

public class StartFragment extends Fragment {

    public StartFragment() {
        super(R.layout.fragment_start);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnGuest = view.findViewById(R.id.btnGuest);

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

    void launchHome(String userType) {
        android.content.SharedPreferences prefs =
                requireActivity().getSharedPreferences("app", requireContext().MODE_PRIVATE);
        // Reset completo + imposta solo i valori guest
        prefs.edit()
                .clear()
                .putString("userType", userType)
                .putString("username", "Spettatore")
                .putString("gender",   "guest")
                .apply();



        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
