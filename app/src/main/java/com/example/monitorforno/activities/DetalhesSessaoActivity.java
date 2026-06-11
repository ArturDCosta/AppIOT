package com.example.monitorforno.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.adapters.EventoSessaoAdapter;
import com.example.monitorforno.models.EventoSessao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import com.example.monitorforno.R;

public class DetalhesSessaoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_detalhes_sessao
        );

        configurarGrafico();

        configurarEventos();
    }

    private void configurarGrafico() {

        LineChart chart =
                findViewById(R.id.chartSessao);

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
                        "Temperatura"
                );

        dataSet.setDrawValues(false);

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

        chart.getLegend().setEnabled(false);

        chart.getAxisRight().setEnabled(false);

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