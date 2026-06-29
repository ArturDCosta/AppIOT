package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.monitorforno.R;
import com.example.monitorforno.models.TelemetriaResponseDTO;
import com.example.monitorforno.models.TemperaturaDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemperaturaFragment extends Fragment {

    // 1. ADICIONADOS: Componentes de texto que estavam faltando para as temperaturas
    private TextView txtEstadoForno, txtEstadoSistema, txtUltimaLeitura;
    private TextView txtTemperaturaAtual, txtTemperaturaUltima;
    private LineChart chart;
    private ImageView btnVoltar;

    private String fornoId = "";
    private final Handler handler = new Handler();
    private Runnable runnableTempoReal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperatura, container, false);

        txtEstadoForno = view.findViewById(R.id.txtEstadoForno);
        txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);
        txtUltimaLeitura = view.findViewById(R.id.txtUltimaLeitura);
        chart = view.findViewById(R.id.chartTemperatura);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // 2. CORREÇÃO: Inicializando os IDs do seu XML (Substitua pelos IDs reais do seu XML se forem diferentes)
        txtTemperaturaAtual = view.findViewById(R.id.txtTempAtual);
        txtTemperaturaUltima = view.findViewById(R.id.txtUltimaTemperatura);

        if (getArguments() != null) {
            fornoId = getArguments().getString("FORNO_ID", "");
        }

        btnVoltar.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        runnableTempoReal = new Runnable() {
            @Override
            public void run() {
                carregarDadosDaApi();
                handler.postDelayed(this, 3000);
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnableTempoReal);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableTempoReal);
    }

    private void carregarDadosDaApi() {
        if (fornoId == null || fornoId.isEmpty()) {
            Toast.makeText(getContext(), "Erro: ID do forno não recebido!", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService(getContext()).getTelemetriaAtual(fornoId)
                .enqueue(new Callback<TelemetriaResponseDTO>() {
                    @Override
                    public void onResponse(Call<TelemetriaResponseDTO> call, Response<TelemetriaResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TelemetriaResponseDTO telemetria = response.body();

                            String estadoForno = telemetria.getEstadoForno() != null ? telemetria.getEstadoForno() : "FORNO_DESLIGADO";

                            switch (estadoForno) {
                                case "FORNO_AQUECENDO":
                                    txtEstadoForno.setText("FORNO AQUECENDO");
                                    txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_aquecendo));
                                    break;
                                case "FORNO_ATIVO":
                                    txtEstadoForno.setText("FORNO ATIVO");
                                    txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_ativo));
                                    break;
                                case "FORNO_ESFRIANDO":
                                    txtEstadoForno.setText("FORNO ESFRIANDO");
                                    txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_esfriando));
                                    break;
                                default:
                                case "FORNO_DESLIGADO":
                                    txtEstadoForno.setText("FORNO DESLIGADO");
                                    txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_desligado));
                                    break;
                            }

                            String estadoSistema = telemetria.getEstadoSistema();
                            if ("SEGURO".equals(estadoSistema) || "OPERACAO_NORMAL".equals(estadoSistema)) {
                                txtEstadoSistema.setText("SEGURO");
                                txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_verde));
                            } else if ("ALERTA".equals(estadoSistema)) {
                                txtEstadoSistema.setText("ALERTA");
                                txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_laranja));
                            } else if ("CRITICO".equals(estadoSistema)) {
                                txtEstadoSistema.setText("CRITICO");
                                txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_vermelho));
                            } else {
                                txtEstadoSistema.setText(estadoSistema);
                                txtEstadoSistema.setTextColor(Color.GRAY);
                            }

                            txtUltimaLeitura.setText(telemetria.getAtualizadoEm());

                            // 3. CORREÇÃO: Setando os valores numéricos capturados da API
                            if (txtTemperaturaAtual != null && telemetria.getTemperaturaAtual() != null) {
                                txtTemperaturaAtual.setText(String.format("%.1f°C", telemetria.getTemperaturaAtual()));
                            }
                            if (txtTemperaturaUltima != null && telemetria.getTemperaturaUltima() != null) {
                                txtTemperaturaUltima.setText(String.format("%.1f°C", telemetria.getTemperaturaUltima()));
                            }
                        } else {
                            Log.e("API_ERROR", "Erro na telemetria: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<TelemetriaResponseDTO> call, Throwable t) {
                        Log.e("API_FAILURE", "Falha de rede telemetria: " + t.getMessage());
                    }
                });

        RetrofitClient.getApiService(getContext()).getHistoricoTemperaturas(fornoId)
                .enqueue(new Callback<List<TemperaturaDTO>>() {
                    @Override
                    public void onResponse(Call<List<TemperaturaDTO>> call, Response<List<TemperaturaDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            exibirDadosNoGrafico(response.body());
                        } else {
                            Log.e("API_ERROR", "Erro no gráfico: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TemperaturaDTO>> call, Throwable t) {
                        Log.e("API_FAILURE", "Falha de rede gráfico: " + t.getMessage());
                    }
                });
    }

    private void exibirDadosNoGrafico(List<TemperaturaDTO> lista) {
        if (lista == null || lista.isEmpty()) {
            // Se cair aqui, a API retornou uma lista vazia [] do banco de dados
            chart.setNoDataText("Nenhum dado de histórico encontrado para este forno.");
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> horarios = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            TemperaturaDTO temp = lista.get(i);
            if (temp.getTemperaturaAtual() != null) {
                entries.add(new Entry(i, temp.getTemperaturaAtual().floatValue()));
                horarios.add(temp.getRegistradoEm() != null ? temp.getRegistradoEm() : "");
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Histórico");
        dataSet.setColor(Color.parseColor("#fc9403"));
        dataSet.setCircleColor(Color.parseColor("#fc9403"));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.WHITE);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis eixoX = chart.getXAxis();
        eixoX.setPosition(XAxis.XAxisPosition.BOTTOM);
        eixoX.setGranularity(1f);
        eixoX.setValueFormatter(new IndexAxisValueFormatter(horarios));
        eixoX.setTextColor(Color.WHITE);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }
}