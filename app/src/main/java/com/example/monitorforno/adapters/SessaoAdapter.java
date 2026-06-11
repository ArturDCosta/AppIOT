package com.example.monitorforno.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.Sessao;

import java.util.List;
import android.content.Intent;

import com.example.monitorforno.activities.DetalhesSessaoActivity;

public class SessaoAdapter
        extends RecyclerView.Adapter<SessaoAdapter.ViewHolder> {

    private final List<Sessao> sessoes;

    public SessaoAdapter(List<Sessao> sessoes) {
        this.sessoes = sessoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_sessao,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Sessao sessao = sessoes.get(position);

        holder.txtData.setText(sessao.getData());
        holder.txtHorario.setText(sessao.getHorario());
        holder.txtDuracao.setText(sessao.getDuracao());
        holder.txtEstado.setText(sessao.getEstado());

        holder.txtEstado.setText(sessao.getEstado());
        switch (sessao.getEstado()) {

            case "FORNO_ATIVO":
                holder.txtEstado.setTextColor(
                        Color.parseColor("#32ad34")
                );
                break;

            case "FORNO_AQUECENDO":
                holder.txtEstado.setTextColor(
                        Color.parseColor("#fc9403")
                );
                break;

            case "FORNO_ESFRIANDO":
                holder.txtEstado.setTextColor(
                        Color.parseColor("#2426ab")
                );
                break;

            case "FORNO_DESLIGADO":
                holder.txtEstado.setTextColor(
                        Color.GRAY
                );
                break;
        }

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    DetalhesSessaoActivity.class
            );

            intent.putExtra(
                    "dataSessao",
                    sessao.getData()
            );

            intent.putExtra(
                    "horarioSessao",
                    sessao.getHorario()
            );

            intent.putExtra(
                    "duracaoSessao",
                    sessao.getDuracao()
            );

            intent.putExtra(
                    "estadoSessao",
                    sessao.getEstado()
            );

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sessoes.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtData;
        TextView txtHorario;
        TextView txtDuracao;
        TextView txtEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtData = itemView.findViewById(R.id.txtData);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtDuracao = itemView.findViewById(R.id.txtDuracao);
            txtEstado = itemView.findViewById(R.id.txtEstado);
        }
    }
}