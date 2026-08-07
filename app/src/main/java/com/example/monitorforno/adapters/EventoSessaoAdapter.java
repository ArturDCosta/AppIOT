package com.example.monitorforno.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.EventoSessao;

import java.util.List;

public class EventoSessaoAdapter extends RecyclerView.Adapter<EventoSessaoAdapter.ViewHolder> {

    private final List<EventoSessao> eventos;

    public EventoSessaoAdapter(List<EventoSessao> eventos) {
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento_sessao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventoSessao evento = eventos.get(position);

        String descricaoApi = evento.getDescricao();
        if (descricaoApi == null) {
            descricaoApi = "DESCONHECIDO";
        }

        String textoExibicao;
        int corStatus;

        // Avalia o tipo do evento e define o texto e a cor correspondente
        switch (descricaoApi) {
            case "ALERTA_ENTRADA":
                textoExibicao = "Sistema entrou em alerta";
                corStatus = Color.parseColor("#fc9403"); // Laranja
                break;
            case "ALERTA_SAIDA":
                textoExibicao = "Sistema voltou ao normal";
                corStatus = Color.parseColor("#2426ab"); // Azul
                break;
            case "CRITICO_ENTRADA":
                textoExibicao = "Estado crítico detectado";
                corStatus = Color.parseColor("#ed0909"); // Vermelho
                break;
            case "CRITICO_SAIDA":
                textoExibicao = "Estado crítico encerrado";
                corStatus = Color.parseColor("#32ad34"); // Verde
                break;
            case "ERRO_SENSOR_ENTRADA":
                textoExibicao = "Falha no sensor";
                corStatus = Color.parseColor("#ebd915"); // Amarelo
                break;
            case "ERRO_SENSOR_SAIDA":
                textoExibicao = "Sensor recuperado";
                corStatus = Color.parseColor("#3c15eb"); // Azul escuro
                break;
            case "DESCONHECIDO":
                textoExibicao = "Evento não identificado";
                corStatus = Color.GRAY;
                break;
            default:
                textoExibicao = descricaoApi;
                corStatus = Color.WHITE;
                break;
        }

        // Aplica o texto e a cor no título do evento
        holder.txtEvento.setText(textoExibicao);
        holder.txtEvento.setTextColor(corStatus);

        // Pinta a bolinha da timeline com a mesma cor do evento
        holder.pontoTimeline.setBackgroundTintList(ColorStateList.valueOf(corStatus));

        // Formata o horário do evento para exibição
        holder.txtHorario.setText(formatarDataHora(evento.getHorario()));
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    // Formata o padrão ISO para "dd/MM/yyyy - HH:mm"
    private String formatarDataHora(String dataOriginal) {
        if (dataOriginal == null || !dataOriginal.contains("T")) {
            return "--/--/---- - --:--";
        }
        try {
            String[] partes = dataOriginal.split("T");
            String[] dataPartes = partes[0].split("-");
            String dataFormatada = dataPartes[2] + "/" + dataPartes[1] + "/" + dataPartes[0];
            String horaFormatada = partes[1].length() >= 5 ? partes[1].substring(0, 5) : partes[1];

            return dataFormatada + " - " + horaFormatada;
        } catch (Exception e) {
            return dataOriginal;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEvento;
        TextView txtHorario;
        View pontoTimeline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEvento = itemView.findViewById(R.id.txtEvento);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            pontoTimeline = itemView.findViewById(R.id.pontoTimeline);
        }
    }
}