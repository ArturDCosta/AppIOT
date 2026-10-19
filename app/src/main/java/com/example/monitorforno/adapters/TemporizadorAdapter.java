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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

        String inicio = formatarHoraInstant(item.getHorarioInicio());
        String fim = formatarHoraInstant(item.getHorarioFim());

        holder.txtHorario.setText("Duração: " + inicio + " até " + fim);

        holder.imgExcluir.setOnClickListener(v -> {
            int posicaoAtual = holder.getAdapterPosition();
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                listener.onTemporizadorRemovido(item.getId(), posicaoAtual);
            }
        });
    }

    private String formatarHoraInstant(String instantString) {
        if (instantString == null) return "00:00";
        try {
            // Retira a parte fracionada e o Z
            String dataLimpa = instantString.split("\\.")[0].replace("Z", "");

            // Entrada configurada como UTC
            SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdfEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));

            java.util.Date data = sdfEntrada.parse(dataLimpa);

            // Saída configurada para o horário local (Locale.getDefault() já utiliza a default TimeZone)
            SimpleDateFormat sdfSaida = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdfSaida.format(data);
        } catch (Exception e) {
            return instantString;
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