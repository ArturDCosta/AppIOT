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
import com.example.monitorforno.adapters.EventoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.EventoDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertasFragment extends Fragment {

    private RecyclerView recyclerAlertas;
    private TextView txtTotalAlertas, txtTotalCriticos, txtUltimoEvento;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alertas, container, false);

        // Mapeando os componentes
        txtTotalAlertas = view.findViewById(R.id.txtTotalAlertas);
        txtTotalCriticos = view.findViewById(R.id.txtTotalCriticos);
        txtUltimoEvento = view.findViewById(R.id.txtUltimoEvento);
        recyclerAlertas = view.findViewById(R.id.recyclerAlertas);

        recyclerAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionManager = new SessionManager(requireContext());

        // Limpa a tela visualmente enquanto carrega
        atualizarEstatisticasVazias();

        buscarAlertasDaApi();

        return view;
    }

    private void buscarAlertasDaApi() {
        String fornoId = sessionManager.getFornoSelecionadoId();

        if (fornoId == null) {
            Toast.makeText(requireContext(), "Nenhum forno selecionado.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.getAlertasDoForno(fornoId).enqueue(new Callback<List<EventoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<EventoDTO>> call, @NonNull Response<List<EventoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventoDTO> eventosDaApi = response.body();

                    // -------------------------------------------------------------
                    // 1. ORDENAÇÃO: Do mais recente para o mais antigo
                    // -------------------------------------------------------------
                    Collections.sort(eventosDaApi, (e1, e2) -> {
                        if (e1.getCriadoEm() == null) return 1;
                        if (e2.getCriadoEm() == null) return -1;
                        // Comparação invertida (e2 vs e1) para ordem decrescente
                        return e2.getCriadoEm().compareTo(e1.getCriadoEm());
                    });

                    // Preenche a lista na tela
                    EventoAdapter adapter = new EventoAdapter(eventosDaApi);
                    recyclerAlertas.setAdapter(adapter);

                    // Calcula as estatísticas
                    calcularEstatisticas(eventosDaApi);
                } else {
                    atualizarEstatisticasVazias();
                    Toast.makeText(requireContext(), "Sem alertas para exibir.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventoDTO>> call, @NonNull Throwable t) {
                Log.e("Alertas", "Falha na conexão: " + t.getMessage());
                atualizarEstatisticasVazias();
                Toast.makeText(requireContext(), "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularEstatisticas(List<EventoDTO> eventos) {
        if (eventos == null || eventos.isEmpty()) {
            atualizarEstatisticasVazias();
            return;
        }

        int countAlertas = 0;
        int countCriticos = 0;

        // Como a lista foi ordenada, o índice 0 é o evento mais recente
        String dataHoraUltimo = formatarDataHora(eventos.get(0).getCriadoEm());

        for (EventoDTO evento : eventos) {
            if (evento.getTipo() != null) {
                if (evento.getTipo().contains("ALERTA_ENTRADA")) {
                    countAlertas++;
                } else if (evento.getTipo().contains("CRITICO_ENTRADA")) {
                    countCriticos++;
                }
            }
        }

        txtTotalAlertas.setText("Total de Alertas: " + countAlertas);
        txtTotalCriticos.setText("Eventos Críticos: " + countCriticos);
        txtUltimoEvento.setText("Último Evento: " + dataHoraUltimo);
    }

    private void atualizarEstatisticasVazias() {
        txtTotalAlertas.setText("Total de Alertas: 0");
        txtTotalCriticos.setText("Eventos Críticos: 0");
        txtUltimoEvento.setText("Último Evento: --/--/---- - --:--");
    }

    //Método para formatar a hora e data certas
    private String formatarDataHora(String dataOriginal) {
        if (dataOriginal == null || !dataOriginal.contains("T")) {
            return "--/--/---- - --:--";
        }
        try {
            // Remove milissegundos caso venham da API e garante a indicação de UTC (Z)
            String dataLimpa = dataOriginal.split("\\.")[0];
            if (!dataLimpa.endsWith("Z")) dataLimpa += "Z";

            // Dizemos ao Java que a data que chegou está em UTC (Fuso 0)
            java.text.SimpleDateFormat sdfEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
            sdfEntrada.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Date dataObj = sdfEntrada.parse(dataLimpa);

            // Dizemos ao Java para formatar a data para o horário local do dispositivo (-3 horas)
            java.text.SimpleDateFormat sdfSaida = new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault());
            sdfSaida.setTimeZone(java.util.TimeZone.getDefault());

            return sdfSaida.format(dataObj);
        } catch (Exception e) {
            return dataOriginal;
        }
    }
}