package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.example.monitorforno.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;


public class TemperaturaFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_temperatura,
                container,
                false
        );

        LineChart chart = view.findViewById(R.id.chartTemperatura);

        configurarGrafico(chart);

        ImageView btnVoltar =
                view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        return view;
    }

    private void configurarGrafico(LineChart chart) {

        ArrayList<Entry> temperaturas = new ArrayList<>();

        temperaturas.add(new Entry(0,120));
        temperaturas.add(new Entry(1,130));
        temperaturas.add(new Entry(2,145));
        temperaturas.add(new Entry(3,160));
        temperaturas.add(new Entry(4,175));
        temperaturas.add(new Entry(5,185));
        temperaturas.add(new Entry(6,190));

        ArrayList<String> horarios = new ArrayList<>();

        horarios.add("18:00");
        horarios.add("18:05");
        horarios.add("18:10");
        horarios.add("18:15");
        horarios.add("18:20");
        horarios.add("18:25");
        horarios.add("18:30");
        horarios.add("18:35");


        LineDataSet dataSet =
                new LineDataSet(temperaturas, "Temperatura");

        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setDrawValues(true);

        chart.getAxisRight().setEnabled(false);

        // Valores dos pontos
        dataSet.setValueTextColor(Color.WHITE);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);

        XAxis eixoX = chart.getXAxis();

        eixoX.setValueFormatter(
                new IndexAxisValueFormatter(horarios)
        );

        eixoX.setGranularity(1f);

        eixoX.setPosition(XAxis.XAxisPosition.BOTTOM);

        eixoX.setLabelCount(horarios.size());

        // Eixo X
        chart.getXAxis().setTextColor(Color.WHITE);

        // Eixo Y esquerdo
        chart.getAxisLeft().setTextColor(Color.WHITE);

        // Eixo Y direito
        chart.getAxisRight().setTextColor(Color.WHITE);

        // Legenda
        chart.getLegend().setTextColor(Color.WHITE);

        chart.getDescription().setEnabled(false);

        //grafico limpo
        chart.getAxisLeft().setDrawGridLines(true);

        chart.getXAxis().setDrawGridLines(false);

        chart.invalidate();
    }
}
