package com.example.dropby.fragments.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.models.OtpRequest;
import com.example.dropby.models.OtpResponse;
import com.example.dropby.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_EMAIL   = "email";

    private int    userId;
    private String email;

    public VerificationFragment() { super(R.layout.fragment_verification); }

    public static VerificationFragment newInstance(int userId, String email) {
        VerificationFragment f = new VerificationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID);
            email  = getArguments().getString(ARG_EMAIL);
        }

        TextView    tvSubtitle  = view.findViewById(R.id.tvVerificationSubtitle);
        EditText    etOtp       = view.findViewById(R.id.etOtpCode);
        Button      btnVerify   = view.findViewById(R.id.btnVerifyOtp);
        Button      btnResend   = view.findViewById(R.id.btnResendOtp);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        tvSubtitle.setText(getString(R.string.otp_sent_to, email));

        // ── Verifica OTP ─────────────────────────────────────────────────────
        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code) || code.length() != 6) {
                etOtp.setError(getString(R.string.insert_otp_code));
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);

            ApiClient.getApiService()
                    .verifyOtp(new OtpRequest(userId, code))
                    .enqueue(new Callback<OtpResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<OtpResponse> call,
                                               @NonNull Response<OtpResponse> response) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnVerify.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {
                                OtpResponse body = response.body();
                                Toast.makeText(getContext(),
                                        getString(R.string.account_verified), Toast.LENGTH_SHORT).show();
                                launchHome(body.userId, body.username,
                                        body.email   != null ? body.email   : "",
                                        body.gender  != null ? body.gender  : "guest",
                                        body.token   != null ? body.token   : "",
                                        body.profileImage,
                                        body.privateFavorites, body.privateReviews);
                            } else {
                                String err = response.code() == 400
                                        ? getString(R.string.invalid_or_expired_code)
                                        : getString(R.string.verification_error_code, response.code());
                                etOtp.setError(err);
                                Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<OtpResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnVerify.setEnabled(true);
                            Toast.makeText(getContext(), getString(R.string.network_absent), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // ── Reinvia OTP ──────────────────────────────────────────────────────
        btnResend.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            btnResend.setEnabled(false);

            ApiClient.getApiService()
                    .resendOtp(new OtpRequest(userId, ""))
                    .enqueue(new Callback<OtpResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<OtpResponse> call,
                                               @NonNull Response<OtpResponse> response) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnResend.setEnabled(true);
                            Toast.makeText(getContext(),
                                    response.isSuccessful()
                                            ? getString(R.string.new_code_sent_to, email)
                                            : getString(R.string.sending_error),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<OtpResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            btnResend.setEnabled(true);
                            Toast.makeText(getContext(), getString(R.string.network_absent), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void launchHome(int userId, String username, String email, String gender, String token,
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
                .apply();



        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
