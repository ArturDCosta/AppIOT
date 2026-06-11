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

        String descricao = evento.getDescricao();

        holder.txtEvento.setText(descricao);

        if (descricao.contains("ALERTA")) {

            holder.txtEvento.setTextColor(
                    Color.parseColor("#fc9403")
            );

        } else if (descricao.contains("CRITICO_ENTRADA")) {

            holder.txtEvento.setTextColor(
                    Color.RED
            );

        } else if (descricao.contains("CRITICO_SAIDA")) {

            holder.txtEvento.setTextColor(
                    Color.parseColor("#32ad34")
            );

        } else if (descricao.contains("ERRO_SENSOR")) {

            holder.txtEvento.setTextColor(
                    Color.parseColor("#2426ab")
            );

        }
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