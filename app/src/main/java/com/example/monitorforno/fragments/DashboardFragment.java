package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.models.Evento;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.adapters.EventoAdapter;
import com.example.monitorforno.utils.CustomDivisor;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.example.monitorforno.fragments.TemperaturaFragment;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_dashboard,
                container,
                false
        );

        MaterialCardView cardEstadoForno =
                view.findViewById(R.id.cardEstadoForno);

        MaterialCardView cardTemperaturaAtual =
                view.findViewById(R.id.cardTemperaturaAtual);

        MaterialCardView cardUltimaTemperatura =
                view.findViewById(R.id.cardUltimaTemperatura);

        String estado = "FORNO_AQUECENDO";

        switch (estado) {

            case "FORNO_AQUECENDO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_aquecendo)
                );
                break;

            case "FORNO_DESLIGADO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_desligado)
                );
                break;

            case "FORNO_ATIVO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_ativo)
                );
                break;

            case "FORNO_ESFRIANDO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_esfriando)
                );
                break;
        }


        //RecyclerView
        RecyclerView recyclerAlertas =
                view.findViewById(R.id.recyclerAlertas);

        List<Evento> eventos = new ArrayList<>();

        eventos.add(
                new Evento(
                        "Sistema entrou em alerta",
                        "18:30"
                )
        );

        eventos.add(
                new Evento(
                        "Estado crítico",
                        "18:20"
                )
        );

        eventos.add(
                new Evento(
                        "Sistema normalizado",
                        "18:05"
                )
        );

        recyclerAlertas.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        recyclerAlertas.addItemDecoration(
                new CustomDivisor(getContext())
        );

        recyclerAlertas.setAdapter(
                new EventoAdapter(eventos)
        );

        cardTemperaturaAtual.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new TemperaturaFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        cardUltimaTemperatura.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            new TemperaturaFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}