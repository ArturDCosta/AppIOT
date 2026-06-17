package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.monitorforno.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        TextView txtEstadoForno = view.findViewById(R.id.txtEstadoForno);

        TextView txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);

        String estado = "FORNO_AQUECENDO";

        switch (estado){
            case "FORNO_AQUECENDO":
                txtEstadoForno.setText("AQUECENDO");
                txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_aquecendo));
                break;

            case "FORNO_DESLIGADO":
                txtEstadoForno.setText("DESLIGADO");
                txtEstadoForno.setTextColor(getResources().getColor(R.color.forno_desligado));
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
        
        String estadoSistema = "CRITICO";
        txtEstadoSistema.setText(estadoSistema);
        
        switch (estadoSistema){

            case "SEGURO":

                txtEstadoSistema.setTextColor(
                        getResources().getColor(R.color.alerta_verde)
                );
                break;

            case "ALERTA":

                txtEstadoSistema.setTextColor(
                        getResources().getColor(R.color.alerta_laranja)
                );
                break;

            case "CRITICO":

                txtEstadoSistema.setTextColor(
                        getResources().getColor(R.color.alerta_vermelho)
                );
                break;

            case "ERRO_SENSOR":

                txtEstadoSistema.setTextColor(Color.GRAY);
                break;
        }

        // Dados temporários até a API estar pronta
        List<Float> temperaturas = Arrays.asList(
                100f, 120f, 130f, 145f, 160f, 175f, 185f
        );
        List<String> horarios = Arrays.asList(
                "18:00", "18:05", "18:10", "18:15",
                "18:20", "18:25", "18:30"
        );

        LineChart chart = view.findViewById(R.id.chartTemperatura);
        configurarGrafico(chart, temperaturas, horarios);

        ImageView btnVoltar =
                view.findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        return view;
    }

    private void configurarGrafico(LineChart chart,
                                   List<Float> valores,
                                   List<String> horarios) {

        // Validação de contrato — falha rápido e visível
        if (valores == null || horarios == null) {
            Log.e("Grafico", "Dados nulos recebidos");
            return;
        }
        if (valores.size() != horarios.size()) {
            Log.e("Grafico", "Tamanhos incompatíveis: "
                    + valores.size() + " valores, "
                    + horarios.size() + " horários");
            return;
        }
        if (valores.isEmpty()) {
            Log.w("Grafico", "Lista de dados vazia");
            chart.clear();
            return;
        }

        // Construção dos pontos
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            entries.add(new Entry(i, valores.get(i)));
        }

        // Dataset
        LineDataSet dataSet = new LineDataSet(entries, "Temperatura");
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setColor(Color.parseColor("#fc9403"));
        dataSet.setCircleColor(Color.parseColor("#fc9403"));
        dataSet.setValueTextColor(Color.WHITE);

        // Eixo X
        XAxis eixoX = chart.getXAxis();
        eixoX.setPosition(XAxis.XAxisPosition.BOTTOM);
        eixoX.setGranularity(1f);
        eixoX.setLabelCount(horarios.size(), true);
        eixoX.setValueFormatter(new IndexAxisValueFormatter(horarios));
        eixoX.setTextColor(Color.WHITE);
        eixoX.setTextSize(10f);
        eixoX.setDrawGridLines(false);

        // Eixo Y esquerdo
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setAxisMinimum(100f);
        chart.getAxisLeft().setAxisMaximum(250f);

        // Linha de limite crítico
        LimitLine limiteCritico = new LimitLine(220f, "Limite Crítico");
        limiteCritico.setLineColor(Color.parseColor("#e85f5f"));
        limiteCritico.setLineWidth(2f);
        limiteCritico.setTextColor(Color.parseColor("#e85f5f"));
        limiteCritico.setTextSize(12f);
        chart.getAxisLeft().addLimitLine(limiteCritico);

        // Eixo Y direito — desabilitado
        chart.getAxisRight().setEnabled(false);

        // Legenda e descrição
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setEnabled(true);
        chart.getDescription().setEnabled(false);

        // Aplicar dados e renderizar
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }
}
