package com.example.monitorforno.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.SessaoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.EstatisticasDTO;
import com.example.monitorforno.models.SessaoResumoDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment {

    private com.example.monitorforno.utils.SessionManager sessionManager;

    private RecyclerView recyclerView;
    private TextView txtTotalSessoes, txtTempoTotal, txtMaiorTemperatura;
    private MaterialButton btnOrdenar;
    private TextInputEditText edtPesquisa;

    private SessaoAdapter adapter;

    // AQUI: Atualizado para o DTO de Resumo
    private List<SessaoResumoDTO> listaOriginal = new ArrayList<>();
    private List<SessaoResumoDTO> listaExibida = new ArrayList<>();

    private boolean ordenadoPorMaisRecentes = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        // Inicialize o session manager
        sessionManager = new com.example.monitorforno.utils.SessionManager(requireContext());
        inicializarViews(view);
        configurarEventos();

        // Mantém a busca de sessões/tempo
        buscarSessoesNaApi();

        // NOVA CHAMADA: Busca a temperatura máxima
        buscarTemperaturaMaximaNaApi();

        return view;
    }

    private void inicializarViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        txtTotalSessoes = view.findViewById(R.id.txtTotalSessoes);
        txtTempoTotal = view.findViewById(R.id.txtTempoTotal);
        txtMaiorTemperatura = view.findViewById(R.id.txtMaiorTemperatura);

        btnOrdenar = view.findViewById(R.id.btnOrdenar);
        edtPesquisa = view.findViewById(R.id.edtPesquisa);
    }

    private void configurarEventos() {
        btnOrdenar.setOnClickListener(v -> alternarOrdenacao());

        edtPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarSessoes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtPesquisa.setOnLongClickListener(v -> {
            abrirSeletorDeData();
            return true;
        });
    }

    private void buscarSessoesNaApi() {
        if (getContext() == null) return;
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.minhasSessoes().enqueue(new Callback<List<SessaoResumoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<SessaoResumoDTO>> call, @NonNull Response<List<SessaoResumoDTO>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginal = response.body();
                    listaExibida = new ArrayList<>(listaOriginal);
                    aplicarOrdenacaoNaLista();
                    adapter = new SessaoAdapter(listaExibida);
                    recyclerView.setAdapter(adapter);
                    calcularEstatisticasGlobais(listaOriginal);
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SessaoResumoDTO>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void alternarOrdenacao() {
        if (listaExibida.isEmpty()) return;

        ordenadoPorMaisRecentes = !ordenadoPorMaisRecentes;
        btnOrdenar.setText(ordenadoPorMaisRecentes ? "Mais recentes" : "Mais antigos");

        aplicarOrdenacaoNaLista();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void aplicarOrdenacaoNaLista() {
        Collections.sort(listaExibida, (sessao1, sessao2) -> {
            String data1 = sessao1.getInicioSessao() != null ? sessao1.getInicioSessao() : "";
            String data2 = sessao2.getInicioSessao() != null ? sessao2.getInicioSessao() : "";

            return ordenadoPorMaisRecentes ? data2.compareTo(data1) : data1.compareTo(data2);
        });
    }

    private void filtrarSessoes(String texto) {
        listaExibida.clear();

        if (texto == null || texto.trim().isEmpty()) {
            listaExibida.addAll(listaOriginal);
        } else {
            String busca = texto.toLowerCase().trim();

            for (SessaoResumoDTO sessao : listaOriginal) {
                String estado = sessao.getEstadoFornoFinal() != null ? sessao.getEstadoFornoFinal().toLowerCase() : "";
                String inicioISO = sessao.getInicioSessao() != null ? sessao.getInicioSessao() : "";

                String dataBR = converterIsoParaBr(inicioISO);

                if (estado.contains(busca) || inicioISO.contains(busca) || dataBR.contains(busca)) {
                    listaExibida.add(sessao);
                }
            }
        }

        aplicarOrdenacaoNaLista();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private String converterIsoParaBr(String iso) {
        if (iso == null || !iso.contains("T")) return "";
        try {
            String dataLimpa = iso.split("\\.")[0];
            if (!dataLimpa.endsWith("Z")) dataLimpa += "Z";

            java.text.SimpleDateFormat sdfEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
            sdfEntrada.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date dataObj = sdfEntrada.parse(dataLimpa);

            java.text.SimpleDateFormat sdfSaida = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            sdfSaida.setTimeZone(java.util.TimeZone.getDefault());
            return sdfSaida.format(dataObj);
        } catch (Exception ignored) {}
        return "";
    }

    private void abrirSeletorDeData() {
        if (getContext() == null) return;

        Calendar calc = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, ano, mes, dia) -> {
            String dataEscolhida = String.format("%02d/%02d/%04d", dia, (mes + 1), ano);
            edtPesquisa.setText(dataEscolhida);
        }, calc.get(Calendar.YEAR), calc.get(Calendar.MONTH), calc.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void calcularEstatisticasGlobais(List<SessaoResumoDTO> sessoes) {
        if (sessoes == null || sessoes.isEmpty()) {
            txtTotalSessoes.setText("Total de Sessões: 0");
            txtTempoTotal.setText("Tempo Total: 0s");
            return;
        }

        int totalSessoes = sessoes.size();
        long tempoTotalSegundos = 0;

        for (SessaoResumoDTO sessao : sessoes) {
            if (sessao.getInicioSessao() != null && sessao.getFimSessao() != null) {
                try {
                    String inicioLimpo = sessao.getInicioSessao().split("\\.")[0];
                    if (!inicioLimpo.endsWith("Z")) inicioLimpo += "Z";
                    String fimLimpo = sessao.getFimSessao().split("\\.")[0];
                    if (!fimLimpo.endsWith("Z")) fimLimpo += "Z";

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    java.util.Date dataInicio = sdf.parse(inicioLimpo);
                    java.util.Date dataFim = sdf.parse(fimLimpo);

                    if (dataInicio != null && dataFim != null) {
                        tempoTotalSegundos += Math.max(0, (dataFim.getTime() - dataInicio.getTime()) / 1000);
                    }
                } catch (Exception ignored) {}
            }
        }

        long horas = tempoTotalSegundos / 3600;
        long minutos = (tempoTotalSegundos % 3600) / 60;
        long segundos = tempoTotalSegundos % 60;

        txtTotalSessoes.setText("Total de Sessões: " + totalSessoes);

        if (horas > 0) {
            txtTempoTotal.setText("Tempo Total: " + horas + "h " + minutos + "m");
        } else if (minutos > 0) {
            txtTempoTotal.setText("Tempo Total: " + minutos + "m " + segundos + "s");
        } else {
            txtTempoTotal.setText("Tempo Total: " + segundos + "s");
        }
    }

    private void buscarTemperaturaMaximaNaApi() {
        String fornoId = sessionManager.getFornoSelecionadoId();

        // 1. Verifica se o ID foi pego do SessionManager
        if (fornoId == null || fornoId.isEmpty()) {
            txtMaiorTemperatura.setText("Erro: ID do forno vazio");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.getEstatisticasGlobais(fornoId).enqueue(new Callback<EstatisticasDTO>() {
            @Override
            public void onResponse(@NonNull Call<EstatisticasDTO> call, @NonNull Response<EstatisticasDTO> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    double maxTemp = response.body().getTemperaturaMaxima();
                    txtMaiorTemperatura.setText("Maior Temperatura: " + Math.round(maxTemp) + "°C");
                } else {
                    // 2. Mostra o código de erro exato da API
                    txtMaiorTemperatura.setText("Erro API: " + response.code());
                    Toast.makeText(getContext(), "Erro Temp: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EstatisticasDTO> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    // 3. Mostra se houve falha de conexão
                    txtMaiorTemperatura.setText("Falha de conexão");
                    Toast.makeText(getContext(), "Falha Temp: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}