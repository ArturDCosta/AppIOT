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

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventoSessao evento = eventos.get(position);

        // 1. Pegamos a string original da API
        String descricaoApi = evento.getDescricao();

        // 2. Se for nula, tratamos para evitar o NullPointerException no switch
        if (descricaoApi == null) {
            descricaoApi = "DESCONHECIDO";
        }

        String textoExibicao;

        // 3. O switch agora avalia a variável tratada (nunca será nula)
        switch (descricaoApi) {
            case "ALERTA_ENTRADA":
                textoExibicao = "Sistema entrou em alerta";
                holder.txtEvento.setTextColor(Color.parseColor("#fc9403"));
                break;

            case "ALERTA_SAIDA":
                textoExibicao = "Sistema voltou ao normal";
                holder.txtEvento.setTextColor(Color.parseColor("#2426ab"));
                break;

            case "CRITICO_ENTRADA":
                textoExibicao = "Estado crítico detectado";
                holder.txtEvento.setTextColor(Color.parseColor("#ed0909"));
                break;

            case "CRITICO_SAIDA":
                textoExibicao = "Estado crítico encerrado";
                holder.txtEvento.setTextColor(Color.parseColor("#32ad34"));
                break;

            case "ERRO_SENSOR_ENTRADA":
                textoExibicao = "Falha no sensor";
                holder.txtEvento.setTextColor(Color.parseColor("#ebd915"));
                break;

            case "ERRO_SENSOR_SAIDA":
                textoExibicao = "Sensor recuperado";
                holder.txtEvento.setTextColor(Color.parseColor("#3c15eb"));
                break;

            case "DESCONHECIDO":
                textoExibicao = "Evento não identificado";
                holder.txtEvento.setTextColor(Color.GRAY);
                break;

            default:
                // Caso venha um texto que não mapeamos nos cases anteriores
                textoExibicao = descricaoApi;
                holder.txtEvento.setTextColor(Color.WHITE);
                break;
        }

        // 4. Seta o texto final traduzido
        holder.txtEvento.setText(textoExibicao);
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