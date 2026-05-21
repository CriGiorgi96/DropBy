package com.example.dropby;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dropby.models.Event;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Centralizza le 3 azioni rapide su un evento (preferiti, promemoria, proponi).
 * Usato da EventGroupAdapter, TheaterReplicaAdapter e — opzionalmente — da
 * EventDetailFragment per non duplicare codice.
 */
public class EventActionsHelper {

    private final Activity activity;
    private final Event    event;

    public EventActionsHelper(Activity activity, Event event) {
        this.activity = activity;
        this.event    = event;
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    public void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenu().add(0, 1, 0, "Aggiungi ai preferiti");
        popup.getMenu().add(0, 2, 1, "Imposta promemoria");
        popup.getMenu().add(0, 3, 2, "Proponi ad un amico");
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if      (id == 1) addToFavorites();
            else if (id == 2) showReminderPicker();
            else if (id == 3) showProposeFriendDialog();
            return true;
        });
        popup.show();
    }

    // ── 1. Preferiti ─────────────────────────────────────────────────────────

    private void addToFavorites() {
        Map<String, Integer> body = new HashMap<>();
        body.put("event_id", event.getId());
        ApiClient.getApiService().addFavorite(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isDead()) return;
                        if (response.isSuccessful()) {
                            toast("Aggiunto ai preferiti!");
                        } else if (response.code() == 400) {
                            toast("Già nei tuoi preferiti");
                        } else {
                            toast("Impossibile aggiungere ai preferiti");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (!isDead()) toast("Errore di rete");
                    }
                });
    }

    // ── 2. Promemoria ─────────────────────────────────────────────────────────

    private void showReminderPicker() {
        if (Build.VERSION.SDK_INT >= 33 &&
                activity.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != PackageManager.PERMISSION_GRANTED) {
            toast("Concedi il permesso per le notifiche nelle impostazioni");
            return;
        }
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(activity, (dp, year, month, day) ->
            new TimePickerDialog(activity, (tp, hour, minute) -> {
                cal.set(year, month, day, hour, minute, 0);
                cal.set(Calendar.MILLISECOND, 0);
                if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                    toast("Scegli una data futura");
                    return;
                }
                sendReminder(cal);
            }, cal.get(Calendar.HOUR_OF_DAY),
               cal.get(Calendar.MINUTE), true).show(),
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void sendReminder(Calendar cal) {
        // Converti in UTC prima di inviare al server
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCal.setTimeInMillis(cal.getTimeInMillis());
        String remindAt = String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:00Z",
                utcCal.get(Calendar.YEAR),
                utcCal.get(Calendar.MONTH) + 1,
                utcCal.get(Calendar.DAY_OF_MONTH),
                utcCal.get(Calendar.HOUR_OF_DAY),
                utcCal.get(Calendar.MINUTE));

        Map<String, Object> body = new HashMap<>();
        body.put("event_id", event.getId());
        body.put("remind_at", remindAt);

        ApiClient.getApiService().setReminder(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isDead()) return;
                        if (response.isSuccessful()) {
                            ReminderScheduler.schedule(activity, event.getId(),
                                    event.getTitle(), cal.getTimeInMillis());
                            toast("Promemoria impostato!");
                        } else {
                            toast("Errore impostazione promemoria");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (!isDead()) toast("Errore di rete");
                    }
                });
    }

    // ── 3. Proponi ad un amico ────────────────────────────────────────────────

    private void showProposeFriendDialog() {
        android.app.AlertDialog loading = new android.app.AlertDialog.Builder(activity)
                .setMessage("Caricamento amici…")
                .setCancelable(false)
                .show();

        ApiClient.getApiService().getFriends()
                .enqueue(new Callback<ApiService.FriendsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.FriendsWrapper> call,
                                           @NonNull Response<ApiService.FriendsWrapper> response) {
                        if (isDead()) { loading.dismiss(); return; }
                        loading.dismiss();
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().friends == null
                                || response.body().friends.isEmpty()) {
                            toast("Nessun amico trovato");
                            return;
                        }
                        List<Friend> friends = response.body().friends;
                        String[] names = new String[friends.size()];
                        for (int i = 0; i < friends.size(); i++)
                            names[i] = friends.get(i).getUsername();

                        new android.app.AlertDialog.Builder(activity)
                                .setTitle("Proponi \"" + event.getTitle() + "\" a:")
                                .setItems(names, (d, which) -> submitProposal(friends.get(which)))
                                .setNegativeButton("Annulla", null)
                                .show();
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiService.FriendsWrapper> call,
                                          @NonNull Throwable t) {
                        if (!isDead()) { loading.dismiss(); toast("Errore di rete"); }
                    }
                });
    }

    private void submitProposal(Friend friend) {
        Map<String, Integer> body = new HashMap<>();
        body.put("to_user_id", friend.getUserId());
        body.put("event_id",   event.getId());
        ApiClient.getApiService().proposeEvent(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (isDead()) return;
                        if (response.isSuccessful()) {
                            toast("Proposta inviata a " + friend.getUsername() + "!");
                        } else if (response.code() == 400) {
                            toast("Proposta già inviata o utente non amico");
                        } else {
                            toast("Errore nell'invio della proposta");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (!isDead()) toast("Errore di rete");
                    }
                });
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private boolean isDead() {
        return activity.isFinishing() || activity.isDestroyed();
    }

    private void toast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
