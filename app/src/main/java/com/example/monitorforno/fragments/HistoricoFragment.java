package com.example.monitorforno.fragments;

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

    // Lista original vinda da API (intacta) e lista exibida na tela (filtrada/ordenada)
    private List<SessaoDetalhesDTO> listaOriginal = new ArrayList<>();
    private List<SessaoDetalhesDTO> listaExibida = new ArrayList<>();

    // Controle do estado da ordenação: true = Mais recentes no topo (padrão)
    private boolean ordenadoPorMaisRecentes = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        inicializarViews(view);
        configurarEventos();
        buscarSessoesNaApi();

        return view;
    }

    private void inicializarViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        txtTotalSessoes = view.findViewById(R.id.txtTotalSessoes);
        txtTempoTotal = view.findViewById(R.id.txtTempoTotal);
        txtMaiorTemperatura = view.findViewById(R.id.txtMaiorTemperatura);

        btnOrdenar = view.findViewById(R.id.btnOrdenar);
        edtPesquisa = view.findViewById(R.id.edtPesquisa);
    }

    private void configurarEventos() {
        // 1. Clique no botão de ordenação
        btnOrdenar.setOnClickListener(v -> alternarOrdenacao());

        // 2. Digitação no campo de pesquisa
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
    }

    private void buscarSessoesNaApi() {
        ApiService apiService = RetrofitClient.getApiService(getContext());

        apiService.minhasSessoes().enqueue(new Callback<List<SessaoDetalhesDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Response<List<SessaoDetalhesDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginal = response.body();

                    // Copia para a lista de exibição e aplica a ordenação padrão inicial
                    listaExibida = new ArrayList<>(listaOriginal);
                    aplicarOrdenacaoNaLista();

                    // Configura o RecyclerView
                    adapter = new SessaoAdapter(listaExibida);
                    recyclerView.setAdapter(adapter);

                    // Atualiza os Cards de resumo no topo
                    calcularEstatisticasGlobais(listaOriginal);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
                Log.e("HistoricoFragment", "Erro API: " + t.getMessage());
            }
        });
    }

    // =========================================================================
    // LÓGICA DE ORDENAÇÃO
    // =========================================================================
    private void alternarOrdenacao() {
        if (listaExibida.isEmpty()) return;

        // Inverte o estado
        ordenadoPorMaisRecentes = !ordenadoPorMaisRecentes;

        // Atualiza o texto do botão para indicar o critério atual
        if (ordenadoPorMaisRecentes) {
            btnOrdenar.setText("Mais recentes");
        } else {
            btnOrdenar.setText("Mais antigos");
        }

        aplicarOrdenacaoNaLista();
        if (adapter != null) {
            adapter.notifyDataSetChanged(); // Avisa a tela que a ordem mudou
        }
    }

    private void aplicarOrdenacaoNaLista() {
        Collections.sort(listaExibida, (sessao1, sessao2) -> {
            String data1 = sessao1.getHorarioInicio() != null ? sessao1.getHorarioInicio() : "";
            String data2 = sessao2.getHorarioInicio() != null ? sessao2.getHorarioInicio() : "";

            // Como o formato ISO do Spring Boot é "2026-07-06T14:30:00",
            // a comparação alfabética de String já ordena perfeitamente de forma cronológica!
            if (ordenadoPorMaisRecentes) {
                return data2.compareTo(data1); // Decrescente (Mais recente primeiro)
            } else {
                return data1.compareTo(data2); // Crescente (Mais antigo primeiro)
            }
        });
    }

    // =========================================================================
    // LÓGICA DE PESQUISA (FILTRO)
    // =========================================================================
    private void filtrarSessoes(String texto) {
        listaExibida.clear();

        if (texto == null || texto.trim().isEmpty()) {
            listaExibida.addAll(listaOriginal);
        } else {
            String busca = texto.toLowerCase().trim();
            for (SessaoDetalhesDTO sessao : listaOriginal) {
                String estado = sessao.getEstadoFinal() != null ? sessao.getEstadoFinal().toLowerCase() : "";
                String inicio = sessao.getHorarioInicio() != null ? sessao.getHorarioInicio() : "";

                // Permite pesquisar pelo estado (ex: "ativo") ou pela data (ex: "2026-07")
                if (estado.contains(busca) || inicio.contains(busca)) {
                    listaExibida.add(sessao);
                }
            }
        }

        aplicarOrdenacaoNaLista(); // Garante que a lista filtrada continue ordenada
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // =========================================================================
    // ESTATÍSTICAS DO TOPO
    // =========================================================================
    private void calcularEstatisticasGlobais(List<SessaoDetalhesDTO> sessoes) {
        if (sessoes == null || sessoes.isEmpty()) {
            txtTotalSessoes.setText("Total de Sessões: 0");
            txtTempoTotal.setText("Tempo Total: 0h");
            txtMaiorTemperatura.setText("Maior Temperatura: --°C");
            return;
        }

        int totalSessoes = sessoes.size();
        double maiorTempGlobal = 0.0;
        long tempoTotalSegundos = 0;

        for (SessaoDetalhesDTO sessao : sessoes) {
            // Soma do tempo total
            if (sessao.getDuracaoSegundos() != null) {
                tempoTotalSegundos += sessao.getDuracaoSegundos();
            }

            // Busca a maior temperatura se houver lista de temperaturas, ou no campo opcional
            if (sessao.getTemperaturas() != null) {
                for (int i = 0; i < sessao.getTemperaturas().size(); i++) {
                    if (sessao.getTemperaturas().get(i).getTemperaturaAtual() != null) {
                        double temp = sessao.getTemperaturas().get(i).getTemperaturaAtual();
                        if (temp > maiorTempGlobal) maiorTempGlobal = temp;
                    }
                }
            } else if (sessao.getTemperaturaMaxima() != null && sessao.getTemperaturaMaxima() > maiorTempGlobal) {
                maiorTempGlobal = sessao.getTemperaturaMaxima();
            }
        }

        // Converte o tempo total de segundos para Horas e Minutos
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