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
import com.example.monitorforno.models.SessaoResumoDTO;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SessaoAdapter extends RecyclerView.Adapter<SessaoAdapter.ViewHolder> {

    private final List<SessaoResumoDTO> sessoes;

    public SessaoAdapter(List<SessaoResumoDTO> sessoes) {
        this.sessoes = sessoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sessao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessaoResumoDTO sessao = sessoes.get(position);
        String inicioSessao = sessao.getInicioSessao();

        holder.txtData.setText(formatarDataHora(inicioSessao, "dd/MM/yyyy"));
        holder.txtHorario.setText(formatarDataHora(inicioSessao, "HH:mm"));

        long minutos = 0;
        if (inicioSessao != null && sessao.getFimSessao() != null) {
            try {
                String inicioLimpo = inicioSessao.split("\\.")[0];
                if (!inicioLimpo.endsWith("Z")) inicioLimpo += "Z";
                String fimLimpo = sessao.getFimSessao().split("\\.")[0];
                if (!fimLimpo.endsWith("Z")) fimLimpo += "Z";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                java.util.Date dataInicio = sdf.parse(inicioLimpo);
                java.util.Date dataFim = sdf.parse(fimLimpo);

                if (dataInicio != null && dataFim != null) {
                    minutos = Math.max(0, (dataFim.getTime() - dataInicio.getTime()) / 60000);
                }
            } catch (Exception ignored) {}
        }
        holder.txtDuracao.setText(minutos + " min");

        String estado = sessao.getEstadoFornoFinal() != null ? sessao.getEstadoFornoFinal() : "FORNO_DESLIGADO";
        holder.txtEstado.setText(estado.replace("FORNO_", ""));

        switch (estado) {
            case "FORNO_ATIVO": holder.txtEstado.setTextColor(Color.parseColor("#4CAF50")); break;
            case "FORNO_AQUECENDO": holder.txtEstado.setTextColor(Color.parseColor("#FF9800")); break;
            case "FORNO_ESFRIANDO": holder.txtEstado.setTextColor(Color.parseColor("#2196F3")); break;
            default: holder.txtEstado.setTextColor(Color.GRAY); break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalhesSessaoActivity.class);
            intent.putExtra("SESSAO_ID", sessao.getId());
            v.getContext().startActivity(intent);
        });
    }

    private String formatarDataHora(String iso, String padraoSaida) {
        if (iso == null || iso.isEmpty()) return "--";
        try {
            String dataLimpa = iso.split("\\.")[0];
            if (!dataLimpa.endsWith("Z")) dataLimpa += "Z";
            SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdfEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            java.util.Date dataObj = sdfEntrada.parse(dataLimpa);
            SimpleDateFormat sdfSaida = new SimpleDateFormat(padraoSaida, Locale.getDefault());
            sdfSaida.setTimeZone(TimeZone.getDefault());
            return sdfSaida.format(dataObj);
        } catch (Exception e) { return iso; }
    }

    @Override
    public int getItemCount() { return sessoes != null ? sessoes.size() : 0; }

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