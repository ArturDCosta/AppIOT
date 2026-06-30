package com.example.monitorforno.adapters;

import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.EventoDTO;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private final List<EventoDTO> eventos;

    public EventoAdapter(List<EventoDTO> eventos) {
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alerta, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        EventoDTO evento = eventos.get(position);

        String tipo = evento.getTipo() != null ? evento.getTipo() : "DESCONHECIDO";
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

        // =========================================================================
        // CORREÇÃO: Tratando o campo criadoEm que vem do LocalDateTime do Spring
        // =========================================================================
        String dataOriginal = evento.getCriadoEm();
        String horarioExibicao = "--:--";

        if (dataOriginal != null) {
            if (dataOriginal.contains("T")) {
                try {
                    // Separa "2026-06-30T14:45:00" em ["2026-06-30", "14:45:00"]
                    String[] partes = dataOriginal.split("T");
                    // Pega apenas os 5 primeiros caracteres do horário ("14:45")
                    horarioExibicao = partes[1].substring(0, 5);
                } catch (Exception e) {
                    horarioExibicao = dataOriginal; // Fallback caso a string venha diferente
                }
            } else {
                horarioExibicao = dataOriginal;
            }
        }
        holder.txtHorarioEvento.setText(horarioExibicao);
        // =========================================================================

        // Configuração de Cores (Mantida a sua lógica original perfeita)
        if (descricaoExibicao.equals("Sistema entrou em alerta")) {
            holder.txtTipoEvento.setTextColor(holder.itemView.getContext().getColor(R.color.alerta_laranja));
        } else if (descricaoExibicao.equals("Estado crítico detectado") || descricaoExibicao.equals("Estado crítico")) {
            holder.txtTipoEvento.setTextColor(holder.itemView.getContext().getColor(R.color.alerta_vermelho));
        } else if (descricaoExibicao.equals("Sistema saiu do estado crítico") || descricaoExibicao.equals("Sistema normalizado")) {
            holder.txtTipoEvento.setTextColor(holder.itemView.getContext().getColor(R.color.alerta_verde));
        } else if (descricaoExibicao.equals("Erro de sensor detectado") || descricaoExibicao.equals("Sensor normalizado")) {
            holder.txtTipoEvento.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return eventos != null ? eventos.size() : 0;
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTipoEvento;
        TextView txtHorarioEvento;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTipoEvento = itemView.findViewById(R.id.txtTipoEvento);
            txtHorarioEvento = itemView.findViewById(R.id.txtHorarioEvento);
        }
    }
}