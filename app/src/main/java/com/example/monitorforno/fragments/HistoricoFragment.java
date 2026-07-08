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
import com.example.monitorforno.models.SessaoDetalhesDTO;
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

    private RecyclerView recyclerView;
    private TextView txtTotalSessoes, txtTempoTotal, txtMaiorTemperatura;
    private MaterialButton btnOrdenar;
    private TextInputEditText edtPesquisa;

    private SessaoAdapter adapter;

    private List<SessaoDetalhesDTO> listaOriginal = new ArrayList<>();
    private List<SessaoDetalhesDTO> listaExibida = new ArrayList<>();

    private boolean ordenadoPorMaisRecentes = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        inicializarViews(view);
        configurarEventos();
        buscarSessoesNaApi();

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
        // Alternar ordenação
        btnOrdenar.setOnClickListener(v -> alternarOrdenacao());

        // Pesquisa em tempo real (digitação)
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

        // Clique longo no campo para abrir o calendário nativo
        edtPesquisa.setOnLongClickListener(v -> {
            abrirSeletorDeData();
            return true;
        });
    }

    private void buscarSessoesNaApi() {
        if (getContext() == null) return;

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.minhasSessoes().enqueue(new Callback<List<SessaoDetalhesDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Response<List<SessaoDetalhesDTO>> response) {
                if (!isAdded() || getContext() == null) return; // Garante que o Fragment ainda está ativo

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
            public void onFailure(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Log.e("HistoricoFragment", "Falha na API: " + t.getMessage());
                    Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // =========================================================================
    // LÓGICA DE ORDENAÇÃO
    // =========================================================================
    private void alternarOrdenacao() {
        if (listaExibida.isEmpty()) return;

        ordenadoPorMaisRecentes = !ordenadoPorMaisRecentes;
        btnOrdenar.setText(ordenadoPorMaisRecentes ? "Mais recentes" : "Mais antigos");

        aplicarOrdenacaoNaLista();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void aplicarOrdenacaoNaLista() {
        Collections.sort(listaExibida, (sessao1, sessao2) -> {
            // NOTA: Se alterou os nomes no DTO, mude aqui para getInicioSessao()
            String data1 = sessao1.getInicioSessao() != null ? sessao1.getInicioSessao() : "";
            String data2 = sessao2.getInicioSessao() != null ? sessao2.getInicioSessao() : "";

            return ordenadoPorMaisRecentes ? data2.compareTo(data1) : data1.compareTo(data2);
        });
    }

    // =========================================================================
    // LÓGICA DE PESQUISA POR DATA E ESTADO
    // =========================================================================
    private void filtrarSessoes(String texto) {
        listaExibida.clear();

        if (texto == null || texto.trim().isEmpty()) {
            listaExibida.addAll(listaOriginal);
        } else {
            String busca = texto.toLowerCase().trim();

            for (SessaoDetalhesDTO sessao : listaOriginal) {
                // NOTA: Se alterou os nomes no DTO, mude aqui para getEstadoFornoFinal() e getInicioSessao()
                String estado = sessao.getEstadoFornoFinal() != null ? sessao.getEstadoFornoFinal().toLowerCase() : "";
                String inicioISO = sessao.getInicioSessao() != null ? sessao.getInicioSessao() : "";

                // 1. Converte a data ISO (ex: "2026-07-06T14:30:00") para o padrão BR ("06/07/2026")
                String dataBR = converterIsoParaBr(inicioISO);

                // 2. Verifica se a busca coincide com o Estado ou com os formatos de data
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
            String[] partes = iso.split("T")[0].split("-");
            if (partes.length == 3) {
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception ignored) {}
        return "";
    }

    // =========================================================================
    // CALENDÁRIO NATIVO
    // =========================================================================
    private void abrirSeletorDeData() {
        if (getContext() == null) return;

        Calendar calc = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, ano, mes, dia) -> {
            String dataEscolhida = String.format("%02d/%02d/%04d", dia, (mes + 1), ano);
            edtPesquisa.setText(dataEscolhida);
        }, calc.get(Calendar.YEAR), calc.get(Calendar.MONTH), calc.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    // =========================================================================
    // ESTATÍSTICAS DO TOPO
    // =========================================================================
    private void calcularEstatisticasGlobais(List<SessaoDetalhesDTO> sessoes) {
        if (sessoes == null || sessoes.isEmpty()) {
            txtTotalSessoes.setText("Total de Sessões: 0");
            txtTempoTotal.setText("Tempo Total: 0min");
            txtMaiorTemperatura.setText("Maior Temperatura: --°C");
            return;
        }

        int totalSessoes = sessoes.size();
        double maiorTempGlobal = 0.0;
        long tempoTotalSegundos = 0;

        for (SessaoDetalhesDTO sessao : sessoes) {
            if (sessao.getDuracaoSegundos() != null) {
                tempoTotalSegundos += sessao.getDuracaoSegundos();
            }

            if (sessao.getTemperaturas() != null) {
                for (int i = 0; i < sessao.getTemperaturas().size(); i++) {
                    if (sessao.getTemperaturas().get(i).getTemperaturaAtual() != null) {
                        double temp = sessao.getTemperaturas().get(i).getTemperaturaAtual();
                        if (temp > maiorTempGlobal) maiorTempGlobal = temp;
                    }
                }
            }
        }

        long horas = tempoTotalSegundos / 3600;
        long minutos = (tempoTotalSegundos % 3600) / 60;

        txtTotalSessoes.setText("Total de Sessões: " + totalSessoes);
        txtMaiorTemperatura.setText("Maior Temperatura: " + Math.round(maiorTempGlobal) + "°C");

        if (horas > 0) {
            txtTempoTotal.setText("Tempo Total: " + horas + "h " + minutos + "min");
        } else {
            txtTempoTotal.setText("Tempo Total: " + minutos + "min");
        }
    }
}