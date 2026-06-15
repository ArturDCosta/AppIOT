package com.example.monitorforno.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.adapters.EventoSessaoAdapter;
import com.example.monitorforno.models.EventoSessao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.monitorforno.R;
import com.github.mikephil.charting.components.LimitLine;

public class DetalhesSessaoActivity extends AppCompatActivity {

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

        String estadoTexto;

        switch (estado) {

            case "FORNO_ATIVO":
                estadoTexto = "Forno Ativo";
                txtEstadoFinal.setTextColor(
                        Color.parseColor("#32ad34")
                );
                break;

            case "FORNO_AQUECENDO":
                estadoTexto = "Forno Aquecendo";
                txtEstadoFinal.setTextColor(
                        Color.parseColor("#fc9403")
                );
                break;

            case "FORNO_ESFRIANDO":
                estadoTexto = "Forno Esfriando";
                txtEstadoFinal.setTextColor(
                        Color.parseColor("#2426ab")
                );
                break;

            default:
                estadoTexto = "Forno Desligado";
                txtEstadoFinal.setTextColor(
                        Color.GRAY
                );
        }

        txtEstadoFinal.setText(estadoTexto);

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
                "Temperatura Máx: 215°C"
        );

        txtTempMedia.setText(
                "Temperatura Média: 187°C"
        );

        txtQtdAlertas.setText(
                "Alertas: 3"
        );

        txtQtdCriticos.setText(
                "Eventos Críticos: 1"
        );

        configurarGrafico();

        configurarEventos();
    }

    private void configurarGrafico() {

        LineChart chart =
                findViewById(R.id.chartSessao);

        final String[] horarios = {
                "14:31",
                "14:40",
                "14:50",
                "15:00",
                "15:10",
                "15:20"
        };

        ArrayList<Entry> temperaturas =
                new ArrayList<>();

        temperaturas.add(new Entry(0,120));
        temperaturas.add(new Entry(1,135));
        temperaturas.add(new Entry(2,150));
        temperaturas.add(new Entry(3,170));
        temperaturas.add(new Entry(4,185));
        temperaturas.add(new Entry(5,190));

        LineDataSet dataSet =
                new LineDataSet(
                        temperaturas,
                        "Temperatura (C°)"
                );

        dataSet.setDrawValues(false);

        chart.getXAxis().setTextColor(Color.WHITE);

        chart.getXAxis().setGranularity(1f);

        chart.getXAxis().setValueFormatter(
                new ValueFormatter() {

                    @Override
                    public String getFormattedValue(float value) {

                        int index = (int) value;

                        if(index >= 0 &&
                                index < horarios.length) {

                            return horarios[index];
                        }

                        return "";
                    }
                }
        );

        chart.getXAxis().setTextSize(10f);

        chart.getAxisLeft().setTextSize(10f);

        chart.getAxisLeft().setTextColor(Color.WHITE);

        chart.getLegend().setTextColor(Color.WHITE);

        dataSet.setLineWidth(3f);

        dataSet.setMode(
                LineDataSet.Mode.CUBIC_BEZIER
        );

        dataSet.setColor(
                Color.parseColor("#fc9403")
        );

        dataSet.setCircleColor(
                Color.parseColor("#fc9403")
        );

        chart.getDescription().setEnabled(false);

        chart.getLegend().setEnabled(true);

        chart.getAxisRight().setEnabled(false);

        LimitLine limiteCritico =
                new LimitLine(
                        220f,
                        "Limite Crítico"
                );

        limiteCritico.setLineColor(
                Color.parseColor("#e85f5f")
        );

        limiteCritico.setLineWidth(2f);

        limiteCritico.setTextColor(
                Color.parseColor("#e85f5f")
        );

        chart.getAxisLeft()
                .addLimitLine(
                        limiteCritico
                );

        chart.getAxisLeft().setAxisMinimum(100f);

        chart.getAxisLeft().setAxisMaximum(250f);

        chart.getAxisLeft().setDrawGridLines(true);

        chart.getXAxis().setDrawGridLines(false);

        limiteCritico.setTextSize(12f);

        chart.setData(
                new LineData(dataSet)
        );

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