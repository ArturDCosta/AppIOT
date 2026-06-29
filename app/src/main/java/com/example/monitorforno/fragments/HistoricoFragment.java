package com.example.monitorforno.fragments;

import android.os.Bundle;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment {

    private RecyclerView recyclerView;
    // Adicionando os textos das estatísticas do topo
    private TextView txtTotalSessoes, txtTempoTotal, txtMaiorTemperatura;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        // Mapeando os componentes do seu fragment_historico.xml
        recyclerView = view.findViewById(R.id.recyclerHistorico);
        txtTotalSessoes = view.findViewById(R.id.txtTotalSessoes);
        txtTempoTotal = view.findViewById(R.id.txtTempoTotal);
        txtMaiorTemperatura = view.findViewById(R.id.txtMaiorTemperatura);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Inicia a busca real na API assim que a tela abre
        buscarSessoesDaApi();

        return view;
    }

    private void buscarSessoesDaApi() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        // Usamos a rota correta do Retrofit que devolve List<SessaoDetalhesDTO>
        apiService.minhasSessoes().enqueue(new Callback<List<SessaoDetalhesDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Response<List<SessaoDetalhesDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<SessaoDetalhesDTO> sessoesDaApi = response.body();

                    // 1. Alimenta o adapter com os dados reais
                    SessaoAdapter adapter = new SessaoAdapter(sessoesDaApi);
                    recyclerView.setAdapter(adapter);

                    // 2. Calcula as estatísticas de cima
                    calcularEstatisticasGlobais(sessoesDaApi);

                } else {
                    Toast.makeText(requireContext(), "Nenhuma sessão encontrada.", Toast.LENGTH_SHORT).show();
                    calcularEstatisticasGlobais(null); // Zera a tela
                }

                Log.d("HISTORICO_API", "Tamanho da lista recebida: " + response.body().size());
            }

            @Override
            public void onFailure(@NonNull Call<List<SessaoDetalhesDTO>> call, @NonNull Throwable t) {
                Log.e("Historico", "Falha na conexão: " + t.getMessage());
                Toast.makeText(requireContext(), "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularEstatisticasGlobais(List<SessaoDetalhesDTO> sessoes) {
        if (sessoes == null || sessoes.isEmpty()) {
            txtTotalSessoes.setText("Total de Sessões: 0");
            txtTempoTotal.setText("Tempo Total: 0h 0min");
            txtMaiorTemperatura.setText("Maior Temperatura: --°C");
            return;
        }

        // Variáveis para somar os totais
        int totalSessoes = sessoes.size();
        double maiorTempGlobal = 0.0;

        // Se a duração vier no formato "1h30m" ou em minutos, você precisaria de uma lógica de soma complexa aqui.
        // Assumiremos que não é possível somar durações em String (ex: "1h10m") facilmente no Android,
        // Então vamos exibir uma mensagem ou extrair caso sua API envie os minutos inteiros.

        for (SessaoDetalhesDTO sessao : sessoes) {
            if (sessao.getTemperaturaMaxima() != null && sessao.getTemperaturaMaxima() > maiorTempGlobal) {
                maiorTempGlobal = sessao.getTemperaturaMaxima();
            }
        }

        txtTotalSessoes.setText("Total de Sessões: " + totalSessoes);
        txtMaiorTemperatura.setText("Maior Temperatura: " + Math.round(maiorTempGlobal) + "°C");

        // O tempo total exige saber como a sua API envia a duração.
        // Se for uma String como "2h 15m", mantemos oculto ou estático. Se for minutos inteiros, some-os aqui.
        txtTempoTotal.setText("Tempo Total: --"); // Placeholder ajustável
    }
}