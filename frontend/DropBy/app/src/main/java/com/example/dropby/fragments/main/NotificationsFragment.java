package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        adapter = new NotificationAdapter(new ArrayList<>(), this::markRead, this::confirmDelete);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.markAllReadButton).setOnClickListener(v -> markAllRead());

        loadNotifications();
    }

    private void loadNotifications() {
        setStatus(getString(R.string.loading_notifications));
        ApiClient.getApiService().getNotifications(false)
                .enqueue(new Callback<ApiService.NotificationsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.NotificationsWrapper> call,
                                           @NonNull Response<ApiService.NotificationsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().notifications != null) {
                            List<Notification> list = response.body().notifications;
                            updateBadge(list);
                            if (list.isEmpty()) {
                                setStatus(getString(R.string.no_notifications));
                            } else {
                                statusText.setVisibility(View.GONE);
                                adapter.update(list);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            setStatus(getString(R.string.error_loading_notifications));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiService.NotificationsWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) setStatus(getString(R.string.network_error));
                    }
                });
    }

    private void markRead(Notification notif) {
        ApiClient.getApiService().markNotificationRead(notif.getId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isAdded()) loadNotifications();
                    }
                    @Override public void onFailure(@NonNull Call<GenericResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void markAllRead() {
        List<Notification> unread = adapter.getItems().stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());

        if (unread.isEmpty()) return;

        AtomicInteger remaining = new AtomicInteger(unread.size());
        for (Notification n : unread) {
            ApiClient.getApiService().markNotificationRead(n.getId())
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GenericResponse> call,
                                               @NonNull Response<GenericResponse> response) {
                            if (isAdded() && remaining.decrementAndGet() == 0) {
                                // Un solo reload dopo che tutte le chiamate sono completate
                                loadNotifications();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<GenericResponse> call,
                                              @NonNull Throwable t) {
                            remaining.decrementAndGet();
                        }
                    });
        }
    }

    private void confirmDelete(Notification notif) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.delete_notification_confirm)
                .setPositiveButton(R.string.delete, (d, w) -> deleteNotification(notif))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteNotification(Notification notif) {
        ApiClient.getApiService().deleteNotification(notif.getId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isAdded() && response.isSuccessful()) loadNotifications();
                    }
                    @Override public void onFailure(@NonNull Call<GenericResponse> call,
                                                    @NonNull Throwable t) {}
                });
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
