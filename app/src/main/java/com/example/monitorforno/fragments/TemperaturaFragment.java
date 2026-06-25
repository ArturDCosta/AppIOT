package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
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
import com.github.mikephil.charting.components.LimitLine;
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

    private TextView txtEstadoForno, txtEstadoSistema, txtUltimaLeitura;
    private LineChart chart;
    private ImageView btnVoltar;

    // ID do Forno mockado ou vindo do Bundle do Dashboard
    private String fornoId = "b06f3899-1060-4bec-b0f7-1bd6c5fcce90";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperatura, container, false);

        // Inicializa componentes da tela
        txtEstadoForno = view.findViewById(R.id.txtEstadoForno);
        txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);
        txtUltimaLeitura = view.findViewById(R.id.txtUltimaLeitura);
        chart = view.findViewById(R.id.chartTemperatura);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        // Carrega as informações da API
        carregarStatusAtual();
        carregarHistoricoGrafico();

        return view;
    }

    private void carregarStatusAtual() {
        RetrofitClient.getApiService(getContext())
                .getTelemetriaAtual(fornoId)
                .enqueue(new Callback<TelemetriaResponseDTO>() {
                    @Override
                    public void onResponse(Call<TelemetriaResponseDTO> call, Response<TelemetriaResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TelemetriaResponseDTO dados = response.body();

                            // Atualiza os cards de status conforme o retorno do Enum
                            txtEstadoForno.setText(dados.getEstadoForno());
                            txtEstadoSistema.setText(dados.getEstadoSistema());

                            // Formata a exibição da temperatura atual ou última leitura
                            txtUltimaLeitura.setText(String.format("%.1f °C", dados.getTemperaturaAtual()));

                            // Altera cor do texto baseado no estado se desejar
                            if ("FORNO_AQUECENDO".equals(dados.getEstadoForno())) {
                                txtEstadoForno.setTextColor(Color.parseColor("#fc9403"));
                            } else {
                                txtEstadoForno.setTextColor(Color.GREEN);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TelemetriaResponseDTO> call, Throwable t) {
                        Log.e("TemperaturaFrag", "Erro status: " + t.getMessage());
                    }
                });
    }

    private void carregarHistoricoGrafico() {
        RetrofitClient.getApiService(getContext())
                .getHistoricoTemperaturas()
                .enqueue(new Callback<List<TemperaturaDTO>>() {
                    @Override
                    public void onResponse(Call<List<TemperaturaDTO>> call, Response<List<TemperaturaDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            configurarGraficoComDados(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TemperaturaDTO>> call, Throwable t) {
                        Toast.makeText(getContext(), "Erro ao carregar o gráfico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void configurarGraficoComDados(List<TemperaturaDTO> historico) {
        ArrayList<Entry> valores = new ArrayList<>();
        ArrayList<String> horarios = new ArrayList<>();

        for (int i = 0; i < historico.size(); i++) {
            TemperaturaDTO leitura = historico.get(i);

            // Adiciona o valor no eixo Y (i é a posição X, valor é o Y)
            valores.add(new Entry(i, leitura.getValor().floatValue()));

            // Trata a String ISO da API (Ex: "2026-06-25T19:44:32") para extrair apenas "19:44"
            String horaFormatada = "00:00";
            if (leitura.getRegistradoEm() != null && leitura.getRegistradoEm().contains("T")) {
                String[] partes = leitura.getRegistradoEm().split("T");
                if (partes.length > 1) {
                    horaFormatada = partes[1].substring(0, 5); // Pega apenas HH:mm
                }
            }
            horarios.add(horaFormatada);
        }

        // Se a lista vier vazia, coloca dados padrão para não quebrar o layout
        if (valores.isEmpty()) {
            valores.add(new Entry(0, 0f));
            horarios.add("--:--");
        }

        LineDataSet dataSet = new LineDataSet(valores, "Variação da Temperatura");
        dataSet.setColor(Color.parseColor("#fc9403"));
        dataSet.setCircleColor(Color.parseColor("#fc9403"));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customização dos Eixos (Mantendo o seu padrão escuro)
        XAxis eixoX = chart.getXAxis();
        eixoX.setPosition(XAxis.XAxisPosition.BOTTOM);
        eixoX.setGranularity(1f);
        eixoX.setLabelCount(horarios.size(), true);
        eixoX.setValueFormatter(new IndexAxisValueFormatter(horarios));
        eixoX.setTextColor(Color.WHITE);
        eixoX.setDrawGridLines(false);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setDrawGridLines(true);

        // Adiciona a linha de limite de segurança se quiser
        LimitLine limiteCritico = new LimitLine(220f, "Limite Crítico");
        limiteCritico.setLineColor(Color.parseColor("#e85f5f"));
        limiteCritico.setTextColor(Color.parseColor("#e85f5f"));
        chart.getAxisLeft().addLimitLine(limiteCritico);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);

        // Atualiza o desenho na tela
        chart.invalidate();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}