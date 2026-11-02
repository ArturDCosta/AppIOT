
package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemperaturaFragment extends Fragment {

    private static final long INTERVALO_TELEMETRIA = 5000;
    private static final long INTERVALO_HISTORICO = 5000;

    private TextView txtEstadoForno, txtEstadoSistema, txtUltimaLeitura;
    private TextView txtTemperaturaAtual, txtTemperaturaUltima;
    private LineChart chart;
    private ImageView btnVoltar;

    private String fornoId = "";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnableTelemetria;
    private Runnable runnableHistorico;

    private boolean fornoDesligado = false;

    private boolean telemetriaEmAndamento = false;
    private boolean historicoEmAndamento = false;
    private boolean avisoFornoIdExibido = false;

    private Call<TelemetriaResponseDTO> telemetriaCall;
    private Call<List<TemperaturaDTO>> historicoCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_temperatura, container, false);

        txtEstadoForno = view.findViewById(R.id.txtEstadoForno);
        txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);
        txtUltimaLeitura = view.findViewById(R.id.txtUltimaLeitura);
        chart = view.findViewById(R.id.chartTemperatura);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        txtTemperaturaAtual = view.findViewById(R.id.txtTempAtual);
        txtTemperaturaUltima = view.findViewById(R.id.txtUltimaTemperatura);

        if (getArguments() != null) {
            fornoId = getArguments().getString("FORNO_ID", "");
        }

        if (fornoId == null || fornoId.isEmpty()) {

            if (!avisoFornoIdExibido) {
                Toast.makeText(requireContext(),
                        "Erro: ID do forno não recebido!", Toast.LENGTH_SHORT).show();

                avisoFornoIdExibido = true;
            }
        }

        btnVoltar.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Atualização da telemetria a cada 5 segundos
        runnableTelemetria = new Runnable() {
            @Override
            public void run() {
                buscarTelemetria();
                handler.postDelayed(this, INTERVALO_TELEMETRIA);
            }
        };

        // Atualização do histórico a cada 30 segundos
        runnableHistorico = new Runnable() {
            @Override
            public void run() {
                buscarHistorico();
                handler.postDelayed(this, INTERVALO_HISTORICO);
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fornoId == null || fornoId.isEmpty()) {
            return;
        }

        handler.removeCallbacks(runnableTelemetria);
        handler.removeCallbacks(runnableHistorico);

        handler.post(runnableTelemetria);
        handler.post(runnableHistorico);
    }

    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(runnableTelemetria);
        handler.removeCallbacks(runnableHistorico);

        cancelarRequisicoes();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(runnableTelemetria);
        handler.removeCallbacks(runnableHistorico);

        cancelarRequisicoes();

        txtEstadoForno = null;
        txtEstadoSistema = null;
        txtUltimaLeitura = null;
        txtTemperaturaAtual = null;
        txtTemperaturaUltima = null;
        chart = null;
        btnVoltar = null;

        super.onDestroyView();
    }

    private void cancelarRequisicoes() {

        Call<TelemetriaResponseDTO> chamadaTelemetria = telemetriaCall;
        Call<List<TemperaturaDTO>> chamadaHistorico = historicoCall;

        telemetriaCall = null;
        historicoCall = null;

        telemetriaEmAndamento = false;
        historicoEmAndamento = false;

        if (chamadaTelemetria != null) {
            chamadaTelemetria.cancel();
        }

        if (chamadaHistorico != null) {
            chamadaHistorico.cancel();
        }
    }

    private void buscarTelemetria() {

        if (!isAdded() || getView() == null || telemetriaEmAndamento) {
            return;
        }

        if (fornoId == null || fornoId.isEmpty()) {
            return;
        }

        telemetriaEmAndamento = true;

        Call<TelemetriaResponseDTO> call = RetrofitClient
                .getApiService(requireContext())
                .getTelemetriaAtual(fornoId);

        telemetriaCall = call;

        call.enqueue(new Callback<TelemetriaResponseDTO>() {
            @Override
            public void onResponse(Call<TelemetriaResponseDTO> call, Response<TelemetriaResponseDTO> response) {

                if (telemetriaCall != call) {
                    return;
                }

                telemetriaCall = null;
                telemetriaEmAndamento = false;

                if (!isAdded() || getView() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    TelemetriaResponseDTO telemetria = response.body();

                    String estadoForno = telemetria.getEstadoForno() != null
                            ? telemetria.getEstadoForno()
                            : "FORNO_DESLIGADO";

                    if ("FORNO_DESLIGADO".equals(estadoForno)) {

                        fornoDesligado = true;

                        txtEstadoForno.setText("DESLIGADO");
                        txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_desligado));

                        txtEstadoSistema.setText("--");
                        txtEstadoSistema.setTextColor(Color.GRAY);

                        txtUltimaLeitura.setText("--");

                        txtTemperaturaAtual.setText("--");
                        txtTemperaturaUltima.setText("--");

                        limparGrafico();

                    } else {

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

                        txtUltimaLeitura.setText(formatarHora(telemetria.getAtualizadoEm()));

                        if (telemetria.getTemperaturaAtual() != null) {
                            txtTemperaturaAtual.setText(String.format(Locale.getDefault(), "%.1f°C", telemetria.getTemperaturaAtual()));
                        } else {
                            txtTemperaturaAtual.setText("--");
                        }

                        if (telemetria.getTemperaturaUltima() != null) {
                            txtTemperaturaUltima.setText(String.format(Locale.getDefault(), "%.1f°C", telemetria.getTemperaturaUltima()));
                        } else {
                            txtTemperaturaUltima.setText("--");
                        }
                    }

                } else {

                    Log.e("API_ERROR", "Erro na telemetria: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TelemetriaResponseDTO> call, Throwable t) {

                if (telemetriaCall != call) {
                    return;
                }

                telemetriaCall = null;
                telemetriaEmAndamento = false;

                if (call.isCanceled()) {
                    return;
                }

                Log.e("API_FAILURE", "Falha de rede telemetria: " + t.getMessage());
            }
        });
    }

    private void buscarHistorico() {

        if (!isAdded() || getView() == null || historicoEmAndamento || fornoDesligado) {
            return;
        }

        if (fornoId == null || fornoId.isEmpty()) {
            return;
        }

        historicoEmAndamento = true;

        Call<List<TemperaturaDTO>> call = RetrofitClient
                .getApiService(requireContext())
                .getHistoricoTemperaturas(fornoId);

        historicoCall = call;

        call.enqueue(new Callback<List<TemperaturaDTO>>() {
            @Override
            public void onResponse(Call<List<TemperaturaDTO>> call, Response<List<TemperaturaDTO>> response) {

                if (historicoCall != call) {
                    return;
                }

                historicoCall = null;
                historicoEmAndamento = false;

                if (!isAdded() || getView() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    List<TemperaturaDTO> lista = response.body();

                    if (!fornoDesligado && !lista.isEmpty()) {
                        exibirDadosNoGrafico(lista);
                    } else {
                        limparGrafico();
                    }

                } else if (response.code() == 404) {

                    Log.d("DEBUG_GRAFICO", "Nenhum histórico encontrado para este forno.");
                    limparGrafico();

                } else {

                    Log.e("DEBUG_GRAFICO_ERRO", "Falha na API. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TemperaturaDTO>> call, Throwable t) {

                if (historicoCall != call) {
                    return;
                }

                historicoCall = null;
                historicoEmAndamento = false;

                if (call.isCanceled()) {
                    return;
                }

                Log.e("DEBUG_GRAFICO_ERRO", "Falha de rede gráfico: " + t.getMessage());
            }
        });
    }

    private void limparGrafico() {

        if (chart == null) {
            return;
        }

        chart.setNoDataText("Ainda não há dados de temperatura para este forno.");
        chart.clear();
        chart.invalidate();
    }

    private void exibirDadosNoGrafico(List<TemperaturaDTO> lista) {

        if (chart == null || lista == null || lista.isEmpty()) {
            limparGrafico();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> horarios = new ArrayList<>();

        for (TemperaturaDTO temp : lista) {

            if (temp != null && temp.getTemperaturaAtual() != null) {

                entries.add(new Entry(entries.size(), temp.getTemperaturaAtual().floatValue()));

                String horaCompleta = temp.getHorarioFormatado();

                String horaSemSegundos = (horaCompleta != null && horaCompleta.length() >= 5)
                        ? horaCompleta.substring(0, 5)
                        : horaCompleta;

                horarios.add(horaSemSegundos != null ? horaSemSegundos : "");
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

        if (dataIso == null || dataIso.isEmpty()) {
            return "--:--:--";
        }

        try {

            java.time.OffsetDateTime dataComFuso =
                    java.time.OffsetDateTime.parse(dataIso);

            return dataComFuso
                    .atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        } catch (Exception e) {

            return dataIso;
        }
    }
}
