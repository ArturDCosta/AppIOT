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

        holder.txtData.setText(sessao.getData() != null ? sessao.getData() : "--/--/----");
        holder.txtHorario.setText(sessao.getHorarioInicio() != null ? sessao.getHorarioInicio() : "--:--");
        holder.txtDuracao.setText(sessao.getDuracao() != null ? sessao.getDuracao() : "0m");

        String estado = sessao.getEstadoFinal() != null ? sessao.getEstadoFinal() : "DESCONHECIDO";
        holder.txtEstado.setText(estado.replace("_", " "));

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

            // O MAIS IMPORTANTE: Passar o ID real da sessão (Ex: "f47ac10b-58cc-4372-a567-0e02b2c3d479")
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