package com.example.monitorforno.models;

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

public class Sessao extends RecyclerView.Adapter<Sessao.ViewHolder> {

    private final List<SessaoDetalhesDTO> sessoes;

    public Sessao(List<SessaoDetalhesDTO> sessoes) {
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

        // Tratamento e formatação da data e hora da sessão (Formato ISO: yyyy-MM-ddTHH:mm:ss)
        String inicioSessao = sessao.getInicioSessao();
        String dataFormatada = "--/--/----";
        String horarioFormatado = "--:--";

        if (inicioSessao != null && inicioSessao.contains("T")) {
            try {
                String[] partes = inicioSessao.split("T");
                String[] dataPartes = partes[0].split("-"); // yyyy-MM-dd

                if (dataPartes.length == 3) {
                    // Converte para dd/MM/yyyy
                    dataFormatada = dataPartes[2] + "/" + dataPartes[1] + "/" + dataPartes[0];
                }

                if (partes[1].length() >= 5) {
                    horarioFormatado = partes[1].substring(0, 5); // Pega apenas HH:mm
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.txtData.setText(dataFormatada);
        holder.txtHorario.setText(horarioFormatado);

        // Formatação da duração vinda em segundos (Ex: 4500s -> 1h 15m)
        Long segundos = sessao.getDuracaoSegundos();
        String duracaoTexto = "0m";
        if (segundos != null && segundos > 0) {
            long horas = segundos / 3600;
            long minutos = (segundos % 3600) / 60;
            if (horas > 0) {
                duracaoTexto = horas + "h " + minutos + "m";
            } else {
                duracaoTexto = minutos + "m";
            }
        }
        holder.txtDuracao.setText(duracaoTexto);

        // Estado final do forno
        String estado = sessao.getEstadoFornoFinal() != null ? sessao.getEstadoFornoFinal() : "DESCONHECIDO";
        holder.txtEstado.setText(estado.replace("_", " ").replace("FORNO ", ""));

        // Cores baseadas no estado da sessão
        switch (estado) {
            case "FORNO_ATIVO":
                holder.txtEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "FORNO_AQUECENDO":
                holder.txtEstado.setTextColor(Color.parseColor("#FF9800")); // Laranja
                break;
            case "FORNO_ESFRIANDO":
                holder.txtEstado.setTextColor(Color.parseColor("#2196F3")); // Azul
                break;
            case "FORNO_DESLIGADO":
            default:
                holder.txtEstado.setTextColor(Color.GRAY);
                break;
        }

        // Evento de clique para abrir os Detalhes da Sessão
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