package com.example.monitorforno.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.EventoAdapter;
import com.example.monitorforno.models.Evento;

import java.util.ArrayList;
import java.util.List;

public class AlertasFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_alertas,
                container,
                false
        );

        TextView txtTotalAlertas =
                view.findViewById(R.id.txtTotalAlertas);

        TextView txtTotalCriticos =
                view.findViewById(R.id.txtTotalCriticos);

        TextView txtUltimoEvento =
                view.findViewById(R.id.txtUltimoEvento);

        txtTotalAlertas.setText(
                "Total de Alertas: 4"
        );

        txtTotalCriticos.setText(
                "Eventos Críticos: 2"
        );

        txtUltimoEvento.setText(
                "Último Evento: 14:45"
        );

        RecyclerView recyclerAlertas =
                view.findViewById(R.id.recyclerAlertas);

        recyclerAlertas.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        List<Evento> eventos =
                new ArrayList<>();

        eventos.add(
                new Evento(
                        "ALERTA_ENTRADA",
                        "14:18"
                )
        );

        eventos.add(
                new Evento(
                        "CRITICO_ENTRADA",
                        "14:32"
                )
        );

        eventos.add(
                new Evento(
                        "CRITICO_SAIDA",
                        "14:40"
                )
        );

        eventos.add(
                new Evento(
                        "ALERTA_SAIDA",
                        "14:45"
                )
        );

        EventoAdapter adapter =
                new EventoAdapter(eventos);

        recyclerAlertas.setAdapter(adapter);

        return view;
    }
}