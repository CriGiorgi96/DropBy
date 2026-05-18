package com.example.dropby.adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.App;
import com.example.dropby.R;
import com.example.dropby.models.Proposal;

import java.util.List;

public class ProposalAdapter extends RecyclerView.Adapter<ProposalAdapter.VH> {

    public interface OnCardClickListener {
        void onCardClick(Proposal proposal);
    }

    private List<Proposal> items;
    private final OnCardClickListener listener;

    public ProposalAdapter(List<Proposal> items, OnCardClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void update(List<Proposal> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proposal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Proposal p = items.get(position);

        SharedPreferences prefs = App.getContext()
                .getSharedPreferences("app", App.getContext().MODE_PRIVATE);
        int myUserId = prefs.getInt("userId", -1);

        boolean isReceived = p.getToUserId() == myUserId;
        String fromLabel = isReceived
                ? "Da: " + p.getFromUsername()
                : "A: " + p.getToUsername();

        holder.from.setText(fromLabel);
        holder.eventTitle.setText(p.getEventTitle() != null ? p.getEventTitle() : "Evento");
        holder.statusEmoji.setText(p.getStatusEmoji());

        StringBuilder meta = new StringBuilder();
        if (p.getEventDate() != null) meta.append(p.getEventDate());
        if (p.getEventCity() != null) meta.append(" · ").append(p.getEventCity());
        holder.eventMeta.setText(meta.toString());

        // I bottoni inline sono rimpiazzati dal BottomSheet: sempre nascosti
        holder.responseButtons.setVisibility(View.GONE);

        // Tap sulla card → apre il BottomSheet solo per proposte ricevute e in attesa
        boolean canRespond = isReceived && "pending".equals(p.getStatus());
        holder.itemView.setOnClickListener(canRespond ? v -> listener.onCardClick(p) : null);
        holder.itemView.setForeground(canRespond
                ? holder.itemView.getContext()
                        .obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground})
                        .getDrawable(0)
                : null);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView from, eventTitle, eventMeta, statusEmoji;
        View responseButtons;
        Button btnAccept, btnMaybe, btnDecline;

        VH(View v) {
            super(v);
            from            = v.findViewById(R.id.proposalFrom);
            eventTitle      = v.findViewById(R.id.proposalEventTitle);
            eventMeta       = v.findViewById(R.id.proposalEventMeta);
            statusEmoji     = v.findViewById(R.id.proposalStatusEmoji);
            responseButtons = v.findViewById(R.id.proposalResponseButtons);
            btnAccept       = v.findViewById(R.id.btnAccept);
            btnMaybe        = v.findViewById(R.id.btnMaybe);
            btnDecline      = v.findViewById(R.id.btnDecline);
        }
    }
}
