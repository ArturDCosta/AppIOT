package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.EventoSessao;

import java.util.List;import android.graphics.Color;

public class EventoSessaoAdapter
        extends RecyclerView.Adapter<EventoSessaoAdapter.ViewHolder> {

    private final List<EventoSessao> eventos;

    public EventoSessaoAdapter(List<EventoSessao> eventos) {
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_evento_sessao,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        EventoSessao evento = eventos.get(position);

        String descricao;

        switch (evento.getDescricao()) {

            case "ALERTA_ENTRADA":
                descricao = "Sistema entrou em alerta";
                holder.txtEvento.setTextColor(Color.parseColor("#fc9403"));
                break;

            case "ALERTA_SAIDA":
                descricao = "Sistema voltou ao normal";
                holder.txtEvento.setTextColor(Color.parseColor("#2426ab"));
                break;

            case "CRITICO_ENTRADA":
                descricao = "Estado crítico detectado";
                holder.txtEvento.setTextColor(Color.parseColor("#ed0909"));
                break;

            case "CRITICO_SAIDA":
                descricao = "Estado crítico encerrado";
                holder.txtEvento.setTextColor(Color.parseColor("#32ad34"));
                break;

            case "ERRO_SENSOR_ENTRADA":
                descricao = "Falha no sensor";
                holder.txtEvento.setTextColor(Color.parseColor("#ebd915"));
                break;

            case "ERRO_SENSOR_SAIDA":
                descricao = "Sensor recuperado";
                holder.txtEvento.setTextColor(Color.parseColor("#3c15eb"));
                break;

            default:
                descricao = evento.getDescricao();
        }

        holder.txtEvento.setText(descricao);
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtEvento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtEvento =
                    itemView.findViewById(R.id.txtEvento);
        }
    }
}