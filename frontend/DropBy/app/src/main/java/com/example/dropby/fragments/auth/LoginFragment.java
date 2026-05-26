package com.example.dropby.fragments.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        if (requireActivity() instanceof HomeActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_height);

                v.setPadding(
                        v.getPaddingLeft(),
                        toolbarHeight + statusBarHeight,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );

                return insets;
            });
        }

        EditText    etEmail        = view.findViewById(R.id.etEmail);
        EditText    etPassword     = view.findViewById(R.id.etPassword);
        Button      btnLogin       = view.findViewById(R.id.btnLogin);
        Button      btnGoRegister  = view.findViewById(R.id.btnGoRegister);
        ImageButton btnBackToStart = view.findViewById(R.id.btnBackToStart);
        ProgressBar progressBar    = view.findViewById(R.id.progressBar);

        btnBackToStart.setOnClickListener(v -> {
            if (requireActivity() instanceof HomeActivity) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else if (requireActivity() instanceof AuthActivity) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new StartFragment())
                        .commit();
            }
        });

        btnGoRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity)
                ((AuthActivity) getActivity()).openRegister();
        });

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.invalid_email)); return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etPassword.setError(getString(R.string.min_6_char)); return;
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
<<<<<<< HEAD
                                        body.token != null ? body.token : "",
                                        body.profileImage,
                                        body.privateFavorites, body.privateReviews);
=======
                                        body.token != null ? body.token : "");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)

                            } else if (response.code() == 403) {
                                // Email non verificata — porta al fragment OTP
                                Toast.makeText(getContext(),
                                        R.string.email_not_verified,
                                        Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getContext(),
                                        R.string.invalid_credentials,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            Toast.makeText(getContext(),
                                    R.string.no_connection_server, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

<<<<<<< HEAD
    private void launchHome(int userId, String username, String gender, String email, String token,
                            String profileImage,
                            boolean privateFavorites, boolean privateReviews) {
        String imgType  = "";
        String imgValue = "";
        if (profileImage != null && !profileImage.isEmpty()) {
            String lp = profileImage.toLowerCase(java.util.Locale.US);
            imgType  = (lp.endsWith(".jpg") || lp.endsWith(".jpeg") || lp.endsWith(".png")
                     || lp.endsWith(".webp") || lp.endsWith(".gif")) ? "server" : "default";
            imgValue = profileImage;
        }

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app", requireContext().MODE_PRIVATE);
        prefs.edit()
                .putString("userType",           "LOGGED")
                .putString("username",            username != null ? username : "Spettatore")
                .putString("gender",              gender)
                .putString("email",               email)
                .putString("token",               token)
                .putInt("userId",                 userId)
                .putString("profileImageType",    imgType)
                .putString("profileImageValue",   imgValue)
                .putBoolean("privateFavorites",   privateFavorites)
                .putBoolean("privateReviews",     privateReviews)
=======
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
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                .apply();



        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
