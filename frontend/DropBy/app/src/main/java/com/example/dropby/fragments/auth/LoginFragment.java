package com.example.dropby.fragments.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.AuthActivity;
import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.models.LoginRequest;
import com.example.dropby.models.LoginResponse;
import com.example.dropby.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    public LoginFragment() { super(R.layout.fragment_login); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText    etEmail        = view.findViewById(R.id.etEmail);
        EditText    etPassword     = view.findViewById(R.id.etPassword);
        Button      btnLogin       = view.findViewById(R.id.btnLogin);
        Button      btnGoRegister  = view.findViewById(R.id.btnGoRegister);
        Button      btnBackToStart = view.findViewById(R.id.btnBackToStart);
        ProgressBar progressBar    = view.findViewById(R.id.progressBar);

        btnBackToStart.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity)
                ((AuthActivity) getActivity()).openRegister();
        });

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email non valida"); return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etPassword.setError("Minimo 6 caratteri"); return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            ApiClient.getApiService()
                    .login(new LoginRequest(email, password))
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginResponse> call,
                                               @NonNull Response<LoginResponse> response) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {
                                LoginResponse body = response.body();
                                launchHome(body.userId, body.username,
                                        body.gender != null ? body.gender : "guest",
                                        body.email != null ? body.email : "",
                                        body.token != null ? body.token : "");

                            } else if (response.code() == 403) {
                                // Email non verificata — porta al fragment OTP
                                Toast.makeText(getContext(),
                                        "Email non verificata. Controlla la tua casella.",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getContext(),
                                        "Credenziali non valide", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            Toast.makeText(getContext(),
                                    "Rete assente o server spento", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void launchHome(int userId, String username, String gender, String email, String token) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        prefs.edit()
                .putString("userType", "LOGGED")
                .putString("username", username != null ? username : "Spettatore")
                .putString("gender",   gender)
                .putString("email",    email)
                .putString("token",    token)
                .putInt("userId",      userId)
                .apply();



        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
