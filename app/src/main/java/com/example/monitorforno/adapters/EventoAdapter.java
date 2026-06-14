package com.example.monitorforno.adapters;

import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.Evento;

import java.util.List;

public class EventoAdapter
        extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private final List<Evento> eventos;

    public EventoAdapter(List<Evento> eventos) {
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_alerta,
                        parent,
                        false
                );

        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventoViewHolder holder,
            int position) {

        Evento evento = eventos.get(position);

        String tipo = evento.getDescricao();
        String descricaoExibicao;

        switch (tipo) {

            case "ALERTA_ENTRADA":
                descricaoExibicao = "Sistema entrou em alerta";
                break;

            case "ALERTA_SAIDA":
                descricaoExibicao = "Sistema normalizado";
                break;

            case "CRITICO_ENTRADA":
                descricaoExibicao = "Estado crítico detectado";
                break;

            case "CRITICO_SAIDA":
                descricaoExibicao = "Sistema saiu do estado crítico";
                break;

            case "ERRO_SENSOR_ENTRADA":
                descricaoExibicao = "Erro de sensor detectado";
                break;

            case "ERRO_SENSOR_SAIDA":
                descricaoExibicao = "Sensor normalizado";
                break;

            default:
                descricaoExibicao = tipo;
        }

        holder.txtTipoEvento.setText(descricaoExibicao);

        holder.txtHorarioEvento.setText(
                evento.getHorario()
        );

        if (tipo.equals("ALERTA_ENTRADA")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_laranja)
            );

        } else if (tipo.equals("CRITICO_ENTRADA")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_vermelho)
            );

        } else if (
                tipo.equals("CRITICO_SAIDA") ||
                        tipo.equals("ALERTA_SAIDA")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_verde)
            );

        } else if (
                tipo.equals("ERRO_SENSOR_ENTRADA") ||
                        tipo.equals("ERRO_SENSOR_SAIDA")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(android.R.color.darker_gray)
            );
        }
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    static class EventoViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTipoEvento;
        TextView txtHorarioEvento;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTipoEvento =
                    itemView.findViewById(R.id.txtTipoEvento);

            txtHorarioEvento =
                    itemView.findViewById(R.id.txtHorarioEvento);
        }
    }
}