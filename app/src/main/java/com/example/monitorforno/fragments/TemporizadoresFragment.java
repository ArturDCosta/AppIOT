
package com.example.monitorforno.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemporizadoresFragment extends Fragment
        implements TemporizadorAdapter.OnTemporizadorInteractionListener {

    private final List<TemporizadorResponseDTO> temporizadores = new ArrayList<>();
    private TemporizadorAdapter adapter;

    private String horaInicioSelecionada;
    private String horaFimSelecionada;

    // Calendar.MONTH utiliza Janeiro = 0 e Dezembro = 11.
    private int ano;
    private int mes;
    private int dia;

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

        // Guarda a data atual inicialmente.
        Calendar calendarioInicial = Calendar.getInstance();
        ano = calendarioInicial.get(Calendar.YEAR);
        mes = calendarioInicial.get(Calendar.MONTH);
        dia = calendarioInicial.get(Calendar.DAY_OF_MONTH);

        // SELECIONAR HORÁRIO DE INÍCIO
        btnSelecionarInicio.setOnClickListener(v -> {

            Calendar agora = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(
                    getContext(),
                    (view1, hourOfDay, minute) -> {

                        horaInicioSelecionada = String.format(
                                Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute
                        );

                        txtInicioSelecionado.setText("Início: " + horaInicioSelecionada.substring(0, 5));
                    },
                    agora.get(Calendar.HOUR_OF_DAY),
                    agora.get(Calendar.MINUTE),
                    true
            );

            dialog.show();
        });

        // SELECIONAR DATA/HORÁRIO DE FIM
        btnSelecionarFim.setOnClickListener(v -> {

            // Cria um Calendar novo para evitar utilizar uma data antiga.
            Calendar agora = Calendar.getInstance();

            int anoAtual = agora.get(Calendar.YEAR);
            int mesAtual = agora.get(Calendar.MONTH);
            int diaAtual = agora.get(Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(
                    getContext(),
                    (view1, year, month, dayOfMonth) -> {

                        ano = year;
                        mes = month;
                        dia = dayOfMonth;

                        Calendar horarioInicial = Calendar.getInstance();

                        new android.app.TimePickerDialog(
                                getContext(),
                                (view2, hourOfDay, minute) -> {

                                    horaFimSelecionada = String.format(
                                            Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute
                                    );

                                    String textoExibicao = String.format(
                                            Locale.getDefault(),
                                            "Fim: %02d/%02d/%04d às %02d:%02d",
                                            dia, mes + 1, ano, hourOfDay, minute
                                    );

                                    txtFimSelecionado.setText(textoExibicao);
                                },
                                horarioInicial.get(Calendar.HOUR_OF_DAY),
                                horarioInicial.get(Calendar.MINUTE),
                                true
                        ).show();

                    },
                    anoAtual, mesAtual, diaAtual
            ).show();
        });

        // CRIAR TEMPORIZADOR
        btnCriarTemporizador.setOnClickListener(v -> {

            if (horaFimSelecionada == null) {
                Toast.makeText(getContext(), "Por favor, selecione ao menos o horário de fim", Toast.LENGTH_SHORT).show();
                return;
            }

            String fornoId = sessionManager.getFornoSelecionadoId();

            if (fornoId == null || fornoId.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum forno selecionado.", Toast.LENGTH_LONG).show();
                return;
            }

            // O backend trabalha com Instant/UTC.
            SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdfIso.setTimeZone(TimeZone.getTimeZone("UTC"));

            String isoHorarioInicio;

            if (horaInicioSelecionada != null) {

                Calendar calInicio = Calendar.getInstance();

                int horaInicio = Integer.parseInt(horaInicioSelecionada.substring(0, 2));
                int minutoInicio = Integer.parseInt(horaInicioSelecionada.substring(3, 5));

                calInicio.set(ano, mes, dia, horaInicio, minutoInicio, 0);
                isoHorarioInicio = sdfIso.format(calInicio.getTime());

            } else {

                // Caso não escolha o início, utiliza o horário atual.
                isoHorarioInicio = sdfIso.format(new Date());
            }

            // HORÁRIO DE FIM
            int horaFim = Integer.parseInt(horaFimSelecionada.substring(0, 2));
            int minutoFim = Integer.parseInt(horaFimSelecionada.substring(3, 5));

            Calendar calFim = Calendar.getInstance();
            calFim.set(ano, mes, dia, horaFim, minutoFim, 0);

            String isoHorarioFim = sdfIso.format(calFim.getTime());

            // A validação do horário fica por conta do backend.
            TemporizadorRequestDTO request = new TemporizadorRequestDTO(isoHorarioInicio, isoHorarioFim);

            RetrofitClient.getApiService(getContext())
                    .criarTemporizador(fornoId, request)
                    .enqueue(new Callback<Void>() {

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
                            Toast.makeText(getContext(), "Falha na rede", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ATUALIZAÇÃO AUTOMÁTICA
        runnableMonitor = new Runnable() {
            @Override
            public void run() {

                if (!isAdded()) return;

                // O aplicativo apenas consulta o backend.
                carregarTemporizadoresDaApi();

                handlerMonitor.postDelayed(this, 5000);
            }
        };

        // Primeira consulta.
        carregarTemporizadoresDaApi();

        return view;
    }

    // CICLO DE VIDA
    @Override
    public void onResume() {
        super.onResume();

        if (runnableMonitor != null) {
            handlerMonitor.postDelayed(runnableMonitor, 5000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (runnableMonitor != null) {
            handlerMonitor.removeCallbacks(runnableMonitor);
        }
    }

    // BUSCAR TEMPORIZADORES
    private void carregarTemporizadoresDaApi() {

        if (!isAdded()) return;

        SessionManager sessionManager = new SessionManager(requireContext());
        String fornoId = sessionManager.getFornoSelecionadoId();

        if (fornoId == null || fornoId.isEmpty()) return;

        RetrofitClient.getApiService(requireContext())
                .getTemporizadoresPorForno(fornoId)
                .enqueue(new Callback<List<TemporizadorResponseDTO>>() {

                    @Override
                    public void onResponse(Call<List<TemporizadorResponseDTO>> call, Response<List<TemporizadorResponseDTO>> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {

                            temporizadores.clear();

                            // O backend informa quais temporizadores já foram executados.
                            for (TemporizadorResponseDTO t : response.body()) {
                                if (!t.isExecutado()) {
                                    temporizadores.add(t);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            atualizarProximoTemporizador();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TemporizadorResponseDTO>> call, Throwable t) {
                        Log.e("TEMPORIZADOR", "Falha ao consultar temporizadores.");
                    }
                });
    }

    // MOSTRAR PRÓXIMO TEMPORIZADOR
    private void atualizarProximoTemporizador() {

        if (temporizadores.isEmpty()) {
            txtProximoTempo.setText("Sem temporizadores\nmarcados");
            txtProximoTempo.setTextSize(22f);
            return;
        }

        SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdfEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat formatoVisor = new SimpleDateFormat("HH:mm\ndd/MM/yyyy", Locale.getDefault());
        formatoVisor.setTimeZone(TimeZone.getDefault());

        Date proximaData = null;

        for (TemporizadorResponseDTO t : temporizadores) {

            try {
                String horarioFim = t.getHorarioFim();

                if (horarioFim == null || horarioFim.isEmpty()) continue;

                String dataLimpa = horarioFim;

                // Remove os milissegundos, se existirem.
                int ponto = dataLimpa.indexOf(".");
                if (ponto != -1) {
                    dataLimpa = dataLimpa.substring(0, ponto) + "Z";
                }

                // Garante que o horário seja interpretado como UTC.
                if (!dataLimpa.endsWith("Z")) {
                    dataLimpa += "Z";
                }

                Date dataFim = sdfEntrada.parse(dataLimpa);

                if (dataFim != null) {
                    if (proximaData == null || dataFim.before(proximaData)) {
                        proximaData = dataFim;
                    }
                }

            } catch (Exception e) {
                Log.e("TEMPORIZADOR", "Erro ao interpretar horário de término.");
            }
        }

        if (proximaData != null) {
            txtProximoTempo.setText(formatoVisor.format(proximaData));
            txtProximoTempo.setTextSize(36f);
        } else {
            txtProximoTempo.setText("Sem temporizadores\nmarcados");
            txtProximoTempo.setTextSize(22f);
        }
    }

    // EXCLUIR TEMPORIZADOR MANUALMENTE
    @Override
    public void onTemporizadorRemovido(String id) {

        if (id == null || id.isEmpty()) return;

        RetrofitClient.getApiService(requireContext())
                .deletarTemporizador(id)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            // Procura pelo ID atual na lista.
                            int posicaoAtual = -1;

                            for (int i = 0; i < temporizadores.size(); i++) {
                                if (id.equals(temporizadores.get(i).getId())) {
                                    posicaoAtual = i;
                                    break;
                                }
                            }

                            // Só remove se o temporizador ainda estiver na lista.
                            if (posicaoAtual != -1) {
                                temporizadores.remove(posicaoAtual);
                                adapter.notifyItemRemoved(posicaoAtual);
                            }

                            Toast.makeText(requireContext(), "Temporizador cancelado", Toast.LENGTH_SHORT).show();
                            atualizarProximoTemporizador();

                        } else {
                            Toast.makeText(requireContext(), "Erro ao cancelar temporizador", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(requireContext(), "Falha ao cancelar temporizador", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
