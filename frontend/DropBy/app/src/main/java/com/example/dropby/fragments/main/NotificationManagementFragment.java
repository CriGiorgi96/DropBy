package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.ReminderScheduler;
import com.example.dropby.adapters.ReminderAdapter;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.Reminder;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView     statusText;
    private ReminderAdapter adapter;

    public NotificationManagementFragment() { super(R.layout.fragment_reminders); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.remindersRecyclerView);
        statusText   = view.findViewById(R.id.remindersStatusText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReminderAdapter(new ArrayList<>(), this::deleteReminder);
        recyclerView.setAdapter(adapter);

        loadReminders();
    }

    private void loadReminders() {
        setStatus(getString(R.string.loading_reminders));
        ApiClient.getApiService().getReminders()
                .enqueue(new Callback<ApiService.RemindersWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.RemindersWrapper> call,
                                           @NonNull Response<ApiService.RemindersWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().reminders != null) {
                            List<Reminder> list = response.body().reminders;
                            if (list.isEmpty()) {
                                setStatus(getString(R.string.no_active_reminders));
                            } else {
                                statusText.setVisibility(View.GONE);
                                adapter.update(list);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            setStatus(getString(R.string.error_loading_reminders));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiService.RemindersWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) setStatus(getString(R.string.network_error));
                    }
                });
    }

    private void deleteReminder(Reminder reminder) {
        ApiClient.getApiService().deleteReminder(reminder.getId())
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            ReminderScheduler.cancel(requireContext(), reminder.getEventId());
                            Toast.makeText(requireContext(),
                                    R.string.reminder_removed, Toast.LENGTH_SHORT).show();
                            loadReminders();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {}
                });
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
