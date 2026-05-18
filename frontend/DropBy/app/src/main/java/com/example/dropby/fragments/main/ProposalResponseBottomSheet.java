package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dropby.R;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.network.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BottomSheet che permette all'Utente 2 di rispondere a una proposta evento
 * con una delle tre emoji: 👍  👎  🤔
 *
 * Utilizzo (dal NotificationsFragment o da HomeActivity):
 *
 *   ProposalResponseBottomSheet.newInstance(eventId, fromUsername, eventTitle)
 *       .show(getChildFragmentManager(), "proposal_response");
 *
 * Il campo related_id della notifica di tipo "event_proposal" contiene l'event_id.
 */
public class ProposalResponseBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_EVENT_ID      = "event_id";
    private static final String ARG_FROM_USERNAME = "from_username";
    private static final String ARG_EVENT_TITLE   = "event_title";

    public static ProposalResponseBottomSheet newInstance(
            int eventId, String fromUsername, String eventTitle) {

        ProposalResponseBottomSheet sheet = new ProposalResponseBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        args.putString(ARG_FROM_USERNAME, fromUsername != null ? fromUsername : "Un amico");
        args.putString(ARG_EVENT_TITLE,   eventTitle   != null ? eventTitle   : "");
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_proposal_response, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) { dismiss(); return; }

        int    eventId      = args.getInt(ARG_EVENT_ID);
        String fromUsername = args.getString(ARG_FROM_USERNAME);
        String eventTitle   = args.getString(ARG_EVENT_TITLE);

        TextView tvMessage = view.findViewById(R.id.tvProposalMessage);
        Button   btnThumbUp   = view.findViewById(R.id.btnThumbUp);
        Button   btnThumbDown = view.findViewById(R.id.btnThumbDown);
        Button   btnMaybe     = view.findViewById(R.id.btnMaybe);

        tvMessage.setText(fromUsername + " ti ha proposto:\n\"" + eventTitle + "\"");

        btnThumbUp.setOnClickListener(v   -> sendResponse(eventId, "👍", btnThumbUp, btnThumbDown, btnMaybe));
        btnThumbDown.setOnClickListener(v -> sendResponse(eventId, "👎", btnThumbUp, btnThumbDown, btnMaybe));
        btnMaybe.setOnClickListener(v     -> sendResponse(eventId, "🤔", btnThumbUp, btnThumbDown, btnMaybe));
    }

    private void sendResponse(int eventId, String emoji,
                              Button... buttons) {
        for (Button b : buttons) b.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("event_id",       eventId);
        body.put("response_emoji", emoji);

        ApiClient.getApiService().respondToProposal(body)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call,
                                           @NonNull Response<GenericResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Risposta inviata " + emoji, Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            for (Button b : buttons) b.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Errore nell'invio della risposta", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        for (Button b : buttons) b.setEnabled(true);
                        Toast.makeText(requireContext(),
                                "Rete assente", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
