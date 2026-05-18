package com.example.dropby.fragments.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dropby.R;
import com.example.dropby.network.ApiClient;

public class UrlConfigFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_url_config, container, false);

        EditText urlEdit = view.findViewById(R.id.urlEditText);
        Button saveBtn = view.findViewById(R.id.saveUrlButton);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_settings", requireActivity().MODE_PRIVATE);
        String currentUrl = prefs.getString("base_url", "http://10.0.2.2:5000/");
        urlEdit.setText(currentUrl);

        saveBtn.setOnClickListener(v -> {
            String newUrl = urlEdit.getText().toString().trim();
            if (!newUrl.endsWith("/")) newUrl += "/";
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "http://" + newUrl;
            }
            ApiClient.setCustomBaseUrl(newUrl);
            Toast.makeText(getContext(), "URL salvato", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        return view;
    }
}