package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.TemporizadorResponseDTO;

import java.util.List;

public class TemporizadorAdapter extends RecyclerView.Adapter<TemporizadorAdapter.ViewHolder> {

    public interface OnTemporizadorInteractionListener {
        void onTemporizadorRemovido(String id, int position);
    }

    private final List<TemporizadorResponseDTO> temporizadores;
    private final OnTemporizadorInteractionListener listener;

    public TemporizadorAdapter(List<TemporizadorResponseDTO> temporizadores, OnTemporizadorInteractionListener listener) {
        this.temporizadores = temporizadores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_temporizador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemporizadorResponseDTO item = temporizadores.get(position);

        if (item.isExecutado()) {
            holder.txtHorario.setText("Status: Concluído");
        } else {
            String inicioStr = item.getHorarioInicio() != null ? item.getHorarioInicio() : item.getCriadoEm();
            String inicio = formatarHoraIso(inicioStr);
            String fim = formatarHoraIso(item.getHorarioFim());

            holder.txtHorario.setText("Duração: " + inicio + " até " + fim);
        }

        holder.imgExcluir.setOnClickListener(v -> {
            int posicaoAtual = holder.getAdapterPosition();
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                listener.onTemporizadorRemovido(item.getId(), posicaoAtual);
            }
        });
    }

    private String formatarHoraIso(String isoString) {
        if (isoString == null || !isoString.contains("T")) return "--:--";
        try {
            String horaParte = isoString.split("T")[1];
            return horaParte.substring(0, 5);
        } catch (Exception e) {
            return isoString;
        }
    }

    @Override
    public int getItemCount() {
        return temporizadores.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtHorario;
        ImageView imgExcluir;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHorario = itemView.findViewById(R.id.txtHorarioTemporizador);
            imgExcluir = itemView.findViewById(R.id.imgExcluir);
        }
    }
}