package com.example.monitorforno.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.EventoSessaoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.SessaoDetalhesDTO;
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

public class DetalhesSessaoActivity extends AppCompatActivity {

    private LineChart chart;
    private TextView txtTituloSessao, txtInicio, txtFim, txtDuracao, txtEstadoFinal;
    private TextView txtTempMax, txtTempMedia, txtQtdAlertas, txtQtdCriticos;
    private RecyclerView recyclerView;
    private ImageView btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_sessao);

        inicializarViews();
        btnVoltar.setOnClickListener(v -> finish());

        String sessaoId = getIntent().getStringExtra("SESSAO_ID");
        if (sessaoId != null && !sessaoId.isEmpty()) {
            buscarDetalhesNaApi(sessaoId);
        } else {
            Toast.makeText(this, "ID da sessão não encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void inicializarViews() {
        chart = findViewById(R.id.chartSessao);
        btnVoltar = findViewById(R.id.btnVoltar);
        txtTituloSessao = findViewById(R.id.txtTituloSessao);
        txtInicio = findViewById(R.id.txtInicio);
        txtFim = findViewById(R.id.txtFim);
        txtDuracao = findViewById(R.id.txtDuracao);
        txtEstadoFinal = findViewById(R.id.txtEstadoFinal);

        txtTempMax = findViewById(R.id.txtTempMax);
        txtTempMedia = findViewById(R.id.txtTempMedia);
        txtQtdAlertas = findViewById(R.id.txtQtdAlertas);
        txtQtdCriticos = findViewById(R.id.txtQtdCriticos);

        recyclerView = findViewById(R.id.recyclerEventosSessao);
    }

    private void buscarDetalhesNaApi(String id) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getSessaoPorId(id).enqueue(new Callback<SessaoDetalhesDTO>() {
            @Override
            public void onResponse(Call<SessaoDetalhesDTO> call, Response<SessaoDetalhesDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    atualizarTela(response.body());
                } else {
                    Toast.makeText(DetalhesSessaoActivity.this, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SessaoDetalhesDTO> call, Throwable t) {
                Toast.makeText(DetalhesSessaoActivity.this, "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarTela(SessaoDetalhesDTO sessao) {
        txtTituloSessao.setText("Resumo da Sessão");
        txtInicio.setText("Início: " + formatarHoraSemSegundos(sessao.getHorarioInicio()));
        txtFim.setText("Fim: " + formatarHoraSemSegundos(sessao.getHorarioFim()));

        long duracaoMin = (sessao.getDuracaoSegundos() != null) ? (sessao.getDuracaoSegundos() / 60) : 0;
        txtDuracao.setText("Duração: " + duracaoMin + " min");

        String estado = sessao.getEstadoFinal() != null ? sessao.getEstadoFinal() : "DESLIGADO";
        txtEstadoFinal.setText(estado.replace("FORNO_", ""));

        // Extrai dados da lista de temperaturas vinda do Spring Boot
        List<Float> valores = new ArrayList<>();
        List<String> horarios = new ArrayList<>();
        double somaTemp = 0;
        double maxTemp = 0;

        if (sessao.getTemperaturas() != null) {
            for (TemperaturaDTO t : sessao.getTemperaturas()) {
                if (t.getTemperaturaAtual() != null) {
                    float val = t.getTemperaturaAtual().floatValue();
                    valores.add(val);
                    horarios.add(t.getHorarioFormatado());

                    somaTemp += val;
                    if (val > maxTemp) maxTemp = val;
                }
            }
        }

        int qtdRegistros = valores.size();
        txtTempMax.setText("Máx: " + Math.round(maxTemp) + "°C");
        txtTempMedia.setText("Média: " + (qtdRegistros > 0 ? Math.round(somaTemp / qtdRegistros) : 0) + "°C");

        // Desenha o gráfico
        atualizarGrafico(valores, horarios);

        // Preenche eventos se existirem
        if (sessao.getEventos() != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new EventoSessaoAdapter(sessao.getEventos()));
            txtQtdAlertas.setText("Eventos: " + sessao.getEventos().size());
        }
    }

    private void atualizarGrafico(List<Float> valores, List<String> horarios) {
        if (valores == null || valores.isEmpty() || valores.size() != horarios.size()) {
            chart.setNoDataText("Nenhum dado de histórico encontrado para esta sessão.");
            chart.clear();
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> horariosFormatados = new ArrayList<>();

        for (int i = 0; i < valores.size(); i++) {
            entries.add(new Entry(i, valores.get(i)));

            // Pegamos a hora e cortamos para "HH:mm" igual ao seu gráfico de referência
            String horaCompleta = horarios.get(i);
            String horaSemSegundos = (horaCompleta != null && horaCompleta.length() >= 5)
                    ? horaCompleta.substring(0, 5)
                    : horaCompleta;

            horariosFormatados.add(horaSemSegundos);
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
        eixoX.setValueFormatter(new IndexAxisValueFormatter(horariosFormatados));
        eixoX.setTextColor(Color.WHITE);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);

        // Remove a legenda do gráfico (bolinha com texto "Histórico")
        chart.getLegend().setEnabled(false);

        // Remove descrições extras do gráfico
        chart.getDescription().setEnabled(false);

        // Atualiza a tela
        chart.invalidate();
    }

    private String formatarHoraSemSegundos(String dataHora) {
        if (dataHora == null) return "--:--";
        if (dataHora.contains("T")) {
            dataHora = dataHora.split("T")[1];
        }
        return dataHora.length() >= 5 ? dataHora.substring(0, 5) : dataHora;
    }
}