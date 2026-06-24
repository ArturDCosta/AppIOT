package com.example.monitorforno.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.SessaoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.Sessao;
import com.example.monitorforno.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoFragment extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_historico,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Inicia a busca real na API assim que a tela abre
            buscarSessoesDaApi();

        return view;
    }

    private void buscarSessoesDaApi() {
        // Usamos requireContext() no Fragment para garantir que o Contexto não seja nulo
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.getHistoricoSessoes().enqueue(new Callback<List<Sessao>>() {
            @Override
            public void onResponse(@NonNull Call<List<Sessao>> call, @NonNull Response<List<Sessao>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Sessao> sessoesReais = response.body();

                    // Alimenta o adapter com os dados do banco
                    SessaoAdapter adapter = new SessaoAdapter(sessoesReais);
                    recyclerView.setAdapter(adapter);

                } else {
                    Log.e("Historico", "Erro da API: " + response.code());
                    Toast.makeText(requireContext(),
                            "Erro ao carregar histórico: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Sessao>> call, @NonNull Throwable t) {
                Log.e("Historico", "Falha na conexão: " + t.getMessage());
                Toast.makeText(requireContext(),
                        "Sem conexão com o servidor.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}