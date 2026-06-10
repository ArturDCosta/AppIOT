package com.example.monitorforno.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.Evento;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

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

        holder.txtTipoEvento.setText(
                evento.getDescricao()
        );

        holder.txtHorarioEvento.setText(
                evento.getHorario()
        );

        String descricao = evento.getDescricao();

        if (descricao.contains("alerta")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_laranja)
            );

        } else if (descricao.contains("crítico")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_vermelho)
            );

        } else if (descricao.contains("normalizado")) {

            holder.txtTipoEvento.setTextColor(
                    holder.itemView.getContext()
                            .getColor(R.color.alerta_verde)
            );

        }
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {

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