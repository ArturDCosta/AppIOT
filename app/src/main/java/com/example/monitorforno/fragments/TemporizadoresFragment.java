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
import com.example.monitorforno.models.TemporizadorRequestDTO;
import com.example.monitorforno.models.TemporizadorResponseDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemporizadoresFragment extends Fragment implements TemporizadorAdapter.OnTemporizadorInteractionListener {

    private final List<TemporizadorResponseDTO> temporizadores = new ArrayList<>();
    private TemporizadorAdapter adapter;

    private String horaInicioSelecionada;
    private String horaFimSelecionada;

    private int ano, mes, dia;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temporizadores, container, false);

        SessionManager sessionManager = new SessionManager(requireContext());

        MaterialButton btnSelecionarInicio = view.findViewById(R.id.btnSelecionarInicio);
        MaterialButton btnSelecionarFim = view.findViewById(R.id.btnSelecionarFim);
        MaterialButton btnCriarTemporizador = view.findViewById(R.id.btnCriarTemporizador);

        TextView txtInicioSelecionado = view.findViewById(R.id.txtInicioSelecionado);
        TextView txtFimSelecionado = view.findViewById(R.id.txtFimSelecionado);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerTemporizadores);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TemporizadorAdapter(temporizadores, this);
        recyclerView.setAdapter(adapter);

        Calendar c = Calendar.getInstance();
        ano = c.get(Calendar.YEAR);
        mes = c.get(Calendar.MONTH);
        dia = c.get(Calendar.DAY_OF_MONTH);

        btnSelecionarInicio.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                horaInicioSelecionada = String.format("%02d:%02d:00", hourOfDay, minute);
                txtInicioSelecionado.setText("Início: " + horaInicioSelecionada.substring(0,5));
            }, 12, 0, true);
            dialog.show();
        });

        btnSelecionarFim.setOnClickListener(v -> {
            int anoAtual = c.get(Calendar.YEAR);
            int mesAtual = c.get(Calendar.MONTH);
            int diaAtual = c.get(Calendar.DAY_OF_MONTH);

            // 1. Abre primeiro o Calendário (DatePicker)
            new android.app.DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {

                // Salva a data escolhida nas variáveis globais
                ano = year;
                mes = month + 1; // No Java, Janeiro é 0, então somamos 1
                dia = dayOfMonth;

                // 2. Quando escolher o dia, abre imediatamente o Relógio (TimePicker)
                new android.app.TimePickerDialog(getContext(), (view2, hourOfDay, minute) -> {

                    horaFimSelecionada = String.format("%02d:%02d:00", hourOfDay, minute);

                    // Mostra na tela a data e hora formatada (Ex: Fim: 25/12/2026 às 14:30)
                    String textoExibicao = String.format("Fim: %02d/%02d/%04d às %02d:%02d", dia, mes, ano, hourOfDay, minute);
                    txtFimSelecionado.setText(textoExibicao);

                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();

            }, anoAtual, mesAtual, diaAtual).show();
        });

        btnCriarTemporizador.setOnClickListener(v -> {
            if (horaFimSelecionada == null) {
                Toast.makeText(getContext(), "Por favor, selecione ao menos o horário de fim", Toast.LENGTH_SHORT).show();
                return;
            }

            // Puxa o ID do forno salvo na sessão
            String fornoId = sessionManager.getFornoSelecionadoId();

            // Verifica se o ID realmente existe (se o usuário selecionou um forno no dashboard)
            if (fornoId == null || fornoId.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum forno selecionado. Volte ao Dashboard e selecione um forno.", Toast.LENGTH_LONG).show();
                return;
            }

            String isoHorarioFim = String.format("%04d-%02d-%02dT%s", ano, mes, dia, horaFimSelecionada);
            TemporizadorRequestDTO request = new TemporizadorRequestDTO(isoHorarioFim);

            // Chamando a API passando o fornoId real
            RetrofitClient.getApiService(getContext()).criarTemporizador(fornoId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Temporizador Criado!", Toast.LENGTH_SHORT).show();

                        carregarTemporizadoresDaApi();

                        horaInicioSelecionada = null;
                        horaFimSelecionada = null;
                        txtInicioSelecionado.setText("Início: Não selecionado");
                        txtFimSelecionado.setText("Fim: Não selecionado");
                    } else {
                        Toast.makeText(getContext(), "Erro ao salvar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        carregarTemporizadoresDaApi();

        return view;
    }

    private void carregarTemporizadoresDaApi() {
        // Chamando a API no padrão do seu projeto
        RetrofitClient.getApiService(getContext()).getTemporizadores().enqueue(new Callback<List<TemporizadorResponseDTO>>() {
            @Override
            public void onResponse(Call<List<TemporizadorResponseDTO>> call, Response<List<TemporizadorResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    temporizadores.clear();
                    temporizadores.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<TemporizadorResponseDTO>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erro ao buscar temporizadores", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onTemporizadorRemovido(String id, int position) {
        if (id == null) return;

        // Chamando a API no padrão do seu projeto
        RetrofitClient.getApiService(getContext()).deletarTemporizador(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    temporizadores.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, temporizadores.size());
                    Toast.makeText(getContext(), "Temporizador removido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erro ao remover", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}