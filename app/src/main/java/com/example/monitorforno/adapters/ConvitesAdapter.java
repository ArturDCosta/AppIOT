package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitorforno.R;
// CORREÇÃO: Importar o modelo correto
import com.example.monitorforno.models.FornoCompartilhadoResponseDTO;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ConvitesAdapter extends RecyclerView.Adapter<ConvitesAdapter.ConviteViewHolder> {

    // CORREÇÃO: Utilizar a nova classe de modelo
    private List<FornoCompartilhadoResponseDTO> listaConvites;
    private OnConviteAcaoListener listener;

    public interface OnConviteAcaoListener {
        // CORREÇÃO: Alterar a interface para passar o novo modelo
        void onAceitar(FornoCompartilhadoResponseDTO convite, int position);
        void onRecusar(FornoCompartilhadoResponseDTO convite, int position);
    }

    public ConvitesAdapter(List<FornoCompartilhadoResponseDTO> listaConvites, OnConviteAcaoListener listener) {
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
        FornoCompartilhadoResponseDTO convite = listaConvites.get(position);

        // CORREÇÃO: O novo DTO traz o Serial Number e o E-mail em vez de Nome e E-mail Dono
        holder.txtNomeForno.setText("Forno (Serial): " + (convite.getSerialNumber() != null ? convite.getSerialNumber() : "Desconhecido"));
        holder.txtRemetente.setText("E-mail: " + (convite.getEmail() != null ? convite.getEmail() : "Desconhecido"));

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