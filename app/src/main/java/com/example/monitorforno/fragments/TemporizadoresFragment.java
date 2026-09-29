package com.example.monitorforno.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemporizadoresFragment extends Fragment implements TemporizadorAdapter.OnTemporizadorInteractionListener {

    private final List<TemporizadorResponseDTO> temporizadores = new ArrayList<>();
    private TemporizadorAdapter adapter;

    private String isoHorarioInicio;
    private String isoHorarioFim;

    private int anoIni, mesIni, diaIni;
    private int anoFim, mesFim, diaFim;

    private TextView txtProximoTempo;
    private TextView txtInicioSelecionado;
    private TextView txtFimSelecionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temporizadores, container, false);

        txtProximoTempo = view.findViewById(R.id.txtProximoTempo);
        txtInicioSelecionado = view.findViewById(R.id.txtInicioSelecionado);
        txtFimSelecionado = view.findViewById(R.id.txtFimSelecionado);

        MaterialButton btnSelecionarInicio = view.findViewById(R.id.btnSelecionarInicio);
        MaterialButton btnSelecionarFim = view.findViewById(R.id.btnSelecionarFim);
        MaterialButton btnCriarTemporizador = view.findViewById(R.id.btnCriarTemporizador);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerTemporizadores);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new TemporizadorAdapter(temporizadores, this);
            recyclerView.setAdapter(adapter);
        }

        Calendar c = Calendar.getInstance();

        // 1. SELEÇÃO DE INÍCIO
        if (btnSelecionarInicio != null) {
            btnSelecionarInicio.setOnClickListener(v -> {
                int anoAtual = c.get(Calendar.YEAR);
                int mesAtual = c.get(Calendar.MONTH);
                int diaAtual = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(requireContext(), (v1, year, month, dayOfMonth) -> {
                    anoIni = year;
                    mesIni = month + 1;
                    diaIni = dayOfMonth;

                    new TimePickerDialog(requireContext(), (v2, hourOfDay, minute) -> {
                        String horaStr = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        isoHorarioInicio = String.format(Locale.getDefault(), "%04d-%02d-%02dT%s", anoIni, mesIni, diaIni, horaStr);
                        if (txtInicioSelecionado != null) {
                            txtInicioSelecionado.setText(String.format(Locale.getDefault(), "Início: %02d/%02d %02d:%02d", diaIni, mesIni, hourOfDay, minute));
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                }, anoAtual, mesAtual, diaAtual).show();
            });
        }

        // 2. SELEÇÃO DE FIM
        if (btnSelecionarFim != null) {
            btnSelecionarFim.setOnClickListener(v -> {
                int anoAtual = c.get(Calendar.YEAR);
                int mesAtual = c.get(Calendar.MONTH);
                int diaAtual = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(requireContext(), (v1, year, month, dayOfMonth) -> {
                    anoFim = year;
                    mesFim = month + 1;
                    diaFim = dayOfMonth;

                    new TimePickerDialog(requireContext(), (v2, hourOfDay, minute) -> {
                        String horaStr = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        isoHorarioFim = String.format(Locale.getDefault(), "%04d-%02d-%02dT%s", anoFim, mesFim, diaFim, horaStr);
                        if (txtFimSelecionado != null) {
                            txtFimSelecionado.setText(String.format(Locale.getDefault(), "Fim: %02d/%02d %02d:%02d", diaFim, mesFim, hourOfDay, minute));
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
                }, anoAtual, mesAtual, diaAtual).show();
            });
        }

        // 3. ENVIO PARA A API
        if (btnCriarTemporizador != null) {
            btnCriarTemporizador.setOnClickListener(v -> {
                if (isoHorarioInicio == null || isoHorarioFim == null) {
                    Toast.makeText(getContext(), "Selecione o início e o fim.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SessionManager sessionManager = new SessionManager(requireContext());
                String fornoId = sessionManager.getFornoSelecionadoId();
                if (fornoId == null || fornoId.isEmpty()) {
                    Toast.makeText(getContext(), "Nenhum forno selecionado.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date dataInicio = sdf.parse(isoHorarioInicio);
                    Date dataFim = sdf.parse(isoHorarioFim);

                    if (dataInicio != null && dataFim != null && dataFim.before(dataInicio)) {
                        Toast.makeText(getContext(), "O término deve ser após o início!", Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TemporizadorRequestDTO request = new TemporizadorRequestDTO(isoHorarioInicio, isoHorarioFim);

                RetrofitClient.getApiService(requireContext()).criarTemporizador(fornoId, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Temporizador Criado!", Toast.LENGTH_SHORT).show();
                            isoHorarioInicio = null;
                            isoHorarioFim = null;
                            if (txtInicioSelecionado != null) txtInicioSelecionado.setText("Início: Não selecionado");
                            if (txtFimSelecionado != null) txtFimSelecionado.setText("Fim: Não selecionado");
                            carregarTemporizadoresDaApi();
                        } else {
                            Toast.makeText(getContext(), "Erro ao salvar: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Falha na rede", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        carregarTemporizadoresDaApi();
        return view;
    }

    private void carregarTemporizadoresDaApi() {
        if (!isAdded() || getContext() == null) return;

        String fornoId = new SessionManager(requireContext()).getFornoSelecionadoId();
        if (fornoId == null || fornoId.isEmpty()) return;

        RetrofitClient.getApiService(requireContext()).getTemporizadoresPorForno(fornoId).enqueue(new Callback<List<TemporizadorResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<TemporizadorResponseDTO>> call, @NonNull Response<List<TemporizadorResponseDTO>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    temporizadores.clear();
                    temporizadores.addAll(response.body());
                    if (adapter != null) adapter.notifyDataSetChanged();
                    atualizarVisorProximoTemporizador();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TemporizadorResponseDTO>> call, @NonNull Throwable t) {
                Log.e("API_ERRO", "Falha ao buscar temporizadores", t);
            }
        });
    }

    private void atualizarVisorProximoTemporizador() {
        if (txtProximoTempo == null) return;

        if (temporizadores.isEmpty()) {
            configurarVisorVazio();
            return;
        }

        TemporizadorResponseDTO proximo = null;
        long menorTempoRestante = Long.MAX_VALUE;
        long agora = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (TemporizadorResponseDTO t : temporizadores) {
            if (t.isExecutado()) continue;

            try {
                if (t.getHorarioFim() == null) continue;
                String dataLimpa = t.getHorarioFim().split("\\.")[0];
                Date dataFim = sdf.parse(dataLimpa);

                if (dataFim != null && dataFim.getTime() > agora) {
                    long diff = dataFim.getTime() - agora;
                    if (diff < menorTempoRestante) {
                        menorTempoRestante = diff;
                        proximo = t;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (proximo != null) {
            try {
                String dataLimpa = proximo.getHorarioFim().split("\\.")[0];
                Date dataFimObj = sdf.parse(dataLimpa);
                SimpleDateFormat formatoVisor = new SimpleDateFormat("HH:mm\ndd/MM/yyyy", Locale.getDefault());
                txtProximoTempo.setText(formatoVisor.format(dataFimObj));
                txtProximoTempo.setTextSize(36f);
            } catch (Exception e) {
                txtProximoTempo.setText(proximo.getHorarioFim());
            }
        } else {
            configurarVisorVazio();
        }
    }

    private void configurarVisorVazio() {
        if (txtProximoTempo != null) {
            txtProximoTempo.setText("Sem temporizadores\nativos");
            txtProximoTempo.setTextSize(22f);
        }
    }

    @Override
    public void onTemporizadorRemovido(String id, int position) {
        if (id == null || getContext() == null) return;

        RetrofitClient.getApiService(requireContext()).deletarTemporizador(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    temporizadores.remove(position);
                    if (adapter != null) {
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, temporizadores.size());
                    }
                    atualizarVisorProximoTemporizador();
                } else {
                    Toast.makeText(getContext(), "Erro ao remover", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}