package com.example.monitorforno.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.adapters.EventoSessaoAdapter;
import com.example.monitorforno.models.EventoSessao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.monitorforno.R;
import com.github.mikephil.charting.components.LimitLine;

public class DetalhesSessaoActivity extends AppCompatActivity {

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_detalhes_sessao
        );

        TextView txtTituloSessao =
                findViewById(R.id.txtTituloSessao);

        TextView txtInicio =
                findViewById(R.id.txtInicio);

        TextView txtFim =
                findViewById(R.id.txtFim);

        TextView txtDuracao =
                findViewById(R.id.txtDuracao);

        TextView txtEstadoFinal =
                findViewById(R.id.txtEstadoFinal);

        chart = findViewById(R.id.chartSessao);

        String data =
                getIntent().getStringExtra("dataSessao");

        String horario =
                getIntent().getStringExtra("horarioSessao");

        String duracao =
                getIntent().getStringExtra("duracaoSessao");

        String estado =
                getIntent().getStringExtra("estadoSessao");

        txtTituloSessao.setText(
                "Sessão " + data
        );

        txtInicio.setText(
                "Início: 14:31"
        );

        txtFim.setText(
                "Fim: 15:42"
        );

        txtDuracao.setText(
                "Duração: " + duracao
        );

        txtEstadoFinal.setText(
                estado
        );

        switch (estado) {

            case "FORNO_ATIVO":
                txtEstadoFinal.setText("Forno Ativo\n");
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
        }

        ImageView btnVoltar =
                findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(
                v -> finish()
        );

        TextView txtTempMax =
                findViewById(R.id.txtTempMax);

        TextView txtTempMedia =
                findViewById(R.id.txtTempMedia);

        TextView txtQtdAlertas =
                findViewById(R.id.txtQtdAlertas);

        TextView txtQtdCriticos =
                findViewById(R.id.txtQtdCriticos);

        txtTempMax.setText(
                "Máx: 215°C"
        );

        txtTempMedia.setText(
                "Média: 187°C"
        );

        txtQtdAlertas.setText(
                "Alertas: 3"
        );

        txtQtdCriticos.setText(
                "Críticos: 1"
        );

        List<Float> temperaturasMock = Arrays.asList(
                120f, 135f, 150f, 170f, 185f, 190f
        );
        List<String> horariosMock = Arrays.asList(
                "14:31", "14:40", "14:50", "15:00", "15:10", "15:20"
        );
        atualizarGrafico(temperaturasMock, horariosMock);

        configurarEventos();
    }

    // Método público — chamado quando a API responder
    public void atualizarGrafico(List<Float> valores, List<String> horarios) {

        if (valores == null || horarios == null) {
            Log.e("Grafico", "Dados nulos recebidos da API");
            return;
        }

        if (valores.size() != horarios.size()) {
            Log.e("Grafico", "Desalinhamento: "
                    + valores.size() + " valores / "
                    + horarios.size() + " horários");
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

    // Método privado — só monta o gráfico, não busca dados
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

        // Limpa limites anteriores antes de adicionar
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

    private void configurarEventos() {

        RecyclerView recyclerView =
                findViewById(
                        R.id.recyclerEventosSessao
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        List<EventoSessao> eventos =
                new ArrayList<>();

        eventos.add(
                new EventoSessao(
                        "ALERTA_ENTRADA"
                )
        );

        eventos.add(
                new EventoSessao(
                        "CRITICO_ENTRADA"
                )
        );

        eventos.add(
                new EventoSessao(
                        "CRITICO_SAIDA"
                )
        );

        EventoSessaoAdapter adapter =
                new EventoSessaoAdapter(eventos);

        recyclerView.setAdapter(adapter);
    }
}