package com.example.monitorforno.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemporizadoresFragment extends Fragment implements TemporizadorAdapter.OnTemporizadorInteractionListener {

    private final List<TemporizadorResponseDTO> temporizadores = new ArrayList<>();
    private TemporizadorAdapter adapter;

    private String horaInicioSelecionada;
    private String horaFimSelecionada;
    private int ano, mes, dia;

    // Novos componentes para o destaque e auto-exclusão
    private View cardProximoTemporizador;
    private TextView txtProximoTempo;
    private final Handler handlerMonitor = new Handler();
    private Runnable runnableMonitor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temporizadores, container, false);

        SessionManager sessionManager = new SessionManager(requireContext());

        cardProximoTemporizador = view.findViewById(R.id.cardProximoTemporizador);
        txtProximoTempo = view.findViewById(R.id.txtProximoTempo);

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
                horaInicioSelecionada = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                txtInicioSelecionado.setText("Início: " + horaInicioSelecionada.substring(0,5));
            }, 12, 0, true);
            dialog.show();
        });

        btnSelecionarFim.setOnClickListener(v -> {
            int anoAtual = c.get(Calendar.YEAR);
            int mesAtual = c.get(Calendar.MONTH);
            int diaAtual = c.get(Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                ano = year;
                mes = month + 1;
                dia = dayOfMonth;

                new android.app.TimePickerDialog(getContext(), (view2, hourOfDay, minute) -> {
                    horaFimSelecionada = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                    String textoExibicao = String.format(Locale.getDefault(), "Fim: %02d/%02d/%04d às %02d:%02d", dia, mes, ano, hourOfDay, minute);
                    txtFimSelecionado.setText(textoExibicao);
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, anoAtual, mesAtual, diaAtual).show();
        });

        btnCriarTemporizador.setOnClickListener(v -> {
            if (horaFimSelecionada == null) {
                Toast.makeText(getContext(), "Por favor, selecione ao menos o horário de fim", Toast.LENGTH_SHORT).show();
                return;
            }

            String fornoId = sessionManager.getFornoSelecionadoId();
            if (fornoId == null || fornoId.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum forno selecionado. Volte ao Dashboard e selecione um forno.", Toast.LENGTH_LONG).show();
                return;
            }

            String isoHorarioFim = String.format(Locale.getDefault(), "%04d-%02d-%02dT%s", ano, mes, dia, horaFimSelecionada);
            TemporizadorRequestDTO request = new TemporizadorRequestDTO(isoHorarioFim);

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

        // Configura o Monitor que vai rodar de tempos em tempos
        runnableMonitor = new Runnable() {
            @Override
            public void run() {
                verificarTemporizadoresExpirados();
                handlerMonitor.postDelayed(this, 5000); // Roda a cada 5 segundos
            }
        };

        carregarTemporizadoresDaApi();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handlerMonitor.post(runnableMonitor); // Liga o monitor
    }

    @Override
    public void onPause() {
        super.onPause();
        handlerMonitor.removeCallbacks(runnableMonitor); // Desliga para economizar bateria
    }

    private void carregarTemporizadoresDaApi() {
        RetrofitClient.getApiService(getContext()).getTemporizadores().enqueue(new Callback<List<TemporizadorResponseDTO>>() {
            @Override
            public void onResponse(Call<List<TemporizadorResponseDTO>> call, Response<List<TemporizadorResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    temporizadores.clear();
                    temporizadores.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    // Logo após carregar, já checa quem é o próximo e exibe
                    verificarTemporizadoresExpirados();
                }
            }
            @Override
            public void onFailure(Call<List<TemporizadorResponseDTO>> call, Throwable t) {}
        });
    }

    private void verificarTemporizadoresExpirados() {
        if (temporizadores.isEmpty()) {
            txtProximoTempo.setText("Sem temporizadores\nmarcados");
            txtProximoTempo.setTextSize(22f); // Fonte um pouco menor para o texto caber bem
            return;
        }

        TemporizadorResponseDTO proximo = null;
        long menorTempoRestante = Long.MAX_VALUE;
        long agora = System.currentTimeMillis();

        SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        List<TemporizadorResponseDTO> listaExpirados = new ArrayList<>();

        for (TemporizadorResponseDTO t : temporizadores) {
            try {
                String horaFimStr = t.getHorarioFim();
                if (horaFimStr == null) continue;

                String dataLimpa = horaFimStr.split("\\.")[0];
                java.util.Date dataFim = sdfEntrada.parse(dataLimpa);

                if (dataFim != null) {
                    long tempoFim = dataFim.getTime();

                    if (tempoFim <= agora) {
                        listaExpirados.add(t);
                    } else {
                        long diff = tempoFim - agora;
                        if (diff < menorTempoRestante) {
                            menorTempoRestante = diff;
                            proximo = t;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Auto-exclusão silenciosa
        for (TemporizadorResponseDTO exp : listaExpirados) {
            temporizadores.remove(exp);
            excluirTemporizadorAutomaticamente(String.valueOf(exp.getId()));
        }

        if (!listaExpirados.isEmpty()) {
            adapter.notifyDataSetChanged();
        }

        // EXIBIR O PRÓXIMO OU A MENSAGEM VAZIA
        if (proximo != null) {
            try {
                String dataLimpa = proximo.getHorarioFim().split("\\.")[0];
                java.util.Date dataFimObj = sdfEntrada.parse(dataLimpa);

                SimpleDateFormat formatoVisor = new SimpleDateFormat("HH:mm\ndd/MM/yyyy", Locale.getDefault());
                txtProximoTempo.setText(formatoVisor.format(dataFimObj));
                txtProximoTempo.setTextSize(36f); // Fonte bem grande para a hora/data
            } catch (Exception e) {
                txtProximoTempo.setText(proximo.getHorarioFim());
            }
        } else {
            // Se existiam itens na lista, mas todos expiraram agora
            txtProximoTempo.setText("Sem temporizadores\nmarcados");
            txtProximoTempo.setTextSize(22f);
        }
    }

    private void excluirTemporizadorAutomaticamente(String id) {
        RetrofitClient.getApiService(getContext()).deletarTemporizador(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Excluído do banco silenciosamente
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    public void onTemporizadorRemovido(String id, int position) {
        if (id == null) return;

        RetrofitClient.getApiService(getContext()).deletarTemporizador(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    temporizadores.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, temporizadores.size());
                    Toast.makeText(getContext(), "Temporizador cancelado", Toast.LENGTH_SHORT).show();

                    // Recalcula o card grande caso o usuário tenha excluído justamente o próximo
                    verificarTemporizadoresExpirados();
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