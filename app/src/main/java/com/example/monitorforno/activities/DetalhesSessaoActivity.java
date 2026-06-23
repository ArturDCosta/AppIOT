package com.example.monitorforno.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.adapters.EventoSessaoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.EventoSessao;
import com.example.monitorforno.models.SessaoDetalhesDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import com.example.monitorforno.R;
import com.github.mikephil.charting.components.LimitLine;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalhesSessaoActivity extends AppCompatActivity {

    private LineChart chart;

    // Declaração das Views para atualizar dinamicamente após a resposta da API
    private TextView txtTituloSessao, txtInicio, txtFim, txtDuracao, txtEstadoFinal;
    private TextView txtTempMax, txtTempMedia, txtQtdAlertas, txtQtdCriticos;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_sessao);

        // 1. Inicialização dos componentes de interface
        txtTituloSessao = findViewById(R.id.txtTituloSessao);
        txtInicio = findViewById(R.id.txtInicio);
        txtFim = findViewById(R.id.txtFim);
        txtDuracao = findViewById(R.id.txtDuracao);
        txtEstadoFinal = findViewById(R.id.txtEstadoFinal);
        txtTempMax = findViewById(R.id.txtTempMax);
        txtTempMedia = findViewById(R.id.txtTempMedia);
        txtQtdAlertas = findViewById(R.id.txtQtdAlertas);
        txtQtdCriticos = findViewById(R.id.txtQtdCriticos);
        chart = findViewById(R.id.chartSessao);
        recyclerView = findViewById(R.id.recyclerEventosSessao);

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        // 2. Resgata o ID da sessão enviado pela tela anterior
        String sessaoId = getIntent().getStringExtra("SESSAO_ID");

        if (sessaoId != null) {
            buscarDadosDaSessao(sessaoId);
        } else {
            Toast.makeText(this, "Erro: ID da sessão não recebido.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void buscarDadosDaSessao(String id) {
        // O RetrofitClient injeta o token automaticamente via Interceptor por trás dos panos
        ApiService apiService = RetrofitClient.getApiService(this);

        apiService.getSessaoPorId(id).enqueue(new Callback<SessaoDetalhesDTO>() {
            @Override
            public void onResponse(@NonNull Call<SessaoDetalhesDTO> call, @NonNull Response<SessaoDetalhesDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessaoDetalhesDTO sessao = response.body();
                    atualizarInterfaceGrafica(sessao);
                } else {
                    Toast.makeText(DetalhesSessaoActivity.this, "Erro ao carregar sessão: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessaoDetalhesDTO> call, @NonNull Throwable t) {
                Toast.makeText(DetalhesSessaoActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarInterfaceGrafica(SessaoDetalhesDTO sessao) {
        // 1. Preenche textos principais da sessão
        txtTituloSessao.setText("Sessão " + (sessao.getData() != null ? sessao.getData() : ""));
        txtInicio.setText("Início: " + (sessao.getHorarioInicio() != null ? sessao.getHorarioInicio() : "--:--"));
        txtFim.setText("Fim: " + (sessao.getHorarioFim() != null ? sessao.getHorarioFim() : "--:--"));
        txtDuracao.setText("Duração: " + (sessao.getDuracao() != null ? sessao.getDuracao() : "0m"));

        // 2. Preenche métricas e telemetrias coletadas
        txtTempMax.setText("Máx: " + sessao.getTemperaturaMaxima() + "°C");
        txtTempMedia.setText("Média: " + sessao.getTemperaturaMedia() + "°C");
        txtQtdAlertas.setText("Alertas: " + sessao.getQuantidadeAlertas());
        txtQtdCriticos.setText("Críticos: " + sessao.getQuantidadeCriticos());

        // 3. Altera as cores baseadas no estado da sessão (reaproveitando sua lógica original)
        String estado = sessao.getEstadoFinal() != null ? sessao.getEstadoFinal() : "FORNO_DESLIGADO";
        switch (estado) {
            case "FORNO_ATIVO":
                txtEstadoFinal.setText("Forno Ativo");
                txtEstadoFinal.setTextColor(getResources().getColor(R.color.forno_ativo));
                break;
            case "FORNO_AQUECENDO":
                txtEstadoFinal.setText("Forno Aquecendo");
                txtEstadoFinal.setTextColor(getResources().getColor(R.color.forno_aquecendo));
                break;
            case "FORNO_ESFRIANDO":
                txtEstadoFinal.setText("Forno Esfriando");
                txtEstadoFinal.setTextColor(getResources().getColor(R.color.forno_esfriando));
                break;
            default:
                txtEstadoFinal.setText("Forno Desligado");
                txtEstadoFinal.setTextColor(getResources().getColor(R.color.forno_desligado));
                break;
        }

        // 4. Atualiza o gráfico com as listas de telemetria vindas da API
        // Certifique-se de que o seu SessaoDetalhesDTO possua esses métodos retornando List<Float> e List<String>
        atualizarGrafico(sessao.getTemperaturasLista(), sessao.getHorariosLista());

        // 5. Configura a lista de eventos/alertas ocorridos nessa sessão específica
        if (sessao.getEventos() != null) {
            configurarEventos(sessao.getEventos());
        }
    }

    public void atualizarGrafico(List<Float> valores, List<String> horarios) {
        if (valores == null || horarios == null) {
            Log.e("Grafico", "Dados nulos recebidos da API");
            return;
        }
        if (valores.size() != horarios.size()) {
            Log.e("Grafico", "Desalinhamento: " + valores.size() + " valores / " + horarios.size() + " horários");
            return;
        }
        if (valores.isEmpty()) {
            Log.w("Grafico", "Sessão sem dados de temperatura");
            chart.clear();
            chart.invalidate();
            return;
        }
        configurarGrafico(valores, horarios);
    }

    private void configurarGrafico(List<Float> valores, List<String> horarios) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            entries.add(new Entry(i, valores.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperatura (C°)");
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.parseColor("#fc9403"));
        dataSet.setCircleColor(Color.parseColor("#fc9403"));

        XAxis eixoX = chart.getXAxis();
        eixoX.setPosition(XAxis.XAxisPosition.BOTTOM);
        eixoX.setGranularity(1f);
        eixoX.setLabelCount(horarios.size(), true);
        eixoX.setValueFormatter(new IndexAxisValueFormatter(horarios));
        eixoX.setTextColor(Color.WHITE);
        eixoX.setTextSize(10f);
        eixoX.setDrawGridLines(false);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setAxisMinimum(100f);
        chart.getAxisLeft().setAxisMaximum(250f);

        chart.getAxisLeft().removeAllLimitLines();
        LimitLine limiteCritico = new LimitLine(220f, "Limite Crítico");
        limiteCritico.setLineColor(Color.parseColor("#e85f5f"));
        limiteCritico.setLineWidth(2f);
        limiteCritico.setTextColor(Color.parseColor("#e85f5f"));
        limiteCritico.setTextSize(12f);
        chart.getAxisLeft().addLimitLine(limiteCritico);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setEnabled(true);
        chart.getDescription().setEnabled(false);

        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private void configurarEventos(List<EventoSessao> listaDeEventosDaApi) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Alimenta o adapter diretamente com os eventos retornados pela sua API
        EventoSessaoAdapter adapter = new EventoSessaoAdapter(listaDeEventosDaApi);
        recyclerView.setAdapter(adapter);
    }
}