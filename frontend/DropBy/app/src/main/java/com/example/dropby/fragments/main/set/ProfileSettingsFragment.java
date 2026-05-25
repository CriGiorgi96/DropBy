package com.example.dropby.fragments.main.set;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.network.ApiClient;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSettingsFragment extends Fragment {

    private ImageView imgProfilePreview;
    private EditText  editUsername;
    private EditText  editCurrentPassword;
    private EditText  editNewPassword;
    private EditText  editConfirmPassword;
    private CheckBox  checkPrivateFavorites;
    private CheckBox  checkPrivateReviews;

    private String selectedImageType = "";
    private String selectedImageValue = "";
    private Uri pendingUploadUri = null;

    private ActivityResultLauncher<String[]> imagePickerLauncher;

    public ProfileSettingsFragment() {
        super(R.layout.fragment_profile_settings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    requireContext().getContentResolver()
                            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    pendingUploadUri = uri;
                    selectedImageType = "custom";
                    selectedImageValue = uri.toString();
                    imgProfilePreview.setImageURI(uri);
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);

        imgProfilePreview      = view.findViewById(R.id.imgProfilePreview);
        editUsername           = view.findViewById(R.id.editUsername);
        editCurrentPassword    = view.findViewById(R.id.editCurrentPassword);
        editNewPassword        = view.findViewById(R.id.editNewPassword);
        editConfirmPassword    = view.findViewById(R.id.editConfirmPassword);
        Button btnChangeImg       = view.findViewById(R.id.btnChangeProfileImg);
        Button btnSave            = view.findViewById(R.id.btnSaveProfile);
        ImageButton btnBack            = view.findViewById(R.id.btnBackToSettings);
        checkPrivateFavorites     = view.findViewById(R.id.checkPrivateFavorites);
        checkPrivateReviews       = view.findViewById(R.id.checkPrivateReviews);

        editUsername.setText(prefs.getString("username", ""));
        checkPrivateFavorites.setChecked(prefs.getBoolean("privateFavorites", false));
        checkPrivateReviews.setChecked(prefs.getBoolean("privateReviews", false));

        if (selectedImageType.isEmpty()) {
            selectedImageType  = prefs.getString("profileImageType", "");
            selectedImageValue = prefs.getString("profileImageValue", "");
        }

        // ── DEBUG LOG: avatar letto all'apertura del fragment ─────────────────
        Log.d("DEBUG_PREFS", "[PROFILE_SETTINGS onViewCreated]"
                + " userId="     + prefs.getInt("userId", -1)
                + " | username=" + prefs.getString("username", "")
                + " | imageType=" + selectedImageType
                + " | imageValue=" + selectedImageValue);

        loadProfileImagePreview(prefs);

        Button btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        btnChangeImg.setOnClickListener(v -> showImageChoiceMenu());
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> onSaveClicked(prefs));
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog(prefs));
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);
        // Sync prefs solo se non c'è un upload dal dispositivo in attesa.
        // Questo gestisce il ritorno da DefaultImagesFragment senza sovrascrivere
        // una foto dal dispositivo già selezionata (il cui callback arriva dopo onResume).
        if (pendingUploadUri == null) {
            selectedImageType  = prefs.getString("profileImageType", "");
            selectedImageValue = prefs.getString("profileImageValue", "");
        }
        if (imgProfilePreview != null) loadProfileImagePreview(prefs);
    }

    // ── SALVATAGGIO ───────────────────────────────────────────────────────────

    private void onSaveClicked(SharedPreferences prefs) {
        String username     = editUsername.getText().toString().trim();
        String currentPwd   = editCurrentPassword.getText().toString();
        String newPwd       = editNewPassword.getText().toString();
        String confirmPwd   = editConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            editUsername.setError(getString(R.string.insert_username));
            return;
        }

        boolean changingPassword = !currentPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty();
        if (changingPassword) {
            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(requireContext(), R.string.fill_password_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                editConfirmPassword.setError(getString(R.string.passwords_not_matching));
                return;
            }
            if (newPwd.length() < 6) {
                editNewPassword.setError(getString(R.string.min_char_password));
                return;
            }
        }

        String currentUsername = prefs.getString("username", "");
        boolean usernameChanged = !username.equals(currentUsername);

        if (usernameChanged) {
            checkUsernameAvailability(username, currentPwd, newPwd, changingPassword);
        } else {
            proceedWithSave(username, currentPwd, newPwd, changingPassword);
        }
    }

    private void checkUsernameAvailability(String username,
                                           String currentPwd,
                                           String newPwd,
                                           boolean changingPassword) {

        ApiClient.getApiService().checkUsername(username)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Object avail = response.body().get("available");
                            if (Boolean.TRUE.equals(avail)) {
                                proceedWithSave(username, currentPwd, newPwd, changingPassword);
                            } else {
                                editUsername.setError(getString(R.string.username_already_exists));
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.error_username_verification, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithSave(String username,
                                 String currentPwd,
                                 String newPwd,
                                 boolean changingPassword) {

        if (pendingUploadUri != null) {
            uploadAvatarThenSave(username, currentPwd, newPwd, changingPassword);
        } else {
            callUpdateProfile(username, currentPwd, newPwd, changingPassword,
                    buildProfileImageName());
        }
    }

    private void uploadAvatarThenSave(String username,
                                      String currentPwd,
                                      String newPwd,
                                      boolean changingPassword) {
        byte[] bytes;
        try {
            bytes = readBytes(pendingUploadUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.impossible_to_read_image, Toast.LENGTH_SHORT).show();
            return;
        }

        String mimeType = requireContext().getContentResolver().getType(pendingUploadUri);
        if (mimeType == null) mimeType = "image/jpeg";

        RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
        String ext = mimeType.contains("png") ? "avatar.png"
                   : mimeType.contains("webp") ? "avatar.webp"
                   : "avatar.jpg";
        MultipartBody.Part part = MultipartBody.Part.createFormData("avatar", ext, requestBody);

        ApiClient.getApiService().uploadAvatar(part)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            String filename = (String) response.body().get("filename");
                            pendingUploadUri = null;
                            selectedImageType  = "server";
                            selectedImageValue = filename != null ? filename : "";
                            callUpdateProfile(username, currentPwd, newPwd, changingPassword, filename);
                        } else {
                            Toast.makeText(requireContext(), R.string.failed_avatar_upload, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), R.string.upload_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void callUpdateProfile(String username,
                                   String currentPwd,
                                   String newPwd,
                                   boolean changingPassword,
                                   String profileImage) {

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        if (changingPassword) {
            body.put("current_password", currentPwd);
            body.put("new_password", newPwd);
        }
        if (profileImage != null) {
            body.put("profile_image", profileImage);
        }
        body.put("private_favorites", String.valueOf(checkPrivateFavorites.isChecked()));
        body.put("private_reviews",   String.valueOf(checkPrivateReviews.isChecked()));

        ApiClient.getApiService().updateProfile(body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                                           @NonNull Response<Map<String, Object>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            onProfileSavedSuccessfully(username, response.body());
                        } else {
                            String msg = getString(R.string.save_profile_upload_error);
                            try {
                                if (response.errorBody() != null) {
                                    String err = response.errorBody().string();
                                    if (err.contains(getString(R.string.current_password_wrong))) msg = getString(R.string.current_password_wrong);
                                    else if (err.contains(getString(R.string.username_already_exists)))  msg = "Username già in uso";
                                }
                            } catch (IOException ignored) { }
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onProfileSavedSuccessfully(String username, Map<String, Object> responseBody) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit()
                .putString("username", username)
                .putString("profileImageType", selectedImageType)
                .putString("profileImageValue", selectedImageValue)
                .putBoolean("privateFavorites", checkPrivateFavorites.isChecked())
                .putBoolean("privateReviews",   checkPrivateReviews.isChecked());

        // Sovrascrivi con "server" SOLO se il backend ha risposto con un filename reale (con estensione).
        // Se profile_image è un drawable predefinito (es. "male_01"), i valori in memoria
        // (selectedImageType="default", selectedImageValue="male_01") sono già corretti.
        String serverFilename = (String) responseBody.get("profile_image");
        if (isServerFilename(serverFilename)) {
            editor.putString("profileImageType", "server")
                  .putString("profileImageValue", serverFilename);
        }

        editor.apply();

        // ── DEBUG LOG: avatar scritto dopo il salvataggio ─────────────────────
        SharedPreferences fresh = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE);
        Log.d("DEBUG_PREFS", "[PROFILE_SETTINGS SAVED]"
                + " userId="      + fresh.getInt("userId", -1)
                + " | username="  + fresh.getString("username", "")
                + " | imageType=" + fresh.getString("profileImageType", "")
                + " | imageValue=" + fresh.getString("profileImageValue", "")
                + " | serverFilename=" + serverFilename);

        if (requireActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).refreshProfileUi();
        }

        Toast.makeText(requireContext(), R.string.updated_profile, Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    // ── ELIMINAZIONE ACCOUNT ─────────────────────────────────────────────────

    private void showDeleteAccountDialog(SharedPreferences prefs) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(requireContext());
        android.view.View dialogView = inflater.inflate(R.layout.dialog_delete_account, null);

        EditText etConfirm = dialogView.findViewById(R.id.etDeleteConfirm);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnDeleteCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnDeleteConfirm).setOnClickListener(v -> {
            String input = etConfirm.getText().toString().trim();
            if (!"CONFERMA".equals(input)) {
                Toast.makeText(requireContext(), R.string.wrong_confirm_input, Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = prefs.getInt("userId", -1);
            if (userId == -1) {
                Toast.makeText(requireContext(), R.string.invalid_session, Toast.LENGTH_SHORT).show();
                return;
            }
            performDeleteAccount(userId, prefs, dialog);
        });

        dialog.show();
    }

    private void performDeleteAccount(int userId, SharedPreferences prefs, AlertDialog dialog) {
        ApiClient.getApiService().deleteAccount(userId)
                .enqueue(new Callback<com.example.dropby.models.GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.dropby.models.GenericResponse> call,
                                           @NonNull Response<com.example.dropby.models.GenericResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            prefs.edit().clear().apply();
                            requireActivity()
                                    .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                    .edit().clear().apply();
                            Intent intent = new Intent(requireContext(),
                                    com.example.dropby.AuthActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(),
                                    R.string.error_while_deleting_account,
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.dropby.models.GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // ── IMMAGINE PROFILO ─────────────────────────────────────────────────────

    private void showImageChoiceMenu() {
        String[] options = {"Scegli tra immagini predefinite", "Carica dal dispositivo"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Immagine profilo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openDefaultImagePicker();
                    else            openImageFromDevice();
                })
                .show();
    }

    private void openDefaultImagePicker() {
        pendingUploadUri = null;   // la scelta predefinita annulla qualsiasi upload pendente
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DefaultImagesFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openImageFromDevice() {
        imagePickerLauncher.launch(new String[]{"image/*"});
    }

    private void loadProfileImagePreview(SharedPreferences prefs) {
        // Heal corrupted prefs written by old code: type="server" was incorrectly set
        // for drawable names (no image extension). Reclassify as "default" and persist.
        if ("server".equals(selectedImageType)
                && !selectedImageValue.isEmpty()
                && !isServerFilename(selectedImageValue)) {
            selectedImageType = "default";
            prefs.edit().putString("profileImageType", "default").apply();
        }

        if ("server".equals(selectedImageType) && isServerFilename(selectedImageValue)) {
            String url = ApiClient.getBaseUrl() + "assets/avatars/" + selectedImageValue;
            loadImageFromUrl(url);
            return;
        }

        if ("custom".equals(selectedImageType) && !selectedImageValue.isEmpty()) {
            imgProfilePreview.setImageURI(Uri.parse(selectedImageValue));
            return;
        }

        if ("default".equals(selectedImageType) && !selectedImageValue.isEmpty()) {
            int resId = getResources().getIdentifier(
                    selectedImageValue, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                imgProfilePreview.setImageResource(resId);
                return;
            }
        }

        String gender = prefs.getString("gender", "guest");
        if ("male".equalsIgnoreCase(gender))        imgProfilePreview.setImageResource(R.drawable.default_male);
        else if ("female".equalsIgnoreCase(gender)) imgProfilePreview.setImageResource(R.drawable.default_female);
        else                                        imgProfilePreview.setImageResource(R.drawable.default_guest);
    }

    private void loadImageFromUrl(String url) {
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) imageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream input = conn.getInputStream();
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                if (bitmap != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> imgProfilePreview.setImageBitmap(bitmap));
                }
            } catch (IOException ignored) { }
        }).start();
    }

    private String buildProfileImageName() {
        if ("default".equals(selectedImageType)) return selectedImageValue;
        if ("server".equals(selectedImageType))  return selectedImageValue;
        return null;
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    private static boolean isServerFilename(String s) {
        if (s == null || s.isEmpty()) return false;
        String lower = s.toLowerCase(java.util.Locale.US);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".webp")
            || lower.endsWith(".gif");
    }

    private byte[] readBytes(Uri uri) throws IOException {
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            if (is == null) throw new IOException("InputStream nullo");
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }
}
