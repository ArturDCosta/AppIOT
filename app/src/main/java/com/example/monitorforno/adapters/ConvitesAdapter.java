package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitorforno.R;
import com.example.monitorforno.models.ConviteResponseDTO;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ConvitesAdapter extends RecyclerView.Adapter<ConvitesAdapter.ConviteViewHolder> {

    private List<ConviteResponseDTO> listaConvites;
    private OnConviteAcaoListener listener;

    public interface OnConviteAcaoListener {
        void onAceitar(ConviteResponseDTO convite, int position);
        void onRecusar(ConviteResponseDTO convite, int position);
    }

    public ConvitesAdapter(List<ConviteResponseDTO> listaConvites, OnConviteAcaoListener listener) {
        this.listaConvites = listaConvites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_convite, parent, false);
        return new ConviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConviteViewHolder holder, int position) {
        ConviteResponseDTO convite = listaConvites.get(position);

        holder.txtNomeForno.setText("Forno: " + (convite.getNomeForno() != null ? convite.getNomeForno() : "Desconhecido"));
        holder.txtRemetente.setText("Enviado por: " + (convite.getEmailDono() != null ? convite.getEmailDono() : "Desconhecido"));

        holder.btnAceitar.setOnClickListener(v -> {
            if (listener != null) listener.onAceitar(convite, position);
        });

        holder.btnRecusar.setOnClickListener(v -> {
            if (listener != null) listener.onRecusar(convite, position);
        });
    }

    @Override
    public int getItemCount() {
        return listaConvites.size();
    }

    public void removerItem(int position) {
        listaConvites.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listaConvites.size());
    }

    static class ConviteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeForno, txtRemetente;
        MaterialButton btnAceitar, btnRecusar;

        public ConviteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeForno = itemView.findViewById(R.id.txtNomeFornoConvite);
            txtRemetente = itemView.findViewById(R.id.txtRemetenteConvite);
            btnAceitar = itemView.findViewById(R.id.btnAceitar);
            btnRecusar = itemView.findViewById(R.id.btnRecusar);
        }
    }
}