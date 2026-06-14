package com.example.monitorforno.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.TemporizadorAdapter;
import com.example.monitorforno.models.Temporizador;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TemporizadoresFragment extends Fragment {

    private final List<Temporizador> temporizadores =
            new ArrayList<>();

    private TemporizadorAdapter adapter;

    private String horarioSelecionado;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_temporizadores,
                container,
                false
        );

        MaterialButton btnSelecionarHorario =
                view.findViewById(R.id.btnSelecionarHorario);

        MaterialButton btnCriarTemporizador =
                view.findViewById(R.id.btnCriarTemporizador);

        TextView txtHorarioSelecionado =
                view.findViewById(R.id.txtHorarioSelecionado);

        TextView txtProximoHorario =
                view.findViewById(R.id.txtProximoHorario);

        TextView txtTempoRestante =
                view.findViewById(R.id.txtTempoRestante);

        RecyclerView recyclerTemporizadores =
                view.findViewById(R.id.recyclerTemporizadores);

        adapter = new TemporizadorAdapter(
                temporizadores,
                () -> {

                    if (temporizadores.isEmpty()) {

                        txtProximoHorario.setText(
                                "Nenhum"
                        );

                        txtTempoRestante.setText(
                                "Sem agendamentos"
                        );
                    }
                }
        );

        recyclerTemporizadores.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        recyclerTemporizadores.setAdapter(
                adapter
        );

        btnSelecionarHorario.setOnClickListener(v -> {

            TimePickerDialog dialog =
                    new TimePickerDialog(
                            getContext(),
                            (timePicker, hour, minute) -> {

                                horarioSelecionado =
                                        String.format(
                                                "%02d:%02d",
                                                hour,
                                                minute
                                        );

                                txtHorarioSelecionado.setText(
                                        "Horário selecionado: "
                                                + horarioSelecionado
                                );

                            },
                            22,
                            0,
                            true
                    );

            dialog.show();

        });

        btnCriarTemporizador.setOnClickListener(v -> {

            if (horarioSelecionado == null) {
                return;
            }

            temporizadores.add(
                    new Temporizador(
                            horarioSelecionado
                    )
            );

            txtProximoHorario.setText(
                    horarioSelecionado
            );

            txtTempoRestante.setText(
                    "Agendado"
            );

            adapter.notifyItemInserted(
                    temporizadores.size() - 1
            );

            Toast.makeText(
                    getContext(),
                    "Temporizador criado",
                    Toast.LENGTH_SHORT
            ).show();

            horarioSelecionado = null;

            txtHorarioSelecionado.setText(
                    "Nenhum horário selecionado"
            );

        });

        return view;
    }
}