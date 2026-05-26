package com.example.dropby.fragments.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.dropby.AuthActivity;
import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.models.RegisterRequest;
import com.example.dropby.models.RegisterResponse;
import com.example.dropby.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RegisterFragment extends Fragment {

    private String birthdayIso = ""; // YYYY-MM-DD — set by DatePickerDialog

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

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

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName  = view.findViewById(R.id.etLastName);
        EditText etUsername  = view.findViewById(R.id.etUsername);
        EditText etEmail     = view.findViewById(R.id.etEmail);
        EditText etBirthday  = view.findViewById(R.id.etBirthday);
        EditText etPassword  = view.findViewById(R.id.etPassword);

<<<<<<< HEAD
        // DatePickerDialog al tap sul campo data — età minima 13 anni
        etBirthday.setFocusable(false);
        etBirthday.setClickable(true);
        etBirthday.setOnClickListener(v -> {
            // Data di default: 20 anni fa
            Calendar defaultCal = Calendar.getInstance();
            defaultCal.add(Calendar.YEAR, -20);

            // Data massima selezionabile: 13 anni fa (vincolo età minima)
            Calendar maxCal = Calendar.getInstance();
            maxCal.add(Calendar.YEAR, -13);

            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (picker, year, month, day) -> {
                        birthdayIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                        etBirthday.setText(String.format(Locale.ITALY, "%02d/%02d/%04d", day, month + 1, year));
                        etBirthday.setError(null);
                    },
                    defaultCal.get(Calendar.YEAR),
                    defaultCal.get(Calendar.MONTH),
                    defaultCal.get(Calendar.DAY_OF_MONTH));

            // Il picker impedisce fisicamente di selezionare date future o sotto i 13 anni
            dialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());
            dialog.show();
=======
        // DatePickerDialog al tap sul campo data
        etBirthday.setFocusable(false);
        etBirthday.setClickable(true);
        etBirthday.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int y = cal.get(Calendar.YEAR) - 20;
            int m = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                birthdayIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                etBirthday.setText(String.format(Locale.ITALY, "%02d/%02d/%04d", day, month + 1, year));
                etBirthday.setError(null);
            }, y, m, d).show();
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        });

        RadioGroup rgGender  = view.findViewById(R.id.rgGender);

        CheckBox cbPrivacy   = view.findViewById(R.id.cbPrivacy);

        Button btnRegister    = view.findViewById(R.id.btnRegister);
        Button btnGoLogin     = view.findViewById(R.id.btnGoLogin);
        ImageButton btnBackToStart = view.findViewById(R.id.btnBackToStart);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        TextView tvPrivacy = view.findViewById(R.id.tvPrivacy);
        setupPrivacyLink(tvPrivacy);

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

        btnGoLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).openLogin();
            }
        });

        btnRegister.setOnClickListener(v -> {

            String firstName = etFirstName.getText().toString().trim();
            String lastName  = etLastName.getText().toString().trim();
            String username  = etUsername.getText().toString().trim();
            String email     = etEmail.getText().toString().trim();
            String password  = etPassword.getText().toString();
            int genderId     = rgGender.getCheckedRadioButtonId();

            if (TextUtils.isEmpty(firstName)) { etFirstName.setError(getString(R.string.insert_name)); return; }
            if (TextUtils.isEmpty(lastName))  { etLastName.setError(getString(R.string.insert_last_name)); return; }
            if (TextUtils.isEmpty(username))  { etUsername.setError(getString(R.string.insert_username)); return; }

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.invalid_email)); return;
            }

            if (birthdayIso.isEmpty()) {
<<<<<<< HEAD
                etBirthday.setError(getString(R.string.insert_birthday));
=======
                etBirthday.setError("Seleziona la data di nascita");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etPassword.setError(getString(R.string.min_6_char)); return;
            }

            if (genderId == -1) {
                Toast.makeText(getContext(), R.string.select_gender, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbPrivacy.isChecked()) {
                cbPrivacy.setError(getString(R.string.accept_terms));
                Toast.makeText(getContext(),
                        R.string.accept_terms,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String gender = genderId == R.id.rbMale ? "male"
                    : genderId == R.id.rbFemale ? "female"
                    : "other";

            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);

            RegisterRequest req = new RegisterRequest(
                    username, email, password,
                    firstName, lastName, birthdayIso, gender
            );

            ApiClient.getApiService().register(req)
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<RegisterResponse> call,
                                               @NonNull Response<RegisterResponse> response) {

                            if (!isAdded()) return;

                            progressBar.setVisibility(View.GONE);
                            btnRegister.setEnabled(true);

                            if (response.isSuccessful() && response.body() != null) {
                                RegisterResponse body = response.body();
                                openVerification(body.userId, body.email);
                            } else {
                                Toast.makeText(getContext(),
                                        getString(R.string.error_while_signing_up, response.code()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<RegisterResponse> call,
                                              @NonNull Throwable t) {

                            if (!isAdded()) return;

                            progressBar.setVisibility(View.GONE);
                            btnRegister.setEnabled(true);

                            Toast.makeText(getContext(),
                                    R.string.no_connection_server,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void openVerification(int userId, String email) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container,
                        VerificationFragment.newInstance(userId, email))
                .addToBackStack(null)
                .commit();
    }

    private void setupPrivacyLink(TextView textView) {
        String fullText = getString(R.string.validation);
        String linkText = getString(R.string.terms_conditions);

        int start = fullText.indexOf(linkText);
        int end = start + linkText.length();

        if (start != -1) {
            SpannableString spannableString = new SpannableString(fullText);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    showTermsPopup();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(ContextCompat.getColor(
                            requireContext(),
                            android.R.color.holo_blue_dark
                    ));
                    ds.setUnderlineText(true);
                }
            };

            spannableString.setSpan(
                    clickableSpan,
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            textView.setText(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setHighlightColor(Color.TRANSPARENT);
        }
    }

    private void showTermsPopup() {
        String termsContent = loadTermsFromRaw();

        TextView textView = new TextView(getContext());
        textView.setText(termsContent);
        textView.setPadding(40, 40, 40, 40);
        textView.setTextSize(14);

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(textView);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.terms_conditions)
                .setView(scrollView)
                .setPositiveButton(R.string.close_button, (d, w) -> d.dismiss())
                .show();
    }

    private String loadTermsFromRaw() {
        StringBuilder sb = new StringBuilder();

        try {
            InputStream is = getResources().openRawResource(R.raw.terms);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            is.close();

        } catch (Exception e) {
            return getString(R.string.error_loading_terms);
        }

        return sb.toString();
    }
}