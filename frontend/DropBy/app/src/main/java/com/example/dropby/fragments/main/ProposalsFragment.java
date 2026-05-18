package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.ProposalAdapter;
import com.example.dropby.models.Proposal;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProposalsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView statusText;
    private ProposalAdapter adapter;

    public ProposalsFragment() { super(R.layout.fragment_proposals); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.proposalsRecyclerView);
        statusText   = view.findViewById(R.id.proposalsStatusText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProposalAdapter(new ArrayList<>(), this::onCardClick);
        recyclerView.setAdapter(adapter);

        loadProposals();
    }

    private void loadProposals() {
        setStatus("Caricamento proposte…");
        ApiClient.getApiService().getReceivedProposals()
                .enqueue(new Callback<ApiService.ProposalsWrapper>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiService.ProposalsWrapper> call,
                                           @NonNull Response<ApiService.ProposalsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().proposals != null) {
                            List<Proposal> list = response.body().proposals;
                            if (list.isEmpty()) {
                                setStatus(getString(R.string.no_proposals));
                            } else {
                                statusText.setVisibility(View.GONE);
                                adapter.update(list);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            setStatus("Errore caricamento proposte");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiService.ProposalsWrapper> call,
                                          @NonNull Throwable t) {
                        if (isAdded()) setStatus("Errore di rete");
                    }
                });
    }

    private void onCardClick(Proposal proposal) {
        ProposalResponseBottomSheet
                .newInstance(
                        proposal.getEventId(),
                        proposal.getFromUsername(),
                        proposal.getEventTitle())
                .show(getChildFragmentManager(), "proposal_response");
    }

    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
