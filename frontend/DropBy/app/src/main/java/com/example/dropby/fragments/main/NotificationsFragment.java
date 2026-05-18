package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.HomeActivity;
import com.example.dropby.R;
import com.example.dropby.adapters.NotificationAdapter;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.Notification;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView statusText;
    private NotificationAdapter adapter;

    public NotificationsFragment() { super(R.layout.fragment_notifications); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        statusText   = view.findViewById(R.id.notificationsStatusText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>(), this::markRead);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.markAllReadButton).setOnClickListener(v -> markAllRead());

        loadNotifications();
    }

    private void loadNotifications() {
        setStatus("Caricamento notifiche…");
        ApiClient.getApiService().getNotifications(false)
                .enqueue(new Callback<ApiService.NotificationsWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.NotificationsWrapper> call,
                                           Response<ApiService.NotificationsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().notifications != null) {
                            List<Notification> list = response.body().notifications;
                            if (list.isEmpty()) {
                                setStatus(getString(R.string.no_notifications));
                            } else {
                                statusText.setVisibility(View.GONE);
                                adapter.update(list);
                                recyclerView.setVisibility(View.VISIBLE);
                                updateBadge(list);
                            }
                        } else {
                            setStatus("Errore caricamento notifiche");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.NotificationsWrapper> call, Throwable t) {
                        if (isAdded()) setStatus("Errore di rete");
                    }
                });
    }

    private void markRead(Notification notif) {
        ApiClient.getApiService().markNotificationRead(notif.getId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call,
                                           Response<GenericResponse> response) {
                        if (isAdded()) loadNotifications();
                    }
                    @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
                });
    }

    private void markAllRead() {
        // Segna tutte le notifiche non lette
        List<Notification> current = adapter.getItems();
        for (Notification n : current) {
            if (!n.isRead()) markRead(n);
        }
    }

    private void updateBadge(List<Notification> list) {
        if (getActivity() instanceof HomeActivity) {
            long unread = list.stream().filter(n -> !n.isRead()).count();
            ((HomeActivity) getActivity()).updateNotificationBadge((int) unread);
        }
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
