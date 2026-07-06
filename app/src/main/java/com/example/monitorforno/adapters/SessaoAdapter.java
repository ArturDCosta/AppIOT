package com.example.monitorforno.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.activities.DetalhesSessaoActivity;
import com.example.monitorforno.models.SessaoDetalhesDTO;

import java.util.List;

public class SessaoAdapter extends RecyclerView.Adapter<SessaoAdapter.ViewHolder> {

    // Substituímos a classe 'Sessao' pela classe DTO da API
    private final List<SessaoDetalhesDTO> sessoes;

    public SessaoAdapter(List<SessaoDetalhesDTO> sessoes) {
        this.sessoes = sessoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sessao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessaoDetalhesDTO sessao = sessoes.get(position);

        // 1. Extrai a Data (Dia/Mês/Ano) de dentro de "inicioSessao" (ex: "2026-07-06T14:30:00")
        String dataFormatada = "--/--/----";
        String inicioSessao = sessao.getHorarioInicio();
        if (inicioSessao != null && inicioSessao.contains("T")) {
            try {
                String[] partes = inicioSessao.split("T")[0].split("-");
                if (partes.length == 3) {
                    dataFormatada = partes[2] + "/" + partes[1] + "/" + partes[0];
                }
            } catch (Exception ignored) {}
        }
        holder.txtData.setText(dataFormatada);

        // 2. Extrai o Horário (HH:mm)
        String horaFormatada = "--:--";
        if (inicioSessao != null && inicioSessao.contains("T")) {
            String hora = inicioSessao.split("T")[1];
            horaFormatada = hora.length() >= 5 ? hora.substring(0, 5) : hora;
        }
        holder.txtHorario.setText(horaFormatada);

        // 3. Converte duracaoSegundos em Minutos
        long minutos = (sessao.getDuracaoSegundos() != null) ? (sessao.getDuracaoSegundos() / 60) : 0;
        holder.txtDuracao.setText(minutos + " min");

        // 4. Cores do Estado Final
        String estado = sessao.getEstadoFinal() != null ? sessao.getEstadoFinal() : "FORNO_DESLIGADO";
        holder.txtEstado.setText(estado.replace("FORNO_", ""));

        switch (estado) {
            case "FORNO_ATIVO":
                holder.txtEstado.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "FORNO_AQUECENDO":
                holder.txtEstado.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "FORNO_ESFRIANDO":
                holder.txtEstado.setTextColor(Color.parseColor("#2196F3"));
                break;
            default:
                holder.txtEstado.setTextColor(Color.GRAY);
                break;
        }

        // Clique para ir para Detalhes
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalhesSessaoActivity.class);
            intent.putExtra("SESSAO_ID", sessao.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sessoes != null ? sessoes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtData, txtHorario, txtDuracao, txtEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtData = itemView.findViewById(R.id.txtData);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtDuracao = itemView.findViewById(R.id.txtDuracao);
            txtEstado = itemView.findViewById(R.id.txtEstado);
        }
    }
}