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

    // 1. Componentes de texto para as temperaturas
    private TextView txtEstadoForno, txtEstadoSistema, txtUltimaLeitura;
    private TextView txtTemperaturaAtual, txtTemperaturaUltima;
    private LineChart chart;
    private ImageView btnVoltar;

    private String fornoId = "";
    private final Handler handler = new Handler();
    private Runnable runnableTempoReal;

    // Flag para controlar se o histórico do gráfico deve ser exibido ou limpo em paralelo
    private boolean fornoDesligado = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperatura, container, false);

        txtEstadoForno = view.findViewById(R.id.txtEstadoForno);
        txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);
        txtUltimaLeitura = view.findViewById(R.id.txtUltimaLeitura);
        chart = view.findViewById(R.id.chartTemperatura);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // Inicializando os IDs do seu XML
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
                handler.postDelayed(this, 1000);
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

                            // REGRA DE NEGÓCIO: Se o forno estiver desligado, zera tudo na tela e limpa o gráfico
                            if ("FORNO_DESLIGADO".equals(estadoForno)) {
                                fornoDesligado = true;

                                txtEstadoForno.setText("DESLIGADO");
                                txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_desligado));

                                txtEstadoSistema.setText("--");
                                txtEstadoSistema.setTextColor(Color.GRAY);

                                txtUltimaLeitura.setText("--");

                                if (txtTemperaturaAtual != null) txtTemperaturaAtual.setText("--");
                                if (txtTemperaturaUltima != null) txtTemperaturaUltima.setText("--");

                                // Limpa o gráfico imediatamente se o forno desligar
                                chart.setNoDataText("Ainda não há dados de temperatura para este forno.");
                                chart.clear();
                                chart.invalidate();

                            } else {
                                // Caso contrário, o forno está ativo (AQUECENDO, ATIVO, ESFRIANDO)
                                fornoDesligado = false;

                                switch (estadoForno) {
                                    case "FORNO_AQUECENDO":
                                        txtEstadoForno.setText("AQUECENDO");
                                        txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_aquecendo));
                                        break;
                                    case "FORNO_ATIVO":
                                        txtEstadoForno.setText("ATIVO");
                                        txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_ativo));
                                        break;
                                    case "FORNO_ESFRIANDO":
                                        txtEstadoForno.setText("ESFRIANDO");
                                        txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_esfriando));
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
                                    txtEstadoSistema.setText(estadoSistema != null ? estadoSistema : "--");
                                    txtEstadoSistema.setTextColor(Color.GRAY);
                                }

                                txtUltimaLeitura.setText(formatarHora(telemetria.getAtualizadoEm() != null ? telemetria.getAtualizadoEm() : telemetria.getAtualizadoEm()));

                                if (txtTemperaturaAtual != null) {
                                    if (telemetria.getTemperaturaAtual() != null) {
                                        txtTemperaturaAtual.setText(String.format("%.1f°C", telemetria.getTemperaturaAtual()));
                                    } else {
                                        txtTemperaturaAtual.setText("--");
                                    }
                                }

                                if (txtTemperaturaUltima != null) {
                                    if (telemetria.getTemperaturaUltima() != null) {
                                        txtTemperaturaUltima.setText(String.format("%.1f°C", telemetria.getTemperaturaUltima()));
                                    } else {
                                        txtTemperaturaUltima.setText("--");
                                    }
                                }
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
                            List<TemperaturaDTO> lista = response.body();
                            // Só renderiza o gráfico se a lista contiver dados E se o forno não estiver marcado como desligado
                            if (!lista.isEmpty() && !fornoDesligado) {
                                exibirDadosNoGrafico(lista);
                            } else {
                                chart.setNoDataText("Ainda não há dados de temperatura para este forno.");
                                chart.clear();
                                chart.invalidate();
                            }
                        } else if (response.code() == 404) {
                            Log.d("DEBUG_GRAFICO", "O servidor retornou 404. Assumindo que não há dados no banco.");
                            chart.setNoDataText("Ainda não há dados de temperatura para este forno.");
                            chart.clear();
                            chart.invalidate();
                        } else {
                            Log.e("DEBUG_GRAFICO_ERRO", "Falha na API. Código: " + response.code());
                            Toast.makeText(getContext(), "Erro no servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TemperaturaDTO>> call, Throwable t) {
                        Log.e("DEBUG_GRAFICO_ERRO", "Falha de rede gráfico: " + t.getMessage());
                    }
                });
    }

    private void exibirDadosNoGrafico(List<TemperaturaDTO> lista) {
        if (lista == null || lista.isEmpty()) {
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

                String horaCompleta = temp.getHorarioFormatado();
                String horaSemSegundos = (horaCompleta != null && horaCompleta.length() >= 5)
                        ? horaCompleta.substring(0, 5)
                        : horaCompleta;

                horarios.add(horaSemSegundos);
            }
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("Dados de temperatura inválidos ou nulos.");
            chart.clear();
            chart.invalidate();
            return;
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

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);

        chart.invalidate();
    }

    private String formatarHora(String dataIso) {
        if (dataIso == null || !dataIso.contains("T")) {
            return dataIso != null ? dataIso : "--:--:--";
        }
        try {
            String horaComMilissegundos = dataIso.split("T")[1];
            if (horaComMilissegundos.length() >= 8) {
                return horaComMilissegundos.substring(0, 8);
            }
            return horaComMilissegundos;
        } catch (Exception e) {
            return dataIso;
        }
    }

    private String formatarHoraSemSegundos(String horaCompleta) {
        if (horaCompleta == null || horaCompleta.length() < 5) return "";
        return horaCompleta.length() >= 5 ? horaCompleta.substring(0, 5) : horaCompleta;
    }
}